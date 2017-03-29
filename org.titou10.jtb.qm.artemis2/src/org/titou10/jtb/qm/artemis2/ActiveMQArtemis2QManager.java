/*
 * Copyright (C) 2015-2017 Denis Forveille titou10.titou10@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.titou10.jtb.qm.artemis2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueRequestor;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.management.ResourceNames;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.api.jms.management.JMSManagementHelper;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.ConnectionData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

/**
 * 
 * Implements Apache ActiveMQ Artemis v2.x Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class ActiveMQArtemis2QManager extends QManager {

   private static final org.slf4j.Logger      log             = LoggerFactory.getLogger(ActiveMQArtemis2QManager.class);

   private static final String                CR              = "\n";

   private static final String                HELP_TEXT;

   private List<QManagerProperty>             parameters      = new ArrayList<QManagerProperty>();

   private Queue                              managementQueue = ActiveMQJMSClient.createQueue("activemq.management");

   private final Map<Integer, Session>        sessionJMSs     = new HashMap<>();
   private final Map<Integer, QueueRequestor> requestorJMSs   = new HashMap<>();

   public ActiveMQArtemis2QManager() {
      log.debug("Apache Active MQ Artemis v2.x+");

      parameters.add(new QManagerProperty(TransportConstants.HTTP_ENABLED_PROP_NAME,
                                          false,
                                          JMSPropertyKind.BOOLEAN,
                                          false,
                                          "Use an HTTP netty acceptor to connect to the server?",
                                          null));
      parameters.add(new QManagerProperty(TransportConstants.SSL_ENABLED_PROP_NAME,
                                          false,
                                          JMSPropertyKind.BOOLEAN,
                                          false,
                                          "Use an SSL netty acceptor to connect to the server?",
                                          null));
      parameters.add(new QManagerProperty(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, false, JMSPropertyKind.STRING, true));
   }

   @Override
   public ConnectionData connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Save System properties
      saveSystemProperties();
      try {

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         String httpEnabled = mapProperties.get(TransportConstants.HTTP_ENABLED_PROP_NAME);
         String sslEnabled = mapProperties.get(TransportConstants.SSL_ENABLED_PROP_NAME);
         String trustStore = mapProperties.get(TransportConstants.TRUSTSTORE_PATH_PROP_NAME);
         String trustStorePassword = mapProperties.get(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME);

         // Netty Connection Properties
         Map<String, Object> connectionParams = new HashMap<String, Object>();
         connectionParams.put(TransportConstants.HOST_PROP_NAME, sessionDef.getHost()); // localhost
         connectionParams.put(TransportConstants.PORT_PROP_NAME, sessionDef.getPort()); // 61616

         if (sslEnabled != null) {
            if (Boolean.valueOf(sslEnabled)) {
               connectionParams.put(TransportConstants.SSL_ENABLED_PROP_NAME, "true");
               // connectionParams.put(TransportConstants.KEYSTORE_PATH_PROP_NAME, keytStore);
               // connectionParams.put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME, keyStorePassword);
               connectionParams.put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, trustStore);
               connectionParams.put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, trustStorePassword);
            }
         }

         if (httpEnabled != null) {
            if (Boolean.valueOf(httpEnabled)) {
               connectionParams.put(TransportConstants.HTTP_ENABLED_PROP_NAME, "true");
            }
         }

         SortedSet<QueueData> listQueueData = new TreeSet<>();
         SortedSet<TopicData> listTopicData = new TreeSet<>();

         TransportConfiguration tcJMS = new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams);

         ConnectionFactory cfJMS = (ConnectionFactory) ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, tcJMS);

         Connection jmsConnection = cfJMS.createConnection(sessionDef.getUserid(), sessionDef.getPassword());
         jmsConnection.setClientID(clientID);
         jmsConnection.start();

         Session sessionJMS = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         QueueRequestor requestorJMS = new QueueRequestor((QueueSession) sessionJMS, managementQueue);

         Message m;
         Message r;

         // Get Server Version

         // Try the v2.0.x way..
         String version = sendAdminMessage(String.class, sessionJMS, requestorJMS, ResourceNames.BROKER, "version");
         log.debug("Server version: {}", version);

         // Get Queues + Topics the v2.0 way
         Object[] queueNames = sendAdminMessage(Object[].class, sessionJMS, requestorJMS, ResourceNames.BROKER, "queueNames");
         for (Object o : queueNames) {
            log.debug("q={}", o);

            String queueName = (String) o;

            Boolean temporary = sendAdminMessage(Boolean.class,
                                                 sessionJMS,
                                                 requestorJMS,
                                                 ResourceNames.QUEUE + queueName,
                                                 "temporary");
            String addressName = sendAdminMessage(String.class,
                                                  sessionJMS,
                                                  requestorJMS,
                                                  ResourceNames.QUEUE + queueName,
                                                  "address");
            String deliveryMode = sendAdminMessage(String.class,
                                                   sessionJMS,
                                                   requestorJMS,
                                                   ResourceNames.ADDRESS + addressName,
                                                   "deliveryModesAsJSON");

            log.debug("queueName={} address={} deliveryMode={} temp? {}", queueName, addressName, deliveryMode, temporary);

            if (!showSystemObjects && temporary) {
               log.debug("Temporay queue + do not show system objets:  forget it");
               continue;
            }

            if (deliveryMode.contains("ANYCAST")) {
               listQueueData.add(new QueueData(queueName));
            } else {
               if (deliveryMode.contains("MULTICAST")) {
                  listTopicData.add(new TopicData(queueName));
               } else {
                  log.warn("Queues associated with an address that is not either ANYCAST or MULTICAST? don't use it...");
               }
            }
         }

         // m = sessionJMS.createMessage();
         // JMSManagementHelper.putAttribute(m, ResourceNames.BROKER, "addressNames");
         // r = requestorJMS.request(m);
         // Object t = JMSManagementHelper.getResult(r);
         // if (t instanceof Object[]) {
         // for (Object o : (Object[]) t) {
         // log.debug("t={}", o);
         //
         // String adressName = (String) o;
         // if (adressName.startsWith("jms.tempqueue")) {
         // continue;
         // }
         //
         // m = sessionJMS.createMessage();
         // JMSManagementHelper.putAttribute(m, ResourceNames.ADDRESS + adressName, "deliveryModesAsJSON");
         // r = requestorJMS.request(m);
         // Object oAC = JMSManagementHelper.getResult(r);
         // String s = (String) oAC;
         // System.out.println(s);
         // if (s.contains("MULTICAST")) {
         // listTopicData.add(new TopicData(adressName));
         // }
         // }
         // } else {
         // log.warn("topicNames failed");
         // }

         log.info("connected to {}", sessionDef.getName());

         // Store per connection related data
         Integer hash = jmsConnection.hashCode();
         sessionJMSs.put(hash, sessionJMS);
         requestorJMSs.put(hash, requestorJMS);

         return new ConnectionData(jmsConnection, listQueueData, listTopicData);
      } finally {
         restoreSystemProperties();
      }

   }

   @SuppressWarnings("unchecked")
   private <T> T sendAdminMessage(Class<T> clazz,
                                  Session sessionJMS,
                                  QueueRequestor requestorJMS,
                                  String resourceName,
                                  String methodName) throws Exception {
      Message m = sessionJMS.createMessage();
      JMSManagementHelper.putAttribute(m, resourceName, methodName);
      Message r = requestorJMS.request(m);
      return (T) JMSManagementHelper.getResult(r);
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      Integer hash = jmsConnection.hashCode();
      QueueRequestor requestorJMS = requestorJMSs.get(hash);
      Session sessionJMS = sessionJMSs.get(hash);

      if (requestorJMS != null) {
         try {
            requestorJMS.close();
         } catch (Exception e) {
            log.warn("Exception occured while closing requestorJMS. Ignore it. Msg={}", e.getMessage());
         }
         requestorJMSs.remove(hash);
      }

      if (sessionJMS != null) {
         try {
            sessionJMS.close();
         } catch (Exception e) {
            log.warn("Exception occured while closing sessionJMS. Ignore it. Msg={}", e.getMessage());
         }
         sessionJMSs.remove(hash);
      }

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occured while closing jmsConnection. Ignore it. Msg={}", e.getMessage());
      }
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {
      Integer hash = jmsConnection.hashCode();
      QueueRequestor requestorJMS = requestorJMSs.get(hash);
      Session sessionJMS = sessionJMSs.get(hash);

      try {
         Message m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, ResourceNames.QUEUE + queueName, "messageCount");
         Message r = requestorJMS.request(m);

         // DF: it seems it changed in v1.4.0 from Integer to Long...
         Object o = JMSManagementHelper.getResult(r);
         if (o instanceof Long) {
            Long count = (Long) o;
            return count.intValue();
         } else {
            return (Integer) o;
         }
      } catch (Exception e) {
         log.error("Exception occurred in getQueueDepth()", e);
         return null;
      }
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      QueueRequestor requestorJMS = requestorJMSs.get(hash);
      Session sessionJMS = sessionJMSs.get(hash);

      String jmsQueueName = ResourceNames.QUEUE + queueName;

      Message m;
      Message r;

      Map<String, Object> properties = new LinkedHashMap<>();
      try {

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "paused");
         r = requestorJMS.request(m);
         properties.put("Paused", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "temporary");
         r = requestorJMS.request(m);
         properties.put("Temporary", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "messageCount");
         r = requestorJMS.request(m);
         properties.put("Message Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "scheduledCount");
         r = requestorJMS.request(m);
         properties.put("Scheduled Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "consumerCount");
         r = requestorJMS.request(m);
         properties.put("Consumer Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "deliveringCount");
         r = requestorJMS.request(m);
         properties.put("Delivering Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "messagesAdded");
         r = requestorJMS.request(m);
         properties.put("Messages Added", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "deadLetterAddress");
         r = requestorJMS.request(m);
         properties.put("Dead Letter Address", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "expiryAddress");
         r = requestorJMS.request(m);
         properties.put("Expiry Address", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "firstMessageAge");
         r = requestorJMS.request(m);
         properties.put("First Message Age (ms)", JMSManagementHelper.getResult(r));

      } catch (Exception e) {
         log.error("Exception occurred in getQueueInformation()", e);
      }

      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {

      Integer hash = jmsConnection.hashCode();
      QueueRequestor requestorJMS = requestorJMSs.get(hash);
      Session sessionJMS = sessionJMSs.get(hash);

      String jmsTopicName = ResourceNames.QUEUE + topicName;

      Message m;
      Message r;

      Map<String, Object> properties = new LinkedHashMap<>();
      try {

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsTopicName, "temporary");
         r = requestorJMS.request(m);
         properties.put("Temporary", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsTopicName, "messageCount");
         r = requestorJMS.request(m);
         properties.put("Message Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsTopicName, "durableMessageCount");
         r = requestorJMS.request(m);
         properties.put("Durable Message Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsTopicName, "nonDurableMessageCount");
         r = requestorJMS.request(m);
         properties.put("Non Durable Message Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsTopicName, "deliveringCount");
         r = requestorJMS.request(m);
         properties.put("Delivering Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsTopicName, "durableSubscriptionCount");
         r = requestorJMS.request(m);
         properties.put("Durable Subscription Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsTopicName, "nonDurableSubscriptionCount");
         r = requestorJMS.request(m);
         properties.put("Non Durable Subscription Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsTopicName, "consumerCount");
         r = requestorJMS.request(m);
         properties.put("Consumer Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsTopicName, "maxConsumers");
         r = requestorJMS.request(m);
         properties.put("Max Consumers", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsTopicName, "messagesAdded");
         r = requestorJMS.request(m);
         properties.put("Messages Added", JMSManagementHelper.getResult(r));

      } catch (Exception e) {
         log.error("Exception occurred in getQueueInformation()", e);
      }

      return properties;
   }

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS :").append(CR);
      sb.append("------------").append(CR);
      sb.append("No extra jar is needed as JMSToolBox is bundled with Apache ActiveMQ Artemis v1.4.0 jars").append(CR);
      sb.append(CR);
      sb.append("Requirements").append(CR);
      sb.append("------------").append(CR);
      sb.append("The following configuration is required in broker.xml for JMSToolBox :").append(CR);
      // sb.append(" <management-address><management address, default=jms.queue.activemq.management></management-address>");
      // sb.append(CR);
      // sb.append(" ...").append(CR);
      sb.append(" <security-setting match=\"jms.queue.activemq.management\">").append(CR);
      sb.append(" <permission type=\"manage\" roles=\"<admin role>\" />").append(CR);
      sb.append(" </security-setting>").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : Apache ActiveMQ Artemis netty acceptor host name (eg localhost)").append(CR);
      sb.append("Port          : Apache ActiveMQ Artemis netty listening port (eg. 61616)").append(CR);
      sb.append("User/Password : User allowed to connect to Apache ActiveMQ Artemis, ie associated to the role defined previously");
      sb.append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("- httpEnabled        : Use an HTTP netty acceptor to connect to the server").append(CR);
      sb.append(CR);
      sb.append("- sslEnabled         : Use an SSL netty acceptor to connect to the server").append(CR);
      // sb.append("- keyStorePath : Key store (eg D:/somewhere/trust.jks)").append(CR);
      // sb.append("- keyStorePassword : Key store password").append(CR);
      sb.append("- trustStorePath     : Trust store (eg D:/somewhere/trust.jks)").append(CR);
      sb.append("- trustStorePassword : Trust store password").append(CR);

      HELP_TEXT = sb.toString();
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }
}

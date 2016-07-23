/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.qm.hornetq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueRequestor;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.management.ResourceNames;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.api.jms.management.JMSManagementHelper;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.client.HornetQDestination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.ConnectionData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;

/**
 * 
 * Implements HornetQ (embedded in JBoss WidlFly and JBoss AS) Q Provider
 * 
 * Connects via JMX for Queue/Topics discovery
 * 
 * @author Denis Forveille
 *
 */
public class HornetQQManager extends QManager {
   private static final Logger                log             = LoggerFactory.getLogger(HornetQQManager.class);

   private static final String                CR              = "\n";

   private static final String                P_USE_CORE_MODE = "use_CORE_queues_instead_of_JMS_destinations";

   private static final String                Q_PREFIX        = ResourceNames.CORE_QUEUE + ResourceNames.JMS_QUEUE;

   private static final String                HELP_TEXT;

   private List<QManagerProperty>             parameters      = new ArrayList<QManagerProperty>();

   private final Map<Integer, Session>        sessionJMSs     = new HashMap<>();
   private final Map<Integer, QueueRequestor> requestorJMSs   = new HashMap<>();

   private Boolean                            useCoreMode;

   public HornetQQManager() {
      log.debug("Instantiate HornetQQManager");

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
      parameters.add(new QManagerProperty(P_USE_CORE_MODE,
                                          false,
                                          JMSPropertyKind.BOOLEAN,
                                          false,
                                          "Access 'core' adresses instead of 'jms' destination",
                                          "false"));

   }

   @Override
   public ConnectionData connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Save System properties
      saveSystemProperties();
      try {

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         String sslEnabled = mapProperties.get(TransportConstants.SSL_ENABLED_PROP_NAME);
         String httpEnabled = mapProperties.get(TransportConstants.HTTP_ENABLED_PROP_NAME);
         String trustStore = mapProperties.get(TransportConstants.TRUSTSTORE_PATH_PROP_NAME);
         String trustStorePassword = mapProperties.get(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME);
         useCoreMode = Boolean.valueOf(mapProperties.get(P_USE_CORE_MODE));

         // Netty Connection Properties
         Map<String, Object> connectionParams = new HashMap<String, Object>();
         connectionParams.put(TransportConstants.HOST_PROP_NAME, sessionDef.getHost()); // localhost
         connectionParams.put(TransportConstants.PORT_PROP_NAME, sessionDef.getPort()); // 5445

         if (sslEnabled != null) {
            if (Boolean.valueOf(sslEnabled)) {
               connectionParams.put(TransportConstants.SSL_ENABLED_PROP_NAME, "true");
               connectionParams.put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, trustStore);
               connectionParams.put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, trustStorePassword);
            }
         }

         if (httpEnabled != null) {
            if (Boolean.valueOf(httpEnabled)) {
               connectionParams.put(TransportConstants.HTTP_ENABLED_PROP_NAME, "true");
            }
         }

         SortedSet<String> queueNames = new TreeSet<>();
         SortedSet<String> topicNames = new TreeSet<>();

         TransportConfiguration tcJMS = new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams);

         HornetQConnectionFactory cfJMS = HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, tcJMS);
         cfJMS.setConnectionTTL(-1);

         Connection jmsConnection = cfJMS.createConnection(sessionDef.getUserid(), sessionDef.getPassword());
         jmsConnection.setClientID(clientID);
         Session sessionJMS = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         Queue managementQueue = HornetQJMSClient.createQueue("hornetq.management");
         QueueRequestor requestorJMS = new QueueRequestor((QueueSession) sessionJMS, managementQueue);
         jmsConnection.start();

         Message m = sessionJMS.createMessage();
         Message r;
         if (useCoreMode) {
            JMSManagementHelper.putAttribute(m, ResourceNames.CORE_SERVER, "queueNames");
            r = requestorJMS.request(m);
            Object q = JMSManagementHelper.getResult(r);
            if (q instanceof Object[]) {
               log.debug("queueNames = {}", q);
               for (Object o : (Object[]) q) {

                  String queueName = (String) o;
                  log.debug("queueName={}", queueName);

                  // In CORE mode, keep only queues that have a name starting with "jms.queue."
                  if (!queueName.startsWith(HornetQDestination.JMS_QUEUE_ADDRESS_PREFIX)) {
                     log.warn("CORE mode in use: Exclude '{}' that does not have a name starting with '{}'",
                              queueName,
                              HornetQDestination.JMS_QUEUE_ADDRESS_PREFIX);
                     continue;
                  }
                  // Remove jms.queue. prefix
                  queueNames.add(queueName.replaceFirst(HornetQDestination.JMS_QUEUE_ADDRESS_PREFIX, ""));
               }
            } else {
               log.warn("queueNames failed");
            }
         } else {
            JMSManagementHelper.putAttribute(m, ResourceNames.JMS_SERVER, "queueNames");
            r = requestorJMS.request(m);
            Object q = JMSManagementHelper.getResult(r);
            if (q instanceof Object[]) {
               log.debug("queueNames = {}", q);
               for (Object o : (Object[]) q) {

                  String queueName = (String) o;
                  log.debug("queueName={}", queueName);
                  queueNames.add(queueName);
               }
            } else {
               log.warn("queueNames failed");
            }

         }

         // Topics exist only in JMS Mode
         if (!useCoreMode) {
            m = sessionJMS.createMessage();
            JMSManagementHelper.putAttribute(m, ResourceNames.JMS_SERVER, "topicNames");
            r = requestorJMS.request(m);
            Object t = JMSManagementHelper.getResult(r);
            if (t instanceof Object[]) {
               log.debug("topicNames = {}", t);
               for (Object o : (Object[]) t) {
                  String topicName = (String) o;
                  log.debug("topicName={}", topicName);
                  topicNames.add(topicName);
               }
            } else {
               log.warn("topicNames failed");
            }
         }

         log.info("connected to {}", sessionDef.getName());

         // Store per connection related data
         Integer hash = jmsConnection.hashCode();
         sessionJMSs.put(hash, sessionJMS);
         requestorJMSs.put(hash, requestorJMS);

         return new ConnectionData(jmsConnection, queueNames, topicNames);
      } finally {
         restoreSystemProperties();
      }
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
         JMSManagementHelper.putAttribute(m, Q_PREFIX + queueName, "messageCount");
         Message r = requestorJMS.request(m);
         Integer count = (Integer) JMSManagementHelper.getResult(r);
         return count;
      } catch (Exception e) {
         log.error("exception occurred in getQueueDepth()", e);
         return null;
      }
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      QueueRequestor requestorJMS = requestorJMSs.get(hash);
      Session sessionJMS = sessionJMSs.get(hash);

      String jmsQueueName = Q_PREFIX + queueName;

      Message m;
      Message r;

      Map<String, Object> properties = new LinkedHashMap<>();
      try {
         m = sessionJMS.createMessage();

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

      } catch (Exception e) {
         log.error("Exception occurred in getQueueInformation()", e);
         return null;
      }

      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {

      Integer hash = jmsConnection.hashCode();
      QueueRequestor requestorJMS = requestorJMSs.get(hash);
      Session sessionJMS = sessionJMSs.get(hash);

      String jmsTopicName = ResourceNames.JMS_TOPIC + topicName;

      Message m;
      Message r;

      Map<String, Object> properties = new LinkedHashMap<>();
      try {
         m = sessionJMS.createMessage();

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
         JMSManagementHelper.putAttribute(m, jmsTopicName, "subscriptionCount");
         r = requestorJMS.request(m);
         properties.put("Subscription Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsTopicName, "messagesAdded");
         r = requestorJMS.request(m);
         properties.put("Messages Added", JMSManagementHelper.getResult(r));

      } catch (Exception e) {
         log.error("Exception occurred in getQueueInformation()", e);
         return null;
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
      sb.append("No extra jar is needed as JMSToolBox is bundled with HornetQ v2.4.6 jars").append(CR);
      sb.append(CR);
      sb.append("Requirements").append(CR);
      sb.append("------------").append(CR);
      sb.append("In hornetq-configuration.xml, add:").append(CR);
      sb.append("<security-setting match=\"jms.queue.hornetq.management\">").append(CR);
      sb.append("   <permission type=\"manage\" roles=\"admin\"/>").append(CR);
      sb.append("</security-setting>").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : HornetQ netty acceptor host name (eg. localhost)").append(CR);
      sb.append("Port          : HornetQ netty acceptor listening port (eg 5445)").append(CR);
      sb.append("User/Password : User allowed to connect to the HornetQ server").append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("use_CORE_queues_instead_of_JMS_destinations : Access Queues by using the CORE API (CORE mode) instead of the JMS API (JMS mode)")
               .append(CR);
      sb.append(" ->If the 'CORE mode' is activated, only queues defined in HornetQ with a name starting with 'jms.queue.' will be accessible")
               .append(CR);
      sb.append("http-enabled                                : Use an HTTP netty acceptor to connect to the server").append(CR);
      sb.append("ssl-enabled                                 : Use an SSL netty acceptor to connect to the server").append(CR);
      sb.append("trust-store-path                            : trust store (eg D:/somewhere/trust.jks)").append(CR);
      sb.append("trust-store-password                        : trust store password").append(CR);

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

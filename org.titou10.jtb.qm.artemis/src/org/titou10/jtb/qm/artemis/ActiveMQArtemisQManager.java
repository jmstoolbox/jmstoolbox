/*
 * Copyright (C) 2015 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.qm.artemis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.DestinationData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

/**
 * 
 * Implements Apache ActiveMQ Artemis Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class ActiveMQArtemisQManager extends QManager {

   private static final Logger                log                  = LoggerFactory.getLogger(ActiveMQArtemisQManager.class);

   private static final String                NA                   = "n/a";
   private static final String                CR                   = "\n";

   private static final String                P_EXTRA_PROPERTIES   = "z_ExtraNettyProperties";
   private static final String                EXTRA_PROPERTIES_SEP = ";";
   private static final String                EXTRA_PROPERTIES_VAL = "=";

   private static final String                HELP_TEXT;

   private List<QManagerProperty>             parameters           = new ArrayList<QManagerProperty>();

   private Queue                              managementQueue      = ActiveMQJMSClient.createQueue("activemq.management");

   private final Map<Integer, Session>        sessionJMSs          = new HashMap<>();
   private final Map<Integer, QueueRequestor> requestorJMSs        = new HashMap<>();

   public ActiveMQArtemisQManager() {
      log.debug("Apache Active MQ Artemis v1.x (legacy)");

      parameters.add(new QManagerProperty(TransportConstants.HTTP_ENABLED_PROP_NAME,
                                          false,
                                          JMSPropertyKind.BOOLEAN,
                                          false,
                                          "Use an HTTP netty acceptor to connect to the server?",
                                          null));
      parameters.add(new QManagerProperty(TransportConstants.HTTP_UPGRADE_ENABLED_PROP_NAME,
                                          false,
                                          JMSPropertyKind.BOOLEAN,
                                          false,
                                          "Multiplexing messaging traffic over HTTP?",
                                          null));
      parameters.add(new QManagerProperty(TransportConstants.SSL_ENABLED_PROP_NAME,
                                          false,
                                          JMSPropertyKind.BOOLEAN,
                                          false,
                                          "Use an SSL netty acceptor to connect to the server?",
                                          null));
      parameters.add(new QManagerProperty(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, false, JMSPropertyKind.STRING, true));
      parameters
               .add(new QManagerProperty(P_EXTRA_PROPERTIES,
                                         false,
                                         JMSPropertyKind.STRING,
                                         false,
                                         "Any netty connector properties separated by semicolons as defined there :\n"
                                                + "   https://activemq.apache.org/artemis/docs/1.5.6/configuring-transports.html \n"
                                                + "eg: trustAll=true;tcpNoDelay=true;tcpSendBufferSize=16000"));

   }

   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Save System properties
      saveSystemProperties();
      try {

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         String httpEnabled = mapProperties.get(TransportConstants.HTTP_ENABLED_PROP_NAME);
         String httpUpgradeEnabled = mapProperties.get(TransportConstants.HTTP_UPGRADE_ENABLED_PROP_NAME);
         String sslEnabled = mapProperties.get(TransportConstants.SSL_ENABLED_PROP_NAME);
         String trustStore = mapProperties.get(TransportConstants.TRUSTSTORE_PATH_PROP_NAME);
         String trustStorePassword = mapProperties.get(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME);
         String extraNettyProperties = mapProperties.get(P_EXTRA_PROPERTIES);

         // Netty Connection Properties
         Map<String, Object> connectionParams = new HashMap<String, Object>();
         connectionParams.put(TransportConstants.HOST_PROP_NAME, sessionDef.getHost()); // localhost
         connectionParams.put(TransportConstants.PORT_PROP_NAME, sessionDef.getPort()); // 61616

         if (httpUpgradeEnabled != null) {
            if (Boolean.valueOf(httpUpgradeEnabled)) {
               connectionParams.put(TransportConstants.HTTP_UPGRADE_ENABLED_PROP_NAME, "true");
            }
         }

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

         if ((extraNettyProperties != null) && (!(extraNettyProperties.trim().isEmpty()))) {
            String[] extraProps = extraNettyProperties.split(EXTRA_PROPERTIES_SEP);
            for (String prop : extraProps) {
               String[] keyValue = prop.trim().split(EXTRA_PROPERTIES_VAL);
               if (keyValue.length > 1) {
                  connectionParams.put(keyValue[0], keyValue[1]);
               } else {
                  connectionParams.put(keyValue[0], null);
               }
            }
         }

         // Connect to Server

         TransportConfiguration tcJMS = new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams);

         ConnectionFactory cfJMS = (ConnectionFactory) ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, tcJMS);

         Connection jmsConnection = cfJMS.createConnection(sessionDef.getActiveUserid(), sessionDef.getActivePassword());
         jmsConnection.setClientID(clientID);
         jmsConnection.start();

         Session sessionJMS = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         QueueRequestor requestorJMS = new QueueRequestor((QueueSession) sessionJMS, managementQueue);

         log.info("connected to {}", sessionDef.getName());

         // Store per connection related data
         Integer hash = jmsConnection.hashCode();
         sessionJMSs.put(hash, sessionJMS);
         requestorJMSs.put(hash, requestorJMS);

         return jmsConnection;
      } finally {
         restoreSystemProperties();
      }

   }

   @Override
   public DestinationData discoverDestinations(Connection jmsConnection, boolean showSystemObjects, SessionDef sessionDef) throws Exception {
      log.debug("discoverDestinations : {} - {}", jmsConnection, showSystemObjects);

      Integer hash = jmsConnection.hashCode();
      QueueRequestor requestorJMS = requestorJMSs.get(hash);
      Session sessionJMS = sessionJMSs.get(hash);

      // Retrieve Queues
      SortedSet<QueueData> listQueueData = new TreeSet<>();
      Object[] queueNames = sendAdminMessage(Object[].class, sessionJMS, requestorJMS, ResourceNames.JMS_SERVER, "queueNames");
      log.debug("queueNames = {} {}", queueNames, queueNames.getClass());
      for (Object o : queueNames) {
         log.debug("q={}", o);
         listQueueData.add(new QueueData((String) o));
      }

      // Retrieve Topics
      SortedSet<TopicData> listTopicData = new TreeSet<>();
      Object[] topicNames = sendAdminMessage(Object[].class, sessionJMS, requestorJMS, ResourceNames.JMS_SERVER, "topicNames");
      for (Object o : topicNames) {
         log.debug("t={}", o.toString());
         listTopicData.add(new TopicData((String) o));
      }

      return new DestinationData(listQueueData, listTopicData);

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

      Number n = samNull(Number.class, sessionJMS, requestorJMS, ResourceNames.JMS_QUEUE + queueName, "messageCount");
      return n == null ? null : n.intValue();
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      QueueRequestor requestorJMS = requestorJMSs.get(hash);
      Session sessionJMS = sessionJMSs.get(hash);

      String jmsQueueName = ResourceNames.JMS_QUEUE + queueName;

      SortedMap<String, Object> properties = new TreeMap<>();
      properties.put("Paused", samNull(Boolean.class, sessionJMS, requestorJMS, jmsQueueName, "paused"));
      properties.put("Temporary", samNull(Boolean.class, sessionJMS, requestorJMS, jmsQueueName, "temporary"));
      properties.put("Message Count", samNull(Long.class, sessionJMS, requestorJMS, jmsQueueName, "messageCount"));
      properties.put("Scheduled Count", samNull(Long.class, sessionJMS, requestorJMS, jmsQueueName, "scheduledCount"));
      properties.put("Consumer Count", samNull(Integer.class, sessionJMS, requestorJMS, jmsQueueName, "consumerCount"));
      properties.put("Delivering Count", samNull(Integer.class, sessionJMS, requestorJMS, jmsQueueName, "deliveringCount"));
      properties.put("Messages Added", samNull(Long.class, sessionJMS, requestorJMS, jmsQueueName, "messagesAdded"));
      properties.put("Dead Letter Address", samNull(String.class, sessionJMS, requestorJMS, jmsQueueName, "deadLetterAddress"));
      properties.put("Expiry Address", samNull(String.class, sessionJMS, requestorJMS, jmsQueueName, "expiryAddress"));

      Long fmAge = samNull(Long.class, sessionJMS, requestorJMS, jmsQueueName, "firstMessageAge");
      properties.put("First Message Age",
                     fmAge == null ? NA
                              : Duration.ofMillis(fmAge).toString().replace("PT", " ").replace("H", "h ").replace("M", "m ")
                                       .replace("S", "s"));

      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {

      Integer hash = jmsConnection.hashCode();
      QueueRequestor requestorJMS = requestorJMSs.get(hash);
      Session sessionJMS = sessionJMSs.get(hash);

      String jmsTopicName = ResourceNames.JMS_TOPIC + topicName;

      SortedMap<String, Object> properties = new TreeMap<>();
      properties.put("Temporary", samNull(Boolean.class, sessionJMS, requestorJMS, jmsTopicName, "temporary"));
      properties.put("Message Count", samNull(Long.class, sessionJMS, requestorJMS, jmsTopicName, "messageCount"));
      properties.put("Durable Message Count",
                     samNull(Integer.class, sessionJMS, requestorJMS, jmsTopicName, "durableMessageCount"));
      properties.put("Non Durable Message Count",
                     samNull(Integer.class, sessionJMS, requestorJMS, jmsTopicName, "nonDurableMessageCount"));
      properties.put("Delivering Count", samNull(Integer.class, sessionJMS, requestorJMS, jmsTopicName, "deliveringCount"));
      properties.put("Durable Subscription Count",
                     samNull(Integer.class, sessionJMS, requestorJMS, jmsTopicName, "durableSubscriptionCount"));
      properties.put("Non Durable Subscription Count",
                     samNull(Integer.class, sessionJMS, requestorJMS, jmsTopicName, "nonDurableSubscriptionCount"));
      properties.put("Subscription Count", samNull(Integer.class, sessionJMS, requestorJMS, jmsTopicName, "subscriptionCount"));
      properties.put("Messages Added", samNull(Long.class, sessionJMS, requestorJMS, jmsTopicName, "messagesAdded"));

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
      sb.append("No extra jar is needed as JMSToolBox is bundled with the latest Apache ActiveMQ Artemis v1.x jars").append(CR);
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
      sb.append("- httpUpgradeEnabled : Multiplexing messaging traffic over HTTP").append(CR);
      sb.append(CR);
      sb.append("- sslEnabled         : Use an SSL netty acceptor to connect to the server").append(CR);
      // sb.append("- keyStorePath : Key store (eg D:/somewhere/trust.jks)").append(CR);
      // sb.append("- keyStorePassword : Key store password").append(CR);
      sb.append("- trustStorePath     : Trust store (eg D:/somewhere/trust.jks)").append(CR);
      sb.append("- trustStorePassword : Trust store password").append(CR);
      sb.append(CR);
      sb.append("- z_ExtraNettyProperties : semicolon separated list of netty connector properties").append(CR);
      sb.append("                         : eg \"trustAll=true;tcpNoDelay=true;tcpSendBufferSize=16000\"").append(CR);
      sb.append("                         : for details, visit https://activemq.apache.org/artemis/docs/1.5.6/configuring-transports.html")
               .append(CR);
      sb.append(CR);

      HELP_TEXT = sb.toString();
   }

   // ------------------------
   // Helpers
   // ------------------------

   @SuppressWarnings("unchecked")
   private <T> T sendAdminMessage(Class<T> clazz,
                                  Session sessionJMS,
                                  QueueRequestor requestorJMS,
                                  String resourceName,
                                  String methodName) throws Exception {
      Message m = sessionJMS.createMessage();
      JMSManagementHelper.putAttribute(m, resourceName, methodName);
      Message r = requestorJMS.request(m);

      if (JMSManagementHelper.hasOperationSucceeded(r)) {
         return (T) JMSManagementHelper.getResult(r);
      } else {
         Object resp = JMSManagementHelper.getResult(r);
         throw new Exception(resp.toString());
      }
   }

   private <T> T samNull(Class<T> clazz, Session sessionJMS, QueueRequestor requestorJMS, String resourceName, String methodName) {
      try {
         return sendAdminMessage(clazz, sessionJMS, requestorJMS, resourceName, methodName);
      } catch (Exception e) {
         log.warn("Exception occurred when processing an admin message: {}", e);
         return null;
      }
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }

}

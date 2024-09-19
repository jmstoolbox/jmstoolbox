/*
 * Copyright (C) 2024 Denis Forveille titou10.titou10@gmail.com
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

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jms.Connection;
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
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQSession;
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
 * Implements Apache ActiveMQ Artemis v2.x Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class ActiveMQArtemis2QManager extends QManager {

   private static final org.slf4j.Logger      log                         = LoggerFactory.getLogger(ActiveMQArtemis2QManager.class);

   private static final SimpleDateFormat      SDF                         = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS");
   private static final String                CR                          = "\n";
   private static final String                NA                          = "n/a";

   private static final String                V200                        = "2.0.0";
   private static final String                V200_GET_ROUTING_MTD        = "deliveryModesAsJSON";
   private static final String                V201_GET_ROUTING_MTD        = "routingTypesAsJSON";

   private static final String                P_EXTRA_PROPERTIES          = "z_ExtraNettyProperties";
   private static final String                EXTRA_PROPERTIES_SEP        = ";";
   private static final String                EXTRA_PROPERTIES_VAL        = "=";

   private static final String                P_CF_MIN_LARGE_MESSAGE_SIZE = "minLargeMessageSize";
   private static final String                P_CF_COMPRESS_LARGE_MESSAGE = "compressLargeMessage";

   private static final String                HELP_TEXT;

   private List<QManagerProperty>             parameters                  = new ArrayList<QManagerProperty>();

   private final Map<Integer, Session>        sessionJMSs                 = new HashMap<>();
   private final Map<Integer, QueueRequestor> requestorJMSs               = new HashMap<>();

   public ActiveMQArtemis2QManager() {
      log.debug("Apache Active MQ Artemis v2.x+");

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
      parameters.add(new QManagerProperty(TransportConstants.KEYSTORE_ALIAS_PROP_NAME, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(TransportConstants.KEYSTORE_PATH_PROP_NAME, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(TransportConstants.KEYSTORE_TYPE_PROP_NAME, false, JMSPropertyKind.STRING, true));

      parameters.add(new QManagerProperty(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(TransportConstants.TRUSTSTORE_TYPE_PROP_NAME, false, JMSPropertyKind.STRING, true));

      parameters
               .add(new QManagerProperty(P_EXTRA_PROPERTIES,
                                         false,
                                         JMSPropertyKind.STRING,
                                         false,
                                         "Any netty connector properties separated by semicolons as defined there:\n" +
                                                "   https://activemq.apache.org/artemis/docs/latest/configuring-transports.html \n" +
                                                "eg: trustAll=true;tcpNoDelay=true;tcpSendBufferSize=16000"));

      parameters.add(new QManagerProperty(P_CF_MIN_LARGE_MESSAGE_SIZE,
                                          false,
                                          JMSPropertyKind.INT,
                                          false,
                                          "minLargeMessageSize connection factory parameter. Defaults to 100K",
                                          null));
      parameters.add(new QManagerProperty(P_CF_COMPRESS_LARGE_MESSAGE,
                                          false,
                                          JMSPropertyKind.BOOLEAN,
                                          false,
                                          "compressLargeMessage connection factory parameter. Defaults to false",
                                          null));
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
         String extraNettyProperties = mapProperties.get(P_EXTRA_PROPERTIES);

         String sslEnabled = mapProperties.get(TransportConstants.SSL_ENABLED_PROP_NAME);

         String keyStore = mapProperties.get(TransportConstants.KEYSTORE_PATH_PROP_NAME);
         String keyStorePassword = mapProperties.get(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME);
         String keyStoreType = mapProperties.get(TransportConstants.KEYSTORE_TYPE_PROP_NAME);
         String keyStoreAlias = mapProperties.get(TransportConstants.KEYSTORE_ALIAS_PROP_NAME);

         String trustStore = mapProperties.get(TransportConstants.TRUSTSTORE_PATH_PROP_NAME);
         String trustStorePassword = mapProperties.get(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME);
         String trustStoreType = mapProperties.get(TransportConstants.TRUSTSTORE_TYPE_PROP_NAME);

         String minLargeMessageSize = mapProperties.get(P_CF_MIN_LARGE_MESSAGE_SIZE);
         String compressLargeMessage = mapProperties.get(P_CF_COMPRESS_LARGE_MESSAGE);

         // Netty Connection Properties
         Map<String, Object> connectionParams = new HashMap<String, Object>();
         connectionParams.put(TransportConstants.HOST_PROP_NAME, sessionDef.getHost()); // localhost
         connectionParams.put(TransportConstants.PORT_PROP_NAME, sessionDef.getPort()); // 61616

         if (httpUpgradeEnabled != null) {
            if (Boolean.valueOf(httpUpgradeEnabled)) {
               connectionParams.put(TransportConstants.HTTP_UPGRADE_ENABLED_PROP_NAME, "true");
            }
         }

         // https://activemq.apache.org/components/artemis/documentation/latest/configuring-transports.html#configuring-netty-ssl
         if (sslEnabled != null) {
            if (Boolean.valueOf(sslEnabled)) {
               connectionParams.put(TransportConstants.SSL_ENABLED_PROP_NAME, "true");
               if (keyStore != null) {
                  connectionParams.put(TransportConstants.KEYSTORE_PATH_PROP_NAME, keyStore);
               }
               if (keyStorePassword != null) {
                  connectionParams.put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME, keyStorePassword);
               }
               if (keyStoreType != null) {
                  connectionParams.put(TransportConstants.KEYSTORE_TYPE_PROP_NAME, keyStoreType);
               }
               if (keyStoreAlias != null) {
                  connectionParams.put(TransportConstants.KEYSTORE_ALIAS_PROP_NAME, keyStoreAlias);
               }
               if (trustStore != null) {
                  connectionParams.put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, trustStore);
               }
               if (trustStorePassword != null) {
                  connectionParams.put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, trustStorePassword);
               }
               if (trustStoreType != null) {
                  connectionParams.put(TransportConstants.TRUSTSTORE_TYPE_PROP_NAME, trustStoreType);
               }
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

         ActiveMQConnectionFactory cfJMS = ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, tcJMS);

         // Connection Factory parameters
         if (minLargeMessageSize != null) {
            cfJMS.setMinLargeMessageSize(Integer.parseInt(minLargeMessageSize));
         }
         if (compressLargeMessage != null) {
            if (Boolean.valueOf(compressLargeMessage)) {
               cfJMS.setCompressLargeMessage(true);
            }
         }

         // JMS Connections

         Connection jmsConnection = cfJMS.createConnection(sessionDef.getActiveUserid(), sessionDef.getActivePassword());
         jmsConnection.setClientID(clientID);
         jmsConnection.start();

         // Admin Objects

         Session sessionJMS = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         Queue managementQueue = ((ActiveMQSession) sessionJMS).createQueue("activemq.management");
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
   public DestinationData discoverDestinations(Connection jmsConnection, boolean showSystemObjects) throws Exception {

      log.debug("discoverDestinations : {} - {}", jmsConnection, showSystemObjects);

      Integer hash = jmsConnection.hashCode();
      QueueRequestor requestorJMS = requestorJMSs.get(hash);
      Session sessionJMS = sessionJMSs.get(hash);

      // Determine server version
      // in v2.0.0, deliveryModesAsJSON is used. In v2.0.1+, getRoutingTypesAsJSON is used
      String version = sendAdminMessage(String.class, sessionJMS, requestorJMS, ResourceNames.BROKER, "version");
      log.info("Apache Active MQ Artemis Server is version '{}'", version);
      String getRoutingTypeMtd = version.equals(V200) ? V200_GET_ROUTING_MTD : V201_GET_ROUTING_MTD;

      // Get Queues + Topics the v2.0 way:
      // https://activemq.apache.org/artemis/docs/2.0.0/address-model.html
      // https://activemq.apache.org/artemis/docs/2.0.0/jms-core-mapping.html

      SortedSet<QueueData> listQueueData = new TreeSet<>();
      SortedSet<TopicData> listTopicData = new TreeSet<>();
      Object[] addressNames = sendAdminMessage(Object[].class, sessionJMS, requestorJMS, ResourceNames.BROKER, "addressNames");
      for (Object o : addressNames) {
         log.debug("addressName: {}", o);

         String addressName = (String) o;

         if (addressName.startsWith("$sys")) {
            log.debug("addressName: {} starts with '$sys'. Skip it.", addressName);
            continue;
         }

         String deliveryMode = sendAdminMessage(String.class,
                                                sessionJMS,
                                                requestorJMS,
                                                ResourceNames.ADDRESS + addressName,
                                                getRoutingTypeMtd);

         Object[] queues = sendAdminMessage(Object[].class,
                                            sessionJMS,
                                            requestorJMS,
                                            ResourceNames.ADDRESS + addressName,
                                            "queueNames");

         log.debug("addressName: {} deliveryMode: {} queues: {}", addressName, deliveryMode, queues);

         if (deliveryMode.contains("MULTICAST")) {
            if (deliveryMode.contains("ANYCAST")) {
               // MULTICAST + ANYCAST addresses are Queues
               log.debug("addressName: {} is a Queue (deliveryMode contains MULTICAST and ANYCAST)", addressName);
               listQueueData.add(new QueueData((String) addressName));
            } else {
               // MULTICAST only addresses are Topics
               log.debug("addressName: {} is a Topic (deliveryMode contains only MULTICAST)", addressName);
               listTopicData.add(new TopicData((String) addressName));
            }
            continue; // DF not sure of this..
         }

         // ANYCAST addresses with no queues are ... (I don't know, ignore them)
         if (queues.length == 0) {
            log.warn("addressName: {} is ANYCAST with no queues, Ignore it.", addressName);
            continue;
         }

         // ANYCAST addresses with one queue with the same name are Queues
         if ((queues.length == 1) && (queues[0].equals(addressName))) {
            log.debug("addressName: {} is a Queue (ANYCAST with first queue name the same)", addressName);
            listQueueData.add(new QueueData((String) addressName));
            continue;
         }

         // Other ANYCAST adresses are Topics
         log.debug("addressName: {} is a Topic (ANYCAST with first queue that do not match address name", addressName);
         listTopicData.add(new TopicData((String) addressName));

         //
         // Object[] queueNames = sendAdminMessage(Object[].class,
         // sessionJMS,
         // requestorJMS,
         // ResourceNames.ADDRESS + addressName,
         // "queueNames");
         //
         // for (Object queueName : queueNames) {
         // log.debug("addressName: {} queueName: {}", addressName, queueName);
         //
         // Boolean temporary = sendAdminMessage(Boolean.class,
         // sessionJMS,
         // requestorJMS,
         // ResourceNames.QUEUE + queueName,
         // "temporary");
         // if (!showSystemObjects && temporary) {
         // log.debug("This is a temporary queue and preference says to not show system objets. Skip it");
         // continue;
         // }
         //
         // // String fullyQualifiedQueueName = addressIsTopic ? (String) queueName : addressName + "::" + (String) queueName;
         // String fullyQualifiedQueueName = (String) queueName;
         // listQueueData.add(new QueueData(fullyQualifiedQueueName));
         // }
      }

      // Exclude Temporary Objects if necessary
      if (!showSystemObjects) {
         SortedSet<QueueData> listQueueDataTemp = new TreeSet<>();
         for (QueueData queueData : listQueueData) {
            Boolean temporary = sendAdminMessage(Boolean.class,
                                                 sessionJMS,
                                                 requestorJMS,
                                                 ResourceNames.QUEUE + queueData.getName(),
                                                 "temporary");
            if (temporary) {
               log.debug("addressName: {} is a temporary queue and preference says to not show system objets. Skip it",
                         queueData.getName());
               continue;
            }
            listQueueDataTemp.add(queueData);
         }
         listQueueData = listQueueDataTemp;
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
            log.warn("Exception occurred while closing requestorJMS. Ignore it. Msg={}", e.getMessage());
         }
         requestorJMSs.remove(hash);
      }

      if (sessionJMS != null) {
         try {
            sessionJMS.close();
         } catch (Exception e) {
            log.warn("Exception occurred while closing sessionJMS. Ignore it. Msg={}", e.getMessage());
         }
         sessionJMSs.remove(hash);
      }

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occurred while closing jmsConnection. Ignore it. Msg={}", e.getMessage());
      }
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {
      Integer hash = jmsConnection.hashCode();
      QueueRequestor requestorJMS = requestorJMSs.get(hash);
      Session sessionJMS = sessionJMSs.get(hash);

      // Number n = samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "messageCount");
      Number n = samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.ADDRESS + queueName, "messageCount");
      return n == null ? null : n.intValue();
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      QueueRequestor requestorJMS = requestorJMSs.get(hash);
      Session sessionJMS = sessionJMSs.get(hash);

      // Source: org.apache.activemq.artemis.api.core.management.QueueControl

      SortedMap<String, Object> properties = new TreeMap<>();
      try {

         properties.put("Consumer Count",
                        samNull(Integer.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "consumerCount"));
         properties.put("Dead Letter Address",
                        samNull(String.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "deadLetterAddress"));
         properties.put("Delivering Count",
                        samNull(Integer.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "deliveringCount"));
         properties.put("Expiry Address",
                        samNull(String.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "ExpiryAddress"));
         properties.put("Filter", samNull(String.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "filter"));
         properties.put("Max Consumers",
                        samNull(Integer.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "maxConsumers"));
         properties.put("Message Count",
                        samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "messageCount"));
         properties.put("Message Acknowledged",
                        samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "messagesAcknowledged"));
         properties.put("Message Added",
                        samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "messagesAdded"));
         properties.put("Message Expired",
                        samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "messagesExpired"));
         properties.put("Message Killed",
                        samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "messagesKilled"));
         properties.put("Scheduled Count",
                        samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "scheduledCount"));
         properties.put("Durable", samNull(Boolean.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "durable"));
         properties.put("Paused", samNull(Boolean.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "paused"));
         properties.put("Purge on no Consumers",
                        samNull(Boolean.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "purgeOnNoConsumers"));
         properties.put("Temporary",
                        samNull(Boolean.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "temporary"));

         Long fmAge = samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "firstMessageAge");
         properties.put("First Message Age",
                        fmAge == null ? NA
                                 : Duration.ofMillis(fmAge.longValue()).toString().replace("PT", " ").replace("H", "h ")
                                          .replace("M", "m ").replace("S", "s"));

         Long ts = samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.QUEUE + queueName, "firstMessageTimestamp");
         properties.put("First Message Timestamp", ts == null ? NA : SDF.format(new Date(ts.longValue())));

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

      // Source: org.apache.activemq.artemis.api.core.management.AddressControl

      TreeMap<String, Object> properties = new TreeMap<>();
      try {

         properties.put("AddressSize",
                        samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.ADDRESS + topicName, "addressSize"));
         properties.put("Message Count",
                        samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.ADDRESS + topicName, "messageCount"));
         properties.put("Nb of bytes per page",
                        samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.ADDRESS + topicName, "numberOfBytesPerPage"));
         properties.put("Nb of messages",
                        samNull(Long.class, sessionJMS, requestorJMS, ResourceNames.ADDRESS + topicName, "numberOfMessages"));
         properties.put("Nb of pages",
                        samNull(Integer.class, sessionJMS, requestorJMS, ResourceNames.ADDRESS + topicName, "numberOfPages"));

         properties.put("Paging", samNull(Boolean.class, sessionJMS, requestorJMS, ResourceNames.ADDRESS + topicName, "paging"));

      } catch (Exception e) {
         log.error("Exception occurred in getTopicInformation()", e);
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
      sb.append("No extra jar is needed as JMSToolBox is bundled with the latest Apache ActiveMQ Artemis v2.x jars").append(CR);
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
      sb.append("- keyStorePath       : Key store (eg D:/somewhere/key.jks)").append(CR);
      sb.append("- keyStorePassword   : Key store password").append(CR);
      sb.append("- keyStoreType       : Key store type. For example, JKS, JCEKS, PKCS12, PEM etc.").append(CR);
      sb.append("- keyStoreAlias      : Alias to select from the SSL key store").append(CR);
      sb.append("- trustStorePath     : Trust store (eg D:/somewhere/trust.jks)").append(CR);
      sb.append("- trustStorePassword : Trust store password").append(CR);
      sb.append("- trustStoreType     : Trust store type. For example, JKS, JCEKS, PKCS12, PEM etc.").append(CR);
      sb.append(CR);
      sb.append("- z_ExtraNettyProperties : semicolon separated list of netty connector properties").append(CR);
      sb.append("                         : eg \"trustAll=true;tcpNoDelay=true;tcpSendBufferSize=16000\"").append(CR);
      sb.append("                         : for details, visit https://activemq.apache.org/artemis/docs/latest/configuring-transports.html")
               .append(CR);
      sb.append(CR);
      sb.append("- minLargeMessageSize    : Connection factory parameter. Artemis will consider larger messages as 'large messages'. Defaults to 100K")
               .append(CR);
      sb.append("- compressLargeMessage   : Connection factory parameter.  Compress large messages? Defaults to false").append(CR);
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
      return (T) JMSManagementHelper.getResult(r);
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

/*
 * Copyright (C) 2020 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.qm.azuresb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.DestinationData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

import com.microsoft.azure.servicebus.jms.ReconnectAmqpOpenServerListAction;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactory;
import com.microsoft.azure.servicebus.jms.ServiceBusJmsConnectionFactorySettings;
import com.microsoft.azure.servicebus.management.ManagementClient;
import com.microsoft.azure.servicebus.management.QueueDescription;
import com.microsoft.azure.servicebus.management.TopicDescription;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;

/**
 *
 * Implements Azure Service Bus Q Provider
 *
 * https://docs.microsoft.com/en-us/azure/service-bus-messaging/how-to-use-java-message-service-20
 *
 * @author Denis Forveille
 * @author anqyan@microsoft.com
 *
 */
public class AzureServiceBusQManager extends QManager {

   private static final Logger                  log                                 = LoggerFactory
            .getLogger(AzureServiceBusQManager.class);

   private static final String                  CR                                  = "\n";

   private static final String                  CONNECTION_STRING_FORMAT            = "Endpoint=sb://%s;SharedAccessKeyName=%s;SharedAccessKey=%s";

   private static final String                  P_CONN_STR                          = "connectionString";
   private static final String                  P_CONN_IDLE_TIMEOUT                 = "idleTimeout";
   private static final String                  P_TRACE_FRAMES                      = "traceAmqpFrames";
   private static final String                  P_SHOULD_RECONNECT                  = "shouldReconnect";
   private static final String                  P_RECONNECT_HOSTS                   = "reconnectHosts";
   private static final String                  P_INITIAL_RECONNECT_DELAY           = "initialReconnectDelay";
   private static final String                  P_RECONNECT_DELAY                   = "reconnectDelay";
   private static final String                  P_MAX_RECONNECT_DELAY               = "maxReconnectDelay";
   private static final String                  P_USE_RECONNECT_BACKOFF             = "useReconnectBackoff";
   private static final String                  P_RECONNECT_BACKOFF_MULTIPLIER      = "reconnectBackoffMultiplier";
   private static final String                  P_MAX_RECONNECT_ATTEMPTS            = "maxReconnectAttempts";
   private static final String                  P_STARTUP_MAX_RECONNECT_ATTEMPTS    = "startUpMaxReconnectAttempts";
   private static final String                  P_WARN_AFTER_MAX_RECONNECT_ATTEMPTS = "warnAfterMaxReconnectAttempts";
   private static final String                  P_RECONNECT_RANDOMIZE               = "reconnectRandomize";
   private static final String                  P_RECONNECT_ACTION                  = "reconnectAmqpOpenServerListAction";

   private static final String                  HELP_TEXT;

   private final List<QManagerProperty>         parameters                          = new ArrayList<>();

   private final Map<Integer, ManagementClient> mgmgClients                         = new HashMap<>();
   private final Map<Integer, Session>          sessionJMSs                         = new HashMap<>();

   public AzureServiceBusQManager() {
      log.debug("Azure Service Bus");

      parameters
               .add(new QManagerProperty(P_CONN_STR,
                                         false,
                                         JMSPropertyKind.STRING,
                                         true,
                                         "Connection String for Azure Service Bus. If provided, it will override dummy host/port in the session configuration.",
                                         null));

      parameters.add(new QManagerProperty(P_CONN_IDLE_TIMEOUT,
                                          false,
                                          JMSPropertyKind.LONG,
                                          false,
                                          "AMQP connection idle timeout for Azure Service Bus",
                                          "120000"));

      parameters.add(new QManagerProperty(P_TRACE_FRAMES, false, JMSPropertyKind.BOOLEAN, false, "AMQP level logging flag", null));

      parameters.add(new QManagerProperty(P_SHOULD_RECONNECT,
                                          false,
                                          JMSPropertyKind.BOOLEAN,
                                          false,
                                          "True if the reconnect functionalities implement by QPID should be leveraged",
                                          null));

      parameters.add(new QManagerProperty(P_RECONNECT_HOSTS,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Hosts to reconnect, seperated by comma",
                                          null));

      parameters.add(new QManagerProperty(P_INITIAL_RECONNECT_DELAY,
                                          false,
                                          JMSPropertyKind.LONG,
                                          false,
                                          "Initial reconnect delay",
                                          null));

      parameters.add(new QManagerProperty(P_RECONNECT_DELAY, false, JMSPropertyKind.LONG, false, "Reconnect delay", null));

      parameters.add(new QManagerProperty(P_MAX_RECONNECT_DELAY,
                                          false,
                                          JMSPropertyKind.LONG,
                                          false,
                                          "Maximum reconnect delay",
                                          null));

      parameters
               .add(new QManagerProperty(P_USE_RECONNECT_BACKOFF,
                                         false,
                                         JMSPropertyKind.BOOLEAN,
                                         false,
                                         "True if the time between reconnection attempts should grow based on a configured multiplier",
                                         null));

      parameters.add(new QManagerProperty(P_RECONNECT_BACKOFF_MULTIPLIER,
                                          false,
                                          JMSPropertyKind.DOUBLE,
                                          false,
                                          "The multiplier used to grow the reconnection delay value",
                                          null));

      parameters
               .add(new QManagerProperty(P_MAX_RECONNECT_ATTEMPTS,
                                         false,
                                         JMSPropertyKind.INT,
                                         false,
                                         "The number of reconnection attempts allowed before reporting the connection as failed to the client",
                                         null));

      parameters
               .add(new QManagerProperty(P_STARTUP_MAX_RECONNECT_ATTEMPTS,
                                         false,
                                         JMSPropertyKind.INT,
                                         false,
                                         "For a client that has never connected to a remote peer before this option control how many attempts are made to connect before reporting the connection as failed",
                                         null));

      parameters
               .add(new QManagerProperty(P_WARN_AFTER_MAX_RECONNECT_ATTEMPTS,
                                         false,
                                         JMSPropertyKind.INT,
                                         false,
                                         "Number of reconnection attempts before the client will log a message indicating that reconnect reconnection is being attempted",
                                         null));

      parameters
               .add(new QManagerProperty(P_RECONNECT_RANDOMIZE,
                                         false,
                                         JMSPropertyKind.BOOLEAN,
                                         false,
                                         "True if the set of reconnect URIs is randomly shuffled prior to attempting to connect to one of them",
                                         null));

      parameters
               .add(new QManagerProperty(P_RECONNECT_ACTION,
                                         false,
                                         JMSPropertyKind.STRING,
                                         false,
                                         "How the reconnect transport behaves when the connection Open frame from the remote peer provides a list of reconnect hosts to the client",
                                         null));
   }

   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Save System properties
      saveSystemProperties();
      try {

         String host = sessionDef.getHost();
         String userId = sessionDef.getActiveUserid();
         String password = sessionDef.getActivePassword();

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         String serviceBusConnectionString = mapProperties.get(P_CONN_STR);
         String connectionIdleTimeout = mapProperties.get(P_CONN_IDLE_TIMEOUT);
         String traceFrames = mapProperties.get(P_TRACE_FRAMES);
         String shouldReconnect = mapProperties.get(P_SHOULD_RECONNECT);
         String reconnectHosts = mapProperties.get(P_RECONNECT_HOSTS);
         String initialReconnectDelay = mapProperties.get(P_INITIAL_RECONNECT_DELAY);
         String reconnectDelay = mapProperties.get(P_RECONNECT_DELAY);
         String maxReconnectDelay = mapProperties.get(P_MAX_RECONNECT_DELAY);
         String shouldUseReconnectBackoff = mapProperties.get(P_USE_RECONNECT_BACKOFF);
         String reconnectBackoffMultiplier = mapProperties.get(P_RECONNECT_BACKOFF_MULTIPLIER);
         String maxReconnectAttempts = mapProperties.get(P_MAX_RECONNECT_ATTEMPTS);
         String startupMaxReconnectAttempts = mapProperties.get(P_STARTUP_MAX_RECONNECT_ATTEMPTS);
         String warnAfterReconnectAttempts = mapProperties.get(P_WARN_AFTER_MAX_RECONNECT_ATTEMPTS);
         String shouldReconnectRandomize = mapProperties.get(P_RECONNECT_RANDOMIZE);
         String reconnectAction = mapProperties.get(P_RECONNECT_ACTION);

         ServiceBusJmsConnectionFactorySettings connectionFactorySettings = new ServiceBusJmsConnectionFactorySettings();
         connectionFactorySettings.setConnectionIdleTimeoutMS(Long.valueOf(connectionIdleTimeout));
         if (traceFrames != null) {
            connectionFactorySettings.setTraceFrames(Boolean.valueOf(traceFrames.toLowerCase()));
         }
         if (reconnectHosts != null) {
            String[] hosts = reconnectHosts.split(",");
            for (int i = 0; i < hosts.length; i++) {
               hosts[i] = hosts[i].trim();
            }
            connectionFactorySettings.setReconnectHosts(hosts);
         }
         if (shouldReconnect != null) {
            connectionFactorySettings.setShouldReconnect(Boolean.valueOf(shouldReconnect.toLowerCase()));
         }
         if (initialReconnectDelay != null) {
            connectionFactorySettings.setInitialReconnectDelay(Long.valueOf(initialReconnectDelay));
         }
         if (reconnectDelay != null) {
            connectionFactorySettings.setReconnectDelay(Long.valueOf(reconnectDelay));
         }
         if (maxReconnectDelay != null) {
            connectionFactorySettings.setMaxReconnectDelay(Long.valueOf(maxReconnectDelay));
         }
         if (shouldUseReconnectBackoff != null) {
            connectionFactorySettings.setUseReconnectBackOff(Boolean.valueOf(shouldUseReconnectBackoff.toLowerCase()));
         }
         if (reconnectBackoffMultiplier != null) {
            connectionFactorySettings.setReconnectBackOffMultiplier(Double.valueOf(reconnectBackoffMultiplier));
         }
         if (maxReconnectAttempts != null) {
            connectionFactorySettings.setMaxReconnectAttempts(Integer.valueOf(maxReconnectAttempts));
         }
         if (startupMaxReconnectAttempts != null) {
            connectionFactorySettings.setStartupMaxReconnectAttempts(Integer.valueOf(startupMaxReconnectAttempts));
         }
         if (warnAfterReconnectAttempts != null) {
            connectionFactorySettings.setWarnAfterReconnectAttempts(Integer.valueOf(warnAfterReconnectAttempts));
         }
         if (shouldReconnectRandomize != null) {
            connectionFactorySettings.setReconnectRandomize(Boolean.valueOf(shouldReconnectRandomize.toLowerCase()));
         }
         if (reconnectAction != null) {
            connectionFactorySettings
                     .setReconnectAmqpOpenServerListAction(ReconnectAmqpOpenServerListAction.valueOf(reconnectAction));
         }

         // Connection string fall-back logic:
         // - The connectionString property is optional
         // - If the property is set in the session definition, use it and ask the user to set a fake host/port
         // - If not, build the connection string from the host/port/user/pwd fields
         ConnectionFactory factory = null;
         ManagementClient mgmtClient = null;
         if (serviceBusConnectionString != null) {
            factory = new ServiceBusJmsConnectionFactory(serviceBusConnectionString, connectionFactorySettings);
            mgmtClient = new ManagementClient(new ConnectionStringBuilder(serviceBusConnectionString));
         } else {
            factory = new ServiceBusJmsConnectionFactory(userId, password, host, connectionFactorySettings);
            mgmtClient = new ManagementClient(new ConnectionStringBuilder(String
                     .format(CONNECTION_STRING_FORMAT, host, userId, password)));
         }

         Connection jmsConnection = factory.createConnection();
         jmsConnection.setClientID(clientID);
         jmsConnection.start();

         Session sessionJMS = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         // Store per connection related data
         Integer hash = jmsConnection.hashCode();
         sessionJMSs.put(hash, sessionJMS);
         mgmgClients.put(hash, mgmtClient);

         return jmsConnection;

      } finally {
         restoreSystemProperties();
      }
   }

   @Override
   public DestinationData discoverDestinations(Connection jmsConnection, boolean showSystemObjects) throws Exception {
      log.debug("discoverDestinations : {} - {}", jmsConnection, showSystemObjects);

      Integer hash = jmsConnection.hashCode();
      ManagementClient mgmtClient = mgmgClients.get(hash);

      List<QueueDescription> queues = mgmtClient.getQueues();
      SortedSet<QueueData> listQueueData = queues.stream().map(q -> new QueueData(q.getPath()))
               .collect(Collectors.toCollection(TreeSet::new));

      List<TopicDescription> topics = mgmtClient.getTopics();
      SortedSet<TopicData> listTopicData = topics.stream().map(t -> new TopicData(t.getPath()))
               .collect(Collectors.toCollection(TreeSet::new));

      return new DestinationData(listQueueData, listTopicData);
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      Integer hash = jmsConnection.hashCode();
      Session sessionJMS = sessionJMSs.get(hash);
      ManagementClient mgmtClient = mgmgClients.get(hash);

      // Cleanup tasks

      if (mgmtClient != null) {
         try {
            mgmtClient.close();
         } catch (Exception e) {
            log.warn("Exception occurred while closing ManagementClient. Ignore it. Msg={}", e.getMessage());
         }
         mgmgClients.remove(hash);
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

      // To be supported
      //
      // ManagementClient.getQueueRuntimeInfo(String path) could block and freeze the UI if the queue is not loaded in Service Bus
      // Service Bus Java SDK currently doesn't have an easy way to reload a queue except for operations like sending messages

      return null;
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      ManagementClient mgmtClient = mgmgClients.get(hash);

      SortedMap<String, Object> properties = new TreeMap<>();
      try {
         QueueDescription queueDescription = mgmtClient.getQueue(queueName);
         properties.put("Path(queue name)", queueDescription.getPath());
         properties.put("AutoDeleteOnIdle", queueDescription.getAutoDeleteOnIdle());
         properties.put("DefaultMessageTimeToLive", queueDescription.getDefaultMessageTimeToLive());
         properties.put("DuplicationDetectionHistoryTimeWindow", queueDescription.getDuplicationDetectionHistoryTimeWindow());
         properties.put("EntityStatus", queueDescription.getEntityStatus());
         properties.put("ForwardDeadLetteredMessagesTo", queueDescription.getForwardDeadLetteredMessagesTo());
         properties.put("ForwardTo", queueDescription.getForwardTo());
         properties.put("LockDuration", queueDescription.getLockDuration());
         properties.put("MaxDeliveryCount", queueDescription.getMaxDeliveryCount());
         properties.put("MaxSizeInMB", queueDescription.getMaxSizeInMB());
         properties.put("EnableBatchedOperations", queueDescription.isEnableBatchedOperations());
         properties.put("EnableDeadLetteringOnMessageExpiration", queueDescription.isEnableDeadLetteringOnMessageExpiration());
         properties.put("EnablePartitioning", queueDescription.isEnablePartitioning());
         properties.put("RequiresDuplicateDetection", queueDescription.isRequiresDuplicateDetection());
         properties.put("RequiresSession", queueDescription.isRequiresSession());
      } catch (Exception e) {
         log.error("Exception occurred in getQueueInformation()", e);
      }

      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {

      Integer hash = jmsConnection.hashCode();
      ManagementClient mgmtClient = mgmgClients.get(hash);

      SortedMap<String, Object> properties = new TreeMap<>();
      try {
         TopicDescription topicDescription = mgmtClient.getTopic(topicName);
         properties.put("Path(queue name)", topicDescription.getPath());
         properties.put("AutoDeleteOnIdle", topicDescription.getAutoDeleteOnIdle());
         properties.put("DefaultMessageTimeToLive", topicDescription.getDefaultMessageTimeToLive());
         properties.put("DuplicationDetectionHistoryTimeWindow", topicDescription.getDuplicationDetectionHistoryTimeWindow());
         properties.put("EntityStatus", topicDescription.getEntityStatus());
         properties.put("MaxSizeInMB", topicDescription.getMaxSizeInMB());
         properties.put("EnableBatchedOperation", topicDescription.isEnableBatchedOperations());
         properties.put("EnablePartitioning", topicDescription.isEnablePartitioning());
         properties.put("RequiresDuplicateDetection", topicDescription.isRequiresDuplicateDetection());
         properties.put("SupportOrdering", topicDescription.isSupportOrdering());
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
      sb.append("Requirements").append(CR);
      sb.append("------------").append(CR);
      sb.append("An Azure Service Bus namespace in Premium tier is required.").append(CR);
      sb.append(CR);
      sb.append("The session definiton requires either:").append(CR);
      sb.append("- the usage of a 'Service Bus connnection string' set in properties").append(CR);
      sb.append("- the usage of the host, user and password fields set on the main session definition page").append(CR);
      sb.append("In the former case, dummy values for host and port must be provided").append(CR);
      sb.append(CR);
      sb.append("https://docs.microsoft.com/en-us/azure/service-bus-messaging/how-to-use-java-message-service-20").append(CR);
      sb.append(CR);
      sb.append("Extra JARS :").append(CR);
      sb.append("------------").append(CR);
      sb.append("No extra jar is needed as JMSToolBox is bundled with the latest Azure Service Bus jars").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : if the 'connectionString' property is not provided, the Azure Service Bus host name (eg abcdef.servicebus.windows.net),")
               .append(CR);
      sb.append("                otherwise a dummy string").append(CR);
      sb.append("Port          : a dummy value").append(CR);
      sb.append("User/Password : if the 'connectionString' property is not provided,the Azure Service Bus SAQ key name and SAS key")
               .append(CR);
      sb.append("                otherwise leave empty").append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("- connectionString : Azure Service Bus connection string. If provided, the host field in the session is useless and must be set to a dummy string")
               .append(CR);
      sb.append("- idleTimeout      : AMQP connection idle timeout for Azure Service Bus").append(CR);
      sb.append("- traceAmqpFrames  : Whether to enable AMQP level logging: https://qpid.apache.org/releases/qpid-jms-0.54.0/docs/index.html#logging")
               .append(CR);
      sb.append("Other reconnection related properties: ").append(CR);
      sb.append("https://github.com/Azure/azure-servicebus-jms/blob/master/src/main/java/com/microsoft/azure/servicebus/jms/ServiceBusJmsConnectionFactorySettings.java")
               .append(CR);
      sb.append(CR);

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

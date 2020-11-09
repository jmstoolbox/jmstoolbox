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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.DestinationData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

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
 * @author Denis Forveille
 * @author anqyan@microsoft.com
 *
 */
public class AzureServiceBusQManager extends QManager {

   private static final org.slf4j.Logger log         = LoggerFactory.getLogger(AzureServiceBusQManager.class);
   private static final String           CR          = "\n";
   private static final String           P_CONN_STR  = "ConnectionString";
   private static final String           HELP_TEXT;
   private final Map<Integer, Session>   sessionJMSs = new HashMap<>();
   private List<QManagerProperty>        parameters  = new ArrayList<>();
   private String                        serviceBusConnectionString;
   private ManagementClient              serviceBusManagementClient;

   public AzureServiceBusQManager() {
      log.debug("Azue Service Bus");

      parameters.add(new QManagerProperty(P_CONN_STR,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Connection String for Azure Service Bus",
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

         serviceBusConnectionString = mapProperties.get(P_CONN_STR);
         serviceBusManagementClient = new ManagementClient(new ConnectionStringBuilder(serviceBusConnectionString));

         // Connect to Server https://docs.microsoft.com/en-us/azure/service-bus-messaging/how-to-use-java-message-service-20

         ServiceBusJmsConnectionFactorySettings connFactorySettings = new ServiceBusJmsConnectionFactorySettings();
         connFactorySettings.setConnectionIdleTimeoutMS(20000);

         ConnectionFactory factory = new ServiceBusJmsConnectionFactory(serviceBusConnectionString, connFactorySettings);

         // JMS Connection

         Connection jmsConnection = factory.createConnection();
         jmsConnection.setClientID(clientID);
         jmsConnection.start();

         Session sessionJMS = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         // Store per connection related data
         Integer hash = jmsConnection.hashCode();
         sessionJMSs.put(hash, sessionJMS);

         return jmsConnection;

      } finally {
         restoreSystemProperties();
      }
   }

   @Override
   public DestinationData discoverDestinations(Connection jmsConnection, boolean showSystemObjects) throws Exception {
      log.debug("discoverDestinations : {} - {}", jmsConnection, showSystemObjects);

      SortedSet<QueueData> listQueueData = new TreeSet<>();
      SortedSet<TopicData> listTopicData = new TreeSet<>();

      // Get list of Queues + Topics here

      List<QueueDescription> queueDescriptions = serviceBusManagementClient.getQueues();
      List<TopicDescription> topicDescriptions = serviceBusManagementClient.getTopics();

      for (QueueDescription queueDescription : queueDescriptions) {
         String queueName = queueDescription.getPath();
         if ((queueName == null) || (queueName.isEmpty())) {
            log.warn("Queue has an empty name. Ignore it");
            continue;
         }
         listQueueData.add(new QueueData(queueName));
      }

      for (TopicDescription topicDescription : topicDescriptions) {
         String topicName = topicDescription.getPath();
         if ((topicName == null) || (topicName.isEmpty())) {
            log.warn("Topic has an empty name. Ignore it");
            continue;
         }
         listTopicData.add(new TopicData(topicName));
      }

      return new DestinationData(listQueueData, listTopicData);
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      Integer hash = jmsConnection.hashCode();
      Session sessionJMS = sessionJMSs.get(hash);

      // Cleanup tasks

      if (sessionJMS != null) {
         try {
            sessionJMS.close();
         } catch (Exception e) {
            log.warn("Exception occurred while closing sessionJMS. Ignore it. Msg={}", e.getMessage());
         }
         sessionJMSs.remove(hash);
      }

      try {
         serviceBusManagementClient.close();
      } catch (Exception e) {
         log.warn("Exception occurred while closing serviceBusManagementClient. Ignore it. Msg={}", e.getMessage());
      }

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occurred while closing jmsConnection. Ignore it. Msg={}", e.getMessage());
      }
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {

      // Logic to get Q Depth
      // Currently not suppported in
      // https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/servicebus/microsoft-azure-servicebus

      return 0;
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {

      SortedMap<String, Object> properties = new TreeMap<>();
      try {
         // Populates Q info here
         QueueDescription queueDescription = serviceBusManagementClient.getQueue(queueName);
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

      SortedMap<String, Object> properties = new TreeMap<>();
      try {
         // Populates Topic info here
         TopicDescription topicDescription = serviceBusManagementClient.getTopic(topicName);
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
      sb.append("Extra JARS :").append(CR);
      sb.append("------------").append(CR);
      sb.append("No extra jar is needed as JMSToolBox is bundled with the latest Azure Service Bus jars").append(CR);
      sb.append(CR);
      sb.append("Requirements").append(CR);
      sb.append("------------").append(CR);
      sb.append("An Azure Service Bus namespace in Premium tier is required.").append(CR);
      sb.append("A corresponding Service Bus connnection string is required. Please add it to the Properties tab.").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : Azure Service Bus host name (eg abcdef.servicebus.windows.net)").append(CR);
      sb.append("Port          : Azure Service Bus listening port (eg. 5762 for AMQP protocol)").append(CR);
      sb.append("User/Password : Azure Service Bus SAS key name and SAS key");
      sb.append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("- ConnectionString   : An Azure Service Bus connection string").append(CR);
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

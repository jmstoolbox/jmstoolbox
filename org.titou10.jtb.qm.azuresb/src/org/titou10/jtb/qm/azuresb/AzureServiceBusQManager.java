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

/**
 *
 * Implements Azure Service Bus Q Provider
 *
 * @author Denis Forveille
 * @author ??
 *
 */
public class AzureServiceBusQManager extends QManager {

   private static final org.slf4j.Logger log         = LoggerFactory.getLogger(AzureServiceBusQManager.class);

   private static final String           CR          = "\n";
   private static final String           NA          = "n/a";

   private static final String           P_PARAM1    = "param1";

   private static final String           HELP_TEXT;

   private List<QManagerProperty>        parameters  = new ArrayList<>();

   private final Map<Integer, Session>   sessionJMSs = new HashMap<>();

   public AzureServiceBusQManager() {
      log.debug("Azue Service Bus");

      parameters.add(new QManagerProperty(P_PARAM1, false, JMSPropertyKind.BOOLEAN, false, "tooltip param1", null));
   }

   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Save System properties
      saveSystemProperties();
      try {

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         String param1 = mapProperties.get(P_PARAM1);

         // Connect to Server

         // https://docs.microsoft.com/en-us/azure/service-bus-messaging/how-to-use-java-message-service-20

         ServiceBusJmsConnectionFactorySettings connFactorySettings = new ServiceBusJmsConnectionFactorySettings();
         connFactorySettings.setConnectionIdleTimeoutMS(20000);

         String serviceBusConnectionString = "<SERVICE_BUS_CONNECTION_STRING_WITH_MANAGE_PERMISSIONS>";
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

      Integer hash = jmsConnection.hashCode();
      Session sessionJMS = sessionJMSs.get(hash);

      SortedSet<QueueData> listQueueData = new TreeSet<>();
      SortedSet<TopicData> listTopicData = new TreeSet<>();

      // Get list of Queues + Topics here

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
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occurred while closing jmsConnection. Ignore it. Msg={}", e.getMessage());
      }
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      Session sessionJMS = sessionJMSs.get(hash);

      // Logic to get Q Depth

      return 0;
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      Session sessionJMS = sessionJMSs.get(hash);

      SortedMap<String, Object> properties = new TreeMap<>();
      try {

         // Populates Q info here

      } catch (Exception e) {
         log.error("Exception occurred in getQueueInformation()", e);
      }

      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {

      Integer hash = jmsConnection.hashCode();
      Session sessionJMS = sessionJMSs.get(hash);

      SortedMap<String, Object> properties = new TreeMap<>();
      try {
         // Populates Topic info here

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
      sb.append(" explanations here").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : Azure Service Bus host name (eg localhost)").append(CR);
      sb.append("Port          : Azure Service Bus listening port (eg. 61616)").append(CR);
      sb.append("User/Password : User allowed to connect to Azure Service Bus");
      sb.append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      sb.append(" properties here").append(CR);
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

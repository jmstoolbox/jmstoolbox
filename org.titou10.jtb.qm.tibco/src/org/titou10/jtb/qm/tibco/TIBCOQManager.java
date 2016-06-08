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
package org.titou10.jtb.qm.tibco;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.ConnectionData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;

import com.tibco.tibjms.TibjmsConnectionFactory;
import com.tibco.tibjms.admin.QueueInfo;
import com.tibco.tibjms.admin.StatData;
import com.tibco.tibjms.admin.TibjmsAdmin;
import com.tibco.tibjms.admin.TibjmsAdminException;
import com.tibco.tibjms.admin.TopicInfo;

/**
 * 
 * Implements TIBCO ems Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class TIBCOQManager extends QManager {

   private static final Logger             log                   = LoggerFactory.getLogger(TIBCOQManager.class);

   private static final String             CR                    = "\n";

   private static final String             P_PROTOCOL            = "connectionProtocol";

   private static final String             HELP_TEXT;

   private List<QManagerProperty>          parameters            = new ArrayList<QManagerProperty>();

   private static final String             SYSTEM_PREFIX         = "$sys.";
   private static final String             INVALID_SYSTEM_PREFIX = ">";

   private final Map<Integer, TibjmsAdmin> queueManagers         = new HashMap<>();

   public TIBCOQManager() {
      log.debug("Instantiate TIBCOQManager");

      parameters.add(new QManagerProperty(P_PROTOCOL, true, JMSPropertyKind.STRING, false, "Connection protocol ('tcp' or 'ssl'"));

   }

   @Override
   public ConnectionData connect(SessionDef sessionDef, boolean showSystemObjects) throws Exception {
      log.info("connecting to {}", sessionDef.getName());

      // Extract properties
      Map<String, String> mapProperties = extractProperties(sessionDef);

      String protocol = mapProperties.get(P_PROTOCOL);

      // Admin connection
      StringBuilder connectionURL = new StringBuilder(256);
      connectionURL.append(protocol);
      connectionURL.append("://");
      connectionURL.append(sessionDef.getHost());
      connectionURL.append(":");
      connectionURL.append(sessionDef.getPort());

      TibjmsAdmin tibcoAdmin = new TibjmsAdmin(connectionURL.toString(), sessionDef.getUserid(), sessionDef.getPassword());

      // Lookup for Queues
      SortedSet<String> queueNames = new TreeSet<>();
      QueueInfo[] queues = tibcoAdmin.getQueues();
      for (QueueInfo queueInfo : queues) {
         String queueName = queueInfo.getName();
         log.debug("Found Queue {}. Temporary? {}", queueName, queueInfo.isTemporary());

         if (queueName.equals(INVALID_SYSTEM_PREFIX)) {
            continue;
         }

         // Add System and Temporary queues depending on preferences
         if (!showSystemObjects) {
            if (queueName.startsWith(SYSTEM_PREFIX)) {
               continue;
            }

            // TODO DF: not sure to exelude temp Q..
            if (queueInfo.isTemporary()) {
               continue;
            }
         }

         queueNames.add(queueName);
      }

      // Lookup for Topics
      SortedSet<String> topicNames = new TreeSet<>();
      TopicInfo[] topics = tibcoAdmin.getTopics();
      for (TopicInfo topicInfo : topics) {
         String topicName = topicInfo.getName();
         log.debug("Found Topic {}. Temporary? {}", topicName, topicInfo.isTemporary());

         if (topicName.equals(INVALID_SYSTEM_PREFIX)) {
            continue;
         }

         // Add System and Temporary queues depending on preferences
         if (!showSystemObjects) {
            if (topicName.startsWith(SYSTEM_PREFIX)) {
               continue;
            }

            // TODO DF: not sure to exclude temp Q..
            if (topicInfo.isTemporary()) {
               continue;
            }
         }

         topicNames.add(topicName);
      }

      // JMS Connection

      TibjmsConnectionFactory factory = new TibjmsConnectionFactory(connectionURL.toString());
      Connection jmsConnection = factory.createConnection(sessionDef.getUserid(), sessionDef.getPassword());

      log.info("connected to {}", sessionDef.getName());

      // Store per connection related data
      queueManagers.put(jmsConnection.hashCode(), tibcoAdmin);

      return new ConnectionData(jmsConnection, queueNames, topicNames);
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      Integer hash = jmsConnection.hashCode();
      TibjmsAdmin tibcoAdmin = queueManagers.get(hash);

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occured while closing session. Ignore it. Msg={}", e.getMessage());
      }

      if (tibcoAdmin != null) {
         try {
            tibcoAdmin.close();
         } catch (TibjmsAdminException e) {
            log.warn("Exception occured while closing TibjmsAdmin. Ignore it. Msg={}", e.getMessage());
         }
         queueManagers.remove(hash);
      }
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {
      Integer hash = jmsConnection.hashCode();
      TibjmsAdmin tibcoAdmin = queueManagers.get(hash);

      try {
         QueueInfo queueInfo = tibcoAdmin.getQueue(queueName);
         Long depth = queueInfo.getOutboundStatistics().getTotalMessages();

         return depth.intValue();
      } catch (TibjmsAdminException e) {
         log.warn("Exception occured while reading Q depth for {}. Msg={}", queueName, e.getMessage());
         return null;
      }
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {
      Map<String, Object> properties = new LinkedHashMap<>();

      Integer hash = jmsConnection.hashCode();
      TibjmsAdmin tibcoAdmin = queueManagers.get(hash);

      try {
         QueueInfo queueInfo = tibcoAdmin.getQueue(queueName);
         if (queueInfo == null) {
            return null;
         }

         properties.put("Consumer Count", queueInfo.getConsumerCount());
         properties.put("Delivered MessageCount", queueInfo.getDeliveredMessageCount());
         properties.put("Expiry Override", queueInfo.getExpiryOverride());
         properties.put("Flow Control Max Bytes", queueInfo.getFlowControlMaxBytes());
         properties.put("In Transit Message Count", queueInfo.getInTransitMessageCount());
         properties.put("Max Bytes", queueInfo.getMaxBytes());
         properties.put("Max Msgs", queueInfo.getMaxMsgs());
         properties.put("Max Redelivery", queueInfo.getMaxRedelivery());
         properties.put("Overflow Policy", queueInfo.getOverflowPolicy());
         properties.put("Pending Message Count", queueInfo.getPendingMessageCount());
         properties.put("Pending Message Size", queueInfo.getPendingMessageSize());
         properties.put("Pending Persistent Message Count", queueInfo.getPendingPersistentMessageCount());
         properties.put("Pending Persistent Message Size", queueInfo.getPendingPersistentMessageSize());
         properties.put("Prefetch", queueInfo.getPrefetch());
         properties.put("Receiver Count", queueInfo.getReceiverCount());
         properties.put("Redelivery Delay", queueInfo.getRedeliveryDelay());
         properties.put("Route Name", queueInfo.getRouteName());
         properties.put("Store", queueInfo.getStore());

         StatData inStats = queueInfo.getInboundStatistics();
         properties.put("Inbound Stats: Byte Rate", inStats.getByteRate());
         properties.put("Inbound Stats: Message Rate", inStats.getMessageRate());
         properties.put("Inbound Stats: Total Bytes", inStats.getTotalBytes());
         properties.put("Inbound Stats: Total Messages", inStats.getTotalMessages());

         StatData outStats = queueInfo.getOutboundStatistics();
         properties.put("Outbound Stats: Byte Rate", outStats.getByteRate());
         properties.put("Outbound Stats: Message Rate", outStats.getMessageRate());
         properties.put("Outbound Stats: Total Bytes", outStats.getTotalBytes());
         properties.put("Outbound Stats: Total Messages", outStats.getTotalMessages());

      } catch (TibjmsAdminException e) {
         log.error("An exception occured when reading information for queue {}. Msg={}", queueName, e);
      }

      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {
      Map<String, Object> properties = new LinkedHashMap<>();

      Integer hash = jmsConnection.hashCode();
      TibjmsAdmin tibcoAdmin = queueManagers.get(hash);

      try {
         TopicInfo topicInfo = tibcoAdmin.getTopic(topicName);
         if (topicInfo == null) {
            return null;
         }

         properties.put("Active Durable Count", topicInfo.getActiveDurableCount());
         properties.put("Channel", topicInfo.getChannel());
         properties.put("Consumer Count", topicInfo.getConsumerCount());
         properties.put("Durable Subscription Count", topicInfo.getDurableSubscriptionCount());
         properties.put("Flow Control Max Bytes", topicInfo.getFlowControlMaxBytes());
         properties.put("Max Bytes", topicInfo.getMaxBytes());
         properties.put("Max Msgs", topicInfo.getMaxMsgs());
         properties.put("Overflow Policy", topicInfo.getOverflowPolicy());
         properties.put("Pending Message Count", topicInfo.getPendingMessageCount());
         properties.put("Pending Message Size", topicInfo.getPendingMessageSize());
         properties.put("Pending Persistent Message Count", topicInfo.getPendingPersistentMessageCount());
         properties.put("Pending Persistent Message Size", topicInfo.getPendingPersistentMessageSize());
         properties.put("Prefetch", topicInfo.getPrefetch());
         properties.put("Redelivery Delay", topicInfo.getRedeliveryDelay());
         properties.put("Store", topicInfo.getStore());
         properties.put("Subscriber Count", topicInfo.getSubscriberCount());
         properties.put("Subscription Count", topicInfo.getSubscriptionCount());

         StatData inStats = topicInfo.getInboundStatistics();
         properties.put("Inbound Stats: Byte Rate", inStats.getByteRate());
         properties.put("Inbound Stats: Message Rate", inStats.getMessageRate());
         properties.put("Inbound Stats: Total Bytes", inStats.getTotalBytes());
         properties.put("Inbound Stats: Total Messages", inStats.getTotalMessages());

         StatData outStats = topicInfo.getOutboundStatistics();
         properties.put("Outbound Stats: Byte Rate", outStats.getByteRate());
         properties.put("Outbound Stats: Message Rate", outStats.getMessageRate());
         properties.put("Outbound Stats: Total Bytes", outStats.getTotalBytes());
         properties.put("Outbound Stats: Total Messages", outStats.getTotalMessages());

      } catch (TibjmsAdminException e) {
         log.error("An exception occured when reading information for topic {}. Msg={}", topicName, e);
      }

      return properties;
   }

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS (From ems/lib):").append(CR);
      sb.append("-----------------------------").append(CR);
      sb.append("tibjms.jar").append(CR);
      sb.append("tibjmsadmin.jar").append(CR);
      sb.append("If using the ssl protocol, add the following jars)").append(CR);
      sb.append("- slf4j-api-1.5.2.jar").append(CR);
      sb.append("- slf4j-simple-1.5.2.jar").append(CR);
      sb.append("- tibcrypt.jar").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : TIBCO ems server host name").append(CR);
      sb.append("Port          : TIBCO ems server  port (eg 7222)").append(CR);
      sb.append("User/Password : User allowed to connect to the TIBCO ems server").append(CR);
      sb.append(CR);
      sb.append("Properties values:").append(CR);
      sb.append("---------------").append(CR);
      sb.append("connectionProtocol : Connection protol used to connect (tcp, ssl)").append(CR);
      sb.append("brokerName          : Broker Name (eg MgmtBroker)").append(CR);
      sb.append("containerName      : Container Name (eg DomainManager)").append(CR);
      sb.append("domainName         : Domain Name (eg Domain1)").append(CR);

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

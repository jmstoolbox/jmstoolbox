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
package org.titou10.jtb.qm.sonicmq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.DestinationData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.TopicData;

import com.sonicsw.mf.jmx.client.JMSConnectorAddress;
import com.sonicsw.mf.jmx.client.JMSConnectorClient;
import com.sonicsw.mq.common.runtime.impl.DurableSubscriptionData;
import com.sonicsw.mq.common.runtime.impl.QueueData;

import progress.message.jclient.ConnectionFactory;

/**
 * 
 * Implements SonicMQ Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class SonicMQQManager extends QManager {

   private static final Logger                    log                     = LoggerFactory.getLogger(SonicMQQManager.class);

   private static final String                    CR                      = "\n";

   private static final String                    P_BROKER                = "brokerName";                                  // MgmtBroker
   private static final String                    P_CONTAINER             = "containerName";                               // DomainManager
   private static final String                    P_DOMAIN                = "domainName";                                  // Domain1
   // tcp, ssl, https
   private static final String                    P_PROTOCOL              = "connectionProtocol";

   private static final String[]                  INVOKE_STRING_SIGNATURE = { String.class.getName() };
   private static final Object[]                  INVOKE_EMPTY_PARAMS     = { null };
   private static final String                    GQ_INVOKE_METHOD        = "getQueues";
   private static final String                    GUDS_INVOKE_METHOD      = "getUsersWithDurableSubscriptions";
   private static final String                    GDS_INVOKE_METHOD       = "getDurableSubscriptions";

   private static final String                    HELP_TEXT;

   private final List<QManagerProperty>           parameters              = new ArrayList<QManagerProperty>();

   private final Map<Integer, JMSConnectorClient> jmxConnectors           = new HashMap<>();
   private final Map<Integer, ObjectName>         brokerObjectNames       = new HashMap<>();

   public SonicMQQManager() {
      log.debug("Instantiate SonicMQQManager");

      parameters.add(new QManagerProperty(P_PROTOCOL,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Connection protocol (eg 'tcp','ssl','https'...)",
                                          "tcp"));
      parameters.add(new QManagerProperty(P_BROKER,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Broker name (eg 'MgmtBroker')",
                                          "MgmtBroker"));
      parameters.add(new QManagerProperty(P_CONTAINER,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Container name (eg 'DomainManager')",
                                          "DomainManager"));
      parameters.add(new QManagerProperty(P_DOMAIN, true, JMSPropertyKind.STRING, false, "Domain name (eg 'Domain1')", "Domain1"));

   }

   @Override
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Extract properties
      Map<String, String> mapProperties = extractProperties(sessionDef);

      String brokerName = mapProperties.get(P_BROKER);
      String containerName = mapProperties.get(P_CONTAINER);
      String domainName = mapProperties.get(P_DOMAIN);
      String protocol = mapProperties.get(P_PROTOCOL);

      // JMX Connection

      StringBuilder connectionURL = new StringBuilder(512);
      connectionURL.append(protocol);
      connectionURL.append("://");
      connectionURL.append(sessionDef.getHost());
      connectionURL.append(":");
      connectionURL.append(sessionDef.getPort());
      if (sessionDef.getHost2() != null) {
         connectionURL.append(",");
         connectionURL.append(protocol);
         connectionURL.append("://");
         connectionURL.append(sessionDef.getHost2());
         if (sessionDef.getPort2() != null) {
            connectionURL.append(":");
            connectionURL.append(sessionDef.getPort2());
         }
      }
      if (sessionDef.getHost3() != null) {
         connectionURL.append(",");
         connectionURL.append(protocol);
         connectionURL.append("://");
         connectionURL.append(sessionDef.getHost3());
         if (sessionDef.getPort3() != null) {
            connectionURL.append(":");
            connectionURL.append(sessionDef.getPort3());
         }
      }

      log.debug("JMX connectionURL: {}", connectionURL);

      Hashtable env = new Hashtable();
      env.put("ConnectionURLs", connectionURL.toString());
      if (sessionDef.getActiveUserid() != null) {
         env.put("DefaultUser", sessionDef.getActiveUserid());
      }
      if (sessionDef.getActivePassword() != null) {
         env.put("DefaultPassword", sessionDef.getActivePassword());
      }

      JMSConnectorAddress address = new JMSConnectorAddress(env);
      JMSConnectorClient jmxConnector = new JMSConnectorClient();
      jmxConnector.connect(address, 30 * 1000); // Wait 30s max for connection

      // Lookup for Queues
      // "Domain1.DomainManager:ID=MgmtBroker"
      StringBuilder beanBrokerName = new StringBuilder(256);
      beanBrokerName.append(domainName);
      beanBrokerName.append(".");
      beanBrokerName.append(containerName);
      beanBrokerName.append(":ID=");
      beanBrokerName.append(brokerName);

      ObjectName brokerObjectName = new ObjectName(beanBrokerName.toString());

      log.debug("JMX broker Object Name: {}", beanBrokerName);

      // JMS Connection
      ConnectionFactory factory = new ConnectionFactory(connectionURL.toString(),
                                                        sessionDef.getActiveUserid(),
                                                        sessionDef.getActivePassword());
      factory.setSequential(true);
      factory.setLoadBalancing(true);

      Connection jmsConnection = factory.createConnection();
      jmsConnection.setClientID(clientID);
      jmsConnection.start();

      log.info("connected to {}", sessionDef.getName());

      // Store per connection related data
      Integer hash = jmsConnection.hashCode();
      jmxConnectors.put(hash, jmxConnector);
      brokerObjectNames.put(hash, brokerObjectName);

      return jmsConnection;
   }

   @Override
   @SuppressWarnings("unchecked")
   public DestinationData discoverDestinations(Connection jmsConnection, boolean showSystemObjects) throws Exception {
      log.debug("discoverDestinations : {} - {}", jmsConnection, showSystemObjects);

      Integer hash = jmsConnection.hashCode();
      JMSConnectorClient jmxConnector = jmxConnectors.get(hash);
      ObjectName brokerObjectName = brokerObjectNames.get(hash);

      SortedSet<org.titou10.jtb.jms.qm.QueueData> listQueueData = new TreeSet<>();
      List<QueueData> qd = (List<QueueData>) jmxConnector
               .invoke(brokerObjectName, GQ_INVOKE_METHOD, INVOKE_EMPTY_PARAMS, INVOKE_STRING_SIGNATURE);
      // QueueData implement IQueueData ..
      for (QueueData queueData : qd) {
         String queueName = queueData.getQueueName();
         log.debug("Found Queue. System? {} Temp? {} Cluster? {} Exclusive? {} Global? {} '{}'",
                   queueData.isSystemQueue(),
                   queueData.isTemporaryQueue(),
                   queueData.isClusteredQueue(),
                   queueData.isExclusiveQueue(),
                   queueData.isGlobalQueue(),
                   queueName);

         // SonicMQ does not allow System Queues to be created as JMS Queues so ignore them
         if (queueData.isSystemQueue()) {
            continue;
         }

         // Add Temporary queues depending on preferences
         if (queueData.isTemporaryQueue()) {
            if (!showSystemObjects) {
               continue;
            }
         }

         listQueueData.add(new org.titou10.jtb.jms.qm.QueueData(queueName));
      }

      // Lookup for Durable Subscription (Topics)
      // Iterate ion each user, then each Durable Subscription
      SortedSet<TopicData> listTopicData = new TreeSet<>();
      List<String> users = (List<String>) jmxConnector
               .invoke(brokerObjectName, GUDS_INVOKE_METHOD, INVOKE_EMPTY_PARAMS, INVOKE_STRING_SIGNATURE);
      for (Object user : users) {

         Object[] params = { user };
         List<DurableSubscriptionData> dsds = (List<DurableSubscriptionData>) jmxConnector
                  .invoke(brokerObjectName, GDS_INVOKE_METHOD, params, INVOKE_STRING_SIGNATURE);
         for (DurableSubscriptionData dsd : dsds) {
            String topicName = dsd.getTopicName();
            log.debug("Found DurableSubscription '{}' - '{}'", topicName, dsd.getSubscriptionName());

            // Some DS have invalid JMS names
            if (!topicName.startsWith("SonicMQ.mf")) {
               listTopicData.add(new TopicData(topicName));
            }
         }
      }

      return new DestinationData(listQueueData, listTopicData);
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      Integer hash = jmsConnection.hashCode();
      JMSConnectorClient jmxConnector = jmxConnectors.get(hash);

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occured while closing connection. Ignore it. Msg={}", e.getMessage());
      }

      if (jmxConnector != null) {
         try {
            jmxConnector.disconnect();
         } catch (Exception e) {
            log.warn("Exception occured while disconnect JMX connector. Ignore it. Msg={}", e.getMessage());
         }
         jmxConnectors.remove(hash);
      }

      brokerObjectNames.remove(hash);
   }

   @Override
   public boolean supportsMultipleHosts() {
      return true;
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {
      try {
         QueueData qd = getQueueData(jmsConnection, queueName);
         if (qd == null) {
            return null;
         }
         return qd.getMessageCount();
      } catch (InstanceNotFoundException | MBeanException | ReflectionException e) {
         log.error("An exception occured when reading information for queue " + queueName, e);
         return null;
      }
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {
      SortedMap<String, Object> properties = new TreeMap<>();

      try {
         QueueData qd = getQueueData(jmsConnection, queueName);
         if (qd == null) {
            return null;
         }

         properties.put("Message Count", qd.getMessageCount());
         properties.put("Total Message Size", qd.getTotalMessageSize());
         properties.put("Has Total Message Size", qd.hasTotalMessageSize());
         properties.put("Clustered Queue", qd.isClusteredQueue());
         properties.put("Exclusive Queue", qd.isExclusiveQueue());
         properties.put("Global Queue", qd.isGlobalQueue());
         properties.put("System Queue", qd.isSystemQueue());
         properties.put("Temporary Queue", qd.isTemporaryQueue());

      } catch (InstanceNotFoundException | MBeanException | ReflectionException e) {
         log.error("An exception occured when reading information for queue " + queueName, e);
      }

      return properties;

   }

   @Override
   @SuppressWarnings("unchecked")
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {

      Integer hash = jmsConnection.hashCode();
      JMSConnectorClient jmxConnector = jmxConnectors.get(hash);
      ObjectName brokerObjectName = brokerObjectNames.get(hash);

      SortedMap<String, Object> properties = new TreeMap<>();

      // lookup for the right DurableSubscription
      // TODO DF To be refactored with above
      try {
         List<String> users = (List<String>) jmxConnector
                  .invoke(brokerObjectName, GUDS_INVOKE_METHOD, INVOKE_EMPTY_PARAMS, INVOKE_STRING_SIGNATURE);
         for (Object user : users) {

            Object[] params = { user };
            List<DurableSubscriptionData> dsds = (List<DurableSubscriptionData>) jmxConnector
                     .invoke(brokerObjectName, GDS_INVOKE_METHOD, params, INVOKE_STRING_SIGNATURE);
            for (DurableSubscriptionData dsd : dsds) {
               if (dsd.getTopicName().equals(topicName)) {
                  properties.put("Message Count", dsd.getMessageCount());
                  properties.put("Message Size", dsd.getMessageSize());
                  properties.put("Client ID", dsd.getClientID());
                  properties.put("Selector", dsd.getSelector());
                  properties.put("Subscription Name", dsd.getSubscriptionName());
                  properties.put("User", dsd.getUser());
                  return properties;
               }
            }
         }
      } catch (InstanceNotFoundException | MBeanException | ReflectionException e) {
         log.error("An exception occured when reading information for topic " + topicName, e);
      }
      return properties;
   }

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS (From MQxx.x/lib):").append(CR);
      sb.append("-----------------------------").append(CR);
      sb.append("broker.jar").append(CR);
      sb.append("mgmt_client.jar").append(CR);
      sb.append("sonic_Client.jar").append(CR);
      sb.append("sonic_Crypto.jar").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : SonicMQ broker host name").append(CR);
      sb.append("Port          : SonicMQ broker port (eg 2506)").append(CR);
      sb.append("User/Password : User allowed to connect to SonicMQ").append(CR);
      sb.append(CR);
      sb.append("Properties values:").append(CR);
      sb.append("------------------").append(CR);
      sb.append("brokerName         : Broker Name (eg MgmtBroker)").append(CR);
      sb.append("containerName      : Container Name (eg DomainManager)").append(CR);
      sb.append("domainName         : Domain Name (eg Domain1)").append(CR);
      sb.append("connectionProtocol : Connection protol used to connect (eg tcp, ssl, https ...)").append(CR);

      HELP_TEXT = sb.toString();
   }

   // ------------------------
   // Helpers
   // ------------------------

   @SuppressWarnings("unchecked")
   private QueueData getQueueData(Connection jmsConnection, String queueName) throws InstanceNotFoundException, MBeanException,
                                                                              ReflectionException {

      Integer hash = jmsConnection.hashCode();
      JMSConnectorClient jmxConnector = jmxConnectors.get(hash);
      ObjectName brokerObjectName = brokerObjectNames.get(hash);

      Object[] params = { queueName };
      List<QueueData> qd = (List<QueueData>) jmxConnector
               .invoke(brokerObjectName, GQ_INVOKE_METHOD, params, INVOKE_STRING_SIGNATURE);
      return qd.get(0);
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }

}

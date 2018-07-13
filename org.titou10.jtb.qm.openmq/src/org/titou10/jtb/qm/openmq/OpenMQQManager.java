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
package org.titou10.jtb.qm.openmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.DestinationData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

import com.sun.messaging.AdminConnectionConfiguration;
import com.sun.messaging.AdminConnectionFactory;
import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.ConnectionFactory;
import com.sun.messaging.jms.management.server.DestinationType;
import com.sun.messaging.jms.management.server.MQObjectName;

/**
 * 
 * Implements OpenMQ (embedded in Oracle GlassFish) Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class OpenMQQManager extends QManager {

   // http://docs.oracle.com/cd/E19798-01/821-1797/gdrru/index.html
   // http://docs.oracle.com/cd/E19798-01/821-1797/gchjb/index.html
   // imqcmd -list jmx (u=admin =admin)

   private static final Logger                       log                    = LoggerFactory.getLogger(OpenMQQManager.class);

   private static final String                       AC_TEMPLATE            = "%s:%d";
   private static final String                       AC_TEMPLATE_SSL        = "mq://%s:%d/ssljmxrmi";

   // private static final String JMX_URL_TEMPLATE = "service:jmx:rmi:///jndi/rmi://%s:%d/server";

   private static final String                       ON_QUEUES              = "com.sun.messaging.jms.server:type=Destination,subtype=Config,desttype=q,*";
   private static final String                       ON_TOPICS              = "com.sun.messaging.jms.server:type=Destination,subtype=Config,desttype=t,*";

   private static final String                       CR                     = "\n";

   private static final String                       P_SSL_ENABLED          = "sslEnabled";
   private static final String                       P_TRUST_STORE          = "javax.net.ssl.trustStore";
   private static final String                       P_TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";

   private static final String                       HELP_TEXT;

   private List<QManagerProperty>                    parameters             = new ArrayList<QManagerProperty>();

   private final Map<Integer, JMXConnector>          jmxcs                  = new HashMap<>();
   private final Map<Integer, MBeanServerConnection> mbscs                  = new HashMap<>();

   public OpenMQQManager() {
      log.debug("Instantiate OpenMQ");

      parameters.add(new QManagerProperty(P_SSL_ENABLED, false, JMSPropertyKind.BOOLEAN));
      parameters.add(new QManagerProperty(P_TRUST_STORE, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_TRUST_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));
   }

   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Save System properties
      saveSystemProperties();
      try {

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         String sslEnabled = mapProperties.get(P_SSL_ENABLED);
         String trustStore = mapProperties.get(P_TRUST_STORE);
         String trustStorePassword = mapProperties.get(P_TRUST_STORE_PASSWORD);

         // Manage System Properties and build serviceURL

         String serviceURL = String.format(AC_TEMPLATE, sessionDef.getHost(), sessionDef.getPort());

         if (sslEnabled != null) {
            if (Boolean.valueOf(sslEnabled)) {
               serviceURL = String.format(AC_TEMPLATE_SSL, sessionDef.getHost(), sessionDef.getPort());
               if (trustStore == null) {
                  System.clearProperty(P_TRUST_STORE);
               } else {
                  System.setProperty(P_TRUST_STORE, trustStore);
               }
               if (trustStorePassword == null) {
                  System.clearProperty(P_TRUST_STORE_PASSWORD);
               } else {
                  System.setProperty(P_TRUST_STORE_PASSWORD, trustStorePassword);
               }
            }
         }

         // Connect with specific factory

         AdminConnectionFactory acf = new AdminConnectionFactory();
         acf.setProperty(AdminConnectionConfiguration.imqAddress, serviceURL);

         JMXConnector jmxc;
         if (sessionDef.getActiveUserid() == null || sessionDef.getActiveUserid().trim().isEmpty()) {
            jmxc = acf.createConnection();
         } else {
            jmxc = acf.createConnection(sessionDef.getActiveUserid(), sessionDef.getActivePassword());
         } 
         MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

         // // Connect using standard JMX
         // HashMap<String, Object> environment = new HashMap<String, Object>();
         // environment.put(JMXConnector.CREDENTIALS, new String[] { sessionDef.getUserid(), sessionDef.getPassword() });
         //
         // String serviceURL = String.format(JMX_URL_TEMPLATE, sessionDef.getHost(), sessionDef.getPort());
         // log.debug("connecting to {}", serviceURL);
         // serviceURL = "service:jmx:rmi://win7udev/jndi/rmi://win7udev.denis.prive:1099/win7udev.denis.prive/7676/jmxrmi";
         //
         // JMXServiceURL url = new JMXServiceURL(serviceURL);
         // JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, environment);
         // connector.connect();
         // MBeanServerConnection mbsc = connector.getMBeanServerConnection();

         // Produce the JMS Connection
         ConnectionFactory cf = new ConnectionFactory();
         cf.setProperty(ConnectionConfiguration.imqAddressList, serviceURL);

         Connection jmsConnection;
         if (sessionDef.getActiveUserid() == null || sessionDef.getActiveUserid().trim().isEmpty()) {
            jmsConnection = cf.createConnection();
         } else {
            jmsConnection = cf.createConnection(sessionDef.getActiveUserid(), sessionDef.getActivePassword());
         }
         jmsConnection.setClientID(clientID);
         jmsConnection.start();

         log.info("connected to {}", sessionDef.getName());

         // Store per connection related data
         Integer hash = jmsConnection.hashCode();
         jmxcs.put(hash, jmxc);
         mbscs.put(hash, mbsc);

         return jmsConnection;
      } finally {
         restoreSystemProperties();
      }
   }

   @Override
   public DestinationData discoverDestinations(Connection jmsConnection, boolean showSystemObjects) throws Exception {
      log.debug("discoverDestinations : {} - {}", jmsConnection, showSystemObjects);

      // Discover Queues and Topics

      Integer hash = jmsConnection.hashCode();
      MBeanServerConnection mbsc = mbscs.get(hash);

      SortedSet<QueueData> listQueueData = new TreeSet<>();
      SortedSet<TopicData> listTopicData = new TreeSet<>();

      Set<ObjectName> setQueues = mbsc.queryNames(new ObjectName(ON_QUEUES), null);
      for (ObjectName objectQueue : setQueues) {
         String name = objectQueue.getKeyProperty("name");
         log.info("q={}", objectQueue);
         // Name is enclosed in ", causing [C4050] Invalid Destination Name later
         listQueueData.add(new QueueData(name.replaceAll("\"", "")));
      }

      Set<ObjectName> setTopics = mbsc.queryNames(new ObjectName(ON_TOPICS), null);
      for (ObjectName objectTopic : setTopics) {
         String name = objectTopic.getKeyProperty("name");
         log.info("t={}", setTopics);
         // Name is enclosed in ", causing [C4050] Invalid Destination Name later
         listTopicData.add(new TopicData(name.replaceAll("\"", "")));
      }

      return new DestinationData(listQueueData, listTopicData);
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      Integer hash = jmsConnection.hashCode();
      JMXConnector jmxc = jmxcs.get(hash);

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occured while closing connection. Ignore it. Msg={}", e.getMessage());
      }

      if (jmxc != null) {
         try {
            jmxc.close();
         } catch (IOException e) {
            log.warn("Exception occured while closing JMXConnector. Ignore it. Msg={}", e.getMessage());
         }
         jmxcs.remove(hash);
         mbscs.remove(hash);
      }
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      MBeanServerConnection mbsc = mbscs.get(hash);

      Integer depth = null;
      try {
         ObjectName on = MQObjectName.createDestinationMonitor(DestinationType.QUEUE, queueName);
         Set<ObjectName> attributesSet = mbsc.queryNames(on, null);
         if ((attributesSet != null) && (!attributesSet.isEmpty())) {
            // TODO Long -> Integer !
            depth = ((Long) mbsc.getAttribute(attributesSet.iterator().next(), "NumMsgs")).intValue();
         }
      } catch (Exception e) {
         log.error("Exception when reading queue depth. Ignoring", e);
      }
      return depth;
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      MBeanServerConnection mbsc = mbscs.get(hash);

      Map<String, Object> properties = new LinkedHashMap<>();

      try {
         ObjectName on = MQObjectName.createDestinationMonitor(DestinationType.QUEUE, queueName);
         Set<ObjectName> attributesSet = mbsc.queryNames(on, null);

         // Display all attributes
         // MBeanInfo info = mbsc.getMBeanInfo(attributesSet.iterator().next());
         // MBeanAttributeInfo[] attrInfo = info.getAttributes();
         // for (MBeanAttributeInfo attr : attrInfo) {
         // System.out.println(" " + attr.getName() + "\n");
         // }

         if ((attributesSet != null) && (!attributesSet.isEmpty())) {
            addInfo(mbsc, properties, attributesSet, "State");
            addInfo(mbsc, properties, attributesSet, "StateLabel");
            addInfo(mbsc, properties, attributesSet, "NextMessageID");
            addInfo(mbsc, properties, attributesSet, "Temporary");
            addInfo(mbsc, properties, attributesSet, "CreatedByAdmin");

            addInfo(mbsc, properties, attributesSet, "AvgNumActiveConsumers");
            addInfo(mbsc, properties, attributesSet, "AvgNumBackupConsumers");
            addInfo(mbsc, properties, attributesSet, "AvgNumConsumers");
            addInfo(mbsc, properties, attributesSet, "AvgNumMsgs");
            addInfo(mbsc, properties, attributesSet, "AvgTotalMsgBytes");
            addInfo(mbsc, properties, attributesSet, "DiskReserved");
            addInfo(mbsc, properties, attributesSet, "DiskUsed");
            addInfo(mbsc, properties, attributesSet, "DiskUtilizationRatio");
            addInfo(mbsc, properties, attributesSet, "MsgBytesIn");
            addInfo(mbsc, properties, attributesSet, "MsgBytesOut");
            addInfo(mbsc, properties, attributesSet, "NumActiveConsumers");
            addInfo(mbsc, properties, attributesSet, "NumBackupConsumers");
            addInfo(mbsc, properties, attributesSet, "NumConsumers");
            addInfo(mbsc, properties, attributesSet, "NumWildcards");
            addInfo(mbsc, properties, attributesSet, "NumWildcardConsumers");
            addInfo(mbsc, properties, attributesSet, "NumWildcardProducers");
            addInfo(mbsc, properties, attributesSet, "NumMsgs");
            addInfo(mbsc, properties, attributesSet, "NumMsgsRemote");
            addInfo(mbsc, properties, attributesSet, "NumMsgsHeldInTransaction");
            addInfo(mbsc, properties, attributesSet, "NumMsgsIn");
            addInfo(mbsc, properties, attributesSet, "NumMsgsOut");
            addInfo(mbsc, properties, attributesSet, "NumMsgsPendingAcks");
            addInfo(mbsc, properties, attributesSet, "NumProducers");
            addInfo(mbsc, properties, attributesSet, "PeakMsgBytes");
            addInfo(mbsc, properties, attributesSet, "PeakNumActiveConsumers");
            addInfo(mbsc, properties, attributesSet, "PeakNumBackupConsumers");
            addInfo(mbsc, properties, attributesSet, "PeakNumConsumers");
            addInfo(mbsc, properties, attributesSet, "PeakNumMsgs");
            addInfo(mbsc, properties, attributesSet, "PeakTotalMsgBytes");
            addInfo(mbsc, properties, attributesSet, "TotalMsgBytes");
            addInfo(mbsc, properties, attributesSet, "TotalMsgBytesRemote");
            addInfo(mbsc, properties, attributesSet, "TotalMsgBytesHeldInTransaction");

         }
      } catch (Exception e) {
         log.error("Exception when reading Queue Information. Ignoring", e);
      }

      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {

      Integer hash = jmsConnection.hashCode();
      MBeanServerConnection mbsc = mbscs.get(hash);

      Map<String, Object> properties = new LinkedHashMap<>();

      try {
         ObjectName on = MQObjectName.createDestinationMonitor(DestinationType.TOPIC, topicName);
         Set<ObjectName> attributesSet = mbsc.queryNames(on, null);

         if ((attributesSet != null) && (!attributesSet.isEmpty())) {
            addInfo(mbsc, properties, attributesSet, "StateLabel");
            addInfo(mbsc, properties, attributesSet, "State");
            addInfo(mbsc, properties, attributesSet, "Temporary");
            addInfo(mbsc, properties, attributesSet, "CreatedByAdmin");

            addInfo(mbsc, properties, attributesSet, "AvgNumActiveConsumers");
            addInfo(mbsc, properties, attributesSet, "AvgNumBackupConsumers");
            addInfo(mbsc, properties, attributesSet, "AvgNumConsumers");
            addInfo(mbsc, properties, attributesSet, "AvgNumMsgs");
            addInfo(mbsc, properties, attributesSet, "AvgTotalMsgBytes");
            addInfo(mbsc, properties, attributesSet, "DiskReserved");
            addInfo(mbsc, properties, attributesSet, "DiskUsed");
            addInfo(mbsc, properties, attributesSet, "DiskUtilizationRatio");
            addInfo(mbsc, properties, attributesSet, "MsgBytesIn");
            addInfo(mbsc, properties, attributesSet, "MsgBytesOut");
            addInfo(mbsc, properties, attributesSet, "NumActiveConsumers");
            addInfo(mbsc, properties, attributesSet, "NumBackupConsumers");
            addInfo(mbsc, properties, attributesSet, "NumConsumers");
            addInfo(mbsc, properties, attributesSet, "NumWildcards");
            addInfo(mbsc, properties, attributesSet, "NumWildcardConsumers");
            addInfo(mbsc, properties, attributesSet, "NumWildcardProducers");
            addInfo(mbsc, properties, attributesSet, "NumMsgs");
            addInfo(mbsc, properties, attributesSet, "NumMsgsRemote");
            addInfo(mbsc, properties, attributesSet, "NumMsgsHeldInTransaction");
            addInfo(mbsc, properties, attributesSet, "NumMsgsIn");
            addInfo(mbsc, properties, attributesSet, "NumMsgsOut");
            addInfo(mbsc, properties, attributesSet, "NumMsgsPendingAcks");
            addInfo(mbsc, properties, attributesSet, "NumProducers");
            addInfo(mbsc, properties, attributesSet, "PeakMsgBytes");
            addInfo(mbsc, properties, attributesSet, "PeakNumActiveConsumers");
            addInfo(mbsc, properties, attributesSet, "PeakNumBackupConsumers");
            addInfo(mbsc, properties, attributesSet, "PeakNumConsumers");
            addInfo(mbsc, properties, attributesSet, "PeakNumMsgs");
            addInfo(mbsc, properties, attributesSet, "PeakTotalMsgBytes");
            addInfo(mbsc, properties, attributesSet, "NextMessageID");
            addInfo(mbsc, properties, attributesSet, "TotalMsgBytes");
            addInfo(mbsc, properties, attributesSet, "TotalMsgBytesRemote");
            addInfo(mbsc, properties, attributesSet, "TotalMsgBytesHeldInTransaction");
         }
      } catch (Exception e) {
         log.error("Exception when reading Queue Information. Ignoring", e);
      }

      return properties;
   }

   private void addInfo(MBeanServerConnection mbsc,
                        Map<String, Object> properties,
                        Set<ObjectName> attributesSet,
                        String propertyName) {
      try {
         properties.put(propertyName, mbsc.getAttribute(attributesSet.iterator().next(), propertyName));
      } catch (Exception e) {
         log.warn("Exception when reading " + propertyName + " Ignoring. " + e.getMessage());
      }
   }

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS :").append(CR);
      sb.append("------------").append(CR);
      sb.append("No extra jar is needed as JMSToolBox is bundled with OpenMQ v5.1 jars").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : OpenMQ broker server name (eg. localhost)").append(CR);
      sb.append("Port          : OpenMQ Message Queue Port (eg 7676)").append(CR);
      sb.append("User/Password : User allowed to connect to the OpenMQ broker").append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("sslEnabled                       : If true,  the connection will be done with connection string \"mq://<host>:<port>/ssljmxrmi\"")
               .append(CR);
      sb.append("                                 : If false, the connection will be done with connection string \"<host>:<port>\"")
               .append(CR);
      sb.append(CR);
      sb.append("javax.net.ssl.trustStore         : trust store (eg D:/somewhere/trust.jks)").append(CR);
      sb.append("javax.net.ssl.trustStorePassword : trust store password").append(CR);

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

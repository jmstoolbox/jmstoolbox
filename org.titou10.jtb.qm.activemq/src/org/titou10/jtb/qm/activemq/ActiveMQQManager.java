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
package org.titou10.jtb.qm.activemq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;

/**
 * 
 * Implements Apache ActiveMQ (embedded in TomEE and Geronimo) Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class ActiveMQQManager extends QManager {

   private static final Logger    log                    = LoggerFactory.getLogger(ActiveMQQManager.class);

   private static final String    JMX_URL_TEMPLATE       = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi";

   private static final String    JMX_QUEUES             = "org.apache.activemq:type=Broker,destinationType=Queue,*";
   private static final String    JMX_TOPICS             = "org.apache.activemq:type=Broker,destinationType=Topic,*";

   private static final String    JMX_QUEUE              = "org.apache.activemq:type=Broker,destinationType=Queue,destinationName=%s,*";
   private static final String    JMX_TOPIC              = "org.apache.activemq:type=Broker,destinationType=Topic,destinationName=%s,*";

   private static final String    SYSTEM_PREFIX          = "ActiveMQ.";

   private static final String    CR                     = "\n";

   private static final String    P_BROKER_URL           = "brokerURL";
   private static final String    P_KEY_STORE            = "javax.net.ssl.keyStore";
   private static final String    P_KEY_STORE_PASSWORD   = "javax.net.ssl.keyStorePassword";
   private static final String    P_TRUST_STORE          = "javax.net.ssl.trustStore";
   private static final String    P_TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
   private static final String    P_TRUST_ALL_PACKAGES   = "trustAllPackages";

   private List<QManagerProperty> parameters             = new ArrayList<QManagerProperty>();
   private SortedSet<String>      queueNames             = new TreeSet<>();
   private SortedSet<String>      topicNames             = new TreeSet<>();

   private JMXConnector           jmxc;
   private MBeanServerConnection  mbsc;

   // ------------------------
   // Constructor
   // ------------------------

   public ActiveMQQManager() {
      log.debug("Apache Active MQ");

      // parameters.add(new QManagerProperty(P_ICF, true, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_BROKER_URL, true, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_KEY_STORE, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_KEY_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(P_TRUST_STORE, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_TRUST_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(P_TRUST_ALL_PACKAGES, false, JMSPropertyKind.BOOLEAN));
   }

   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects) throws Exception {

      /* <managementContext> <managementContext createConnector="true"/> </managementContext> */

      // Save System properties
      saveSystemProperties();
      try {

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         String brokerURL = mapProperties.get(P_BROKER_URL);
         // String icf = mapProperties.get(P_ICF);
         String keyStore = mapProperties.get(P_KEY_STORE);
         String keyStorePassword = mapProperties.get(P_KEY_STORE_PASSWORD);
         String trustStore = mapProperties.get(P_TRUST_STORE);
         String trustStorePassword = mapProperties.get(P_TRUST_STORE_PASSWORD);
         String trustAllPackages = mapProperties.get(P_TRUST_ALL_PACKAGES);

         if (keyStore == null) {
            System.clearProperty(P_KEY_STORE);
         } else {
            System.setProperty(P_KEY_STORE, keyStore);
         }
         if (keyStorePassword == null) {
            System.clearProperty(P_KEY_STORE_PASSWORD);
         } else {
            System.setProperty(P_KEY_STORE_PASSWORD, keyStorePassword);
         }
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

         JMXServiceURL url = new JMXServiceURL(String.format(JMX_URL_TEMPLATE, sessionDef.getHost(), sessionDef.getPort()));
         log.debug("JMX URL : {}", url);

         Map<String, String[]> jmxEnv = Collections.singletonMap(JMXConnector.CREDENTIALS,
                                                                 new String[] { sessionDef.getUserid(), sessionDef.getPassword() });

         jmxc = JMXConnectorFactory.connect(url, jmxEnv);
         mbsc = jmxc.getMBeanServerConnection();
         log.debug(mbsc.toString());

         // Discover queues and topics

         // ObjectName activeMQ1 = new ObjectName(String.format(JMX_QUEUES, brokerName));
         ObjectName activeMQ1 = new ObjectName(JMX_QUEUES);
         Set<ObjectName> a = mbsc.queryNames(activeMQ1, null);
         for (ObjectName objectName : a) {
            String dName = objectName.getKeyProperty("destinationName");
            log.debug("queue={}", dName);
            if (showSystemObjects) {
               queueNames.add(dName);
            } else {
               if (!dName.startsWith(SYSTEM_PREFIX)) {
                  queueNames.add(dName);
               }
            }
         }
         // ObjectName activeMQ2 = new ObjectName(String.format(JMX_TOPICS, brokerName));
         ObjectName activeMQ2 = new ObjectName(JMX_TOPICS);
         Set<ObjectName> b = mbsc.queryNames(activeMQ2, null);
         for (ObjectName objectName : b) {
            String dName = objectName.getKeyProperty("destinationName");
            log.debug("topic={}", dName);
            if (showSystemObjects) {
               topicNames.add(dName);
            } else {
               if (!dName.startsWith(SYSTEM_PREFIX)) {
                  topicNames.add(dName);
               }
            }
         }

         // -------------------

         // tcp://localhost:61616"
         // "org.apache.activemq.jndi.ActiveMQInitialContextFactory"

         log.debug("connecting to {}", brokerURL);

         // Hashtable<String, String> environment = new Hashtable<>();
         // environment.put(Context.PROVIDER_URL, brokerURL);
         // environment.put(Context.INITIAL_CONTEXT_FACTORY, icf);
         //
         // Context ctx = new InitialDirContext(environment);
         // ConnectionFactory cf = (ConnectionFactory) ctx.lookup("ConnectionFactory");
         // ActiveMQConnectionFactory cf2 = (ActiveMQConnectionFactory) cf;

         ActiveMQConnectionFactory cf2 = new ActiveMQConnectionFactory(sessionDef.getUserid(), sessionDef.getPassword(), brokerURL);
         cf2.setTransactedIndividualAck(true); // Without this, browsing messages spends 15s+ on the last element
         if (trustAllPackages != null) {
            if (Boolean.valueOf(trustAllPackages)) {
               cf2.setTrustAllPackages(true);
            }
         }

         // Create JMS Connection
         Connection c = cf2.createConnection(sessionDef.getUserid(), sessionDef.getPassword());
         log.info("connected to {}", sessionDef.getName());

         log.debug("Discovered {} queues and {} topics", queueNames.size(), topicNames.size());

         return c;
      } finally {
         restoreSystemProperties();
      }
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection");
      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occured while closing session. Ignore it. Msg={}", e.getMessage());
      }
      queueNames.clear();
      topicNames.clear();
      if (jmxc != null) {
         try {
            jmxc.close();
         } catch (IOException e) {
            log.warn("Exception occured while closing JMXConnector. Ignore it. Msg={}", e.getMessage());
         }
         jmxc = null;
         mbsc = null;
      }
   }

   @Override
   public Integer getQueueDepth(String queueName) {
      Integer depth = null;
      try {
         ObjectName on = new ObjectName(String.format(JMX_QUEUE, queueName));
         Set<ObjectName> attributesSet = mbsc.queryNames(on, null);
         if ((attributesSet != null) && (!attributesSet.isEmpty())) {
            // TODO Long -> Integer !
            depth = ((Long) mbsc.getAttribute(attributesSet.iterator().next(), "QueueSize")).intValue();
         }
      } catch (Exception e) {
         log.error("Exception when reading queue depth. Ignoring", e);
      }
      return depth;
   }

   @Override
   public Map<String, Object> getQueueInformation(String queueName) {
      Map<String, Object> properties = new LinkedHashMap<>();

      try {
         ObjectName on = new ObjectName(String.format(JMX_QUEUE, queueName));
         Set<ObjectName> attributesSet = mbsc.queryNames(on, null);

         if ((attributesSet != null) && (!attributesSet.isEmpty())) {
            addInfo(properties, attributesSet, "QueueSize");
            addInfo(properties, attributesSet, "Paused");
            addInfo(properties, attributesSet, "DLQ");

            addInfo(properties, attributesSet, "CacheEnabled");
            addInfo(properties, attributesSet, "UseCache");
            addInfo(properties, attributesSet, "CursorMemoryUsage");
            addInfo(properties, attributesSet, "CursorPercentUsage");
            addInfo(properties, attributesSet, "CursorFull");
            addInfo(properties, attributesSet, "MessageGroupType");
            addInfo(properties, attributesSet, "MessageGroups");
            addInfo(properties, attributesSet, "MemoryPercentUsage");
            addInfo(properties, attributesSet, "MemoryUsagePortion");
            addInfo(properties, attributesSet, "MemoryUsageByteCount");
            addInfo(properties, attributesSet, "MemoryLimit");
            addInfo(properties, attributesSet, "Options");
            addInfo(properties, attributesSet, "SlowConsumerStrategy");
            addInfo(properties, attributesSet, "ProducerFlowControl");
            addInfo(properties, attributesSet, "AlwaysRetroactive");
            addInfo(properties, attributesSet, "MaxProducersToAudit");
            addInfo(properties, attributesSet, "PrioritizedMessages");
            addInfo(properties, attributesSet, "MaxAuditDepth");
            addInfo(properties, attributesSet, "AverageMessageSize");
            addInfo(properties, attributesSet, "MaxMessageSize");
            addInfo(properties, attributesSet, "MinMessageSize");
            addInfo(properties, attributesSet, "MaxPageSize");
            addInfo(properties, attributesSet, "BlockedProducerWarningInterval");
            addInfo(properties, attributesSet, "BlockedSends");
            addInfo(properties, attributesSet, "StoreMessageSize");
            addInfo(properties, attributesSet, "ProducerCount");
            addInfo(properties, attributesSet, "ConsumerCount");
            addInfo(properties, attributesSet, "EnqueueCount");
            addInfo(properties, attributesSet, "DequeueCount");
            addInfo(properties, attributesSet, "ForwardCount");
            addInfo(properties, attributesSet, "DispatchCount");
            addInfo(properties, attributesSet, "InFlightCount");
            addInfo(properties, attributesSet, "ExpiredCount");
            addInfo(properties, attributesSet, "AverageEnqueueTime");
            addInfo(properties, attributesSet, "MaxEnqueueTime");
            addInfo(properties, attributesSet, "MinEnqueueTime");
            addInfo(properties, attributesSet, "AverageBlockedTime");
            addInfo(properties, attributesSet, "TotalBlockedTime");
            // addInfo(properties, attributesSet, "Subscriptions");
         }
      } catch (Exception e) {
         log.error("Exception when reading Queue Information. Ignoring", e);
      }

      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(String topicName) {
      Map<String, Object> properties = new LinkedHashMap<>();

      try {
         ObjectName on = new ObjectName(String.format(JMX_TOPIC, topicName));
         Set<ObjectName> attributesSet = mbsc.queryNames(on, null);

         // Display all attributes
         MBeanInfo info = mbsc.getMBeanInfo(attributesSet.iterator().next());
         MBeanAttributeInfo[] attrInfo = info.getAttributes();
         for (MBeanAttributeInfo attr : attrInfo) {
            System.out.println(" " + attr.getName() + "\n");
         }

         if ((attributesSet != null) && (!attributesSet.isEmpty())) {
            addInfo(properties, attributesSet, "QueueSize");
            addInfo(properties, attributesSet, "DLQ");
            addInfo(properties, attributesSet, "UseCache");

            addInfo(properties, attributesSet, "ProducerCount");
            addInfo(properties, attributesSet, "ConsumerCount");
            addInfo(properties, attributesSet, "EnqueueCount");
            addInfo(properties, attributesSet, "DequeueCount");
            addInfo(properties, attributesSet, "ForwardCount");
            addInfo(properties, attributesSet, "MemoryPercentUsage");
            addInfo(properties, attributesSet, "MemoryUsagePortion");
            addInfo(properties, attributesSet, "Options");
            addInfo(properties, attributesSet, "MemoryLimit");
            addInfo(properties, attributesSet, "MemoryUsageByteCount");
            addInfo(properties, attributesSet, "SlowConsumerStrategy");
            addInfo(properties, attributesSet, "ProducerFlowControl");
            addInfo(properties, attributesSet, "AlwaysRetroactive");
            addInfo(properties, attributesSet, "MaxProducersToAudit");
            addInfo(properties, attributesSet, "PrioritizedMessages");
            addInfo(properties, attributesSet, "AverageMessageSize");
            addInfo(properties, attributesSet, "MaxMessageSize");
            addInfo(properties, attributesSet, "MinMessageSize");
            addInfo(properties, attributesSet, "MaxAuditDepth");
            addInfo(properties, attributesSet, "MaxPageSize");
            addInfo(properties, attributesSet, "BlockedProducerWarningInterval");
            addInfo(properties, attributesSet, "BlockedSends");
            addInfo(properties, attributesSet, "StoreMessageSize");
            addInfo(properties, attributesSet, "AverageEnqueueTime");
            addInfo(properties, attributesSet, "MaxEnqueueTime");
            addInfo(properties, attributesSet, "MinEnqueueTime");
            addInfo(properties, attributesSet, "AverageBlockedTime");
            addInfo(properties, attributesSet, "TotalBlockedTime");
            addInfo(properties, attributesSet, "DispatchCount");
            addInfo(properties, attributesSet, "InFlightCount");
            addInfo(properties, attributesSet, "ExpiredCount");
            // addInfo(properties, attributesSet, "Subscriptions");
         }
      } catch (Exception e) {
         log.error("Exception when reading Queue Information. Ignoring", e);
      }

      return properties;
   }

   private void addInfo(Map<String, Object> properties, Set<ObjectName> attributesSet, String propertyName) {
      try {
         properties.put(propertyName, mbsc.getAttribute(attributesSet.iterator().next(), propertyName));
      } catch (Exception e) {
         log.warn("Exception when reading " + propertyName + " Ignoring. " + e.getMessage());
      }
   }

   @Override
   public String getHelpText() {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS :").append(CR);
      sb.append("------------").append(CR);
      sb.append("No extra jar is needed as JMSToolBox is bundled with Apache ActiveMQ v5.13.2 jars").append(CR);
      sb.append(CR);
      sb.append("Requirements").append(CR);
      sb.append("------------").append(CR);
      sb.append("JMX must be activated on the broker:").append(CR);
      sb.append("--> http://activemq.apache.org/jmx.html").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : Apache ActiveMQ broker server name for JMX Connection (eg localhost)").append(CR);
      sb.append("Port          : Apache ActiveMQ broker port for JMX Connection (eg. 1099)").append(CR);
      sb.append("User/Password : User allowed to connect to Apache ActiveMQ").append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      // sb.append("- initialContextFactory : org.apache.activemq.jndi.ActiveMQInitialContextFactory").append(CR);
      sb.append("- brokerURL             : broker url. Examples:").append(CR);
      sb.append("                          tcp://localhost:61616").append(CR);
      sb.append("                          https://localhost:8443").append(CR);
      sb.append("                          ssl://localhost:61616").append(CR);
      sb.append("                          ssl://localhost:61616?socket.enabledCipherSuites=SSL_RSA_WITH_RC4_128_SHA,SSL_DH_anon_WITH_3DES_EDE_CBC_SHA");
      sb.append(CR);
      sb.append("- trustAllPackages                 : If true, allows to display ObjectMessage payload (Needs some config on the server also)");
      sb.append(CR);
      sb.append("- javax.net.ssl.trustStore         : trust store (eg D:/somewhere/trust.jks)").append(CR);
      sb.append("- javax.net.ssl.trustStorePassword : trust store password").append(CR);
      sb.append(CR);
      sb.append("If the \"transportConnector\" on the server is configured with \"transport.needClientAuth=true\":").append(CR);
      sb.append("- javax.net.ssl.keyStore           : key store (eg D:/somewhere/key.jks)").append(CR);
      sb.append("- javax.net.ssl.keyStorePassword   : key store password").append(CR);

      return sb.toString();

   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   @Override
   public SortedSet<String> getQueueNames() {
      return queueNames;
   }

   @Override
   public SortedSet<String> getTopicNames() {
      return topicNames;
   }

   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }

}

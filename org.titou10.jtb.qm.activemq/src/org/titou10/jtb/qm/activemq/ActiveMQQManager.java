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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.directory.InitialDirContext;

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

   // private static final String JMX_QUEUES = "org.apache.activemq:type=Broker,brokerName=%s,destinationType=Queue,*";
   // private static final String JMX_TOPICS = "org.apache.activemq:type=Broker,brokerName=%s,destinationType=Topic,*";
   private static final String    JMX_QUEUES             = "org.apache.activemq:type=Broker,destinationType=Queue,*";
   private static final String    JMX_TOPICS             = "org.apache.activemq:type=Broker,destinationType=Topic,*";

   private static final String    JMX_QUEUE              = "org.apache.activemq:type=Broker,destinationType=Queue,destinationName=%s,*";

   // private static final String JMS_CONNECT = "tcp://%s:%d";

   private static final String    CR                     = "\n";

   private static final String    P_ICF                  = "initialContextFactory";
   private static final String    P_BROKER_URL           = "brokerURL";
   private static final String    P_KEY_STORE            = "javax.net.ssl.keyStore";
   private static final String    P_KEY_STORE_PASSWORD   = "javax.net.ssl.keyStorePassword";
   private static final String    P_TRUST_STORE          = "javax.net.ssl.trustStore";
   private static final String    P_TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";

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

      parameters.add(new QManagerProperty(P_ICF, true, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_BROKER_URL, true, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_KEY_STORE, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_KEY_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(P_TRUST_STORE, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_TRUST_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));
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
         String icf = mapProperties.get(P_ICF);
         String keyStore = mapProperties.get(P_KEY_STORE);
         String keyStorePassword = mapProperties.get(P_KEY_STORE_PASSWORD);
         String trustStore = mapProperties.get(P_TRUST_STORE);
         String trustStorePassword = mapProperties.get(P_TRUST_STORE_PASSWORD);

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

         jmxc = JMXConnectorFactory.connect(url);
         mbsc = jmxc.getMBeanServerConnection();
         log.debug(mbsc.toString());

         // Discover queues and topics

         // ObjectName activeMQ1 = new ObjectName(String.format(JMX_QUEUES, brokerName));
         ObjectName activeMQ1 = new ObjectName(JMX_QUEUES);
         Set<ObjectName> a = mbsc.queryNames(activeMQ1, null);
         for (ObjectName objectName : a) {
            log.debug("queue={}", objectName.getKeyProperty("destinationName"));
            queueNames.add(objectName.getKeyProperty("destinationName"));
         }
         // ObjectName activeMQ2 = new ObjectName(String.format(JMX_TOPICS, brokerName));
         ObjectName activeMQ2 = new ObjectName(JMX_TOPICS);
         Set<ObjectName> b = mbsc.queryNames(activeMQ2, null);
         for (ObjectName objectName : b) {
            log.debug("topic={}", objectName.getKeyProperty("destinationName"));
            topicNames.add(objectName.getKeyProperty("destinationName"));
         }

         // -------------------

         // tcp://localhost:61616"
         // "org.apache.activemq.jndi.ActiveMQInitialContextFactory"

         String serviceURL = brokerURL;
         log.debug("connecting to {}", serviceURL);

         Hashtable<String, String> environment = new Hashtable<>();
         environment.put(Context.PROVIDER_URL, serviceURL);
         environment.put(Context.INITIAL_CONTEXT_FACTORY, icf);

         Context ctx = new InitialDirContext(environment);
         ConnectionFactory cf = (ConnectionFactory) ctx.lookup("ConnectionFactory");

         // Create JMS Connection
         Connection c = cf.createConnection();
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
         Set<ObjectName> queueSet = mbsc.queryNames(on, null);
         if ((queueSet != null) && (!queueSet.isEmpty())) {
            // TODO Long -> Integer !
            depth = ((Long) mbsc.getAttribute(queueSet.iterator().next(), "QueueSize")).intValue();
         }
      } catch (Exception e) {
         log.error("Exception when reading queue depth. Ignoring", e);
      }
      return depth;
   }

   @Override
   public Map<String, Object> getQueueInformation(String queueName) {
      SortedMap<String, Object> properties = new TreeMap<>();

      try {
         ObjectName on = new ObjectName(String.format(JMX_QUEUE, queueName));
         Set<ObjectName> queueSet = mbsc.queryNames(on, null);
         if ((queueSet != null) && (!queueSet.isEmpty())) {
            addInfo(properties, queueSet, "AverageEnqueueTime");
            addInfo(properties, queueSet, "ConsumerCount");
            addInfo(properties, queueSet, "DequeueCount");
            addInfo(properties, queueSet, "EnqueueCount");
            addInfo(properties, queueSet, "ExpiredCount");
            addInfo(properties, queueSet, "InFlightCount");
            addInfo(properties, queueSet, "MemoryLimit");
            addInfo(properties, queueSet, "MemoryPercentUsage");
            addInfo(properties, queueSet, "QueueSize");
         }
      } catch (Exception e) {
         log.error("Exception when reading Queue Information. Ignoring", e);
      }

      return properties;
   }

   private void addInfo(Map<String, Object> properties, Set<ObjectName> queueSet, String propertyName) {
      try {
         properties.put(propertyName, mbsc.getAttribute(queueSet.iterator().next(), propertyName));
      } catch (Exception e) {
         log.warn("Exception when reading " + propertyName + " Ignoring. " + e.getMessage());
      }
   }

   @Override
   public String getHelpText() {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS :").append(CR);
      sb.append("------------").append(CR);
      sb.append("No extra jar is needed as JMSToolBox is bundled with Apache ActiveMQ v5.11 jars").append(CR);
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
      sb.append("- initialContextFactory : org.apache.activemq.jndi.ActiveMQInitialContextFactory").append(CR);
      sb.append("- brokerURL             : broker url. Examples:").append(CR);
      sb.append("                          tcp://localhost:61616").append(CR);
      sb.append("                          https://localhost:8443").append(CR);
      sb.append("                          ssl://localhost:61616").append(CR);
      sb.append("                          ssl://localhost:61616?socket.enabledCipherSuites=SSL_RSA_WITH_RC4_128_SHA,SSL_DH_anon_WITH_3DES_EDE_CBC_SHA");
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

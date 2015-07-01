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
package org.titou10.jtb.qm.liberty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;

import com.ibm.websphere.sib.api.jms.JmsConnectionFactory;
import com.ibm.websphere.sib.api.jms.JmsFactoryFactory;

/**
 * 
 * Implements IBM WebSphere Liberty profile Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class LibertyQManager extends QManager {

   private static final Logger    log                      = LoggerFactory.getLogger(LibertyQManager.class);

   private static final String    JMX_URL_TEMPLATE         = "service:jmx:rest://%s:%d/IBMJMXConnectorREST";

   private static final String    ON_QUEUES                = "WebSphere:feature=wasJmsServer,type=Queue,name=*";
   private static final String    ON_TOPICS                = "WebSphere:feature=wasJmsServer,type=Topic,name=*";

   private static final String    ON_QUEUE                 = "WebSphere:feature=wasJmsServer,type=Queue,name=%s,*";

   private static final String    SYSTEM_PREFIX            = "_";

   private static final String    CR                       = "\n";

   private static final String    P_BUS_NAME               = "busName";
   private static final String    P_PROVIDER_ENDPOINTS     = "providerEndPoints";
   private static final String    P_TARGET_TRANSPORT_CHAIN = "targetTransportChain";
   private static final String    P_TRUST_STORE            = "javax.net.ssl.trustStore";
   private static final String    P_TRUST_STORE_PASSWORD   = "javax.net.ssl.trustStorePassword";
   private static final String    P_TRUST_STORE_TYPE       = "javax.net.ssl.trustStoreType";

   private List<QManagerProperty> parameters               = new ArrayList<QManagerProperty>();
   private SortedSet<String>      queueNames               = new TreeSet<>();
   private SortedSet<String>      topicNames               = new TreeSet<>();

   private JMXConnector           jmxc;
   private MBeanServerConnection  mbsc;

   public LibertyQManager() {
      log.debug("Instantiate LibertyQManager");

      parameters.add(new QManagerProperty(P_BUS_NAME,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Bus name is Liberty (Default DefaultME)"));
      parameters.add(new QManagerProperty(P_PROVIDER_ENDPOINTS,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "JmsConnectionFactory providerEndPoints (eg. localhost:7276:BootstrapBasicMessaging)"));
      parameters.add(new QManagerProperty(P_TARGET_TRANSPORT_CHAIN, true, JMSPropertyKind.STRING));

      parameters.add(new QManagerProperty(P_TRUST_STORE, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_TRUST_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(P_TRUST_STORE_TYPE, false, JMSPropertyKind.STRING));
   }

   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects) throws Exception {

      // Save System properties
      saveSystemProperties();
      try {

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         String busName = mapProperties.get(P_BUS_NAME);
         String providerEndPoints = mapProperties.get(P_PROVIDER_ENDPOINTS);
         String targetTransportChain = mapProperties.get(P_TARGET_TRANSPORT_CHAIN);

         String trustStore = mapProperties.get(P_TRUST_STORE);
         String trustStorePassword = mapProperties.get(P_TRUST_STORE_PASSWORD);
         String trustStoreType = mapProperties.get(P_TRUST_STORE_TYPE);

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
         if (trustStoreType == null) {
            System.clearProperty(P_TRUST_STORE_TYPE);
         } else {
            System.setProperty(P_TRUST_STORE_TYPE, trustStoreType);
         }

         // Set REST/JMX Connection properties
         HashMap<String, Object> environment = new HashMap<String, Object>();
         environment.put("jmx.remote.protocol.provider.pkgs", "com.ibm.ws.jmx.connector.client");
         environment.put("com.ibm.ws.jmx.connector.client.disableURLHostnameVerification", Boolean.TRUE);
         environment.put("com.ibm.ws.jmx.connector.client.rest.maxServerWaitTime", 0);
         environment.put("com.ibm.ws.jmx.connector.client.rest.notificationDeliveryInterval", 65000);
         environment.put(JMXConnector.CREDENTIALS, new String[] { sessionDef.getUserid(), sessionDef.getPassword() });

         // Connect

         String serviceURL = String.format(JMX_URL_TEMPLATE, sessionDef.getHost(), sessionDef.getPort());
         log.debug("connecting to {}", serviceURL);

         JMXServiceURL url = new JMXServiceURL(serviceURL);
         jmxc = JMXConnectorFactory.newJMXConnector(url, environment);
         jmxc.connect();
         mbsc = jmxc.getMBeanServerConnection();

         // Discover Queues and Topics

         Set<ObjectName> setQueues = mbsc.queryNames(new ObjectName(ON_QUEUES), null);
         for (ObjectName objectQueue : setQueues) {
            String name = objectQueue.getKeyProperty("name");
            log.debug("q={}", objectQueue);
            if (!showSystemObjects) {
               if (name.startsWith(SYSTEM_PREFIX)) {
                  continue;
               }
            }
            queueNames.add(name);
         }

         Set<ObjectName> setTopics = mbsc.queryNames(new ObjectName(ON_TOPICS), null);
         for (ObjectName objectTopic : setTopics) {
            String name = objectTopic.getKeyProperty("name");
            log.debug("t={} p={}", setTopics, objectTopic.getKeyPropertyListString());
            if (!showSystemObjects) {
               if (name.startsWith(SYSTEM_PREFIX)) {
                  continue;
               }
            }
            topicNames.add(name);
         }

         // Produce the JMS Connection

         log.debug("providerEndPoints: {} targetTransportChain:{}", providerEndPoints, targetTransportChain);
         JmsFactoryFactory jff = JmsFactoryFactory.getInstance();
         JmsConnectionFactory jcf = jff.createConnectionFactory();
         jcf.setBusName(busName);
         jcf.setProviderEndpoints(providerEndPoints);
         jcf.setTargetTransportChain(targetTransportChain);
         jcf.setUserName(sessionDef.getUserid());
         jcf.setPassword(sessionDef.getPassword());

         Connection con = jcf.createConnection();
         log.debug("con = {}", con);

         return con;

      } finally {
         restoreSystemProperties();
      }
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      jmsConnection.close();
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
         ObjectName on = new ObjectName(String.format(ON_QUEUE, queueName));
         Set<ObjectName> queueSet = mbsc.queryNames(on, null);
         if ((queueSet != null) && (!queueSet.isEmpty())) {
            // TODO Long -> Integer !
            depth = ((Long) mbsc.getAttribute(queueSet.iterator().next(), "Depth")).intValue();
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
         ObjectName on = new ObjectName(String.format(ON_QUEUE, queueName));
         Set<ObjectName> queueSet = mbsc.queryNames(on, null);
         if ((queueSet != null) && (!queueSet.isEmpty())) {
            addInfo(properties, queueSet, "Id");
            addInfo(properties, queueSet, "State");
            addInfo(properties, queueSet, "Depth");
            addInfo(properties, queueSet, "MaxQueueDepth");
            addInfo(properties, queueSet, "SendAllowed");
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
      sb.append("Extra JARS:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("- restConnector.jar                        (from <wlp_home>/clients)").append(CR);
      sb.append("- com.ibm.ws.ejb.thinclient_8.5.0.jar      (from <was_full_home>/runtimes)").append(CR);
      sb.append("- com.ibm.ws.orb_8.5.0.jar                 (from <was_full_home>/runtimes)").append(CR);
      sb.append("- com.ibm.ws.sib.client.thin.jms_8.5.0.jar (from <was_full_home>/runtimes)").append(CR);
      sb.append(CR);
      sb.append("WLP Server features:").append(CR);
      sb.append("--------------------").append(CR);
      sb.append("- restConnector-1.0+ (Mandatory)").append(CR);
      sb.append("- appSecurity-2.0+").append(CR);
      sb.append("- wasJmsClient-2.0+ (Mandatory)").append(CR);
      sb.append("- wasJmsServer-1.0+ (Mandatory)").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : WLP server host name (eg. localhost)").append(CR);
      sb.append("Port          : WLP rest port (9080, 9443)").append(CR);
      sb.append("User/Password : User allowed to perform rest calls").append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("busname              : WLP bus name (eg DefaultME)").append(CR);
      sb.append("providerEndPoints    : <SIB serveur name>:<JMS inbound port>:<Mode>").append(CR);
      sb.append("                     : eg. localhost:7276:BootstrapBasicMessaging").append(CR);
      sb.append("                     : Mode: BootstrapBasicMessaging|BootStrapSecureMessaging (SSL)");
      sb.append(CR);
      sb.append("targetTransportChain : InboundBasicMessaging").append(CR);
      sb.append(CR);
      sb.append("javax.net.ssl.trustStore         : keystore").append(CR);
      sb.append("javax.net.ssl.trustStorePassword : keystore password").append(CR);
      sb.append("javax.net.ssl.trustStoreType     : JKS (default), PKCS12, ...").append(CR);
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

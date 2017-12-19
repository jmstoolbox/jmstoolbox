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
package org.titou10.jtb.qm.wassib;

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
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.ConnectionData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.sib.api.jms.JmsConnectionFactory;
import com.ibm.websphere.sib.api.jms.JmsFactoryFactory;

/**
 * 
 * Implements IBM WebSphere full profile Q Provider via JMX
 * 
 * @author Denis Forveille
 *
 */
public class WASSIBQManager extends QManager {

   private static final Logger             log                      = LoggerFactory.getLogger(WASSIBQManager.class);

   private static final String             ON_QUEUES_TEMPLATE       = "WebSphere:SIBus=%s,type=SIBQueuePoint,*";
   private static final String             ON_TOPICS_TEMPLATE       = "WebSphere:SIBus=%s,type=SIBPublicationPoint,*";

   private static final String             ON_QUEUE                 = "WebSphere:SIBus=%s,type=SIBQueuePoint,name=%s,*";
   private static final String             ON_TOPIC                 = "WebSphere:SIBus=%s,type=SIBPublicationPoint,name=%s,*";

   private static final String             SYSTEM_PREFIX            = "_";

   private static final String             CR                       = "\n";

   private static final String             P_BUS_NAME               = "busName";
   private static final String             P_PROVIDER_ENDPOINTS     = "providerEndPoints";
   private static final String             P_TARGET_TRANSPORT_CHAIN = "targetTransportChain";

   private static final String             P_TRUST_STORE            = "javax.net.ssl.trustStore";
   private static final String             P_TRUST_STORE_PASSWORD   = "javax.net.ssl.trustStorePassword";

   private static final String             HELP_TEXT;

   private List<QManagerProperty>          parameters               = new ArrayList<QManagerProperty>();

   private final Map<Integer, AdminClient> adminClients             = new HashMap<>();
   private final Map<Integer, String>      busNames                 = new HashMap<>();

   public WASSIBQManager() {
      log.debug("Instantiate LibertyQManager");

      parameters.add(new QManagerProperty(P_BUS_NAME, true, JMSPropertyKind.STRING, false, "Bus name is WAS"));
      parameters.add(new QManagerProperty(P_PROVIDER_ENDPOINTS,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "JmsConnectionFactory providerEndPoints (eg. localhost:7276:BootstrapBasicMessaging)",
                                          "localhost:7276:BootstrapBasicMessaging"));
      parameters.add(new QManagerProperty(P_TARGET_TRANSPORT_CHAIN,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "The name of the protocol that resolves to a messaging engine",
                                          "InboundBasicMessaging"));
      parameters.add(new QManagerProperty(P_TRUST_STORE, false, JMSPropertyKind.STRING, false, "Must be of kind '.jks'"));
      parameters.add(new QManagerProperty(P_TRUST_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));

      parameters.add(new QManagerProperty(AdminClient.CONNECTOR_SECURITY_ENABLED,
                                          false,
                                          JMSPropertyKind.BOOLEAN,
                                          false,
                                          "SOAP Connector security enabled?"));

   }

   @SuppressWarnings("unchecked")
   @Override
   public ConnectionData connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Save System properties
      saveSystemProperties();
      try {

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         Boolean soapSecurityEnabled = Boolean.valueOf(mapProperties.get(AdminClient.CONNECTOR_SECURITY_ENABLED));
         String busName = mapProperties.get(P_BUS_NAME);
         String providerEndPoints = mapProperties.get(P_PROVIDER_ENDPOINTS);
         String targetTransportChain = mapProperties.get(P_TARGET_TRANSPORT_CHAIN);

         String trustStore = mapProperties.get(P_TRUST_STORE);
         String trustStorePassword = mapProperties.get(P_TRUST_STORE_PASSWORD);

         // Prepare Connection Properties
         java.util.Properties props = new java.util.Properties();
         props.setProperty(AdminClient.CONNECTOR_TYPE, AdminClient.CONNECTOR_TYPE_SOAP);
         props.setProperty(AdminClient.CONNECTOR_HOST, sessionDef.getHost());
         props.setProperty(AdminClient.CONNECTOR_PORT, String.valueOf(sessionDef.getPort()));
         props.setProperty(AdminClient.CACHE_DISABLED, "false");
         props.setProperty(AdminClient.CONNECTOR_AUTO_ACCEPT_SIGNER, "true");
         props.setProperty("com.ibm.ssl.trustStoreFileBased", "true");

         if (sessionDef.getActiveUserid() != null) {
            props.setProperty(AdminClient.USERNAME, sessionDef.getActiveUserid());
         }
         if (sessionDef.getActivePassword() != null) {
            props.setProperty(AdminClient.PASSWORD, sessionDef.getActivePassword());
         }

         if (soapSecurityEnabled != null) {
            if (soapSecurityEnabled) {
               props.setProperty(AdminClient.CONNECTOR_SECURITY_ENABLED, "true");
            }
         }
         if (trustStore != null) {
            props.setProperty(P_TRUST_STORE, trustStore);
            props.setProperty("com.ibm.ssl.trustStore", trustStore);
         }
         if (trustStorePassword != null) {
            props.setProperty(P_TRUST_STORE_PASSWORD, trustStorePassword);
            props.setProperty("com.ibm.ssl.trustStorePassword", trustStorePassword);
         }

         AdminClient adminClient;
         try {
            adminClient = AdminClientFactory.createAdminClient(props);
         } catch (Exception e) {
            log.error("AdminClient Exception", e);
            throw e;
         }
         // log.debug("ac={}", adminClient);

         // Discover Queue and Topics

         SortedSet<QueueData> listQueueData = new TreeSet<>();
         SortedSet<TopicData> listTopicData = new TreeSet<>();

         ObjectName queuesOn = new ObjectName(String.format(ON_QUEUES_TEMPLATE, busName));
         Set<ObjectName> queues = (Set<ObjectName>) adminClient.queryNames(queuesOn, null);
         for (ObjectName o : queues) {
            String name = o.getKeyProperty("name");
            log.debug("Found Queue '{}'", name);
            if (!showSystemObjects) {
               if (name.startsWith(SYSTEM_PREFIX)) {
                  continue;
               }
            }
            listQueueData.add(new QueueData(name));
         }

         ObjectName topicsOn = new ObjectName(String.format(ON_TOPICS_TEMPLATE, busName));
         Set<ObjectName> topics = (Set<ObjectName>) adminClient.queryNames(topicsOn, null);
         for (ObjectName o : topics) {
            String name = o.getKeyProperty("name");
            log.debug("Found Topic '{}'", name);
            if (!showSystemObjects) {
               if (name.startsWith(SYSTEM_PREFIX)) {
                  continue;
               }
            }
            listTopicData.add(new TopicData(name));
         }

         // Create JMS Connection

         JmsFactoryFactory jff = JmsFactoryFactory.getInstance();
         JmsConnectionFactory jcf = jff.createConnectionFactory();
         jcf.setBusName(busName); // sib_saq
         jcf.setProviderEndpoints(providerEndPoints); // localhost:47281:BootstrapBasicMessaging
         jcf.setTargetTransportChain(targetTransportChain); // InboundBasicMessaging
         jcf.setUserName(sessionDef.getActiveUserid());
         jcf.setPassword(sessionDef.getActivePassword());

         Connection jmsConnection = jcf.createConnection();
         jmsConnection.setClientID(clientID);
         jmsConnection.start();

         log.info("connected to {}", sessionDef.getName());

         // Store per connection related data
         Integer hash = jmsConnection.hashCode();
         adminClients.put(hash, adminClient);
         busNames.put(hash, busName);

         return new ConnectionData(jmsConnection, listQueueData, listTopicData);

      } finally {
         restoreSystemProperties();
      }
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occured while closing connection. Ignore it. Msg={}", e.getMessage());
      }

      Integer hash = jmsConnection.hashCode();
      adminClients.remove(hash);
      busNames.remove(hash);
   }

   @Override
   @SuppressWarnings("unchecked")
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      AdminClient adminClient = adminClients.get(hash);
      String busName = busNames.get(hash);

      Integer depth = null;
      try {
         ObjectName on = new ObjectName(String.format(ON_QUEUE, busName, queueName));
         Set<ObjectName> queueSet = adminClient.queryNames(on, null);
         if ((queueSet != null) && (!queueSet.isEmpty())) {
            // TODO Long -> Integer !
            depth = ((Long) adminClient.getAttribute(queueSet.iterator().next(), "depth")).intValue();
         }
      } catch (Exception e) {
         log.error("Exception when reading queue depth. Ignoring", e);
      }
      return depth;
   }

   @Override
   @SuppressWarnings("unchecked")
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      AdminClient adminClient = adminClients.get(hash);
      String busName = busNames.get(hash);

      Map<String, Object> properties = new LinkedHashMap<>();

      try {
         ObjectName on = new ObjectName(String.format(ON_QUEUE, busName, queueName));
         Set<ObjectName> attributesSet = adminClient.queryNames(on, null);
         if ((attributesSet != null) && (!attributesSet.isEmpty())) {
            addInfo(adminClient, properties, attributesSet, "id");
            addInfo(adminClient, properties, attributesSet, "sendAllowed");
            addInfo(adminClient, properties, attributesSet, "state");
            addInfo(adminClient, properties, attributesSet, "highMessageThreshold");
            addInfo(adminClient, properties, attributesSet, "depth");
         }
      } catch (Exception e) {
         log.error("Exception when reading Queue Information. Ignoring", e);
      }

      return properties;
   }

   @Override
   @SuppressWarnings("unchecked")
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {

      Integer hash = jmsConnection.hashCode();
      AdminClient adminClient = adminClients.get(hash);
      String busName = busNames.get(hash);

      Map<String, Object> properties = new LinkedHashMap<>();

      try {
         ObjectName on = new ObjectName(String.format(ON_TOPIC, busName, topicName));
         Set<ObjectName> attributesSet = adminClient.queryNames(on, null);

         // // Display all attributes
         // MBeanInfo info = adminClient.getMBeanInfo(attributesSet.iterator().next());
         // MBeanAttributeInfo[] attrInfo = info.getAttributes();
         // for (MBeanAttributeInfo attr : attrInfo) {
         // System.out.println(" " + attr.getName() + "\n");
         // }

         if ((attributesSet != null) && (!attributesSet.isEmpty())) {
            addInfo(adminClient, properties, attributesSet, "id");
            addInfo(adminClient, properties, attributesSet, "sendAllowed");
            addInfo(adminClient, properties, attributesSet, "highMessageThreshold");
            addInfo(adminClient, properties, attributesSet, "depth");
         }
      } catch (Exception e) {
         log.error("Exception when reading Topic Information. Ignoring", e);
      }

      return properties;
   }

   private void addInfo(AdminClient adminClient,
                        Map<String, Object> properties,
                        Set<ObjectName> attributesSet,
                        String propertyName) {
      try {
         properties.put(propertyName, adminClient.getAttribute(attributesSet.iterator().next(), propertyName));
      } catch (Exception e) {
         log.warn("Exception when reading attribute '" + propertyName + "'. Ignoring: " + e.getMessage());
      }
   }

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("- com.ibm.ws.admin.client_x.y.z.jar        (from <was_full_home>/runtimes)").append(CR);
      sb.append("- com.ibm.ws.orb_z.y.z.jar                 (from <was_full_home>/runtimes)").append(CR);
      sb.append("- com.ibm.ws.sib.client.thin.jms_z.y.z.jar (from <was_full_home>/runtimes)").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : WebSphere server host name").append(CR);
      sb.append("Port          : SOAP_CONNECTOR_ADDRESS port of the server (eg 8890)").append(CR);
      sb.append("User/Password : User allowed to connect to the SOAP port and SIB").append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("busName                          : SI Bus name").append(CR);
      sb.append("securityEnabled                  : true if SSL security is active on the SOAP connector").append(CR);
      sb.append("javax.net.ssl.trustStore         : (optional) trust store (JKS format)").append(CR);
      sb.append("javax.net.ssl.trustStorePassword : (optional) trust store password").append(CR);
      sb.append(CR);
      sb.append("If SSL is disabled:").append(CR);
      sb.append("  providerEndPoints    : The list of comma separated endpoints used to connect to a bootstrap server").append(CR);
      sb.append("                       : example: <WAS host name>:<SIB_ENDPOINT_ADDRESS>:BootstrapBasicMessaging").append(CR);
      sb.append("  targetTransportChain : The name of the protocol that resolves to a messaging engine").append(CR);
      sb.append("                       : example: InboundBasicMessaging").append(CR);
      sb.append(CR);
      sb.append("If SSL is enabled:").append(CR);
      sb.append("  providerEndPoints    : The list of comma separated endpoints used to connect to a bootstrap server").append(CR);
      sb.append("                       : example: <WAS host name>:<SIB_ENDPOINT_SECURE_ADDRESS>:BootStrapSecureMessaging")
               .append(CR);
      sb.append("  targetTransportChain : The name of the protocol that resolves to a messaging engine").append(CR);
      sb.append("                       : example : InboundSecureMessaging").append(CR);
      // sb.append("com.ibm.CORBA.ConfigURL : (optional) points to a 'sas.client.props' client configuration file").append(CR);
      // sb.append("com.ibm.SSL.ConfigURL : (optional) points to a 'ssl.client.props' client configuration file").append(CR);

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

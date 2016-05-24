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
package org.titou10.jtb.qm.wassib;

import java.util.ArrayList;
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
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;

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

   private static final Logger    log                      = LoggerFactory.getLogger(WASSIBQManager.class);

   private static final String    ON_QUEUES_TEMPLATE       = "WebSphere:SIBus=%s,type=SIBQueuePoint,*";
   private static final String    ON_TOPICS_TEMPLATE       = "WebSphere:SIBus=%s,type=SIBPublicationPoint,*";

   private static final String    ON_QUEUE                 = "WebSphere:SIBus=%s,type=SIBQueuePoint,name=%s,*";
   private static final String    ON_TOPIC                 = "WebSphere:SIBus=%s,type=SIBPublicationPoint,name=%s,*";

   private static final String    SYSTEM_PREFIX            = "_";

   private static final String    CR                       = "\n";

   private static final String    P_BUS_NAME               = "busName";
   private static final String    P_PROVIDER_ENDPOINTS     = "providerEndPoints";
   private static final String    P_TARGET_TRANSPORT_CHAIN = "targetTransportChain";

   private static final String    P_TRUST_STORE            = "javax.net.ssl.trustStore";
   private static final String    P_TRUST_STORE_PASSWORD   = "javax.net.ssl.trustStorePassword";

   private List<QManagerProperty> parameters               = new ArrayList<QManagerProperty>();
   private SortedSet<String>      queueNames               = new TreeSet<>();
   private SortedSet<String>      topicNames               = new TreeSet<>();

   private AdminClient            adminClient;
   private String                 busName;

   public WASSIBQManager() {
      log.debug("Instantiate LibertyQManager");

      parameters.add(new QManagerProperty(P_BUS_NAME, true, JMSPropertyKind.STRING, false, "Bus name is WAS"));
      parameters.add(new QManagerProperty(P_PROVIDER_ENDPOINTS,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "JmsConnectionFactory providerEndPoints (eg. localhost:7276:BootstrapBasicMessaging)"));
      parameters.add(new QManagerProperty(P_TARGET_TRANSPORT_CHAIN, true, JMSPropertyKind.STRING));

      parameters.add(new QManagerProperty(P_TRUST_STORE, false, JMSPropertyKind.STRING, false, "Must be of kind .jks"));
      parameters.add(new QManagerProperty(P_TRUST_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));

      parameters.add(new QManagerProperty(AdminClient.CONNECTOR_SECURITY_ENABLED,
                                          false,
                                          JMSPropertyKind.BOOLEAN,
                                          false,
                                          "SOAP Connector security enabled?"));

   }

   @SuppressWarnings("unchecked")
   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects) throws Exception {

      // Save System properties
      saveSystemProperties();
      try {

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         Boolean soapSecurityEnabled = Boolean.valueOf(mapProperties.get(AdminClient.CONNECTOR_SECURITY_ENABLED));
         busName = mapProperties.get(P_BUS_NAME);
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

         if (sessionDef.getUserid() != null) {
            props.setProperty(AdminClient.USERNAME, sessionDef.getUserid());
         }
         if (sessionDef.getPassword() != null) {
            props.setProperty(AdminClient.PASSWORD, sessionDef.getPassword());
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

         try {
            adminClient = AdminClientFactory.createAdminClient(props);
         } catch (Exception e) {
            log.error("AdminClient Exception", e);
            throw e;
         }
         log.warn("ac={}", adminClient);

         // Discover Queue and Topics

         ObjectName queuesOn = new ObjectName(String.format(ON_QUEUES_TEMPLATE, busName));
         Set<ObjectName> queues = (Set<ObjectName>) adminClient.queryNames(queuesOn, null);
         for (ObjectName o : queues) {
            log.debug("q=" + o);
            String name = o.getKeyProperty("name");
            if (!showSystemObjects) {
               if (name.startsWith(SYSTEM_PREFIX)) {
                  continue;
               }
            }
            queueNames.add(name);
         }

         ObjectName topicsOn = new ObjectName(String.format(ON_TOPICS_TEMPLATE, busName));
         Set<ObjectName> topics = (Set<ObjectName>) adminClient.queryNames(topicsOn, null);
         for (ObjectName o : topics) {
            log.debug("t=" + o);
            String name = o.getKeyProperty("name");
            if (!showSystemObjects) {
               if (name.startsWith(SYSTEM_PREFIX)) {
                  continue;
               }
            }
            topicNames.add(name);
         }

         // Create JMS Connection

         JmsFactoryFactory jff = JmsFactoryFactory.getInstance();
         JmsConnectionFactory jcf = jff.createConnectionFactory();
         jcf.setBusName(busName); // sib_saq
         jcf.setProviderEndpoints(providerEndPoints); // localhost:47281:BootstrapBasicMessaging
         jcf.setTargetTransportChain(targetTransportChain); // InboundBasicMessaging
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
      log.debug("close connection");
      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occured while closing session. Ignore it. Msg={}", e.getMessage());
      }
      queueNames.clear();
      topicNames.clear();
   }

   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }

   @Override
   @SuppressWarnings("unchecked")
   public Integer getQueueDepth(String queueName) {
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
   public Map<String, Object> getQueueInformation(String queueName) {
      Map<String, Object> properties = new LinkedHashMap<>();

      try {
         ObjectName on = new ObjectName(String.format(ON_QUEUE, busName, queueName));
         Set<ObjectName> attributesSet = adminClient.queryNames(on, null);
         if ((attributesSet != null) && (!attributesSet.isEmpty())) {
            addInfo(properties, attributesSet, "id");
            addInfo(properties, attributesSet, "sendAllowed");
            addInfo(properties, attributesSet, "state");
            addInfo(properties, attributesSet, "highMessageThreshold");
            addInfo(properties, attributesSet, "depth");
         }
      } catch (Exception e) {
         log.error("Exception when reading Queue Information. Ignoring", e);
      }

      return properties;
   }

   @Override
   @SuppressWarnings("unchecked")
   public Map<String, Object> getTopicInformation(String topicName) {
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
            addInfo(properties, attributesSet, "id");
            addInfo(properties, attributesSet, "sendAllowed");
            addInfo(properties, attributesSet, "highMessageThreshold");
            addInfo(properties, attributesSet, "depth");
         }
      } catch (Exception e) {
         log.error("Exception when reading Topic Information. Ignoring", e);
      }

      return properties;
   }

   private void addInfo(Map<String, Object> properties, Set<ObjectName> attributesSet, String propertyName) {
      try {
         properties.put(propertyName, adminClient.getAttribute(attributesSet.iterator().next(), propertyName));
      } catch (Exception e) {
         log.warn("Exception when reading attribute '" + propertyName + "'. Ignoring: " + e.getMessage());
      }
   }

   @Override
   public String getHelpText() {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("- com.ibm.ws.admin.client_8.5.0.jar        (from <was_full_home>/runtimes)").append(CR);
      sb.append("- com.ibm.ws.orb_8.5.0.jar                 (from <was_full_home>/runtimes)").append(CR);
      sb.append("- com.ibm.ws.sib.client.thin.jms_8.5.0.jar (from <was_full_home>/runtimes)").append(CR);
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
}

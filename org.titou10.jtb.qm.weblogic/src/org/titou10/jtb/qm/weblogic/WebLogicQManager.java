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

package org.titou10.jtb.qm.weblogic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.directory.InitialDirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.Properties;
import org.titou10.jtb.config.gen.Properties.Property;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.ConnectionData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;

/**
 * 
 * Implements Oracle WebLogic Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class WebLogicQManager extends QManager {

   private static final Logger                       log                              = LoggerFactory
            .getLogger(WebLogicQManager.class);

   // service:jmx:t3://localhost:7001/jndi/weblogic.management.mbeanservers.domainruntime
   // service:jmx:t3s://localhost:7001/jndi/weblogic.management.mbeanservers.domainruntime
   private static final String                       JMX_URL                          = "service:jmx:%s://%s:%d/jndi/%s";
   private static final String                       PROVIDER_URL                     = "%s://%s:%d";

   private static final String                       ON_DESTINATIONS                  = "com.bea:Type=JMSDestinationRuntime,ServerRuntime=%s,JMSServerRuntime=%s,*";
   private static final String                       ON_DESTINATION                   = "com.bea:Type=JMSDestinationRuntime,Name=%s";

   private static final String                       DESTINATION_NAME_PREFIX          = "./";
   private static final String                       WLS_DEFAULT_CONNECTION_FACTORY   = "weblogic.jms.ConnectionFactory";

   private static final String[]                     WLS_DESTINATION_ATTRIBUTES_NAMES = { "BytesCurrentCount", "BytesHighCount",
                                                                                          "BytesPendingCount", "BytesReceivedCount",
                                                                                          "BytesThresholdTime",
                                                                                          "ConsumersCurrentCount",
                                                                                          "ConsumersHighCount",
                                                                                          "ConsumersTotalCount",
                                                                                          "ConsumptionPaused",
                                                                                          "ConsumptionPausedState",
                                                                                          "DestinationType", "InsertionPaused",
                                                                                          "InsertionPausedState",
                                                                                          "MessagesCurrentCount",
                                                                                          "MessagesDeletedCurrentCount",
                                                                                          "MessagesHighCount",
                                                                                          "MessagesMovedCurrentCount",
                                                                                          "MessagesPendingCount",
                                                                                          "MessagesReceivedCount",
                                                                                          "MessagesThresholdTime", "Paused",
                                                                                          "ProductionPaused",
                                                                                          "ProductionPausedState", "State" };

   private static final String                       CR                               = "\n";

   private static final String                       P_JMX_CONNECTION_PROTOCOL        = "JMX connection protocol";
   private static final String                       P_JMX_MBEAN_SERVER               = "JMX MBean Server";
   private static final String                       P_SERVER_RUNTIME_NAME            = "Server Runtime Name";
   private static final String                       P_JMS_SERVER_RUNTIME_NAME        = "JMS Server Runtime Name";
   private static final String                       P_JNDI_PROVIDER_PROTOCOL         = "JNDI Provider URL";

   private static final String                       P_TRUST_STORE                    = "javax.net.ssl.trustStore";
   private static final String                       P_TRUST_STORE_PASSWORD           = "javax.net.ssl.trustStorePassword";
   private static final String                       P_TRUST_STORE_TYPE               = "javax.net.ssl.trustStoreType";

   private static final String                       HELP_TEXT;

   private List<QManagerProperty>                    parameters                       = new ArrayList<QManagerProperty>();

   private final Map<Integer, JMXConnector>          jmxcs                            = new HashMap<>();
   private final Map<Integer, MBeanServerConnection> mbscs                            = new HashMap<>();

   // ------------------------
   // Constructor
   // ------------------------

   public WebLogicQManager() {

      log.debug("Instantiate MQQManager");

      parameters.add(new QManagerProperty(P_JMX_CONNECTION_PROTOCOL,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "JMX connection protocol (eg 't3', 't3s')",
                                          "t3"));
      parameters.add(new QManagerProperty(P_JMX_MBEAN_SERVER,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "JMX connection protocol (eg 'weblogic.management.mbeanservers.domainruntime', 'weblogic.management.mbeanservers.runtime')",
                                          "weblogic.management.mbeanservers.domainruntime"));
      parameters.add(new QManagerProperty(P_SERVER_RUNTIME_NAME,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Server Name (eg 'AdminServer')"));
      parameters.add(new QManagerProperty(P_JMS_SERVER_RUNTIME_NAME,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "JMS Server Name (eg 'JMSServer-0')"));
      parameters.add(new QManagerProperty(P_JNDI_PROVIDER_PROTOCOL,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "JNDI Provider URL (eg 't3', 't3s')",
                                          "t3"));

      parameters.add(new QManagerProperty(P_TRUST_STORE, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_TRUST_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(P_TRUST_STORE_TYPE, false, JMSPropertyKind.STRING));

   }

   // ------------------------
   // Business Interface
   // ------------------------

   @Override
   public ConnectionData connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Save System properties
      saveSystemProperties();
      try {

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         String jmxProtocol = mapProperties.get(P_JMX_CONNECTION_PROTOCOL);
         String jmxMBeanServer = mapProperties.get(P_JMX_MBEAN_SERVER);
         String serverRuntimeName = mapProperties.get(P_SERVER_RUNTIME_NAME);
         String jmsServerRuntimeName = mapProperties.get(P_JMS_SERVER_RUNTIME_NAME);
         String jndiProtocol = mapProperties.get(P_JNDI_PROVIDER_PROTOCOL);

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

         // JMX Connection
         // JMX SSL: http://www.dba-oracle.com/zzz_weblogic_security_automation_with_jmx.htm
         HashMap<String, Object> jmxEnv = new HashMap<String, Object>();
         jmxEnv.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
         jmxEnv.put(JMXConnector.CREDENTIALS, new String[] { sessionDef.getUserid(), sessionDef.getPassword() });
         // jmxEnv.put("weblogic.security.SSL.ignoreHostnameVerification", "true");
         // -Dweblogic.security.SSL.ignoreHostnameVerification=true
         // -Dweblogic.security.allowCryptoJDefaultJCEVerification=true
         // -Dweblogic.security.allowCryptoJDefaultPRNG=true
         // -Dweblogic.security.TrustKeyStore=CustomTrust
         // -Dweblogic.security.CustomTrustKeyStoreFileName=<directoy>\DemoTrust.jks

         // service:jmx:t3://localhost:7001/jndi/weblogic.management.mbeanservers.domainruntime
         String jmxURL = String.format(JMX_URL, jmxProtocol, sessionDef.getHost(), sessionDef.getPort(), jmxMBeanServer);
         JMXServiceURL serviceURL = new JMXServiceURL(jmxURL);
         log.debug("connecting to {}", serviceURL);

         JMXConnector jmxc = JMXConnectorFactory.connect(serviceURL, jmxEnv);
         jmxc.connect();
         MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

         // Discover Queues and Topics

         SortedSet<String> queueNames = new TreeSet<>();
         SortedSet<String> topicNames = new TreeSet<>();

         ObjectName destON = new ObjectName(String.format(ON_DESTINATIONS, serverRuntimeName, jmsServerRuntimeName));
         Set<ObjectName> setDestinations = mbsc.queryNames(destON, null);
         for (ObjectName objectDestination : setDestinations) {
            log.debug("q={}", objectDestination);
            String name = objectDestination.getKeyProperty("Name");
            String type = (String) mbsc.getAttribute(objectDestination, "DestinationType");
            if (type.equals("Queue")) {
               queueNames.add(DESTINATION_NAME_PREFIX + name);
            } else {
               topicNames.add(DESTINATION_NAME_PREFIX + name);
            }
         }

         // Produce the JMS Connection

         String providerURL = String.format(PROVIDER_URL, jndiProtocol, sessionDef.getHost(), sessionDef.getPort());
         log.debug("Provider URL : {}", providerURL);

         Hashtable<String, String> jndiEnv = new Hashtable<String, String>();
         jndiEnv.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
         jndiEnv.put(Context.PROVIDER_URL, providerURL);
         jndiEnv.put(Context.REFERRAL, "throw");

         InitialContext ctx = new InitialDirContext(jndiEnv);
         ConnectionFactory connFactory = (ConnectionFactory) ctx.lookup(WLS_DEFAULT_CONNECTION_FACTORY);

         Connection jmsConnection = connFactory.createConnection(sessionDef.getUserid(), sessionDef.getPassword());
         jmsConnection.setClientID(clientID);
         jmsConnection.start();

         log.info("connected to {}", sessionDef.getName());

         // Store per connection related data
         Integer hash = jmsConnection.hashCode();
         jmxcs.put(hash, jmxc);
         mbscs.put(hash, mbsc);

         return new ConnectionData(jmsConnection, queueNames, topicNames);
      } finally {
         restoreSystemProperties();
      }
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
         ObjectName on = new ObjectName(String.format(ON_DESTINATION, queueName));
         Long mcc = (Long) mbsc.getAttribute(on, "MessagesCurrentCount");
         return mcc.intValue();
      } catch (Exception e) {
         log.error("Exception when reading queue depth. Ignoring", e);
      }
      return depth;

   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {
      return getDestinationInformation(jmsConnection, queueName);
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {
      return getDestinationInformation(jmsConnection, topicName);
   }

   private Map<String, Object> getDestinationInformation(Connection jmsConnection, String destinationName) {
      Integer hash = jmsConnection.hashCode();
      MBeanServerConnection mbsc = mbscs.get(hash);

      Map<String, Object> properties = new LinkedHashMap<>();

      try {
         ObjectName on = new ObjectName(String.format(ON_DESTINATION, destinationName));
         AttributeList attributes = mbsc.getAttributes(on, WLS_DESTINATION_ATTRIBUTES_NAMES);
         for (Object object : attributes) {
            Attribute a = (Attribute) object;
            properties.put(a.getName(), a.getValue());
         }
      } catch (Exception e) {
         log.error("Exception when reading destination attributes. Ignoring", e);
      }

      return properties;

   }

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("- wljmsclient.jar (from the <WLS_SERVER>/lib").append(CR);
      sb.append("- wljmxclient.jar (from the <WLS_SERVER>/lib").append(CR);
      sb.append("").append(CR);
      sb.append("Also required only if SSL is used for the connection (protocol 't3s' or 'iiops') :").append(CR);
      sb.append("- crypto.jar (from the <WLS_SERVER>/lib").append(CR);
      sb.append("- cryptoFIPS.jar (from the <WLS_SERVER>/lib").append(CR);
      sb.append("- wlcipher.jar (from the <WLS_SERVER>/lib").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : Oracle WebLogic server host name").append(CR);
      sb.append("Port          : Oracle WebLogic port (eg 7001)").append(CR);
      sb.append("User/Password : User allowed to connect to Oracle WebLogic and perform JMX operations").append(CR);
      sb.append(CR);
      sb.append("Properties values:").append(CR);
      sb.append("---------------").append(CR);
      sb.append("JMX connection protocol     : Protocol to connect to the JMX server. Usually 't3' or 't3s'").append(CR);
      sb.append("JMX MBean Server            : name of the JMX MBean Server. Usually 'weblogic.management.mbeanservers.domainruntime' or 'weblogic.management.mbeanservers.runtime'")
               .append(CR);
      sb.append("                              See  https://docs.oracle.com/cd/E13222_01/wls/docs90/jmx/accessWLS.html");
      sb.append("Server Runtime Name         : Oracle WebLogic Server Name (eg 'AdminServer')").append(CR);
      sb.append("JMS Server Runtime Name     : Oracle WebLogic JMS Server Name (eg 'JMSServer-0')").append(CR);
      sb.append("JNDI Provider URL           : JNDI Provider URL (eg 't3', 't3s')").append(CR);
      sb.append(CR);
      sb.append("javax.net.ssl.trustStore         : trust store").append(CR);
      sb.append("javax.net.ssl.trustStorePassword : trust store password").append(CR);
      sb.append("javax.net.ssl.trustStoreType     : JKS (default), PKCS12, ...").append(CR);

      HELP_TEXT = sb.toString();
   }

   // ---------
   // Utilities
   // ---------

   protected Map<String, String> extractProperties(SessionDef sessionDef) {
      List<Properties.Property> p = sessionDef.getProperties().getProperty();
      Map<String, String> mapProperties = new HashMap<>(p.size());
      for (Property property : p) {
         mapProperties.put(property.getName(), property.getValue());
      }
      return mapProperties;
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }

}

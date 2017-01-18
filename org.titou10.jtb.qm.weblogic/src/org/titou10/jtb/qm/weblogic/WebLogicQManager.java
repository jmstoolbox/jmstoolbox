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
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.ConnectionData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

/**
 * 
 * Implements Oracle WebLogic Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class WebLogicQManager extends QManager {

   private static final Logger                          log                              = LoggerFactory
            .getLogger(WebLogicQManager.class);

   // service:jmx:t3://localhost:7001/jndi/weblogic.management.mbeanservers.domainruntime
   // service:jmx:t3s://localhost:7001/jndi/weblogic.management.mbeanservers.domainruntime
   private static final String                          JMX_URL                          = "service:jmx:%s://%s:%d/jndi/%s";
   private static final String                          PROVIDER_URL                     = "%s://%s:%d";
   private static final String                          WLS_DEFAULT_CONNECTION_FACTORY   = "weblogic.jms.ConnectionFactory";

   private static final String                          ON_JMSRUNTIME                    = "com.bea:Type=ServerRuntime,Name=%s";

   private static final String[]                        WLS_DESTINATION_ATTRIBUTES_NAMES = { "BytesCurrentCount", "BytesHighCount",
                                                                                             "BytesPendingCount",
                                                                                             "BytesReceivedCount",
                                                                                             "BytesThresholdTime",
                                                                                             "CachingDisabled",
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
                                                                                             "ProductionPausedState", "Registered",
                                                                                             "State" };

   private static final String                          P_JMX_CONNECTION_PROTOCOL        = "JMX connection protocol";
   // private static final String P_JMX_MBEAN_SERVER_NAME = "JMX MBean Server Name";
   private static final String                          P_SERVER_RUNTIME_NAME            = "Server Runtime Name";
   private static final String                          P_JNDI_CONNECTION_PROTOCOL       = "JNDI connection protocol";

   private static final String                          P_TRUST_STORE                    = "javax.net.ssl.trustStore";
   private static final String                          P_TRUST_STORE_PASSWORD           = "javax.net.ssl.trustStorePassword";
   private static final String                          P_TRUST_STORE_TYPE               = "javax.net.ssl.trustStoreType";

   private static final String                          CR                               = "\n";

   private static final String                          HELP_TEXT;

   private List<QManagerProperty>                       parameters                       = new ArrayList<QManagerProperty>();

   private final Map<Integer, JMXConnector>             jmxcs                            = new HashMap<>();
   private final Map<Integer, MBeanServerConnection>    mbscs                            = new HashMap<>();

   // Keep JMX ObjectName corresponding to the destinationName because the ON must be fully qualified to work
   // ie inclusing Location=...
   // ObjectName :
   // com.bea:ServerRuntime=AdminServer,Name=SystemModule-0!Queue-2,Type=JMSDestinationRuntime,JMSServerRuntime=JMSServer-0
   private final Map<Integer, Map<Integer, ObjectName>> destinationONPerConnection       = new HashMap<>();

   // ------------------------
   // Constructor
   // ------------------------

   public WebLogicQManager() {
      log.debug("Instantiate WebLogicQManager");

      parameters.add(new QManagerProperty(P_JMX_CONNECTION_PROTOCOL,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "JMX connection protocol (eg 't3', 't3s')",
                                          "t3"));
      // parameters.add(new QManagerProperty(P_JMX_MBEAN_SERVER_NAME,
      // true,
      // JMSPropertyKind.STRING,
      // false,
      // "JMX MBean Server Name (eg 'weblogic.management.mbeanservers.runtime','weblogic.management.mbeanservers.domainruntime')",
      // "weblogic.management.mbeanservers.runtime"));
      parameters.add(new QManagerProperty(P_SERVER_RUNTIME_NAME,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Server Name (eg 'AdminServer')"));
      parameters
               .add(new QManagerProperty(P_JNDI_CONNECTION_PROTOCOL,
                                         true,
                                         JMSPropertyKind.STRING,
                                         false,
                                         "Protocol used to connect to JNDI (eg 't3', 't3s')",
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
         // String jmxMBeanServer = mapProperties.get(P_JMX_MBEAN_SERVER_NAME);
         String jmxMBeanServer = "weblogic.management.mbeanservers.runtime";
         String serverRuntimeName = mapProperties.get(P_SERVER_RUNTIME_NAME);
         String jndiProtocol = mapProperties.get(P_JNDI_CONNECTION_PROTOCOL);

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

         System.setProperty("weblogic.security.SSL.ignoreHostnameVerification", "true");
         // System.setProperty("weblogic.security.allowCryptoJDefaultJCEVerification", "true");
         // System.setProperty("weblogic.security.allowCryptoJDefaultPRNG", "true");

         // SSL Debug
         // System.setProperty("weblogic.StdoutDebugEnabled", "true");
         // System.setProperty("ssl.debug", "true");
         // System.setProperty("javax.net.debug", "ssl");

         // JMX Connection
         HashMap<String, Object> jmxEnv = new HashMap<String, Object>();
         jmxEnv.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
         jmxEnv.put(JMXConnector.CREDENTIALS, new String[] { sessionDef.getUserid(), sessionDef.getPassword() });
         jmxEnv.put("jmx.remote.x.request.waiting.timeout", Long.parseLong("30000")); // 30 secs

         // service:jmx:t3://localhost:7001/jndi/weblogic.management.mbeanservers.domainruntime
         String jmxURL = String.format(JMX_URL, jmxProtocol, sessionDef.getHost(), sessionDef.getPort(), jmxMBeanServer);
         JMXServiceURL serviceURL = new JMXServiceURL(jmxURL);
         log.debug("connecting to {}", serviceURL);

         JMXConnector jmxc = JMXConnectorFactory.connect(serviceURL, jmxEnv);
         MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

         // Discover Queues and Topics in all the JMSServers attached to the Server

         Map<Integer, ObjectName> destinationObjectName = new HashMap<>();

         SortedSet<QueueData> listQueueData = new TreeSet<>();
         SortedSet<TopicData> listTopicData = new TreeSet<>();

         // Get the JMSServers currently running
         ObjectName serverRuntimeON = new ObjectName(String.format(ON_JMSRUNTIME, serverRuntimeName));
         ObjectName jmsRuntimeON = (ObjectName) mbsc.getAttribute(serverRuntimeON, "JMSRuntime");
         ObjectName[] jmsServersON = (ObjectName[]) mbsc.getAttribute(jmsRuntimeON, "JMSServers");

         // Iterate on each JMSSerr and get the attachedd estinations
         for (ObjectName jmsServerON : jmsServersON) {
            ObjectName[] destinationsON = (ObjectName[]) mbsc.getAttribute(jmsServerON, "Destinations");
            String jmsServerName = jmsServerON.getKeyProperty("Name");
            for (ObjectName onDestination : destinationsON) {
               log.debug("q={}", onDestination);

               String destinationName = onDestination.getKeyProperty("Name");
               String jmsDestinationName = buildJMSDestinationName(jmsServerName, destinationName);

               String type = (String) mbsc.getAttribute(onDestination, "DestinationType");
               if (type.equals("Queue")) {
                  listQueueData.add(new QueueData(jmsDestinationName));
               } else {
                  listTopicData.add(new TopicData(jmsDestinationName));
               }

               destinationObjectName.put(jmsDestinationName.hashCode(), onDestination);
            }
         }

         // Produce the JMS Connection

         String providerURL = String.format(PROVIDER_URL, jndiProtocol, sessionDef.getHost(), sessionDef.getPort());
         log.debug("Provider URL : {}", providerURL);

         Hashtable<String, String> jndiEnv = new Hashtable<String, String>();
         jndiEnv.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
         jndiEnv.put(Context.PROVIDER_URL, providerURL);
         jndiEnv.put(Context.REFERRAL, "follow");

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
         destinationONPerConnection.put(hash, destinationObjectName);

         return new ConnectionData(jmsConnection, listQueueData, listTopicData);
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
         destinationONPerConnection.remove(hash);
      }
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {
      Integer hash = jmsConnection.hashCode();
      MBeanServerConnection mbsc = mbscs.get(hash);
      Map<Integer, ObjectName> destinationObjectNames = destinationONPerConnection.get(hash);

      Integer depth = null;
      try {
         Long mcc = (Long) mbsc.getAttribute(destinationObjectNames.get(queueName.hashCode()), "MessagesCurrentCount");
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

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS (All from <WLS_SERVER>/lib):").append(CR);
      sb.append("---------------------------------------").append(CR);
      sb.append("- wlthint3client.jar").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : Oracle WebLogic server host name").append(CR);
      sb.append("Port          : Oracle WebLogic port (eg 7001)").append(CR);
      sb.append("User/Password : User allowed to connect to Oracle WebLogic and perform JMX operations").append(CR);
      sb.append(CR);
      sb.append("Properties values:").append(CR);
      sb.append("------------------").append(CR);
      // sb.append("The JMX connection URL is of the form 'jmx:<JMX protocol>://<host>:<port>/jndi/<JMX MBean Server Name>'")
      // .append(CR);
      // sb.append("(See https://docs.oracle.com/cd/E13222_01/wls/docs90/jmx/accessWLS.html)").append(CR);
      // sb.append(CR);
      // sb.append("JMX MBean Server Name : Name of the JMX MBean Server.").append(CR);
      // sb.append(" : Usually 'weblogic.management.mbeanservers.domainruntime' or 'weblogic.management.mbeanservers.runtime'")
      // .append(CR);
      sb.append("The JMX connection URL is of the form 'jmx:<JMX protocol>://<host>:<port>/jndi/weblogic.management.mbeanservers.runtime'")
               .append(CR);
      sb.append("(See  https://docs.oracle.com/cd/E13222_01/wls/docs90/jmx/accessWLS.html)").append(CR);
      sb.append(CR);
      sb.append("JMX connection protocol     : Protocol to connect to the JMX server. Usually 't3' or 't3s'").append(CR);
      sb.append("Server Runtime Name         : Oracle WebLogic Server Name (eg 'AdminServer')").append(CR);
      sb.append("JNDI connection protocol    : Protocol used to connect to JNDI (eg 't3', 't3s')").append(CR);
      sb.append(CR);
      sb.append("Extra properties used when connecting via SSL:").append(CR);
      sb.append("----------------------------------------------").append(CR);
      sb.append("javax.net.ssl.trustStore         : Trust store filename (eg D:/somewhere/trust.jks)").append(CR);
      sb.append("javax.net.ssl.trustStorePassword : Trust store password").append(CR);
      sb.append("javax.net.ssl.trustStoreType     : Optionnal: JKS (default), PKCS12, ...").append(CR);

      HELP_TEXT = sb.toString();
   }

   // ---------
   // Utilities
   // ---------

   private Map<String, Object> getDestinationInformation(Connection jmsConnection, String destinationName) {
      Integer hash = jmsConnection.hashCode();
      MBeanServerConnection mbsc = mbscs.get(hash);
      Map<Integer, ObjectName> destinationObjectNames = destinationONPerConnection.get(hash);

      Map<String, Object> properties = new LinkedHashMap<>();

      try {
         AttributeList attributes = mbsc.getAttributes(destinationObjectNames.get(destinationName.hashCode()),
                                                       WLS_DESTINATION_ATTRIBUTES_NAMES);
         for (Object object : attributes) {
            Attribute a = (Attribute) object;
            properties.put(a.getName(), a.getValue());
         }
      } catch (Exception e) {
         log.error("Exception when reading destination attributes. Ignoring", e);
      }

      return properties;
   }

   private String buildJMSDestinationName(String jmsServerName, String destinationName) {
      if (destinationName == null) {
         return null;
      }
      return jmsServerName + "/" + destinationName;
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }

}

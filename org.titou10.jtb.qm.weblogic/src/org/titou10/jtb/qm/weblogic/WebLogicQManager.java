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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
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

   private static final Logger                       log                                 = LoggerFactory
            .getLogger(WebLogicQManager.class);

   private static final String                       ON_DESTINATIONS                     = "com.bea:Type=JMSDestinationRuntime,ServerRuntime=%s,JMSServerRuntime=%s,*";

   private static final String[]                     WLSJMS_DESTINATION_ATTRIBUTES_NAMES = { "BytesCurrentCount", "BytesHighCount",
                                                                                             "BytesPendingCount",
                                                                                             "BytesReceivedCount",
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

   private static final String                       CR                                  = "\n";

   private static final String                       P_SERVER_RUNTIME_NAME               = "Server Runtime Name";
   private static final String                       P_JMS_SERVER_RUNTIME_NAME           = "JMS Server Runtime Name";

   private static final String                       P_TRUST_STORE                       = "javax.net.ssl.trustStore";
   private static final String                       P_TRUST_STORE_PASSWORD              = "javax.net.ssl.trustStorePassword";
   private static final String                       P_TRUST_STORE_TYPE                  = "javax.net.ssl.trustStoreType";

   private static final String                       HELP_TEXT;

   private List<QManagerProperty>                    parameters                          = new ArrayList<QManagerProperty>();

   private final Map<Integer, JMXConnector>          jmxcs                               = new HashMap<>();
   private final Map<Integer, MBeanServerConnection> mbscs                               = new HashMap<>();

   // ------------------------
   // Constructor
   // ------------------------

   public WebLogicQManager() {

      log.debug("Instantiate MQQManager");

      parameters.add(new QManagerProperty(P_SERVER_RUNTIME_NAME,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Server Name (eg AdminServer)"));
      parameters.add(new QManagerProperty(P_JMS_SERVER_RUNTIME_NAME,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "JMS Server Name (eg JMSServer-0)"));

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

      // Extract properties
      Map<String, String> mapProperties = extractProperties(sessionDef);

      String serverRuntimeName = mapProperties.get(P_SERVER_RUNTIME_NAME);
      String jmsServerRuntimeName = mapProperties.get(P_JMS_SERVER_RUNTIME_NAME);

      // Set REST/JMX Connection properties
      HashMap<String, Object> environment = new HashMap<String, Object>();
      environment.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
      environment.put(JMXConnector.CREDENTIALS, new String[] { sessionDef.getUserid(), sessionDef.getPassword() });

      // Connect
      // service:jmx:t3://localhost:7001/jndi/weblogic.management.mbeanservers.domainruntime
      String protocol = "t3";
      String jndiroot = "/jndi/weblogic.management.mbeanservers.runtime";
      JMXServiceURL serviceURL = new JMXServiceURL(protocol, sessionDef.getHost(), sessionDef.getPort(), jndiroot);
      log.debug("connecting to {}", serviceURL);

      JMXConnector jmxc = JMXConnectorFactory.connect(serviceURL, environment);
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
         // String[] x = name.split("!");
         // name = x[1];
         if (type.equals("Queue")) {
            queueNames.add("./" + name);
         } else {
            topicNames.add("./" + name);
         }
      }

      // Produce the JMS Connection

      Hashtable<String, String> e = new Hashtable<String, String>();
      e.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
      e.put(Context.PROVIDER_URL, "t3://localhost:7001");
      e.put(Context.REFERRAL, "throw");

      InitialContext ctx = new InitialDirContext(e);
      // ConnectionFactory connFactory = (ConnectionFactory) ctx.lookup("jms/toto");
      ConnectionFactory connFactory = (ConnectionFactory) ctx.lookup("weblogic.jms.ConnectionFactory");

      // JMSFactoryFactory jff = JmsFactoryFactory.getInstance();
      // JMSConnectionFactory jcf = jff.createConnectionFactory();
      // jcf.setBusName(busName);
      // jcf.setProviderEndpoints(providerEndPoints);
      // jcf.setTargetTransportChain(targetTransportChain);
      // jcf.setUserName(sessionDef.getUserid());
      // jcf.setPassword(sessionDef.getPassword());

      Connection jmsConnection = connFactory.createConnection(sessionDef.getUserid(), sessionDef.getPassword());

      // Connection jmsConnection = jcf.createConnection();
      jmsConnection.setClientID(clientID);
      jmsConnection.start();

      log.info("connected to {}", sessionDef.getName());

      // Store per connection related data
      Integer hash = jmsConnection.hashCode();
      jmxcs.put(hash, jmxc);
      mbscs.put(hash, mbsc);

      return new ConnectionData(jmsConnection, queueNames, topicNames);

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
      return 0;

   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {
      return null;
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {
      return null;
   }

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Recommended: com.ibm.mq.allclient.jar (from the MQ 8+ support pac)").append(CR);
      sb.append(CR);
      sb.append("IBM Support pac Site: http://www-01.ibm.com/support/docview.wss?uid=swg27007197").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : MQ server host name").append(CR);
      sb.append("Port          : MQ port").append(CR);
      sb.append("User/Password : User allowed to connect to MQ").append(CR);
      sb.append(CR);
      sb.append("Properties values:").append(CR);
      sb.append("---------------").append(CR);
      sb.append("queueManager                : Queue Manager Name").append(CR);
      sb.append("channel                     : Channel Name").append(CR);
      sb.append("channelSecurityExit         : Class name of a security exit (Will be loaded from the extra jars)").append(CR);
      sb.append("channelSecurityExitUserData : Security exit data").append(CR);
      sb.append("channelReceiveExit          : Class name of a receive exit (Will be loaded from the extra jars)").append(CR);
      sb.append("channelReceiveExitUserData  : Receive exit data").append(CR);
      sb.append("channelSendExit             : Class name of a send exit (Will be loaded from the extra jars)").append(CR);
      sb.append("channelSendExitUserData     : Send exit data").append(CR);
      sb.append(CR);
      sb.append("sslCipherSuite              : SSl Cipher Suite (Check MQ Documentation)").append(CR);
      sb.append("sslFipsRequired             : SSl FIPS Required? (Check MQ Documentation)").append(CR);
      sb.append("com.ibm.mq.cfg.useIBMCipherMappings : see http://www-01.ibm.com/support/docview.wss?uid=swg1IV66840").append(CR);
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

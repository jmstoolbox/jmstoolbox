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
package org.titou10.jtb.qm.hornetq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueRequestor;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.management.ResourceNames;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.api.jms.management.JMSManagementHelper;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;

/**
 * 
 * Implements HornetQ (embedded in JBoss WidlFly and JBoss AS) Q Provider
 * 
 * Connects via JMX for Queue/Topics discovery
 * 
 * @author Denis Forveille
 *
 */
public class HornetQQManager extends QManager {
   private static final Logger    log        = LoggerFactory.getLogger(HornetQQManager.class);

   // private static final String JMX_URL_TEMPLATE = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi";
   //
   // private static final String ON_QUEUES = "com.sun.messaging.jms.server:type=Destination,subtype=Config,desttype=q,*";
   // private static final String ON_TOPICS = "com.sun.messaging.jms.server:type=Destination,subtype=Config,desttype=t,*";

   private static final String    CR         = "\n";

   private List<QManagerProperty> parameters = new ArrayList<QManagerProperty>();
   private SortedSet<String>      queueNames = new TreeSet<>();
   private SortedSet<String>      topicNames = new TreeSet<>();

   public HornetQQManager() {
      log.debug("Instantiate HornetQQManager");

      parameters.add(new QManagerProperty(TransportConstants.HTTP_ENABLED_PROP_NAME, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(TransportConstants.SSL_ENABLED_PROP_NAME, false, JMSPropertyKind.BOOLEAN));
      // parameters.add(new QManagerProperty(TransportConstants.KEYSTORE_PATH_PROP_NAME, false, JMSPropertyKind.STRING));
      // parameters.add(new QManagerProperty(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, false, JMSPropertyKind.STRING, true));

   }

   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects) throws Exception {

      // Save System properties
      saveSystemProperties();
      try {

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         String sslEnabled = mapProperties.get(TransportConstants.SSL_ENABLED_PROP_NAME);
         String httpEnabled = mapProperties.get(TransportConstants.HTTP_ENABLED_PROP_NAME);
         // String keyStore = mapProperties.get(TransportConstants.KEYSTORE_PATH_PROP_NAME);
         // String keyStorePassword = mapProperties.get(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME);
         String trustStore = mapProperties.get(TransportConstants.TRUSTSTORE_PATH_PROP_NAME);
         String trustStorePassword = mapProperties.get(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME);

         // Netty Connection Properties
         Map<String, Object> connectionParams = new HashMap<String, Object>();
         connectionParams.put(TransportConstants.HOST_PROP_NAME, sessionDef.getHost()); // localhost
         connectionParams.put(TransportConstants.PORT_PROP_NAME, sessionDef.getPort()); // 5445

         if (sslEnabled != null) {
            if (Boolean.valueOf(sslEnabled)) {
               connectionParams.put(TransportConstants.SSL_ENABLED_PROP_NAME, "true");

               // connectionParams.put(TransportConstants.KEYSTORE_PATH_PROP_NAME, keyStore);
               // connectionParams.put(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME, keyStorePassword);
               connectionParams.put(TransportConstants.TRUSTSTORE_PATH_PROP_NAME, trustStore);
               connectionParams.put(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME, trustStorePassword);
            }
         }

         if (httpEnabled != null) {
            if (Boolean.valueOf(httpEnabled)) {
               connectionParams.put(TransportConstants.HTTP_ENABLED_PROP_NAME, "true");
            }
         }

         // // "30.3. Using Management Via Core API"
         //
         // TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(),
         // connectionParams);
         //
         // ClientRequestor requestor = null;
         // ServerLocator locator = HornetQClient.createServerLocatorWithoutHA(transportConfiguration);
         // ClientSessionFactory sf = locator.createSessionFactory();
         // ClientSession session = sf.createSession(false, true, true);
         // try {
         // session.start();
         //
         // requestor = new ClientRequestor(session, "jms.queue.hornetq.management");
         //
         // ClientMessage m = session.createMessage(false);
         // ManagementHelper.putAttribute(m, ResourceNames.CORE_SERVER, "queueNames");
         // ClientMessage r = requestor.request(m);
         // Object queueNames = ManagementHelper.getResult(r);
         // log.debug("queueNames = {}", queueNames);
         //
         // m = session.createMessage(false);
         // ManagementHelper.putAttribute(m, ResourceNames.CORE_SERVER, "topicNames");
         // r = requestor.request(m);
         // Object topicNames = ManagementHelper.getResult(r);
         // log.debug("topicNames = {}", topicNames);
         //
         // } finally {
         // if (requestor != null) {
         // requestor.close();
         // }
         // session.stop();
         // session.close();
         // }

         // "30.4. Using Management Via JMS"

         // localhot:5445
         // Dans hornetq-configuration.xml
         // <security-setting match="jms.queue.hornetq.management">
         // <permission type="manage" roles="admin" />
         // </security-setting>
         TransportConfiguration tcJMS = new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams);

         ConnectionFactory cfJMS = (ConnectionFactory) HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, tcJMS);

         QueueRequestor requestorJMS = null;
         Connection conJMS = cfJMS.createConnection(sessionDef.getUserid(), sessionDef.getPassword());
         Session sessionJMS = conJMS.createSession(false, Session.AUTO_ACKNOWLEDGE);
         try {
            Queue managementQueue = HornetQJMSClient.createQueue("hornetq.management");
            requestorJMS = new QueueRequestor((QueueSession) sessionJMS, managementQueue);
            conJMS.start();

            Message m = sessionJMS.createMessage();
            // JMSManagementHelper.putAttribute(m, ResourceNames.CORE_SERVER, "queueNames");
            JMSManagementHelper.putAttribute(m, ResourceNames.JMS_SERVER, "queueNames");
            Message r = requestorJMS.request(m);
            Object q = JMSManagementHelper.getResult(r);
            if (q instanceof Object[]) {
               log.debug("queueNames = {} class={}", q, q.getClass().getName());
               for (Object o : (Object[]) q) {
                  log.debug("o={}", o);
                  queueNames.add((String) o);
               }
            } else {
               log.warn("queueNames failed");
            }

            m = sessionJMS.createMessage();
            JMSManagementHelper.putAttribute(m, ResourceNames.JMS_SERVER, "topicNames");
            r = requestorJMS.request(m);
            Object t = JMSManagementHelper.getResult(r);
            if (t instanceof Object[]) {
               log.debug("topicNames = {}", topicNames);
               for (Object o : (Object[]) t) {
                  log.debug("o={}", o);
                  topicNames.add((String) o);
               }
            } else {
               log.warn("topicNames failed");
            }

         } finally {
            if (requestorJMS != null) {
               requestorJMS.close();
            }
            sessionJMS.close();
         }

         // "30.2. Using Management Via JMX"
         //
         // HashMap<String, Object> environment = new HashMap<String, Object>();
         // environment.put(JMXConnector.CREDENTIALS, new String[] { sessionDef.getUserid(), sessionDef.getPassword() });
         //
         // // Connect
         //
         // //jmx:rmi:///jndi/rmi://localhost:1090/jmxconnector
         // String serviceURL = String.format(JMX_URL_TEMPLATE, sessionDef.getHost(), sessionDef.getPort());
         // log.debug("connecting to {}", serviceURL);
         //
         // JMXServiceURL url = new JMXServiceURL(serviceURL);
         // JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, environment);
         // connector.connect();
         // MBeanServerConnection mbsc = connector.getMBeanServerConnection();
         //
         // // Discover Queues and Topics
         //
         // Set<ObjectName> setQueues = mbsc.queryNames(new ObjectName(ON_QUEUES), null);
         // for (ObjectName objectQueue : setQueues) {
         // String name = objectQueue.getKeyProperty("name");
         // log.info("q={}", objectQueue);
         // queueNames.add(name);
         // }
         //
         // Set<ObjectName> setTopics = mbsc.queryNames(new ObjectName(ON_TOPICS), null);
         // for (ObjectName objectTopic : setTopics) {
         // String name = objectTopic.getKeyProperty("name");
         // log.info("t={} p={}", setTopics, objectTopic.getKeyPropertyListString());
         // topicNames.add(name);
         // }
         //
         // connector.close();

         return conJMS;
      } finally {
         restoreSystemProperties();
      }
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      jmsConnection.close();
      queueNames.clear();
      topicNames.clear();
   }

   @Override
   public Integer getQueueDepth(String queueName) {
      // http://hornetq.sourceforge.net/docs/hornetq-2.0.0.GA/user-manual/en/html/management.html#management.message-counters
      return null;
   }

   @Override
   public Map<String, Object> getQueueInformation(String queueName) {
      SortedMap<String, Object> properties = new TreeMap<>();
      return properties;
   }

   @Override
   public String getHelpText() {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS :").append(CR);
      sb.append("------------").append(CR);
      sb.append("No extra jar is needed as JMSToolBox is bundled with HornetQ v2.4.6 jars").append(CR);
      sb.append(CR);
      sb.append("Requirements").append(CR);
      sb.append("------------").append(CR);
      sb.append("In hornetq-configuration.xml, add:").append(CR);
      sb.append("<security-setting match=\"jms.queue.hornetq.management\">").append(CR);
      sb.append("   <permission type=\"manage\" roles=\"admin\"/>").append(CR);
      sb.append("</security-setting>").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : HornetQ netty acceptor host name (eg. localhost)").append(CR);
      sb.append("Port          : HornetQ netty acceptor listening port (eg 5445)").append(CR);
      sb.append("User/Password : User allowed to connect to the HornetQ server").append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("http-enabled         : Use an HTTP netty acceptor to connect to the server").append(CR);
      sb.append("ssl-enabled          : Use an SSL netty acceptor to connect to the server").append(CR);
      // sb.append("key-store-path : key store (eg D:/somewhere/key.jks)").append(CR);
      // sb.append("key-store-password : key store password").append(CR);
      sb.append("trust-store-path     : trust store (eg D:/somewhere/trust.jks)").append(CR);
      sb.append("trust-store-password : trust store password").append(CR);

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

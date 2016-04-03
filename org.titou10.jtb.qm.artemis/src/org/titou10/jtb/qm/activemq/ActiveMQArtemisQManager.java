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

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.api.core.management.ResourceNames;
import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.api.jms.JMSFactoryType;
import org.apache.activemq.artemis.api.jms.management.JMSManagementHelper;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.TransportConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;

/**
 * 
 * Implements Apache ActiveMQ Artemis Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class ActiveMQArtemisQManager extends QManager {

   private static final Logger    log             = LoggerFactory.getLogger(ActiveMQArtemisQManager.class);

   private static final String    CR              = "\n";

   private List<QManagerProperty> parameters      = new ArrayList<QManagerProperty>();
   private SortedSet<String>      queueNames      = new TreeSet<>();
   private SortedSet<String>      topicNames      = new TreeSet<>();

   private Queue                  managementQueue = ActiveMQJMSClient.createQueue("activemq.management");

   private Session                sessionJMS;
   private QueueRequestor         requestorJMS;

   public ActiveMQArtemisQManager() {
      log.debug("Apache Active MQ Artemis");

      parameters.add(new QManagerProperty(TransportConstants.HTTP_ENABLED_PROP_NAME, false, JMSPropertyKind.BOOLEAN));
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

         String httpEnabled = mapProperties.get(TransportConstants.HTTP_ENABLED_PROP_NAME);
         String sslEnabled = mapProperties.get(TransportConstants.SSL_ENABLED_PROP_NAME);
         // String keytStore = mapProperties.get(TransportConstants.KEYSTORE_PATH_PROP_NAME);
         // String keyStorePassword = mapProperties.get(TransportConstants.KEYSTORE_PASSWORD_PROP_NAME);
         String trustStore = mapProperties.get(TransportConstants.TRUSTSTORE_PATH_PROP_NAME);
         String trustStorePassword = mapProperties.get(TransportConstants.TRUSTSTORE_PASSWORD_PROP_NAME);

         // Netty Connection Properties
         Map<String, Object> connectionParams = new HashMap<String, Object>();
         connectionParams.put(TransportConstants.HOST_PROP_NAME, sessionDef.getHost()); // localhost
         connectionParams.put(TransportConstants.PORT_PROP_NAME, sessionDef.getPort()); // 61616

         if (sslEnabled != null) {
            if (Boolean.valueOf(sslEnabled)) {
               connectionParams.put(TransportConstants.SSL_ENABLED_PROP_NAME, "true");
               // connectionParams.put(TransportConstants.KEYSTORE_PATH_PROP_NAME, keytStore);
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

         TransportConfiguration tcJMS = new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams);

         ConnectionFactory cfJMS = (ConnectionFactory) ActiveMQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, tcJMS);

         Connection conJMS = cfJMS.createConnection(sessionDef.getUserid(), sessionDef.getPassword());
         sessionJMS = conJMS.createSession(false, Session.AUTO_ACKNOWLEDGE);

         // try {
         requestorJMS = new QueueRequestor((QueueSession) sessionJMS, managementQueue);
         conJMS.start();

         Message m = sessionJMS.createMessage();
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

         // } finally {
         // if (requestorJMS != null) {
         // requestorJMS.close();
         // }
         // sessionJMS.close();
         // }

         return conJMS;
      } finally {
         restoreSystemProperties();
      }

   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection");
      if (requestorJMS != null) {
         try {
            requestorJMS.close();
         } catch (Exception e) {
            log.warn("Exception occured while closing requestorJMS. Ignore it. Msg={}", e.getMessage());
         }
      }
      if (sessionJMS != null) {
         try {
            sessionJMS.close();
         } catch (Exception e) {
            log.warn("Exception occured while closing sessionJMS. Ignore it. Msg={}", e.getMessage());
         }
      }

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occured while closing jmsConnection. Ignore it. Msg={}", e.getMessage());
      }
      queueNames.clear();
      topicNames.clear();
   }

   @Override
   public Integer getQueueDepth(String queueName) {
      try {
         Message m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, "jms.queue." + queueName, "messageCount");
         Message r = requestorJMS.request(m);
         Integer count = (Integer) JMSManagementHelper.getResult(r);
         return count;
      } catch (Exception e) {
         log.error("Exception occurred in getQueueDepth()", e);
         return null;
      }
   }

   @Override
   public Map<String, Object> getQueueInformation(String queueName) {

      String jmsQueueName = "jms.queue." + queueName;

      Message m;
      Message r;

      SortedMap<String, Object> properties = new TreeMap<>();
      try {
         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "consumerCount");
         r = requestorJMS.request(m);
         properties.put("Consumer Count", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "deadLetterAddress");
         r = requestorJMS.request(m);
         properties.put("Dead Letter Address", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "expiryAddress");
         r = requestorJMS.request(m);
         properties.put("Expiry Address", JMSManagementHelper.getResult(r));

         m = sessionJMS.createMessage();
         JMSManagementHelper.putAttribute(m, jmsQueueName, "firstMessageAge");
         r = requestorJMS.request(m);
         properties.put("First Message Age (ms)", JMSManagementHelper.getResult(r));

      } catch (Exception e) {
         log.error("Exception occurred in getQueueInformation()", e);
         return null;
      }

      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(String topicName) {
      SortedMap<String, Object> properties = new TreeMap<>();
      return properties;
   }

   @Override
   public String getHelpText() {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS :").append(CR);
      sb.append("------------").append(CR);
      sb.append("No extra jar is needed as JMSToolBox is bundled with Apache ActiveMQ Artemis v1.2.0 jars").append(CR);
      sb.append(CR);
      sb.append("Requirements").append(CR);
      sb.append("------------").append(CR);
      sb.append("The following configuration is required in broker.xml for JMSToolBox :").append(CR);
      // sb.append(" <management-address><management address, default=jms.queue.activemq.management></management-address>");
      // sb.append(CR);
      // sb.append(" ...").append(CR);
      sb.append(" <security-setting match=\"jms.queue.activemq.management\">").append(CR);
      sb.append(" <permission type=\"manage\" roles=\"<admin role>\" />").append(CR);
      sb.append(" </security-setting>").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : Apache ActiveMQ Artemis netty acceptor host name (eg localhost)").append(CR);
      sb.append("Port          : Apache ActiveMQ Artemis netty listening port (eg. 61616)").append(CR);
      sb.append("User/Password : User allowed to connect to Apache ActiveMQ Artemis, ie associated to the role defined previously");
      sb.append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("- managementAddress  : management-address as defined in broker.xml").append(CR);
      sb.append("- httpEnabled        : Use an HTTP netty acceptor to connect to the server").append(CR);
      sb.append(CR);
      sb.append("- sslEnabled         : Use an SSL netty acceptor to connect to the server").append(CR);
      // sb.append("- keyStorePath : Key store (eg D:/somewhere/trust.jks)").append(CR);
      // sb.append("- keyStorePassword : Key store password").append(CR);
      sb.append("- trustStorePath     : Trust store (eg D:/somewhere/trust.jks)").append(CR);
      sb.append("- trustStorePassword : Trust store password").append(CR);

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

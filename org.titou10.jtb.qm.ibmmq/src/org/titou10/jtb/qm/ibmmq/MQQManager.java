/*
 * Copyright (C) 2022 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.qm.ibmmq;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.DestinationData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.MQReceiveExit;
import com.ibm.mq.MQSecurityExit;
import com.ibm.mq.MQSendExit;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.ibm.msg.client.jms.JmsConstants;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 *
 * Implements IBM MQ Q Provider
 *
 * @author Denis Forveille
 *
 */
public class MQQManager extends QManager {

   private static final Logger                 log                      = LoggerFactory.getLogger(MQQManager.class);

   private static final SimpleDateFormat       SDF                      = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS");
   private static final String                 CR                       = "\n";

   private static final String                 P_QUEUE_MANAGER          = "queueManager";
   private static final String                 P_CHANNEL                = "channel";
   private static final String                 P_SECURITY_EXIT          = "channelSecurityExit";
   private static final String                 P_SECURITY_EXIT_DATA     = "channelSecurityExitUserData";
   private static final String                 P_RECEIVE_EXIT           = "channelReceiveExit";
   private static final String                 P_RECEIVE_EXIT_DATA      = "channelReceiveExitUserData";
   private static final String                 P_SEND_EXIT              = "channelSendExit";
   private static final String                 P_SEND_EXIT_DATA         = "channelSendExitUserData";

   private static final String                 P_SSL_CIPHER_SUITE       = "sslCipherSuite";
   private static final String                 P_SSL_FIPS_REQUIRED      = "sslFipsRequired";

   private static final String                 P_KEY_STORE              = "javax.net.ssl.keyStore";
   private static final String                 P_KEY_STORE_PASSWORD     = "javax.net.ssl.keyStorePassword";
   private static final String                 P_KEY_STORE_TYPE         = "javax.net.ssl.keyStoreType";

   private static final String                 P_TRUST_STORE            = "javax.net.ssl.trustStore";
   private static final String                 P_TRUST_STORE_PASSWORD   = "javax.net.ssl.trustStorePassword";
   private static final String                 P_TRUST_STORE_TYPE       = "javax.net.ssl.trustStoreType";

   private static final String                 P_USE_IBM_CIPHER_MAPPING = "com.ibm.mq.cfg.useIBMCipherMappings";

   private static final String                 P_CCSID                  = "ccsid";

   private static final List<String>           SYSTEM_PREFIXES_1        = List.of("LOOPBACK");
   private static final List<String>           SYSTEM_PREFIXES_2        = List.of("LOOPBACK", "AMQ.", "SYSTEM.");
   private static final String                 SYSTEM_GHOST_PREFIX      = "!!GHOST!";

   private static final String                 HELP_TEXT;

   private final List<QManagerProperty>        parameters               = new ArrayList<>();

   private final Map<Integer, MQQueueManager>  queueManagers            = new HashMap<>();
   private final Map<Integer, PCFMessageAgent> mqAgents                 = new HashMap<>();

   // ------------------------
   // Constructor
   // ------------------------

   public MQQManager() {

      log.debug("Instantiate MQQManager");

      parameters.add(new QManagerProperty(P_QUEUE_MANAGER, false, JMSPropertyKind.STRING, false, "Queue Manager Name"));
      parameters.add(new QManagerProperty(P_CHANNEL, true, JMSPropertyKind.STRING, false, "Channel Name"));
      parameters.add(new QManagerProperty(P_SECURITY_EXIT,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Class name of a channel security exit"));
      parameters.add(new QManagerProperty(P_SECURITY_EXIT_DATA, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_RECEIVE_EXIT,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Class name of a channel receive exit"));
      parameters.add(new QManagerProperty(P_RECEIVE_EXIT_DATA, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_SEND_EXIT, false, JMSPropertyKind.STRING, false, "Class name of a channel send exit"));
      parameters.add(new QManagerProperty(P_SEND_EXIT_DATA, false, JMSPropertyKind.STRING));

      parameters.add(new QManagerProperty(P_KEY_STORE, false, JMSPropertyKind.STRING, false, "For client certificate"));
      parameters.add(new QManagerProperty(P_KEY_STORE_PASSWORD, false, JMSPropertyKind.STRING, true, "For client certificate"));
      parameters.add(new QManagerProperty(P_KEY_STORE_TYPE, false, JMSPropertyKind.STRING, false, "For client certificate"));

      parameters.add(new QManagerProperty(P_TRUST_STORE, false, JMSPropertyKind.STRING, false, "For server certificate"));
      parameters.add(new QManagerProperty(P_TRUST_STORE_PASSWORD, false, JMSPropertyKind.STRING, true, "For server certificate"));
      parameters.add(new QManagerProperty(P_TRUST_STORE_TYPE, false, JMSPropertyKind.STRING, false, "For server certificate"));

      parameters.add(new QManagerProperty(P_SSL_CIPHER_SUITE, false, JMSPropertyKind.STRING, false, "SSl Cipher Suite"));
      parameters.add(new QManagerProperty(P_SSL_FIPS_REQUIRED, false, JMSPropertyKind.BOOLEAN));

      parameters.add(new QManagerProperty(P_USE_IBM_CIPHER_MAPPING, false, JMSPropertyKind.BOOLEAN));

      parameters.add(new QManagerProperty(P_CCSID,
                                          false,
                                          JMSPropertyKind.INT,
                                          false,
                                          "The coded character set ID to be used for a connection or destination"));
   }

   // ------------------------
   // Business Interface
   // ------------------------

   @Override
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Trace
      // System.setProperty("com.ibm.msg.client.commonservices.trace.outputName", "D:/toto/xxx_%PID%.trc");
      // Trace.setTraceLevel(6);
      // Trace.setOn(true);

      // Save System properties
      saveSystemProperties();
      try {

         var mapProperties = extractProperties(sessionDef);

         var qmName = mapProperties.get(P_QUEUE_MANAGER);
         var channel = mapProperties.get(P_CHANNEL);
         var securityExit = mapProperties.get(P_SECURITY_EXIT);
         var securityExitData = mapProperties.get(P_SECURITY_EXIT_DATA);
         var receiveExit = mapProperties.get(P_RECEIVE_EXIT);
         var receiveExitData = mapProperties.get(P_RECEIVE_EXIT_DATA);
         var sendExit = mapProperties.get(P_SEND_EXIT);
         var sendExitData = mapProperties.get(P_SEND_EXIT_DATA);

         var sslCipherSuite = mapProperties.get(P_SSL_CIPHER_SUITE);
         var sslFipsRequired = Boolean.valueOf(mapProperties.get(P_SSL_FIPS_REQUIRED));

         var useIBMCipherMapping = Boolean.valueOf(mapProperties.get(P_USE_IBM_CIPHER_MAPPING));

         var ccsid = mapProperties.get(P_CCSID) == null ? null : Integer.valueOf(mapProperties.get(P_CCSID));

         var keyStore = mapProperties.get(P_KEY_STORE);
         var keyStorePassword = mapProperties.get(P_KEY_STORE_PASSWORD);
         var keyStoreType = mapProperties.get(P_KEY_STORE_TYPE);
         var trustStore = mapProperties.get(P_TRUST_STORE);
         var trustStorePassword = mapProperties.get(P_TRUST_STORE_PASSWORD);
         var trustStoreType = mapProperties.get(P_TRUST_STORE_TYPE);

         // Set System Properties
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
         if (keyStoreType == null) {
            System.clearProperty(P_KEY_STORE_TYPE);
         } else {
            System.setProperty(P_KEY_STORE_TYPE, keyStoreType);
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
         if (trustStoreType == null) {
            System.clearProperty(P_TRUST_STORE_TYPE);
         } else {
            System.setProperty(P_TRUST_STORE_TYPE, trustStoreType);
         }

         if (useIBMCipherMapping == null) {
            System.clearProperty(P_USE_IBM_CIPHER_MAPPING);
         } else {
            System.setProperty(P_USE_IBM_CIPHER_MAPPING, useIBMCipherMapping.toString());
         }

         // Connection properties
         Hashtable<String, Object> props = new Hashtable();

         // Target MQ
         props.put(CMQC.CHANNEL_PROPERTY, channel);

         // user/password
         if (sessionDef.getActiveUserid() != null) {
            props.put(CMQC.USER_ID_PROPERTY, sessionDef.getActiveUserid());
         } else {
            // Adds the possibility to connect to MQ without a userid
            // Force the System Property "user.name" to blank because MQ client classes v7.1+ will use the "user.name" property to
            // get the userid
            // http://www-01.ibm.com/support/docview.wss?uid=swg21662193
            System.setProperty("user.name", "");
         }

         if (sessionDef.getActivePassword() != null) {
            props.put(CMQC.PASSWORD_PROPERTY, sessionDef.getActivePassword());
         }

         // channelSecurityExit
         if (securityExit != null) {
            Class clazz = getClass().getClassLoader().loadClass(securityExit);
            var securityExitInstance = (MQSecurityExit) clazz.getDeclaredConstructor().newInstance();
            props.put(CMQC.CHANNEL_SECURITY_EXIT_PROPERTY, securityExitInstance);
         }
         if (securityExitData != null) {
            props.put(CMQC.CHANNEL_SECURITY_EXIT_USER_DATA_PROPERTY, securityExitData);
         }

         // channelReceiveExit
         if (receiveExit != null) {
            Class clazz = getClass().getClassLoader().loadClass(receiveExit);
            var receiveExitInstance = (MQReceiveExit) clazz.getDeclaredConstructor().newInstance(clazz);
            props.put(CMQC.CHANNEL_RECEIVE_EXIT_PROPERTY, receiveExitInstance);
         }
         if (receiveExitData != null) {
            props.put(CMQC.CHANNEL_RECEIVE_EXIT_USER_DATA_PROPERTY, receiveExitData);
         }

         // channelSendExit
         if (sendExit != null) {
            Class clazz = getClass().getClassLoader().loadClass(sendExit);
            var sendExitInstance = (MQSendExit) clazz.getDeclaredConstructor().newInstance(clazz);
            props.put(CMQC.CHANNEL_SEND_EXIT_PROPERTY, sendExitInstance);
         }
         if (sendExitData != null) {
            props.put(CMQC.CHANNEL_SEND_EXIT_USER_DATA_PROPERTY, sendExitData);
         }

         // SSL
         if (sslCipherSuite != null) {
            props.put(CMQC.SSL_CIPHER_SUITE_PROPERTY, sslCipherSuite);
         }
         if (sslFipsRequired != null) {
            props.put(CMQC.SSL_FIPS_REQUIRED_PROPERTY, sslFipsRequired);
         }

         // ccsid/encoding
         if (ccsid != null) {
            props.put(CMQC.CCSID_PROPERTY, ccsid);
         }

         // Generic Properties
         props.put(CMQC.APPNAME_PROPERTY, "JMSToolBox");
         props.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);
         props.put(CMQC.CONNECT_OPTIONS_PROPERTY, CMQC.MQCNO_RECONNECT_Q_MGR);

         // Host / Port
         props.put(CMQC.HOST_NAME_PROPERTY, sessionDef.getHost());
         props.put(CMQC.PORT_PROPERTY, sessionDef.getPort());

         // -----------------------------------------------
         // Connect and open Administrative Command channel
         // -----------------------------------------------
         log.debug("Connecting to {}:{}, QM/Channel: {}/{}", sessionDef.getHost(), sessionDef.getPort(), qmName, channel);
         var queueManager = new MQQueueManager(qmName, props);
         var agent = new PCFMessageAgent(queueManager);

         // Create and store JMS Connection
         var ff = JmsFactoryFactory.getInstance(JmsConstants.WMQ_PROVIDER);

         var factory = ff.createConnectionFactory();
         factory.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JMSToolBox");
         factory.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
         factory.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, qmName);
         factory.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
         factory.setIntProperty(WMQConstants.WMQ_CLIENT_RECONNECT_OPTIONS, WMQConstants.WMQ_CLIENT_RECONNECT);

         factory.setStringProperty(WMQConstants.WMQ_HOST_NAME, sessionDef.getHost());
         factory.setIntProperty(WMQConstants.WMQ_PORT, sessionDef.getPort());

         if (sslCipherSuite != null) {
            factory.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, sslCipherSuite);
         }
         if (sslFipsRequired != null) {
            factory.setBooleanProperty(WMQConstants.WMQ_SSL_FIPS_REQUIRED, sslFipsRequired);
         }

         // channelSecurityExit
         if (securityExit != null) {
            factory.setStringProperty(WMQConstants.WMQ_SECURITY_EXIT, securityExit);
         }
         if (securityExitData != null) {
            factory.setStringProperty(WMQConstants.WMQ_SECURITY_EXIT_INIT, securityExitData);
         }

         // channelReceiveExit
         if (receiveExit != null) {
            factory.setStringProperty(WMQConstants.WMQ_RECEIVE_EXIT, securityExit);
         }
         if (receiveExitData != null) {
            factory.setStringProperty(WMQConstants.WMQ_RECEIVE_EXIT_INIT, securityExit);
         }

         // channelSendExit
         if (sendExit != null) {
            factory.setStringProperty(WMQConstants.WMQ_SEND_EXIT, securityExit);
         }
         if (sendExitData != null) {
            factory.setStringProperty(WMQConstants.WMQ_SEND_EXIT_INIT, securityExit);
         }

         // ccsid/encoding
         if (ccsid != null) {
            factory.setIntProperty(WMQConstants.WMQ_CCSID, ccsid);
         }

         // If set, can not open 2 connections on 2 different MQ Q Managers...
         // Done at the connection level later
         // factory.setStringProperty(WMQConstants.CLIENT_ID, "JMSToolBox");

         // -----------------------------------------------
         // Get JMS Connection
         // -----------------------------------------------
         // Only user user/password if set
         var jmsConnection = sessionDef.getActiveUserid() == null ? factory.createConnection()
                  : factory.createConnection(sessionDef.getActiveUserid(), sessionDef.getActivePassword());
         jmsConnection.setClientID(clientID);
         jmsConnection.start();

         log.info("connected to {}", sessionDef.getName());

         // Store per connection related data
         queueManagers.put(jmsConnection.hashCode(), queueManager);
         mqAgents.put(jmsConnection.hashCode(), agent);

         return jmsConnection;

      } finally {
         restoreSystemProperties();
      }
   }

   @Override
   public DestinationData discoverDestinations(Connection jmsConnection, boolean showSystemObjects) throws Exception {
      log.debug("discoverDestinations : {} - {}", jmsConnection, showSystemObjects);

      Integer hash = jmsConnection.hashCode();
      var agent = mqAgents.get(hash);

      var excludedPrefixes = showSystemObjects ? SYSTEM_PREFIXES_1 : SYSTEM_PREFIXES_2;

      // Get list of Queues and Topics
      var listQueueData = buildQueueList(agent, excludedPrefixes);
      var listTopicData = buildTopicList(agent, excludedPrefixes);

      return new DestinationData(listQueueData, listTopicData);
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      Integer hash = jmsConnection.hashCode();
      var queueManager = queueManagers.get(hash);
      var agent = mqAgents.get(hash);

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occured while closing connection. Ignore it. Msg={}", e.getMessage());
      }

      try {
         agent.disconnect();
         queueManager.disconnect();
         queueManager.close();
      } catch (MQException | MQDataException e) {
         throw new JMSException(e.getMessage());
      }

      mqAgents.remove(hash);
      queueManagers.remove(hash);
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      var queueManager = queueManagers.get(hash);

      MQQueue destQueue = null;
      Integer depth = null;
      try {
         destQueue = queueManager.accessQueue(queueName, CMQC.MQOO_INQUIRE);
         depth = destQueue.getCurrentDepth();
         log.debug("Q Depth for {} : {}", queueName, depth);
      } catch (MQException e) {
         log.error("Exception when reading queue depth. Ignoring", e);
      } finally {
         if (destQueue != null) {
            try {
               destQueue.close();
            } catch (MQException e) {}
         }
      }

      return depth;
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      var queueManager = queueManagers.get(hash);

      SortedMap<String, Object> properties = new TreeMap<>();
      MQQueue destQueue = null;
      try {
         try {
            destQueue = queueManager.accessQueue(queueName, CMQC.MQOO_INQUIRE);

            properties.put("Real name", destQueue.getName());

            try {
               properties.put("AlternateUserId", destQueue.getAlternateUserId());
            } catch (MQException e) {
               log.warn("Exception when reading AlternateUserId. Ignoring. " + e.getMessage());
            }

            try {
               switch (destQueue.getCloseOptions()) {
                  case CMQC.MQCO_NONE:
                     properties.put("CloseOptions", "NONE");
                     break;
                  case CMQC.MQCO_DELETE:
                     properties.put("CloseOptions", "DELETE");
                     break;
                  case CMQC.MQCO_DELETE_PURGE:
                     properties.put("CloseOptions", "DELETE PURGE");
                     break;
                  case CMQC.MQCO_KEEP_SUB:
                     properties.put("CloseOptions", "KEEP SUB");
                     break;
                  case CMQC.MQCO_REMOVE_SUB:
                     properties.put("CloseOptions", "REMOVE SUB");
                     break;
                  default:
                     properties.put("CloseOptions", destQueue.getCloseOptions());
                     break;
               }

            } catch (MQException e) {
               log.warn("Exception when reading CloseOptions. Ignoring. " + e.getMessage());
            }

            try {
               properties.put("CreationDateTime", SDF.format(destQueue.getCreationDateTime().getTime()));
            } catch (MQException e) {
               log.warn("Exception when reading CreationDateTime. Ignoring. " + e.getMessage());
            }

            try {
               properties.put("CurrentDepth", destQueue.getCurrentDepth());
            } catch (MQException e) {
               log.warn("Exception when reading CurrentDepth. Ignoring. " + e.getMessage());
            }

            try {
               switch (destQueue.getDefinitionType()) {
                  case CMQC.MQQDT_PREDEFINED:
                     properties.put("DefinitionType", "PREDEFINED");
                     break;
                  case CMQC.MQQDT_PERMANENT_DYNAMIC:
                     properties.put("DefinitionType", "PERMANENT_DYNAMIC");
                     break;
                  case CMQC.MQQDT_TEMPORARY_DYNAMIC:
                     properties.put("DefinitionType", "TEMPORARY_DYNAMIC");
                     break;
                  default:
                     properties.put("DefinitionType", destQueue.getDefinitionType());
                     break;
               }
            } catch (MQException e) {
               log.warn("Exception when reading DefinitionType. Ignoring" + e.getMessage());
            }

            try {
               properties.put("Description", destQueue.getDescription());
            } catch (MQException e) {
               log.warn("Exception when reading Description. Ignoring. " + e.getMessage());
            }

            switch (destQueue.getDestinationType()) {
               case CMQC.MQOT_Q:
                  properties.put("DestinationType", "Queue");
                  break;
               case CMQC.MQOT_TOPIC:
                  properties.put("DestinationType", "Topic");
                  break;
               default:
                  properties.put("DestinationType", destQueue.getDestinationType());
                  break;
            }

            try {
               switch (destQueue.getInhibitGet()) {
                  case CMQC.MQQA_PUT_INHIBITED:
                     properties.put("InhibitGet", "INHIBITED");
                     break;
                  case CMQC.MQQA_PUT_ALLOWED:
                     properties.put("InhibitGet", "ALLOWED");
                     break;
                  default:
                     properties.put("InhibitGet", destQueue.getInhibitGet());
                     break;
               }
            } catch (MQException e) {
               log.warn("Exception when reading InhibitGet. Ignoring" + e.getMessage());
            }

            try {
               switch (destQueue.getInhibitPut()) {
                  case CMQC.MQQA_PUT_INHIBITED:
                     properties.put("InhibitPut", "INHIBITED");
                     break;
                  case CMQC.MQQA_PUT_ALLOWED:
                     properties.put("InhibitPut", "ALLOWED");
                     break;
                  default:
                     properties.put("InhibitPut", destQueue.getInhibitPut());
                     break;
               }
            } catch (MQException e) {
               log.warn("Exception when reading InhibitPut. Ignoring" + e.getMessage());
            }

            try {
               properties.put("MaximumDepth", destQueue.getMaximumDepth());
            } catch (MQException e) {
               log.warn("Exception when reading MaximumDepth. Ignoring" + e.getMessage());
            }

            try {
               properties.put("MaximumMessageLength", destQueue.getMaximumMessageLength());
            } catch (MQException e) {
               log.warn("Exception when reading MaximumMessageLength. Ignoring" + e.getMessage());
            }

            try {
               properties.put("OpenOptions", destQueue.getOpenOptions());
               switch (destQueue.getQueueType()) {
                  case CMQC.MQOO_ALTERNATE_USER_AUTHORITY:
                     properties.put("OpenOptions", "ALTERNATE_USER_AUTHORITY");
                     break;
                  case CMQC.MQOO_BIND_AS_Q_DEF:
                     properties.put("OpenOptions", "BIND_AS_QDEF");
                     break;
                  case CMQC.MQOO_BIND_NOT_FIXED:
                     properties.put("OpenOptions", "BIND_NOT_FIXED");
                     break;
                  case CMQC.MQOO_BIND_ON_OPEN:
                     properties.put("OpenOptions", "BIND_ON_OPEN");
                     break;
                  case CMQC.MQOO_BROWSE:
                     properties.put("OpenOptions", "BROWSE");
                     break;
                  case CMQC.MQOO_FAIL_IF_QUIESCING:
                     properties.put("OpenOptions", "FAIL_IF_QUIESCING");
                     break;
                  case CMQC.MQOO_INPUT_AS_Q_DEF:
                     properties.put("OpenOptions", "INPUT_AS_Q_DEF");
                     break;
                  case CMQC.MQOO_INPUT_SHARED:
                     properties.put("OpenOptions", "INPUT_SHARED");
                     break;
                  case CMQC.MQOO_INPUT_EXCLUSIVE:
                     properties.put("OpenOptions", "INPUT_EXCLUSIVE");
                     break;
                  case CMQC.MQOO_INQUIRE:
                     properties.put("OpenOptions", "INQUIRE");
                     break;
                  case CMQC.MQOO_OUTPUT:
                     properties.put("OpenOptions", "OUTPUT");
                     break;
                  case CMQC.MQOO_PASS_ALL_CONTEXT:
                     properties.put("OpenOptions", "PASS_ALL_CONTEXT");
                     break;
                  case CMQC.MQOO_PASS_IDENTITY_CONTEXT:
                     properties.put("OpenOptions", "PASS_IDENTITY_CONTEXT");
                     break;
                  case CMQC.MQOO_SAVE_ALL_CONTEXT:
                     properties.put("OpenOptions", "SAVE_ALL_CONTEXT");
                     break;
                  case CMQC.MQOO_SET:
                     properties.put("OpenOptions", "SET");
                     break;
                  case CMQC.MQOO_SET_ALL_CONTEXT:
                     properties.put("OpenOptions", "SET_ALL_CONTEXT");
                     break;
                  case CMQC.MQOO_SET_IDENTITY_CONTEXT:
                     properties.put("OpenOptions", "SET_IDENTITY_CONTEXT");
                     break;
                  default:
                     properties.put("OpenOptions", destQueue.getOpenOptions());
                     break;
               }
            } catch (MQException e) {
               log.warn("Exception when reading OpenOptions. Ignoring" + e.getMessage());
            }

            try {
               properties.put("OpenInputCount", destQueue.getOpenInputCount());
            } catch (MQException e) {
               log.warn("Exception when reading OpenInputCount. Ignoring" + e.getMessage());
            }

            try {
               properties.put("OpenOutputCount", destQueue.getOpenOutputCount());
            } catch (MQException e) {
               log.warn("Exception when reading OpenOutputCount. Ignoring" + e.getMessage());
            }

            properties.put("QueueManagerCmdLevel", destQueue.getQueueManagerCmdLevel());

            try {
               switch (destQueue.getQueueType()) {
                  case CMQC.MQQT_ALIAS:
                     properties.put("QueueType", "ALIAS");
                     break;
                  case CMQC.MQQT_LOCAL:
                     properties.put("QueueType", "LOCAL");
                     break;
                  case CMQC.MQQT_MODEL:
                     properties.put("QueueType", "MODEL");
                     break;
                  case CMQC.MQQT_REMOTE:
                     properties.put("QueueType", "REMOTE");
                     break;
                  default:
                     properties.put("QueueType", destQueue.getQueueType());
                     break;
               }
            } catch (MQException e) {
               log.warn("Exception when reading QueueType. Ignoring" + e.getMessage());
            }

            properties.put("ResolvedObjectString", destQueue.getResolvedObjectString());
            properties.put("ResolvedQName", destQueue.getResolvedQName());

            switch (destQueue.getResolvedType()) {
               case CMQC.MQOT_Q:
                  properties.put("ResolvedType", "Queue");
                  break;
               case CMQC.MQOT_TOPIC:
                  properties.put("ResolvedType", "Topic");
                  break;
               case CMQC.MQOT_NONE:
                  properties.put("ResolvedType", "None");
                  break;
               default:
                  properties.put("ResolvedType", destQueue.getResolvedType());
                  break;
            }

            try {
               switch (destQueue.getShareability()) {
                  case CMQC.MQQA_SHAREABLE:
                     properties.put("Shareability", "SHAREABLE");
                     break;
                  case CMQC.MQQA_NOT_SHAREABLE:
                     properties.put("Shareability", "NOT_SHAREABLE");
                     break;
                  default:
                     properties.put("Shareability", destQueue.getShareability());
                     break;
               }
            } catch (MQException e) {
               log.warn("Exception when reading Shareability. Ignoring" + e.getMessage());
            }

            try {
               switch (destQueue.getTriggerControl()) {
                  case CMQC.MQTC_OFF:
                     properties.put("TriggerControl", "OFF");
                     break;
                  case CMQC.MQTC_ON:
                     properties.put("TriggerControl", "ON");
                     break;
                  default:
                     properties.put("TriggerControl", destQueue.getTriggerControl());
                     break;
               }
            } catch (MQException e) {
               log.warn("Exception when reading TriggerControl. Ignoring" + e.getMessage());
            }

            try {
               properties.put("TriggerData", destQueue.getTriggerData());
            } catch (MQException e) {
               log.warn("Exception when reading TriggerData. Ignoring" + e.getMessage());
            }

            try {
               properties.put("TriggerDepth", destQueue.getTriggerDepth());
            } catch (MQException e) {
               log.warn("Exception when reading TriggerDepth. Ignoring" + e.getMessage());
            }

            try {
               properties.put("TriggerMessagePriority", destQueue.getTriggerMessagePriority());
            } catch (MQException e) {
               log.warn("Exception when reading TriggerMessagePriority. Ignoring" + e.getMessage());
            }

            try {
               switch (destQueue.getTriggerType()) {
                  case CMQC.MQTT_NONE:
                     properties.put("TriggerType", "NONE");
                     break;
                  case CMQC.MQTT_FIRST:
                     properties.put("TriggerType", "FIRST");
                     break;
                  case CMQC.MQTT_EVERY:
                     properties.put("TriggerType", "EVERY");
                     break;
                  case CMQC.MQTT_DEPTH:
                     properties.put("TriggerType", "DEPTH");
                     break;
                  default:
                     properties.put("TriggerType", destQueue.getTriggerType());
                     break;
               }
            } catch (MQException e) {
               log.warn("Exception when reading TriggerType. Ignoring" + e.getMessage());
            }

         } catch (MQException e) {
            log.error("Exception when reading Queue Information. Ignoring", e);
         }
      } finally {
         if (destQueue != null) {
            try {
               destQueue.close();
            } catch (MQException e) {}
         }
      }
      log.debug("Queue Information : {}", properties);
      return properties;

   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {

      Integer hash = jmsConnection.hashCode();
      var queueManager = queueManagers.get(hash);

      Map<String, Object> properties = new LinkedHashMap<>();

      PCFMessageAgent agent = null;
      try {
         agent = new PCFMessageAgent(queueManager);

         var request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_TOPIC);
         request.addParameter(CMQC.MQCA_TOPIC_NAME, topicName);

         var responses = agent.send(request);
         var m = responses[0];

         try {
            properties.put("Alteration Date", m.getStringParameterValue(CMQC.MQCA_ALTERATION_DATE));
         } catch (PCFException e) {
            log.warn("Exception when reading Alteration Date. Ignoring" + e.getMessage());
         }
         try {
            properties.put("Alteration Time", m.getStringParameterValue(CMQC.MQCA_ALTERATION_TIME));
         } catch (PCFException e) {
            log.warn("Exception when reading Alteration Time. Ignoring" + e.getMessage());
         }
         try {
            properties.put("Cluster Name", m.getStringParameterValue(CMQC.MQCA_CLUSTER_NAME));
         } catch (PCFException e) {
            log.warn("Exception when reading Cluster Name. Ignoring" + e.getMessage());
         }
         try {
            switch (m.getIntParameterValue(CMQC.MQIA_TOPIC_DEF_PERSISTENCE)) {
               case CMQC.MQPER_PERSISTENCE_AS_PARENT:
                  properties.put("Default persistence", "PERSISTENCE_AS_PARENT");
                  break;
               case CMQC.MQPER_PERSISTENT:
                  properties.put("Default persistence", "PERSISTENT");
                  break;
               case CMQC.MQPER_NOT_PERSISTENT:
                  properties.put("Default persistence", "NOT_PERSISTENT");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading Default persistence. Ignoring" + e.getMessage());
         }
         try {
            properties.put("Default priority", m.getIntParameterValue(CMQC.MQIA_DEF_PRIORITY));
         } catch (PCFException e) {
            log.warn("Exception when reading Default priority. Ignoring" + e.getMessage());
         }
         try {

            switch (m.getIntParameterValue(CMQC.MQIA_DEF_PUT_RESPONSE_TYPE)) {
               case CMQC.MQPRT_ASYNC_RESPONSE:
                  properties.put("Default put response", "ASYNC_RESPONSE");
                  break;
               case CMQC.MQPRT_RESPONSE_AS_PARENT:
                  properties.put("Default put response", "RESPONSE_AS_PARENT");
                  break;
               case CMQC.MQPRT_SYNC_RESPONSE:
                  properties.put("Default put response", "SYNC_RESPONSE");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading Default put response. Ignoring" + e.getMessage());
         }
         try {
            properties.put("Durable Model Q Name", m.getStringParameterValue(CMQC.MQCA_MODEL_DURABLE_Q));
         } catch (PCFException e) {
            log.warn("Exception when reading Durable Model Q Name. Ignoring" + e.getMessage());
         }
         try {
            switch (m.getIntParameterValue(CMQC.MQIA_DURABLE_SUB)) {
               case CMQC.MQSUB_DURABLE_AS_PARENT:
                  properties.put("Durable subscriptions", "DURABLE_AS_PARENT");
                  break;
               case CMQC.MQSUB_DURABLE_NO:
                  properties.put("Durable subscriptions", "DURABLE_NO");
                  break;
               case CMQC.MQSUB_DURABLE_YES:
                  properties.put("Durable subscriptions", "DURABLE_YES");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading Durable subscriptions. Ignoring" + e.getMessage());
         }
         try {
            switch (m.getIntParameterValue(CMQC.MQIA_INHIBIT_PUB)) {
               case CMQC.MQTA_PUB_AS_PARENT:
                  properties.put("Inhibit Publications", "PUB_AS_PARENT");
                  break;
               case CMQC.MQTA_PUB_INHIBITED:
                  properties.put("Inhibit Publications", "PUB_INHIBITED");
                  break;
               case CMQC.MQTA_PUB_ALLOWED:
                  properties.put("Inhibit Publications", "PUB_ALLOWED");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading Inhibit Publications. Ignoring" + e.getMessage());
         }
         try {
            switch (m.getIntParameterValue(CMQC.MQIA_INHIBIT_SUB)) {
               case CMQC.MQTA_SUB_AS_PARENT:
                  properties.put("Inhibit Subscriptions", "SUB_AS_PARENT");
                  break;
               case CMQC.MQTA_SUB_INHIBITED:
                  properties.put("Inhibit Subscriptions", "SUB_INHIBITED");
                  break;
               case CMQC.MQTA_SUB_ALLOWED:
                  properties.put("Inhibit Subscriptions", "SUB_ALLOWED");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading Inhibit Subscriptions. Ignoring" + e.getMessage());
         }
         try {
            properties.put("Non Durable Model Q Name", m.getStringParameterValue(CMQC.MQCA_MODEL_NON_DURABLE_Q));
         } catch (PCFException e) {
            log.warn("Exception when reading Non Durable Model Q Name. Ignoring" + e.getMessage());
         }
         try {
            switch (m.getIntParameterValue(CMQC.MQIA_NPM_DELIVERY)) {
               case CMQC.MQDLV_AS_PARENT:
                  properties.put("Non Persistent Msg Delivery", "AS_PARENT");
                  break;
               case CMQC.MQDLV_ALL:
                  properties.put("Non Persistent Msg Delivery", "ALL");
                  break;
               case CMQC.MQDLV_ALL_DUR:
                  properties.put("Non Persistent Msg Delivery", "ALL_DUR");
                  break;
               case CMQC.MQDLV_ALL_AVAIL:
                  properties.put("Non Persistent Msg Delivery", "ALL_AVAIL");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading on Persistent Msg Delivery. Ignoring" + e.getMessage());
         }
         try {
            switch (m.getIntParameterValue(CMQC.MQIA_PM_DELIVERY)) {
               case CMQC.MQDLV_AS_PARENT:
                  properties.put("Persistent Msg Delivery", "AS_PARENT");
                  break;
               case CMQC.MQDLV_ALL:
                  properties.put("Persistent Msg Delivery", "ALL");
                  break;
               case CMQC.MQDLV_ALL_DUR:
                  properties.put("Persistent Msg Delivery", "ALL_DUR");
                  break;
               case CMQC.MQDLV_ALL_AVAIL:
                  properties.put("Persistent Msg Delivery", "ALL_AVAIL");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading Persistent Msg Delivery. Ignoring" + e.getMessage());
         }
         try {
            switch (m.getIntParameterValue(CMQC.MQIA_PROXY_SUB)) {
               case CMQC.MQTA_PROXY_SUB_FORCE:
                  properties.put("Proxy Subscriptions", "SUB_FORCE");
                  break;
               case CMQC.MQTA_PROXY_SUB_FIRSTUSE:
                  properties.put("Proxy Subscriptions", "SUB_FIRSTUSE");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading Proxy Subscriptions. Ignoring" + e.getMessage());
         }
         try {
            switch (m.getIntParameterValue(CMQC.MQIA_PUB_SCOPE)) {
               case CMQC.MQSCOPE_ALL:
                  properties.put("Publication Scope", "ALL");
                  break;
               case CMQC.MQSCOPE_AS_PARENT:
                  properties.put("Publication Scope", "AS_PARENT");
                  break;
               case CMQC.MQSCOPE_QMGR:
                  properties.put("Publication Scope", "QMGR");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading Publication Scope. Ignoring" + e.getMessage());
         }
         try {
            switch (m.getIntParameterValue(CMQC.MQIA_SUB_SCOPE)) {
               case CMQC.MQSCOPE_ALL:
                  properties.put("Subscription Scope", "ALL");
                  break;
               case CMQC.MQSCOPE_AS_PARENT:
                  properties.put("Subscription Scope", "AS_PARENT");
                  break;
               case CMQC.MQSCOPE_QMGR:
                  properties.put("Subscription Scope", "QMGR");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading Subscription Scope. Ignoring" + e.getMessage());
         }
         try {
            properties.put("Topic Description", m.getStringParameterValue(CMQC.MQCA_TOPIC_DESC));
         } catch (PCFException e) {
            log.warn("Exception when readingCluster Name. Ignoring" + e.getMessage());
         }
         try {
            properties.put("Topic String", m.getStringParameterValue(CMQC.MQCA_TOPIC_STRING));
         } catch (PCFException e) {
            log.warn("Exception when reading Topic Description. Ignoring" + e.getMessage());
         }
         try {
            switch (m.getIntParameterValue(CMQC.MQIA_TOPIC_TYPE)) {
               case CMQC.MQTOPT_LOCAL:
                  properties.put("Topic Type", "LOCAL");
                  break;
               case CMQC.MQTOPT_CLUSTER:
                  properties.put("Topic Type", "CLUSTER");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading Topic Type. Ignoring" + e.getMessage());
         }
         try {
            switch (m.getIntParameterValue(CMQC.MQIA_USE_DEAD_LETTER_Q)) {
               case CMQC.MQUSEDLQ_NO:
                  properties.put("Use DLQ ", "NO");
                  break;
               case CMQC.MQUSEDLQ_YES:
                  properties.put("Use DLQ", "YES");
                  break;
               case CMQC.MQUSEDLQ_AS_PARENT:
                  properties.put("Use DLQ", "AS_PARENT");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading Use DLQ. Ignoring" + e.getMessage());
         }
         try {
            switch (m.getIntParameterValue(CMQC.MQIA_WILDCARD_OPERATION)) {
               case CMQC.MQTA_PASSTHRU:
                  properties.put("Wildcard Operation", "PASSTHRU");
                  break;
               case CMQC.MQTA_BLOCK:
                  properties.put("Wildcard Operation", "BLOCK");
                  break;
               default:
                  break;
            }
         } catch (PCFException e) {
            log.warn("Exception when reading Wildcard Operation. Ignoring" + e.getMessage());
         }
      } catch (IOException | MQDataException e) {
         log.warn("Exception when getting PCF Agent. Ignoring" + e.getMessage());
      } finally {
         // Disconnect/Close
         if (agent != null) {
            try {
               agent.disconnect();
            } catch (MQDataException e) {
               log.error("MQException occurred when disconnecting agent", e);
            }
         }
      }

      log.debug("Topic Information : {}", properties);
      return properties;
   }

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      var sb = new StringBuilder(2048);
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
      sb.append("queueManager                : Queue Manager Name, If not set, the connection is made to the default queue manager.")
               .append(CR);
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
      sb.append("ccsid                       : see https://www.ibm.com/docs/en/ibm-mq/9.0?topic=objects-ccsid").append(CR);
      // sb.append("encoding : see https://www.ibm.com/docs/en/ibm-mq/9.0?topic=objects-encoding").append(CR);
      sb.append(CR);
      sb.append("javax.net.ssl.keyStore         : Client side certificate key store").append(CR);
      sb.append("javax.net.ssl.keyStorePassword : Client key store password").append(CR);
      sb.append("javax.net.ssl.keyStoreType     : JKS (default), PKCS12, ...").append(CR);
      sb.append(CR);
      sb.append("javax.net.ssl.trustStore         : Trust store (For server certificate)").append(CR);
      sb.append("javax.net.ssl.trustStorePassword : Trust store password").append(CR);
      sb.append("javax.net.ssl.trustStoreType     : JKS (default), PKCS12, ...").append(CR);

      HELP_TEXT = sb.toString();
   }

   // -------
   // Helpers
   // -------

   private SortedSet<QueueData> buildQueueList(PCFMessageAgent agent, List<String> excludedPrefixes) throws MQDataException,
                                                                                                     IOException {
      SortedSet<QueueData> listQueueData = new TreeSet<>();

      var request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_NAMES);
      request.addParameter(CMQC.MQCA_Q_NAME, "*");

      var responses = agent.send(request);
      var qNames = responses[0].getStringListParameterValue(CMQCFC.MQCACF_Q_NAMES);
      var qTypes = responses[0].getIntListParameterValue(CMQCFC.MQIACF_Q_TYPES);
      boolean systemQueue;
      String qName = null;
      QType type = null;
      for (var i = 0; i < qNames.length; i++) {
         qName = qNames[i].trim();
         type = QType.fromValue(qTypes[i]);
         log.debug("Found Queue '{}'. Type: {} isBrowsable? {}", qName, type, type.isBrowsable());

         // Exclude ghost queues
         if (qName.startsWith(SYSTEM_GHOST_PREFIX)) {
            log.info("Discarding IBM MQ Ghost Queue: {}", qName);
            continue;
         }

         systemQueue = false;
         for (String prefix : excludedPrefixes) {
            if (qName.startsWith(prefix)) {
               systemQueue = true;
               break;
            }
         }
         if (!systemQueue) {
            listQueueData.add(new QueueData(qName, type.isBrowsable()));
         }
      }
      return listQueueData;
   }

   private SortedSet<TopicData> buildTopicList(PCFMessageAgent agent, List<String> excludedPrefixes) throws MQDataException,
                                                                                                     IOException {
      SortedSet<TopicData> topics = new TreeSet<>();

      var request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_TOPIC_NAMES);
      request.addParameter(CMQC.MQCA_TOPIC_NAME, "*");

      try {
         var responses = agent.send(request);
         var tn = responses[0].getStringListParameterValue(CMQCFC.MQCACF_TOPIC_NAMES);
         boolean systemTopic;
         String topicName = null;
         for (var i = 0; i < tn.length; i++) {
            topicName = tn[i].trim();

            log.debug("Found Topic '{}'", topicName);

            // Exclude ghost queues. DF not sure if those exist...
            if (topicName.startsWith(SYSTEM_GHOST_PREFIX)) {
               log.info("Discarding IBM MQ Ghost Topic: {}", topicName);
               continue;
            }

            systemTopic = false;
            for (String prefix : excludedPrefixes) {
               if (topicName.startsWith(prefix)) {
                  systemTopic = true;
                  break;
               }
            }
            if (!systemTopic) {
               topics.add(new TopicData(topicName));
            }
         }
      } catch (PCFException e) {
         // This happens with MQ Serveurs that are not configured with a Broker, like MQ v6.0 in some cases
         // com.ibm.mq.pcf.PCFException: MQJE001: Codice di completamento '2', ragione '3007'.
         log.warn("A PCFException have been thrown while trying to retrieve the Topics defined in the QM. Ignoring", e);
      }

      return topics;
   }

   // ------------------------
   // MQ Types
   // ------------------------

   private enum QType {
                       ALIAS(3, false),
                       LOCAL(1, true),
                       REMOTE(6, false),
                       MODEL(2, false);

      private Integer _value;
      private boolean browsable;

      QType(Integer _value, boolean browsable) {
         this._value = _value;
         this.browsable = browsable;
      }

      public static QType fromValue(Integer _value) {
         for (QType qType : values()) {
            if (qType._value == _value) {
               return qType;
            }
         }
         throw new IllegalArgumentException(_value + " is not a known QType");
      }

      public boolean isBrowsable() {
         return browsable;
      }
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }

}

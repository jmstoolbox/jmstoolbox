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
package org.titou10.jtb.qm.ibmmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
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
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.MQSecurityExit;
import com.ibm.mq.MQTopic;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.pcf.PCFException;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsConstants;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 * 
 * Implements IBM WebSphere MQ Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class MQQManager extends QManager {

   private static final Logger       log                    = LoggerFactory.getLogger(MQQManager.class);

   private static final String       CR                     = "\n";

   private static final String       P_QUEUE_MANAGER        = "queueManager";
   private static final String       P_CHANNEL              = "channel";
   private static final String       P_SECURITY_EXIT        = "channelSecurityExit";
   private static final String       P_SECURITY_EXIT_DATA   = "channelSecurityExitUserData";
   private static final String       P_RECEIVE_EXIT         = "channelReceiveExit";
   private static final String       P_RECEIVE_EXIT_DATA    = "channelReceiveExitUserData";
   private static final String       P_SEND_EXIT            = "channelSendExit";
   private static final String       P_SEND_EXIT_DATA       = "channelSendExitUserData";

   private static final String       P_SSL_CIPHER_SUITE     = "sslCipherSuite";
   private static final String       P_SSL_FIPS_REQUIRED    = "sslFipsRequired";

   private static final String       P_TRUST_STORE          = "javax.net.ssl.trustStore";
   private static final String       P_TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
   private static final String       P_TRUST_STORE_TYPE     = "javax.net.ssl.trustStoreType";

   private static final List<String> SYSTEM_PREFIXES_1      = Arrays.asList("LOOPBACK");
   private static final List<String> SYSTEM_PREFIXES_2      = Arrays.asList("LOOPBACK", "AMQ.", "SYSTEM.");

   private List<QManagerProperty>    parameters             = new ArrayList<QManagerProperty>();
   private SortedSet<String>         queueNames             = new TreeSet<>();
   private SortedSet<String>         topicNames             = new TreeSet<>();

   private MQQueueManager            queueManager;

   // ------------------------
   // Constructor
   // ------------------------

   public MQQManager() {

      log.debug("Instantiate MQQManager");

      parameters.add(new QManagerProperty(P_CHANNEL, true, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_QUEUE_MANAGER, true, JMSPropertyKind.STRING));
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

      parameters.add(new QManagerProperty(P_TRUST_STORE, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_TRUST_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(P_TRUST_STORE_TYPE, false, JMSPropertyKind.STRING));

      parameters.add(new QManagerProperty(P_SSL_CIPHER_SUITE, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_SSL_FIPS_REQUIRED, false, JMSPropertyKind.BOOLEAN));

   }

   // ------------------------
   // Business Interface
   // ------------------------

   @Override
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects) throws Exception {
      log.info("connecting to {}", sessionDef.getName());

      // Trace
      // System.setProperty("com.ibm.msg.client.commonservices.trace.outputName", "D:/toto/xxx_%PID%.trc");
      // Trace.setTraceLevel(6);
      // Trace.setOn(true);

      // Save System properties
      saveSystemProperties();
      try {

         List<String> excludedPrefixes;
         if (showSystemObjects) {
            excludedPrefixes = SYSTEM_PREFIXES_1;
         } else {
            excludedPrefixes = SYSTEM_PREFIXES_2;
         }

         Map<String, String> mapProperties = extractProperties(sessionDef);

         String qmName = mapProperties.get(P_QUEUE_MANAGER);
         String channel = mapProperties.get(P_CHANNEL);
         String securityExit = mapProperties.get(P_SECURITY_EXIT);
         String securityExitData = mapProperties.get(P_SECURITY_EXIT_DATA);
         String receiveExit = mapProperties.get(P_RECEIVE_EXIT);
         String receiveExitData = mapProperties.get(P_RECEIVE_EXIT_DATA);
         String sendExit = mapProperties.get(P_SEND_EXIT);
         String sendExitData = mapProperties.get(P_SEND_EXIT_DATA);

         String sslCipherSuite = mapProperties.get(P_SSL_CIPHER_SUITE);
         Boolean sslFipsRequired = Boolean.valueOf(mapProperties.get(P_SSL_FIPS_REQUIRED));

         String trustStore = mapProperties.get(P_TRUST_STORE);
         String trustStorePassword = mapProperties.get(P_TRUST_STORE_PASSWORD);
         String trustStoreType = mapProperties.get(P_TRUST_STORE_TYPE);

         // Set System Properties
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

         // Connection properties
         Hashtable<String, Object> props = new Hashtable();

         props.put(CMQC.APPNAME_PROPERTY, "JMSToolBox");
         props.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);

         // Target MQ
         props.put(CMQC.HOST_NAME_PROPERTY, sessionDef.getHost());
         props.put(CMQC.PORT_PROPERTY, sessionDef.getPort());
         props.put(CMQC.CHANNEL_PROPERTY, channel);

         // user/Password
         if (sessionDef.getUserid() != null) {
            props.put(CMQC.USER_ID_PROPERTY, sessionDef.getUserid());
         }
         if (sessionDef.getPassword() != null) {
            props.put(CMQC.PASSWORD_PROPERTY, sessionDef.getPassword());
         }

         // channelSecurityExit
         if (securityExit != null) {
            Class clazz = getClass().getClassLoader().loadClass(securityExit);
            MQSecurityExit securityExitInstance = (MQSecurityExit) clazz.newInstance();
            props.put(CMQC.CHANNEL_SECURITY_EXIT_PROPERTY, securityExitInstance);
         }
         if (securityExitData != null) {
            props.put(CMQC.CHANNEL_SECURITY_EXIT_USER_DATA_PROPERTY, securityExitData);
         }

         // channelReceiveExit
         if (receiveExit != null) {
            Class clazz = getClass().getClassLoader().loadClass(receiveExit);
            Object receiveExitInstance = clazz.newInstance();
            props.put(CMQC.CHANNEL_RECEIVE_EXIT_PROPERTY, receiveExitInstance);
         }
         if (receiveExitData != null) {
            props.put(CMQC.CHANNEL_RECEIVE_EXIT_USER_DATA_PROPERTY, receiveExitData);
         }

         // channelSendExit
         if (sendExit != null) {
            Class clazz = getClass().getClassLoader().loadClass(sendExit);
            Object sendExitInstance = clazz.newInstance();
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

         // Generic Properties
         props.put(CMQC.APPNAME_PROPERTY, "JMSToolBox");
         props.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);

         // Connect and open Administrative Command channel
         PCFMessageAgent agent = null;
         queueManager = new MQQueueManager(qmName, props);
         try {
            agent = new PCFMessageAgent(queueManager);

            // Get list of Queues and Topics
            queueNames = builQNamesList(agent, excludedPrefixes);
            topicNames = builTopicNamesList(agent, excludedPrefixes);
         } finally {
            // Disconnect/Close
            if (agent != null) {
               agent.disconnect();
            }

         }

         // Create and store JMS Connection
         JmsFactoryFactory ff = JmsFactoryFactory.getInstance(JmsConstants.WMQ_PROVIDER);

         JmsConnectionFactory factory = ff.createConnectionFactory();
         factory.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JMSToolBox");
         factory.setStringProperty(WMQConstants.WMQ_HOST_NAME, sessionDef.getHost());
         factory.setIntProperty(WMQConstants.WMQ_PORT, sessionDef.getPort());
         factory.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
         factory.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, qmName);
         factory.setStringProperty(WMQConstants.WMQ_CHANNEL, channel);
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

         // TODO If set, can not open 2 connections on 2 different MQ Q Managers...
         // factory.setStringProperty(WMQConstants.CLIENT_ID, "JMSToolBox");

         // Get Connection
         Connection c = factory.createConnection(sessionDef.getUserid(), sessionDef.getPassword());

         log.info("connected to {}", sessionDef.getName());
         return c;

      } finally {
         restoreSystemProperties();
      }
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      try {
         queueManager.disconnect();
         queueManager.close();
      } catch (MQException e) {
         throw new JMSException(e.getMessage());
      }

      jmsConnection.close();
      queueNames.clear();
      topicNames.clear();
   }

   @Override
   public Integer getQueueDepth(String queueName) {

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
   public Map<String, Object> getQueueInformation(String queueName) {

      SortedMap<String, Object> properties = new TreeMap<>();
      MQQueue destQueue = null;
      try {
         try {
            destQueue = queueManager.accessQueue(queueName, CMQC.MQOO_INQUIRE);

            try {
               properties.put("CurrentDepth", destQueue.getCurrentDepth());
            } catch (MQException e) {
               log.warn("Exception when reading CurrentDepth. Ignoring. " + e.getMessage());
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
               properties.put("OpenInputCount", destQueue.getOpenInputCount());
            } catch (MQException e) {
               log.warn("Exception when reading OpenInputCount. Ignoring" + e.getMessage());
            }
            try {
               properties.put("OpenOutputCount", destQueue.getOpenOutputCount());
            } catch (MQException e) {
               log.warn("Exception when reading OpenOutputCount. Ignoring" + e.getMessage());
            }
            try {
               properties.put("TriggerData", destQueue.getTriggerData());
            } catch (MQException e) {
               log.warn("Exception when reading TriggerData. Ignoring" + e.getMessage());
            }
            try {
               properties.put("TriggerMessagePriority", destQueue.getTriggerMessagePriority());
            } catch (MQException e) {
               log.warn("Exception when reading TriggerMessagePriority. Ignoring" + e.getMessage());
            }
            try {
               properties.put("TriggerDepth", destQueue.getTriggerDepth());
            } catch (MQException e) {
               log.warn("Exception when reading TriggerDepth. Ignoring" + e.getMessage());
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
   public Map<String, Object> getTopicInformation(String topicName) {

      SortedMap<String, Object> properties = new TreeMap<>();
      MQTopic destTopic = null;
      try {
         try {
            destTopic = queueManager.accessTopic(topicName, null, CMQC.MQTOPIC_OPEN_AS_SUBSCRIPTION, CMQC.MQOO_INQUIRE);
            try {
               properties.put("Alternate User ID", destTopic.getAlternateUserId());
            } catch (MQException e) {
               log.warn("Exception when reading getAlternateUserId. Ignoring. " + e.getMessage());
            }
            try {
               properties.put("Description ", destTopic.getDescription());
            } catch (MQException e) {
               log.warn("Exception when reading Description. Ignoring" + e.getMessage());
            }

         } catch (MQException e) {
            log.error("Exception when reading Topic Information. Ignoring", e);
         }
      } finally {
         if (destTopic != null) {
            try {
               destTopic.close();
            } catch (MQException e) {}
         }
      }
      log.debug("Topic Information : {}", properties);
      return properties;
   }

   @Override
   public String getHelpText() {
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
      sb.append(CR);
      sb.append("javax.net.ssl.trustStore         : keystore").append(CR);
      sb.append("javax.net.ssl.trustStorePassword : keystore password").append(CR);
      sb.append("javax.net.ssl.trustStoreType     : JKS (default), PKCS12, ...").append(CR);
      return sb.toString();
   }

   // -------
   // Helpers
   // -------

   private SortedSet<String> builQNamesList(PCFMessageAgent agent, List<String> excludedPrefixes) throws PCFException,
                                                                                                  MQException,
                                                                                                  IOException {
      SortedSet<String> queues = new TreeSet<>();

      PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_NAMES);
      request.addParameter(CMQC.MQCA_Q_NAME, "*");

      PCFMessage[] responses = agent.send(request);
      String[] qNames = responses[0].getStringListParameterValue(CMQCFC.MQCACF_Q_NAMES);
      int[] qTypes = responses[0].getIntListParameterValue(CMQCFC.MQIACF_Q_TYPES);
      boolean systemQueue;
      String qName = null;
      for (int i = 0; i < qNames.length; i++) {
         qName = qNames[i].trim();
         log.debug("q={} t={}", qName, QType.fromValue(qTypes[i]));
         systemQueue = false;
         for (String prefix : excludedPrefixes) {
            if (qName.startsWith(prefix)) {
               systemQueue = true;
               break;
            }
         }
         if (!systemQueue) {
            queues.add(qName);
         }
      }
      return queues;
   }

   private SortedSet<String> builTopicNamesList(PCFMessageAgent agent, List<String> excludedPrefixes) throws PCFException,
                                                                                                      MQException,
                                                                                                      IOException {
      SortedSet<String> topics = new TreeSet<>();

      PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_TOPIC_NAMES);
      request.addParameter(CMQC.MQCA_TOPIC_NAME, "*");

      PCFMessage[] responses = agent.send(request);
      String[] tn = responses[0].getStringListParameterValue(CMQCFC.MQCACF_TOPIC_NAMES);
      boolean systemTopic;
      String topicName = null;
      for (int i = 0; i < tn.length; i++) {
         topicName = tn[i].trim();

         log.debug("t={}", topicName);

         systemTopic = false;
         for (String prefix : excludedPrefixes) {
            if (topicName.startsWith(prefix)) {
               systemTopic = true;
               break;
            }
         }
         if (!systemTopic) {
            topics.add(topicName);
         }
      }

      return topics;
   }

   // ------------------------
   // MQ Types
   // ------------------------

   private enum QType {
                       ALIAS(3),
                       LOCAL(1),
                       REMOTE(6),
                       MODEL(2);
      private Integer _value;

      private QType(Integer _value) {
         this._value = _value;
      }

      public static QType fromValue(Integer _value) {
         for (QType qType : values()) {
            if (qType._value == _value) {
               return qType;
            }
         }
         throw new IllegalArgumentException(_value + " is not a known QType");
      }
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }

   @Override
   public SortedSet<String> getQueueNames() {
      return queueNames;
   }

   @Override
   public SortedSet<String> getTopicNames() {
      return topicNames;
   }

}

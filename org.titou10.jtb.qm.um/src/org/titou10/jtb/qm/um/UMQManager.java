/*
 * Copyright (C) 2017 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.qm.um;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.ConnectionData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

import com.pcbsys.nirvana.base.nConstants;
import com.pcbsys.nirvana.client.nBaseClientException;
import com.pcbsys.nirvana.client.nChannel;
import com.pcbsys.nirvana.client.nChannelAttributes;
import com.pcbsys.nirvana.client.nQueue;
import com.pcbsys.nirvana.client.nQueueDetails;
import com.pcbsys.nirvana.client.nSessionAttributes;
import com.pcbsys.nirvana.nAdmin.nAdminSession;
import com.pcbsys.nirvana.nAdmin.nAdminSessionFactory;
import com.pcbsys.nirvana.nJMS.ConnectionFactoryImpl;

/**
 * 
 * Implements Software AG Universal Messaging Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class UMQManager extends QManager {

   private static final Logger               log                        = LoggerFactory.getLogger(UMQManager.class);

   private static final String               CR                         = "\n";
   private static final String               HELP_TEXT;

   private static final String               RNAME_TEMPLATE             = "%s://%s:%d";

   // http://tech.forums.softwareag.com/techjforum/posts/list/57967.page
   private static final String               P_PROTOCOL                 = "protocol";
   private static final String               P_SSL_PROTOCOL             = "ssl_protocol";
   private static final String               P_SSL_ENABLED_CIPHERS      = "ssl_enabled_ciphers";
   private static final String               P_SSL_KEY_STORE            = "ssl_keyStore path";
   private static final String               P_SSL_KEY_STORE_PASSWORD   = "ssl_keyStore password";
   private static final String               P_SSL_KEY_STORE_CERT       = "ssl_keyStore certificate";
   private static final String               P_SSL_TRUST_STORE          = "ssl_trustStore path";
   private static final String               P_SSL_TRUST_STORE_PASSWORD = "ssl_trustStore password";

   private List<QManagerProperty>            parameters                 = new ArrayList<QManagerProperty>();

   private final Map<Integer, nAdminSession> adminSessions              = new HashMap<>();

   // ------------------------
   // Constructor
   // ------------------------

   public UMQManager() {
      log.debug("Instantiate UMQManager");

      parameters.add(new QManagerProperty(P_PROTOCOL,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Connection protocol ('nsp','nhp','nsps','nhps')",
                                          "nsp"));
      parameters.add(new QManagerProperty(P_SSL_PROTOCOL,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "The SSL protocol for this client (eg 'TLSv1' ...)"));
      parameters.add(new QManagerProperty(P_SSL_ENABLED_CIPHERS,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "The SSL enabled ciphers for this client (Separated by commas)"));
      parameters.add(new QManagerProperty(P_SSL_KEY_STORE,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "For client side ssl authentication"));
      parameters.add(new QManagerProperty(P_SSL_KEY_STORE_PASSWORD,
                                          false,
                                          JMSPropertyKind.STRING,
                                          true,
                                          "For Client ssl authentication"));
      parameters.add(new QManagerProperty(P_SSL_KEY_STORE_CERT,
                                          false,
                                          JMSPropertyKind.STRING,
                                          true,
                                          "For Client ssl authentication"));
      parameters.add(new QManagerProperty(P_SSL_TRUST_STORE, false, JMSPropertyKind.STRING));
      parameters.add(new QManagerProperty(P_SSL_TRUST_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));
   }

   // ------------------------
   // Business Interface
   // ------------------------
   @Override
   public ConnectionData connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Extract properties
      Map<String, String> mapProperties = extractProperties(sessionDef);

      // Build connection string
      String protocol = mapProperties.get(P_PROTOCOL);
      String sslProtocol = mapProperties.get(P_SSL_PROTOCOL);
      String sslEnabledCiphers = mapProperties.get(P_SSL_ENABLED_CIPHERS);
      String keyStore = mapProperties.get(P_SSL_KEY_STORE);
      String keyStorePassword = mapProperties.get(P_SSL_KEY_STORE_PASSWORD);
      String keyStoreCert = mapProperties.get(P_SSL_KEY_STORE_CERT);
      String trustStore = mapProperties.get(P_SSL_TRUST_STORE);
      String trustStorePassword = mapProperties.get(P_SSL_TRUST_STORE_PASSWORD);

      String[] enabledCiphers = null;
      if (sslEnabledCiphers != null) {
         enabledCiphers = sslEnabledCiphers.split("\\s*,\\s*");
      }

      StringBuilder connectionURL = new StringBuilder(512);
      connectionURL.append(String.format(RNAME_TEMPLATE, protocol, sessionDef.getHost(), sessionDef.getPort()));
      if (sessionDef.getHost2() != null) {
         connectionURL.append(",");
         connectionURL.append(String.format(RNAME_TEMPLATE, protocol, sessionDef.getHost2(), sessionDef.getPort2()));
      }
      if (sessionDef.getHost3() != null) {
         connectionURL.append(",");
         connectionURL.append(String.format(RNAME_TEMPLATE, protocol, sessionDef.getHost3(), sessionDef.getPort3()));
      }

      // Connect

      // Save System properties
      saveSystemProperties();

      // if (trustStore == null) {
      // System.clearProperty(P_TRUST_STORE);
      // } else {
      // System.setProperty(P_TRUST_STORE, trustStore);
      // }
      // if (trustStorePassword == null) {
      // System.clearProperty(P_TRUST_STORE_PASSWORD);
      // } else {
      // System.setProperty(P_TRUST_STORE_PASSWORD, trustStorePassword);
      // }

      try {
         nSessionAttributes adminSessionAttributes = new nSessionAttributes(connectionURL.toString());
         if (keyStore != null) {
            adminSessionAttributes.setKeystore(keyStore, keyStorePassword, keyStoreCert);
         }
         if (trustStore != null) {
            adminSessionAttributes.setTruststore(trustStore, trustStorePassword);
         }
         if (sslProtocol != null) {
            adminSessionAttributes.setSSLProtocol(sslProtocol);
         }
         if (enabledCiphers != null) {
            adminSessionAttributes.setEnabledCiphers(enabledCiphers);
         }

         nAdminSession adminSession = nAdminSessionFactory
                  .createAdmin(adminSessionAttributes, sessionDef.getUserid(), sessionDef.getPassword());
         adminSession.init();

         SortedSet<QueueData> listQueueData = new TreeSet<>();
         SortedSet<TopicData> listTopicData = new TreeSet<>();
         nQueue queue;
         nChannel topic;
         for (nChannelAttributes channelAttributes : adminSession.getChannels()) {
            log.debug("Found nChannelAttributes {} JMS? {}", channelAttributes.getFullName(), channelAttributes.isJMSEngine());
            switch (channelAttributes.getChannelMode()) {
               case nConstants.CHAN_MODE_NORMAL:
                  topic = adminSession.findChannel(channelAttributes);
                  log.debug("Found Topic '{}'", topic.getName());
                  listTopicData.add(new TopicData(topic.getName()));
                  break;
               case nConstants.CHAN_MODE_QUEUE:
                  queue = adminSession.findQueue(channelAttributes);
                  log.debug("Found Queue '{}'", queue.getName());
                  listQueueData.add(new QueueData(queue.getName()));
                  break;
               default:
                  log.debug("channelAttributes with channelMode '{}' not managed by JMSToolBox",
                            channelAttributes.getChannelMode());
                  break;
            }
         }

         // JMS Connection

         ConnectionFactoryImpl c = new ConnectionFactoryImpl();
         c.setRNAME(connectionURL.toString());
         c.setUseJMSEngine(true);
         c.setAutoCreateResource(false);
         if ((keyStore != null) || (trustStore != null)) {
            c.setSSLStores(keyStore, keyStorePassword, keyStoreCert, trustStore, trustStorePassword);
         }
         if (sslProtocol != null) {
            c.setSSLProtocol(sslProtocol);
         }
         if (enabledCiphers != null) {
            c.setSSLEnabledCiphers(enabledCiphers);
         }

         // c.setEnableSingleQueueAck(true);
         // c.setEnableSingleSharedDurableAck(true);
         // c.setSyncNamedTopicAcks(true);
         // c.setSyncQueueAcks(true);
         // c.setSyncTopicAcks(true);

         Connection jmsConnection = c.createConnection(sessionDef.getUserid(), sessionDef.getPassword());
         jmsConnection.setClientID(clientID);
         jmsConnection.start();

         // Store per connection related data
         Integer hash = jmsConnection.hashCode();
         adminSessions.put(hash, adminSession);

         return new ConnectionData(jmsConnection, listQueueData, listTopicData);

      } finally {
         restoreSystemProperties();
      }
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      Integer hash = jmsConnection.hashCode();
      nAdminSession adminSession = adminSessions.get(hash);

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occured while closing connection. Ignore it. Msg={}", e.getMessage());
      }

      if (adminSession != null) {
         try {
            adminSession.close();
         } catch (Exception e) {
            log.warn("Exception occured while closing nAdminSession. Ignore it. Msg={}", e.getMessage());
         }
         adminSessions.remove(hash);
      }
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {
      Integer hash = jmsConnection.hashCode();
      nAdminSession adminSession = adminSessions.get(hash);

      try {
         nChannelAttributes x = new nChannelAttributes(queueName);
         nQueue queue = adminSession.findQueue(x);
         return queue.getDetails().getNoOfEvents();
      } catch (Exception e) {
         log.error("Exception occurred while reading depth for Queue '{}'", queueName, e);
         return null;
      }
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {
      Map<String, Object> properties = new LinkedHashMap<>();

      Integer hash = jmsConnection.hashCode();
      nAdminSession adminSession = adminSessions.get(hash);

      try {
         nChannelAttributes x = new nChannelAttributes(queueName);
         nQueue queue = adminSession.findQueue(x);
         nQueueDetails details = queue.getDetails();

         long fet = details.getFirstEventTime();
         long let = details.getLastEventTime();

         String firstEventTime = fet == 0 ? "-"
                  : fet + " [" + LocalDateTime.ofInstant(Instant.ofEpochMilli(fet), TimeZone.getDefault().toZoneId()).toString()
                    + "]";
         String lastEventTime = let == 0 ? "-"
                  : let + " [" + LocalDateTime.ofInstant(Instant.ofEpochMilli(let), TimeZone.getDefault().toZoneId()).toString()
                    + "]";

         properties.put("NoOfEvents", details.getNoOfEvents());
         properties.put("NoOfReaders", details.getNoOfReaders());
         properties.put("TotalMemorySize", details.getTotalMemorySize());
         properties.put("FirstEventTime", firstEventTime);
         properties.put("LastEventTime", lastEventTime);

         addChannelAttributes(properties, queue.getQueueAttributes());
      } catch (Exception e) {
         log.error("Exception occurred while reading depth for Queue '{}'", queueName, e);
      }
      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {
      Map<String, Object> properties = new LinkedHashMap<>();

      Integer hash = jmsConnection.hashCode();
      nAdminSession adminSession = adminSessions.get(hash);

      try {
         nChannelAttributes x = new nChannelAttributes(topicName);
         nChannel topic = adminSession.findChannel(x);

         properties.put("EventCount", topic.getEventCount());
         properties.put("LastEID", topic.getLastEID());
         properties.put("LastStoredEID", topic.getLastStoredEID());

         addChannelAttributes(properties, topic.getChannelAttributes());
      } catch (Exception e) {
         log.error("Exception occurred while reading depth for Queue '{}'", topicName, e);
      }
      return properties;
   }

   private void addChannelAttributes(Map<String, Object> properties,
                                     nChannelAttributes channelAttributes) throws nBaseClientException {

      properties.put("isAutoDelete?", channelAttributes.isAutoDelete());
      properties.put("isClusterWide?", channelAttributes.isClusterWide());
      properties.put("isDurable?", channelAttributes.isDurable());
      properties.put("isExternal?", channelAttributes.isExternal());
      properties.put("isJMSEngine?", channelAttributes.isJMSEngine());
      properties.put("isMergeEngine?", channelAttributes.isMergeEngine());
      properties.put("ChannelMode", channelAttributes.getChannelMode() == nConstants.CHAN_MODE_NORMAL ? "NORMAL" : "QUEUE");
      properties.put("MaxEvents", channelAttributes.getMaxEvents());
      properties.put("FullName", channelAttributes.getFullName());
      properties.put("TTL", channelAttributes.getTTL());
      properties.put("UniqueId", channelAttributes.getUniqueId());
      switch (channelAttributes.getType()) {
         case nConstants.CHAN_MIXED:
            properties.put("Type", "MIXED");
            break;
         case nConstants.CHAN_OFF_HEAP:
            properties.put("Type", "OFF_HEAP");
            break;
         case nConstants.CHAN_PAGED:
            properties.put("Type", "PAGED");
            break;
         case nConstants.CHAN_PERSISTENT:
            properties.put("Type", "PERSISTENT");
            break;
         case nConstants.CHAN_RELIABLE:
            properties.put("Type", "RELIABLE");
            break;
         case nConstants.CHAN_SIMPLE:
            properties.put("Type", "SIMPLE");
            break;
         case nConstants.CHAN_TRANSIENT:
            properties.put("Type", "TRANSIENT");
            break;
         default:
            properties.put("Type", channelAttributes.getType());
            break;
      }
   }

   @Override
   public boolean supportsMultipleHosts() {
      return true;
   }

   @Override
   public boolean mustRemoveIDFromJMSMessageID() {
      return true;
   }

   @Override
   public boolean manulAcknoledge() {
      return false;
   }

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS (From <um root>/lib):").append(CR);
      sb.append("--------------------------------").append(CR);
      sb.append("nAdmin.jar").append(CR);
      sb.append("nClient.jar").append(CR);
      sb.append("nJMS.jar").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : Universal Messaging server host name").append(CR);
      sb.append("Port          : Universal Messaging port (eg 9000)").append(CR);
      sb.append("User/Password : User allowed to connect to the server").append(CR);
      sb.append(CR);
      sb.append("Properties values:").append(CR);
      sb.append("------------------").append(CR);
      sb.append("protocol                         : Protocol to connect to the server: nsp, nhp, nsps, nhps").append(CR);
      sb.append(CR);
      sb.append("ssl_protocol             : SSL Protocol (eg 'TLSv1')").append(CR);
      sb.append("ssl_enabled_ciphers      : Enabled Ciphers").append(CR);
      sb.append("ssl_keyStore path        : Key store filename (eg D:/somewhere/trust.jks)").append(CR);
      sb.append("ssl_keyStore password    : Key store password (For client certificate validation)").append(CR);
      sb.append("ssl_keyStore certificate : Certificate name in key store (For client certificate validation)").append(CR);
      sb.append("ssl_trustStore path      : Trust store filename (eg D:/somewhere/trust.jks)").append(CR);
      sb.append("ssl_trustStore password  : Trust store password").append(CR);

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

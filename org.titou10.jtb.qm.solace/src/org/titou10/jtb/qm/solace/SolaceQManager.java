/*
 * Copyright (C) 2019 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.qm.solace;

import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.DestinationData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;
import org.titou10.jtb.qm.solace.semp.SempJndiTopicData;
import org.titou10.jtb.qm.solace.semp.SempQueueData;
import org.titou10.jtb.qm.solace.semp.SempResponse;
import org.titou10.jtb.qm.solace.semp.SempResponseMetaError;
import org.titou10.jtb.qm.solace.utils.PType;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

/**
 * 
 * Implements Solace Q Provider
 * 
 * @author Denis Forveille
 * @author Monica Zhang (Monica.Zhang@solace.com)
 *
 */
public class SolaceQManager extends QManager {

   private static final org.slf4j.Logger   log                           = LoggerFactory.getLogger(SolaceQManager.class);

   private static final String             CR                            = "\n";
   private static final String             HELP_TEXT;

   // HTTP REST stuff
   private static final HttpClient         HTTP_CLIENT                   = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL).build();

   // JSON-B stuff
   private static final Jsonb              JSONB                         = JsonbBuilder.create();
   private static final PType              JSONB_Q_DATA_RESP             = new PType(SempResponse.class, SempQueueData.class);
   private static final PType              JSONB_Q_DATA_LIST             = new PType(List.class, SempQueueData.class);
   private static final PType              JSONB_Q_DATA_LIST_RESP        = new PType(SempResponse.class, JSONB_Q_DATA_LIST);

   private static final PType              JSONB_JNDI_T_DATA_LIST        = new PType(List.class, SempJndiTopicData.class);
   private static final PType              JSONB_JNDI_T_DATA_LIST_RESP   = new PType(SempResponse.class, JSONB_JNDI_T_DATA_LIST);

   // Properties
   private List<QManagerProperty>          parameters                    = new ArrayList<QManagerProperty>();

   private static final String             MESSAGE_VPN                   = "VPN";
   private static final String             MGMT_URL                      = "mgmt_url";
   private static final String             MGMT_USERNAME                 = "mgmt_username";
   private static final String             MGMT_PASSWORD                 = "mgmt_password";
   private static final String             BROWSER_TIMEOUT               = "browser_timeout";

   private static final String             SSL_CIPHER_SUITE              = "ssl_cipher_suite";
   private static final String             SSL_CONNECTION_DOWNGRADE_TO   = "ssl_connection_downgrade_to";
   private static final String             SSL_EXCLUDED_PROTOCOLS        = "ssl_excluded_protocols";
   private static final String             SSL_KEY_STORE                 = "ssl_key_store";
   private static final String             SSL_KEY_STORE_FORMAT          = "ssl_key_store_format";
   private static final String             SSL_KEY_STORE_PASSWORD        = "ssl_key_store_password";
   private static final String             SSL_PRIVATE_KEY_ALIAS         = "ssl_private_key_alias";
   private static final String             SSL_PRIVATE_KEY_PASSWORD      = "ssl_private_key_password";
   private static final String             SSL_PROTOCOL                  = "ssl_protocol";
   private static final String             SSL_TRUST_STORE               = "ssl_trust_store";
   private static final String             SSL_TRUST_STORE_FORMAT        = "ssl_trust_store_format";
   private static final String             SSL_TRUST_STORE_PASSWORD      = "ssl_trust_store_password";
   private static final String             SSL_TRUSTED_COMMON_NAME_LIST  = "ssl_trusted_common_name_list";
   private static final String             SSL_VALIDATE_CERTIFICATE      = "ssl_validate_certificate";
   private static final String             SSL_VALIDATE_CERTIFICATE_DATE = "ssl_validate_certificate_date";

   // Operations
   private final Map<Integer, SEMPContext> sempContexts                  = new HashMap<>();

   public SolaceQManager() {
      log.debug("Instantiate Solace");

      parameters.add(new QManagerProperty(MESSAGE_VPN, true, JMSPropertyKind.STRING, false, "VPN name", "default"));
      parameters.add(new QManagerProperty(MGMT_URL,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Management url (eg 'http://localhost:8080','https://localhost:8943)"));
      parameters.add(new QManagerProperty(MGMT_USERNAME, true, JMSPropertyKind.STRING, false, "Management user name"));
      parameters.add(new QManagerProperty(MGMT_PASSWORD, true, JMSPropertyKind.STRING, true, "Management user password"));
      parameters
               .add(new QManagerProperty(BROWSER_TIMEOUT,
                                         true,
                                         JMSPropertyKind.INT,
                                         false,
                                         "The maximum time in milliseconds for a QueueBrowser Enumeration.hasMoreElements() to wait for a message "
                                                + "to arrive in the Browser’s local message buffer before returning. If there is already a message waiting, "
                                                + "Enumeration.hasMoreElements() returns immediately.",
                                         "250"));

      parameters.add(new QManagerProperty(SSL_CIPHER_SUITE,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "The TLS/ SSL cipher suites to use to negotiate a secure connection"));
      parameters
               .add(new QManagerProperty(SSL_CONNECTION_DOWNGRADE_TO,
                                         false,
                                         JMSPropertyKind.STRING,
                                         false,
                                         "Transport protocol that TLS/SSL connections will be downgraded to after client authentication (eg 'PLAIN_TEXT')"));
      parameters.add(new QManagerProperty(SSL_EXCLUDED_PROTOCOLS,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Protocols that should not be used"));
      parameters.add(new QManagerProperty(SSL_KEY_STORE,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Client side certificate key store ((eg D:/somewhere/key.jks)"));
      parameters.add(new QManagerProperty(SSL_KEY_STORE_FORMAT,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Client side certificate key store format ('jks' or 'pkcs12')"));
      parameters.add(new QManagerProperty(SSL_KEY_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(SSL_PRIVATE_KEY_ALIAS,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Key alias for client client certificate authentication"));
      parameters.add(new QManagerProperty(SSL_PRIVATE_KEY_PASSWORD, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(SSL_PROTOCOL,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Comma-separated list of the encryption protocolsl ('sslv3,tlsv1,tlsv1.1,tlsv1.2')"));
      parameters.add(new QManagerProperty(SSL_TRUST_STORE,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Trust store (eg D:/somewhere/trust.jks)"));
      parameters.add(new QManagerProperty(SSL_TRUST_STORE_FORMAT,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Trust store format ('jks' or 'pkcs12')"));
      parameters.add(new QManagerProperty(SSL_TRUST_STORE_PASSWORD, false, JMSPropertyKind.STRING, true));
      parameters.add(new QManagerProperty(SSL_TRUSTED_COMMON_NAME_LIST,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "A list of up to 16 acceptable common names for matching in server certificates"));
      parameters
               .add(new QManagerProperty(SSL_VALIDATE_CERTIFICATE,
                                         false,
                                         JMSPropertyKind.BOOLEAN,
                                         false,
                                         "Indicates whether the API should validate server certificates with the trusted certificates in the trust store"));
      parameters
               .add(new QManagerProperty(SSL_VALIDATE_CERTIFICATE_DATE,
                                         false,
                                         JMSPropertyKind.BOOLEAN,
                                         false,
                                         "Indicates whether the Session connection should fail when an expired certificate or a certificate not yet in use is received"));
   }

   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Extract properties
      Map<String, String> mapProperties = extractProperties(sessionDef);

      String vpn = mapProperties.get(MESSAGE_VPN);
      String mgmtUrl = mapProperties.get(MGMT_URL);
      String mgmtUsername = mapProperties.get(MGMT_USERNAME);
      String mgmtPassword = mapProperties.get(MGMT_PASSWORD);
      int browserTimeout = Integer.parseInt(mapProperties.get(BROWSER_TIMEOUT));
      String sslCipherSuite = mapProperties.get(SSL_CIPHER_SUITE);
      String sslConnectionDowngradeTo = mapProperties.get(SSL_CONNECTION_DOWNGRADE_TO);
      String sslExcludedProtocols = mapProperties.get(SSL_EXCLUDED_PROTOCOLS);
      String sslKeyStore = mapProperties.get(SSL_KEY_STORE);
      String sslKeyStoreFormat = mapProperties.get(SSL_KEY_STORE_FORMAT);
      String sslKeyStorePassword = mapProperties.get(SSL_KEY_STORE_PASSWORD);
      String sslPrivateKeyAlias = mapProperties.get(SSL_PRIVATE_KEY_ALIAS);
      String sslPrivateKeyPassword = mapProperties.get(SSL_PRIVATE_KEY_PASSWORD);
      String sslProtocol = mapProperties.get(SSL_PROTOCOL);
      String sslTrustStore = mapProperties.get(SSL_TRUST_STORE);
      String sslTrustStoreFormat = mapProperties.get(SSL_TRUST_STORE_FORMAT);
      String sslTrustStorePassword = mapProperties.get(SSL_TRUST_STORE_PASSWORD);
      String sslTrustedCommonNameList = mapProperties.get(SSL_TRUSTED_COMMON_NAME_LIST);
      String sslValidateCertificate = mapProperties.get(SSL_VALIDATE_CERTIFICATE);
      String sslValidateCertificateDate = mapProperties.get(SSL_VALIDATE_CERTIFICATE_DATE);

      if (browserTimeout < 250) {
         throw new Exception("Browser timeout must be an integer greater than or equal to 250.");
      }

      // JMS Connections

      SolConnectionFactory cf = SolJmsUtility.createConnectionFactory();
      cf.setDirectTransport(false);

      cf.setHost(sessionDef.getHost());
      cf.setPort(sessionDef.getPort());

      cf.setVPN(vpn);
      cf.setUsername(sessionDef.getActiveUserid());
      cf.setPassword(sessionDef.getActivePassword());
      cf.setBrowserTimeoutInMS(browserTimeout);

      if (sslCipherSuite != null) {
         cf.setSSLCipherSuites(sslCipherSuite);
      }
      if (sslConnectionDowngradeTo != null) {
         cf.setSSLConnectionDowngradeTo(sslConnectionDowngradeTo);
      }
      if (sslExcludedProtocols != null) {
         cf.setSSLExcludedProtocols(sslExcludedProtocols);
      }
      if (sslKeyStore != null) {
         cf.setSSLKeyStore(sslKeyStore);
      }
      if (sslKeyStoreFormat != null) {
         cf.setSSLKeyStoreFormat(sslKeyStoreFormat);
      }
      if (sslKeyStorePassword != null) {
         cf.setSSLKeyStorePassword(sslKeyStorePassword);
      }
      if (sslPrivateKeyAlias != null) {
         cf.setSSLPrivateKeyAlias(sslPrivateKeyAlias);
      }
      if (sslPrivateKeyPassword != null) {
         cf.setSSLPrivateKeyPassword(sslPrivateKeyPassword);
      }
      if (sslProtocol != null) {
         cf.setSSLProtocol(sslProtocol);
      }
      if (sslTrustStore != null) {
         cf.setSSLTrustStore(sslTrustStore);
      }
      if (sslTrustStoreFormat != null) {
         cf.setSSLTrustStoreFormat(sslTrustStoreFormat);
      }
      if (sslTrustStorePassword != null) {
         cf.setSSLTrustStorePassword(sslTrustStorePassword);
      }
      if (sslTrustedCommonNameList != null) {
         cf.setSSLTrustedCommonNameList(sslTrustedCommonNameList);
      }
      if (sslValidateCertificate != null) {
         cf.setSSLValidateCertificate(Boolean.parseBoolean(sslValidateCertificate));
      }
      if (sslValidateCertificateDate != null) {
         cf.setSSLValidateCertificateDate(Boolean.parseBoolean(sslValidateCertificateDate));
      }

      Connection jmsConnection = cf.createConnection();
      jmsConnection.setClientID(clientID);
      jmsConnection.start();

      // Store per connection related data
      Integer hash = jmsConnection.hashCode();
      sempContexts.put(hash, new SEMPContext(vpn, mgmtUrl, mgmtUsername, mgmtPassword));

      return jmsConnection;
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      Integer hash = jmsConnection.hashCode();
      sempContexts.remove(hash);

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occurred while closing jmsConnection. Ignore it. Msg={}", e.getMessage());
      }
   }

   @Override
   public DestinationData discoverDestinations(Connection jmsConnection, boolean showSystemObjects) throws Exception {
      log.debug("discoverDestinations. showSystemObjects? {}", showSystemObjects);

      Integer hash = jmsConnection.hashCode();
      SEMPContext sempContext = sempContexts.get(hash);

      // Build Queues list
      SortedSet<QueueData> listQueueData = new TreeSet<>();

      HttpRequest request = sempContext.getSempListQueuesRequest();
      log.debug("SEMP request: {}", request);
      HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
      String body = response.body();
      log.debug("statusCode={}", response.statusCode());
      log.trace("body={}", response.body());
      if (response.statusCode() != HttpURLConnection.HTTP_OK) {
         String msg = formatSempError("Error received from Solace server when retrieving Queue List", response.statusCode(), body);
         log.error(msg);
         throw new Exception(msg);
      }

      SempResponse<List<SempQueueData>> queues = JSONB.fromJson(body, JSONB_Q_DATA_LIST_RESP);
      for (SempQueueData q : queues.data) {
         log.debug("q={}", q.queueName);
         listQueueData.add(new QueueData(q.queueName));
      }

      // Build Topics lists
      SortedSet<TopicData> listTopicData = new TreeSet<>();

      request = sempContext.getSempListJndiTopicsRequest();
      log.debug("SEMP request: {}", request);
      response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
      body = response.body();
      log.debug("statusCode={}", response.statusCode());
      log.trace("body={}", response.body());
      if (response.statusCode() != HttpURLConnection.HTTP_OK) {
         String msg = formatSempError("Error received from Solace server when retrieving JNDITopic List",
                                      response.statusCode(),
                                      body);
         log.error(msg);
         throw new Exception(msg);
      }

      SempResponse<List<SempJndiTopicData>> topics = JSONB.fromJson(body, JSONB_JNDI_T_DATA_LIST_RESP);
      for (SempJndiTopicData sempJndiTopicData : topics.data) {
         log.debug("t={}", sempJndiTopicData.physicalName);
         sempContext.putJndiTopicData(sempJndiTopicData);
         listTopicData.add(new TopicData(sempJndiTopicData.physicalName));
      }

      return new DestinationData(listQueueData, listTopicData);
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {

      Integer hash = jmsConnection.hashCode();
      SEMPContext sempContext = sempContexts.get(hash);

      SortedMap<String, Object> properties = new TreeMap<>();

      try {
         HttpRequest request = sempContext.buildQueueInfoRequest(queueName);
         log.debug("SEMP request: {}", request);
         HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
         String body = response.body();
         log.debug("statusCode={}", response.statusCode());
         log.trace("body={}", response.body());
         if (response.statusCode() != HttpURLConnection.HTTP_OK) {
            String msg = formatSempError("Error received from Solace server when retrieving Queue information for '{}'",
                                         response.statusCode(),
                                         body);
            log.error(msg, queueName);
            return properties;
         }

         SempResponse<SempQueueData> qResp = JSONB.fromJson(body, JSONB_Q_DATA_RESP);
         SempQueueData qData = qResp.data;

         properties.put("accessType", qData.accessType);
         properties.put("consumerAckPropagationEnabled", qData.consumerAckPropagationEnabled);
         properties.put("deadMsgQueue", qData.deadMsgQueue);
         properties.put("egressEnabled", qData.egressEnabled);
         properties.put("maxBindCount", qData.maxBindCount);
         properties.put("maxDeliveredUnackedMsgsPerFlow ", qData.maxDeliveredUnackedMsgsPerFlow);
         properties.put("maxMsgSize", qData.maxMsgSize);
         properties.put("maxMsgSpoolUsage", qData.maxMsgSpoolUsage);
         properties.put("maxRedeliveryCount", qData.maxRedeliveryCount);
         properties.put("maxTtl", qData.maxTtl);
         properties.put("permission", qData.permission);
         properties.put("rejectLowPriorityMsgEnabled", qData.rejectLowPriorityMsgEnabled);
         properties.put("rejectLowPriorityMsgLimit", qData.rejectLowPriorityMsgLimit);
         properties.put("rejectMsgToSenderOnDiscardBehavior", qData.rejectMsgToSenderOnDiscardBehavior);
         properties.put("respectMsgPriorityEnabled", qData.respectMsgPriorityEnabled);
         properties.put("respectTtlEnabled", qData.respectTtlEnabled);

      } catch (Exception e) {
         log.error("Exception occurred in getQueueInformation()", e);
      }

      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {

      Integer hash = jmsConnection.hashCode();
      SEMPContext sempContext = sempContexts.get(hash);

      TreeMap<String, Object> properties = new TreeMap<>();

      properties.put("physicalName", sempContext.getJndiTopicData(topicName).physicalName);
      properties.put("topicName", sempContext.getJndiTopicData(topicName).topicName);

      return properties;
   }

   // -------
   // Helpers
   // -------

   private String formatSempError(String message, int httpStatusCode, String body) {

      SempResponse<SempQueueData> resp = JSONB.fromJson(body, JSONB_Q_DATA_RESP); // Whatever struct

      SempResponseMetaError sempResponseMetaError = null;
      if (resp.meta != null) {
         sempResponseMetaError = resp.meta.error;
      }

      StringBuilder sb = new StringBuilder(256);
      sb.append(message).append(CR);
      sb.append("Details of the error encountered:").append(CR);
      sb.append("HTTP Status Code: " + httpStatusCode).append(CR);
      if (sempResponseMetaError != null) {
         sb.append("SEMP Error Code: " + sempResponseMetaError.code).append(CR);
         sb.append("SEMP Error Status: " + sempResponseMetaError.status).append(CR);
         sb.append("SEMP Error Descriptions: " + sempResponseMetaError.description).append(CR);
      }

      return sb.toString();
   }

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("No extra jar is needed as JMSToolBox is bundled with the latest Solace client jars").append(CR);
      sb.append(CR);
      sb.append("Information:").append(CR);
      sb.append("------------").append(CR);
      sb.append("Internally, JMSToolBox uses the Managment interface (SEMP) of Solace to interact with the Solace server")
               .append(CR);
      sb.append("For more information: https://docs.solace.com/SEMP/Using-SEMP.htm").append(CR);
      sb.append("For information on the SSL configuration: https://docs.solace.com/Configuring-and-Managing/TLS-SSL-Service-Connections.htm")
               .append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : Solace server host name (eg localhost)").append(CR);
      sb.append("Port          : Solace server message port (eg. 55555)").append(CR);
      sb.append("User/Password : User allowed to connect to get a JMS Connection ");
      sb.append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("- VPN             : Name of the VPN (eg 'default')").append(CR);
      sb.append("- browser_timeout : The maximum time in ms for a QueueBrowser to wait for a message to arrive in the Browser’s local message buffer before returning.")
               .append(CR);
      sb.append("                    If there is already a message waiting, Enumeration.hasMoreElements() returns immediately.")
               .append(CR);
      sb.append("                    Minimum value: 250").append(CR);
      sb.append(CR);
      sb.append("- mgmt_url      : Managment URL (scheme+host+port) of the SEMP managment interface (eg 'http://localhost:8080'")
               .append(CR);
      sb.append("- mgmt_username : Managment user name").append(CR);
      sb.append("- mgmt_password : Managment user password").append(CR);
      sb.append(CR);

      sb.append("- ssl_cipher_suite              : The TLS/ SSL cipher suites to use to negotiate a secure connection").append(CR);
      sb.append("- ssl_connection_downgrade_to   : Transport protocol that TLS/SSL connections will be downgraded to after client authentication (eg 'PLAIN_TEXT')")
               .append(CR);
      sb.append("- ssl_excluded_protocols        : Protocols that should not be used").append(CR);
      sb.append("- ssl_key_store                 : Client side certificate key store ((eg D:/somewhere/key.jks)").append(CR);
      sb.append("- ssl_key_store_format          : Client side certificate key store format ('jks' or 'pkcs12')").append(CR);
      sb.append("- ssl_key_store_password        : Client side certificate key store password").append(CR);
      sb.append("- ssl_private_key_alias         : Key alias for client client certificate authentication").append(CR);
      sb.append("- ssl_private_key_password      : Key password").append(CR);
      sb.append("- ssl_protocol                  : Comma-separated list of the encryption protocolsl ('sslv3,tlsv1,tlsv1.1,tlsv1.2')")
               .append(CR);
      sb.append("- ssl_trust_store               : Trust store (eg D:/somewhere/trust.jks). Mandatory if the SSL Certificate Validation property is set to true")
               .append(CR);
      sb.append("- ssl_trust_store_format        : Trust store format ('jks' or 'pkcs12')").append(CR);
      sb.append("- ssl_trust_store_password      : Trust store password").append(CR);
      sb.append("- ssl_trusted_common_name_list  : A list of up to 16 acceptable common names for matching in server certificates")
               .append(CR);
      sb.append("- ssl_validate_certificate      : Indicates whether the API should validate server certificates with the trusted certificates in the trust store")
               .append(CR);
      sb.append("- ssl_validate_certificate_date : Indicates whether the Session connection should fail when an expired certificate or a certificate not yet in use is received")
               .append(CR);

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

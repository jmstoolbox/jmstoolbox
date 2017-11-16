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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.ConnectionData;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;

/**
 * 
 * Implements Software AG Universal Messaging Q Provider
 * 
 * @author Denis Forveille
 *
 */
public class UMQManager extends QManager {

   private static final Logger    log        = LoggerFactory.getLogger(UMQManager.class);

   private static final String    CR         = "\n";
   private static final String    HELP_TEXT;

   private List<QManagerProperty> parameters = new ArrayList<QManagerProperty>();

   // private final Map<Integer, MQQueueManager> queueManagers = new HashMap<>();

   // ------------------------
   // Constructor
   // ------------------------

   public UMQManager() {
      log.debug("Instantiate UniversalMessagingQManager");
   }

   // ------------------------
   // Business Interface
   // ------------------------
   @Override
   public ConnectionData connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      return null;
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      Integer hash = jmsConnection.hashCode();
      // MQQueueManager queueManager = queueManagers.get(hash);
      //
      // try {
      // jmsConnection.close();
      // } catch (Exception e) {
      // log.warn("Exception occured while closing connection. Ignore it. Msg={}", e.getMessage());
      // }
      //
      // try {
      // queueManager.disconnect();
      // queueManager.close();
      // } catch (MQException e) {
      // throw new JMSException(e.getMessage());
      // }
      //
      // queueManagers.remove(hash);
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {
      return super.getQueueDepth(jmsConnection, queueName);
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {
      return super.getQueueInformation(jmsConnection, queueName);
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {
      return super.getTopicInformation(jmsConnection, topicName);
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

}

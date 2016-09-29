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
package org.titou10.jtb.qm.rabbitmq;

import java.util.ArrayList;
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
import org.titou10.jtb.jms.qm.ConnectionData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;

import com.rabbitmq.client.ConnectionFactory;

/**
 * 
 * Implements Pivotal RabbitMQ Q Provider
 * 
 * http://rabbitmq.docs.pivotal.io/doc/34/topics/install-jms-client.html
 * 
 * http://developer.kaazing.com/documentation/jms/4.0/integration-jms/p_jms_integrate_rabbitmq.html
 * 
 * https://my.vmware.com/web/vmware/details?downloadGroup=VFRMQ_JMS_105&productId=349
 * 
 * @author Denis Forveille
 *
 */
public class RabbitMQQManager extends QManager {

   private static final Logger    log             = LoggerFactory.getLogger(RabbitMQQManager.class);

   private static final String    CR              = "\n";

   private List<QManagerProperty> parameters      = new ArrayList<QManagerProperty>();
   private SortedSet<String>      queueNames      = new TreeSet<>();
   private SortedSet<String>      topicNames      = new TreeSet<>();

   private static final String    P_QUEUE_MANAGER = "queueManager";

   public RabbitMQQManager() {
      log.debug("Instantiate MQQManager");

      parameters.add(new QManagerProperty(P_QUEUE_MANAGER, true, JMSPropertyKind.STRING));
   }

   @Override
   public ConnectionData connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(sessionDef.getHost());
      factory.setPort(sessionDef.getPort());
      if (sessionDef.getUserid() != null) {
         factory.setUsername(sessionDef.getUserid());
      }
      if (sessionDef.getPassword() != null) {
         factory.setPassword(sessionDef.getPassword());
      }

      com.rabbitmq.client.Connection connection = factory.newConnection();

      log.info("connected to {}", sessionDef.getName());

      return null;
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occured while closing connection. Ignore it. Msg={}", e.getMessage());
      }

      queueNames.clear();
      topicNames.clear();
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {
      return null;
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {
      SortedMap<String, Object> properties = new TreeMap<>();
      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {
      SortedMap<String, Object> properties = new TreeMap<>();
      return properties;
   }

   @Override
   public String getHelpText() {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS:").append(CR);
      sb.append("-----------").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : RabbitMQ server host name").append(CR);
      sb.append("Port          : RabbitMQ port").append(CR);
      sb.append("User/Password : User allowed to connect to RabbitMQ").append(CR);
      sb.append(CR);
      sb.append("Properties values:").append(CR);
      sb.append("---------------").append(CR);
      sb.append("queueManager                : Queue Manager Name").append(CR);
      return sb.toString();
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }

}

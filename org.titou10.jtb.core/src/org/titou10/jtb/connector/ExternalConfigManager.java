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
package org.titou10.jtb.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.jms.JMSException;
//import javax.jms.Destination;
import javax.jms.TextMessage;

import org.eclipse.jface.preference.PreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.connector.ex.ExecutionException;
import org.titou10.jtb.connector.ex.UnknownDestinationException;
import org.titou10.jtb.connector.ex.UnknownQueueException;
import org.titou10.jtb.connector.ex.UnknownSessionException;
import org.titou10.jtb.connector.transport.Destination;
import org.titou10.jtb.connector.transport.Destination.Type;
import org.titou10.jtb.connector.transport.Message;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBTopic;

/**
 * Exposes ConfigManager services to connector plugins
 * 
 * @author Denis Forveille
 *
 */
public class ExternalConfigManager {

   private static final Logger log = LoggerFactory.getLogger(ExternalConfigManager.class);

   private ConfigManager       cm;

   // -------------------------------
   // Constructors + basic properties
   // -------------------------------

   public ExternalConfigManager(ConfigManager cm) {
      this.cm = cm;
   }

   public PreferenceStore getPreferenceStore() {
      return cm.getPreferenceStore();
   }

   // ----------------------------
   // Services related to Sessions
   // ----------------------------

   public List<Destination> getDestination(String sessionName) throws ExecutionException, UnknownSessionException {

      JTBSession jtbSession = getJTBSession(sessionName);

      List<Destination> destinations = new ArrayList<>();

      try {
         if (!(jtbSession.isConnected())) {
            jtbSession.connectOrDisconnect();
         }
      } catch (Exception e) {
         log.error("Exception when reading destinations of '{}'", sessionName, e);
         throw new ExecutionException(e);
      }

      for (JTBQueue jtbQueue : jtbSession.getJtbQueues()) {
         destinations.add(new Destination(jtbQueue.getName(), Type.QUEUE));
      }
      for (JTBTopic jtbTopic : jtbSession.getJtbTopics()) {
         destinations.add(new Destination(jtbTopic.getName(), Type.TOPIC));
      }

      return destinations;
   }

   // ----------------------------
   // Services related to Messages
   // ----------------------------

   public List<Message> browseMessages(String sessionName, String queueName, int limit) throws ExecutionException,
                                                                                        UnknownSessionException,
                                                                                        UnknownDestinationException,
                                                                                        UnknownQueueException {

      JTBSession jtbSession = getJTBSession(sessionName);

      try {
         if (!(jtbSession.isConnected())) {
            jtbSession.connectOrDisconnect();
         }
      } catch (Exception e) {
         log.error("Exception when browsing messages in queue '{}::{}'", sessionName, queueName, e);
         throw new ExecutionException(e);
      }

      JTBQueue jtbQueue = getJTBQueue(jtbSession, queueName);

      List<Message> messages = new ArrayList<>();

      try {
         List<JTBMessage> jtbMessages = jtbSession.browseQueue(jtbQueue, limit);
         if (jtbMessages.isEmpty()) {
            return messages;
         }

         javax.jms.Message jmsMessage = jtbMessages.get(0).getJmsMessage();
         Message m = new Message();
         m.setJmsCorrelationID(jmsMessage.getJMSCorrelationID());
         m.setJmsExpiration(jmsMessage.getJMSExpiration());
         m.setJmsPriority(jmsMessage.getJMSPriority());
         m.setJmsType(jmsMessage.getJMSType());
         if (jmsMessage instanceof TextMessage) {
            m.setPayload(((TextMessage) jmsMessage).getText());
         }
         return messages;
      } catch (Exception e) {
         log.error("Exception when browsing messages in queue '{}::{}'", sessionName, queueName, e);
         throw new ExecutionException(e);
      }

   }

   public void postMessage(String sessionName, String destinationName, Message message) throws ExecutionException,
                                                                                        UnknownSessionException,
                                                                                        UnknownDestinationException {
      log.warn("postMessage");

      JTBSession jtbSession = getJTBSession(sessionName);
      try {
         if (!(jtbSession.isConnected())) {
            jtbSession.connectOrDisconnect();
         }
      } catch (Exception e) {
         log.error("Exception when posting message to destination '{}::{}'", sessionName, destinationName, e);
         throw new ExecutionException(e);
      }
      JTBDestination jtbDestination = getJTBDestination(jtbSession, destinationName);

      // Create Message
      try {
         TextMessage jmsMessage = (TextMessage) jtbSession.createJMSMessage(JTBMessageType.TEXT);
         jmsMessage.setJMSCorrelationID(message.getJmsCorrelationID());
         jmsMessage.setJMSExpiration(message.getJmsExpiration());
         jmsMessage.setJMSPriority(message.getJmsPriority());
         jmsMessage.setJMSType(message.getJmsType());

         jmsMessage.setText(message.getPayload());

         if ((message.getProperties() != null) && (!message.getProperties().isEmpty())) {
            for (Entry<String, String> e : message.getProperties().entrySet()) {
               jmsMessage.setStringProperty(e.getKey(), e.getValue());
            }
         }

         JTBMessage jtbMessage = new JTBMessage(jtbDestination, jmsMessage);

         // Post Message
         jtbSession.sendMessage(jtbMessage);
      } catch (Exception e) {
         log.error("Exception when posting message to destination '{}::{}'", sessionName, destinationName, e);
         throw new ExecutionException(e);
      }

   }

   public void postMessageTemplate(String sessionName, String destinationName, String templateName) {
      log.warn("postMessage");
   }

   public void emptyQueue(String sessionName, String queueName) throws ExecutionException,
                                                                UnknownSessionException,
                                                                UnknownDestinationException,
                                                                UnknownQueueException {
      log.warn("emptyQueue");

      JTBSession jtbSession = getJTBSession(sessionName);
      JTBDestination jtbDestination = getJTBQueue(jtbSession, queueName);

      try {
         jtbSession.emptyQueue((JTBQueue) jtbDestination);
      } catch (JMSException e) {
         log.error("Exception when emptying queue '{}::{}'", sessionName, queueName, e);
         throw new ExecutionException(e);
      }
   }

   // ----------------------------
   // Services related to Scripts
   // ----------------------------
   public void executeScript(String scriptName) {
      log.warn("executeScript");

   }

   // ----------------------------
   // Helpers
   // ----------------------------
   private JTBSession getJTBSession(String sessionName) throws UnknownSessionException {
      JTBSession jtbSession = cm.getJTBSessionByName(sessionName);
      if (jtbSession == null) {
         log.warn("Session '{}' does not exist", sessionName);
         throw new UnknownSessionException(sessionName);
      }
      return jtbSession;
   }

   private JTBDestination getJTBDestination(JTBSession jtbSession, String destinationName) throws UnknownDestinationException {
      JTBDestination jtbDestination = jtbSession.getJTBDestinationByName(destinationName);
      if (jtbDestination == null) {
         log.warn("Destination '{}' does not exist", destinationName);
         throw new UnknownDestinationException(destinationName);
      }
      return jtbDestination;
   }

   private JTBQueue getJTBQueue(JTBSession jtbSession, String queueName) throws UnknownDestinationException, UnknownQueueException {
      JTBDestination jtbDestination = getJTBDestination(jtbSession, queueName);
      if (!(jtbDestination instanceof JTBQueue)) {
         log.warn("Destination '{}' is not a Queue", queueName);
         throw new UnknownQueueException(queueName);
      }
      return (JTBQueue) jtbDestination;
   }
}

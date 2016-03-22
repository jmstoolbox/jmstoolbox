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

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.bind.JAXBException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.connector.ex.EmptyMessageException;
import org.titou10.jtb.connector.ex.ExecutionException;
import org.titou10.jtb.connector.ex.UnknownDestinationException;
import org.titou10.jtb.connector.ex.UnknownQueueException;
import org.titou10.jtb.connector.ex.UnknownSessionException;
import org.titou10.jtb.connector.ex.UnknownTemplateException;
import org.titou10.jtb.connector.transport.Destination;
import org.titou10.jtb.connector.transport.Destination.Type;
import org.titou10.jtb.connector.transport.MessageInput;
import org.titou10.jtb.connector.transport.MessageOutput;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBTopic;
import org.titou10.jtb.template.TemplatesUtils;
import org.titou10.jtb.variable.VariablesUtils;

/**
 * Exposes ConfigManager services to connector plugins
 * 
 * @author Denis Forveille
 *
 */
public class ExternalConfigManager {

   private static final Logger log         = LoggerFactory.getLogger(ExternalConfigManager.class);

   private static final String UNSPECIFIED = "<unspecified>";

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

   public List<MessageOutput> browseMessages(String sessionName, String queueName, int limit) throws ExecutionException,
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

      List<MessageOutput> messages = new ArrayList<>();

      try {
         List<JTBMessage> jtbMessages = jtbSession.browseQueue(jtbQueue, limit);
         for (JTBMessage jtbMessage : jtbMessages) {
            messages.add(new MessageOutput(jtbMessage, null));
         }
         return messages;
      } catch (Exception e) {
         log.error("Exception when browsing messages in queue '{}::{}'", sessionName, queueName, e);
         throw new ExecutionException(e);
      }

   }

   public List<MessageOutput> removeMessages(String sessionName, String queueName, int limit) throws ExecutionException,
                                                                                              UnknownSessionException,
                                                                                              UnknownDestinationException,
                                                                                              UnknownQueueException {

      JTBSession jtbSession = getJTBSession(sessionName);

      try {
         if (!(jtbSession.isConnected())) {
            jtbSession.connectOrDisconnect();
         }
      } catch (Exception e) {
         log.error("Exception when removing messages from queue '{}::{}'", sessionName, queueName, e);
         throw new ExecutionException(e);
      }

      JTBQueue jtbQueue = getJTBQueue(jtbSession, queueName);

      List<MessageOutput> messages = new ArrayList<>();

      try {
         List<JTBMessage> jtbMessages = jtbSession.removeFirstMessages(jtbQueue, limit);
         for (JTBMessage jtbMessage : jtbMessages) {
            messages.add(new MessageOutput(jtbMessage, null));
         }
         return messages;
      } catch (Exception e) {
         log.error("Exception when removing messages from queue '{}::{}'", sessionName, queueName, e);
         throw new ExecutionException(e);
      }

   }

   public void postMessage(String sessionName, String destinationName, MessageInput messageInput) throws ExecutionException,
                                                                                                  UnknownSessionException,
                                                                                                  UnknownDestinationException,
                                                                                                  EmptyMessageException {
      log.warn("postMessage");

      if (messageInput == null) {
         throw new EmptyMessageException();
      }
      if (messageInput.getMessageInputType() == null) {
         throw new EmptyMessageException();
      }

      // Get JTBSession
      JTBSession jtbSession = getJTBSession(sessionName);
      try {
         if (!(jtbSession.isConnected())) {
            jtbSession.connectOrDisconnect();
         }
      } catch (Exception e) {
         log.error("Exception when posting message to destination '{}::{}'", sessionName, destinationName, e);
         throw new ExecutionException(e);
      }

      // Get JTBDestination
      JTBDestination jtbDestination = getJTBDestination(jtbSession, destinationName);

      try {
         // Create a JTBMessage from the MessageInput received
         JTBMessage jtbMessage = messageInput.toJTBMessage(jtbSession, jtbDestination);

         // Post Message
         jtbSession.sendMessage(jtbMessage);
      } catch (Exception e) {
         log.error("Exception when posting message to destination '{}::{}'", sessionName, destinationName, e);
         throw new ExecutionException(e);
      }

   }

   public MessageOutput postMessageTemplate(String sessionName,
                                            String destinationName,
                                            String templateName) throws EmptyMessageException,
                                                                 UnknownSessionException,
                                                                 ExecutionException,
                                                                 UnknownDestinationException,
                                                                 UnknownTemplateException {
      log.warn("postMessageTemplate");

      // Get JTBSession
      JTBSession jtbSession = getJTBSession(sessionName);
      try {
         if (!(jtbSession.isConnected())) {
            jtbSession.connectOrDisconnect();
         }
      } catch (Exception e) {
         log.error("Exception when posting message to destination '{}::{}' with template",
                   sessionName,
                   destinationName,
                   templateName,
                   e);
         throw new ExecutionException(e);
      }

      // Get JTBDestination
      JTBDestination jtbDestination = getJTBDestination(jtbSession, destinationName);

      // Get JTBTemplate
      JTBMessageTemplate jtbMessageTemplate = getJTBMessageTemplate(templateName);

      try {
         Message m = jtbSession.createJMSMessage(jtbMessageTemplate.getJtbMessageType());

         // Resolve variables
         byte[] payloadBytes = null;
         switch (jtbMessageTemplate.getJtbMessageType()) {
            case TEXT:
               String payload = VariablesUtils.replaceTemplateVariables(cm.getVariables(), jtbMessageTemplate.getPayloadText());
               jtbMessageTemplate.setPayloadText(payload);
               break;

            case BYTES:
               payloadBytes = jtbMessageTemplate.getPayloadBytes();
               break;
            default:
               break;
         }

         jtbMessageTemplate.toJMSMessage(m);

         // Send Message
         JTBMessage jtbMessage = new JTBMessage(jtbDestination, m);

         jtbDestination.getJtbSession().sendMessage(jtbMessage);

         return new MessageOutput(jtbMessage, payloadBytes);
      } catch (Exception e) {
         log.error("Exception when posting message to destination '{}::{}' with template",
                   sessionName,
                   destinationName,
                   templateName,
                   e);
         throw new ExecutionException(e);
      }

   }

   public int emptyQueue(String sessionName, String queueName) throws ExecutionException,
                                                               UnknownSessionException,
                                                               UnknownDestinationException,
                                                               UnknownQueueException {
      log.warn("emptyQueue");

      JTBSession jtbSession = getJTBSession(sessionName);
      JTBDestination jtbDestination = getJTBQueue(jtbSession, queueName);

      try {
         return jtbSession.emptyQueue((JTBQueue) jtbDestination);
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
      if (sessionName == null) {
         throw new UnknownSessionException(UNSPECIFIED);
      }

      JTBSession jtbSession = cm.getJTBSessionByName(sessionName);
      if (jtbSession == null) {
         log.warn("Session '{}' does not exist", sessionName);
         throw new UnknownSessionException(sessionName);
      }
      return jtbSession;
   }

   private JTBDestination getJTBDestination(JTBSession jtbSession, String destinationName) throws UnknownDestinationException {
      if (destinationName == null) {
         throw new UnknownDestinationException(UNSPECIFIED);
      }

      JTBDestination jtbDestination = jtbSession.getJTBDestinationByName(destinationName);
      if (jtbDestination == null) {
         log.warn("Destination '{}' does not exist", destinationName);
         throw new UnknownDestinationException(destinationName);
      }
      return jtbDestination;
   }

   private JTBQueue getJTBQueue(JTBSession jtbSession, String queueName) throws UnknownDestinationException, UnknownQueueException {
      if (queueName == null) {
         throw new UnknownQueueException(UNSPECIFIED);
      }

      JTBDestination jtbDestination = getJTBDestination(jtbSession, queueName);
      if (!(jtbDestination instanceof JTBQueue)) {
         log.warn("Destination '{}' is not a Queue", queueName);
         throw new UnknownQueueException(queueName);
      }
      return (JTBQueue) jtbDestination;
   }

   private JTBMessageTemplate getJTBMessageTemplate(String templateName) throws UnknownTemplateException, ExecutionException {
      if (templateName == null) {
         throw new UnknownTemplateException(UNSPECIFIED);
      }

      JTBMessageTemplate jtbMessageTemplate;
      try {
         jtbMessageTemplate = TemplatesUtils.getTemplateFromName(cm.getTemplateFolder(), templateName);
         if (jtbMessageTemplate == null) {
            throw new UnknownTemplateException(templateName);
         }
         return jtbMessageTemplate;
      } catch (CoreException | JAXBException e) {
         throw new ExecutionException(e);
      }
   }
}

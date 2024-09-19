/*
 * Copyright (C) 2023 Denis Forveille titou10.titou10@gmail.com
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import jakarta.xml.bind.JAXBException;

import jakarta.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.config.JTBPreferenceStore;
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
import org.titou10.jtb.connector.transport.QueueOutput;
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.script.ScriptExecutionEngine;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.variable.VariablesManager;

/**
 * Exposes ConfigManager services to connector plugins
 *
 * @author Denis Forveille
 *
 */
public class ExternalConnectorManager {

   private static final Logger   log         = LoggerFactory.getLogger(ExternalConnectorManager.class);

   private static final String   UNSPECIFIED = "<unspecified>";

   @Inject
   private ConfigManager         cm;

   @Inject
   private JTBPreferenceStore    ps;

   @Inject
   private TemplatesManager      templatesManager;

   @Inject
   private VariablesManager      variablesManager;

   @Inject
   private ScriptExecutionEngine scriptExecutionEngine;

   // -------------------------------
   // Helpers
   // -------------------------------
   public IPreferenceStore getIPreferenceStore() {
      return ps;
   }

   // ----------------------------
   // Services related to Queues
   // ----------------------------

   public List<QueueOutput> getQueuesDepth(String sessionName, String queueName) throws ExecutionException,
                                                                                 UnknownSessionException {

      // Get JTBConnection
      JTBConnection jtbConnection = getJTBConnection(sessionName);
      try {
         jtbConnection.connect();
      } catch (Exception e) {
         log.error("Exception when reading queue depth of '{}'", sessionName, e);
         throw new ExecutionException(e);
      }

      QManager qm = jtbConnection.getQm();
      Connection jmsConnection = jtbConnection.getJmsConnection();

      List<QueueOutput> queues = new ArrayList<>();
      Integer depth = null;
      String qName = null;

      for (JTBQueue jtbQueue : jtbConnection.getJtbQueues()) {

         // Do not try to get Q Depth if the Queue is not browsable
         if (!jtbQueue.isBrowsable()) {
            continue;
         }

         qName = jtbQueue.getName();

         if (queueName != null) {
            if (qName.equals(queueName)) {
               depth = qm.getQueueDepth(jmsConnection, qName);
               queues.add(new QueueOutput(jtbQueue, depth == null ? null : depth.longValue()));
               return queues;
            }
         } else {
            depth = qm.getQueueDepth(jmsConnection, qName);
            queues.add(new QueueOutput(jtbQueue, depth == null ? null : depth.longValue()));
         }
      }

      return queues;

   }

   // ----------------------------
   // Services related to Sessions
   // ----------------------------

   public List<Destination> getDestination(String sessionName) throws ExecutionException, UnknownSessionException {

      // Get JTBConnection
      JTBConnection jtbConnection = getJTBConnection(sessionName);
      try {
         jtbConnection.connect();
      } catch (Exception e) {
         log.error("Exception when reading destinations of '{}'", sessionName, e);
         throw new ExecutionException(e);
      }

      var destinations = new ArrayList<Destination>();
      jtbConnection.getJtbQueues().stream().map(q -> new Destination(q.getName(), Type.QUEUE)).forEach(destinations::add);
      jtbConnection.getJtbTopics().stream().map(t -> new Destination(t.getName(), Type.TOPIC)).forEach(destinations::add);

      return destinations;
   }

   // ----------------------------
   // Services related to Messages
   // ----------------------------

   public List<MessageOutput> browseMessages(String sessionName,
                                             String queueName,
                                             String payloadSearchText,
                                             String selectorsSearchText,
                                             int limit) throws ExecutionException, UnknownSessionException,
                                                        UnknownDestinationException, UnknownQueueException {

      // Get JTBConnection
      JTBConnection jtbConnection = getJTBConnection(sessionName);
      try {
         jtbConnection.connect();
      } catch (Exception e) {
         log.error("Exception when browsing messages in queue '{}::{}'", sessionName, queueName, e);
         throw new ExecutionException(e);
      }

      JTBQueue jtbQueue = getJTBQueue(jtbConnection, queueName);

      var messages = new ArrayList<MessageOutput>();

      try {
         var jtbMessages = jtbConnection.browseQueue(jtbQueue, limit, payloadSearchText, selectorsSearchText);

         // Pbm with exception handling
         // jtbMessages.stream().map(m -> new MessageOutput(m, null)).forEach(messages::add);
         for (JTBMessage jtbMessage : jtbMessages) {
            messages.add(new MessageOutput(jtbMessage, null));
         }

         return messages;
      } catch (Exception e) {
         log.error("Exception when browsing messages in queue '{}::{}'", sessionName, queueName, e);
         throw new ExecutionException(e);
      }

   }

   public List<MessageOutput> removeMessages(String sessionName,
                                             String queueName,
                                             String selectorsSearchText,
                                             int limit) throws ExecutionException, UnknownSessionException,
                                                        UnknownDestinationException, UnknownQueueException {

      // Get JTBConnection
      JTBConnection jtbConnection = getJTBConnection(sessionName);
      try {
         jtbConnection.connect();
      } catch (Exception e) {
         log.error("Exception when removing messages from queue '{}::{}'", sessionName, queueName, e);
         throw new ExecutionException(e);
      }

      JTBQueue jtbQueue = getJTBQueue(jtbConnection, queueName);

      List<MessageOutput> messages = new ArrayList<>();

      try {
         List<JTBMessage> jtbMessages = jtbConnection.removeMessages(jtbQueue, limit, selectorsSearchText);
         for (JTBMessage jtbMessage : jtbMessages) {
            messages.add(new MessageOutput(jtbMessage, null));
         }
         return messages;
      } catch (Exception e) {
         log.error("Exception when removing messages from queue '{}::{}'", sessionName, queueName, e);
         throw new ExecutionException(e);
      }

   }

   public MessageOutput postMessage(String sessionName,
                                    String destinationName,
                                    MessageInput messageInput) throws ExecutionException, UnknownSessionException,
                                                               UnknownDestinationException, EmptyMessageException {
      log.debug("postMessage");

      if (messageInput == null) {
         throw new EmptyMessageException();
      }
      if (messageInput.getType() == null) {
         throw new EmptyMessageException();
      }

      // Get JTBConnection
      JTBConnection jtbConnection = getJTBConnection(sessionName);
      try {
         jtbConnection.connect();
      } catch (Exception e) {
         log.error("Exception when posting message to destination '{}::{}'", sessionName, destinationName, e);
         throw new ExecutionException(e);
      }

      // Get JTBDestination
      JTBDestination jtbDestination = getJTBDestination(jtbConnection, destinationName);

      try {
         // Create a JTBMessage from the MessageInput received
         JTBMessage jtbMessage = messageInput.toJTBMessage(jtbConnection, jtbDestination);

         // Post Message
         jtbConnection.sendMessage(jtbMessage);

         return new MessageOutput(jtbMessage, null);

      } catch (Exception e) {
         log.error("Exception when posting message to destination '{}::{}'", sessionName, destinationName, e);
         throw new ExecutionException(e);
      }

   }

   public MessageOutput postMessageTemplate(String sessionName,
                                            String destinationName,
                                            String templateName) throws EmptyMessageException, UnknownSessionException,
                                                                 ExecutionException, UnknownDestinationException,
                                                                 UnknownTemplateException {
      log.debug("postMessageTemplate");

      // Get JTBConnection
      JTBConnection jtbConnection = getJTBConnection(sessionName);
      try {
         jtbConnection.connect();
      } catch (Exception e) {
         log.error("Exception when posting message to destination '{}::{}' with template",
                   sessionName,
                   destinationName,
                   templateName,
                   e);
         throw new ExecutionException(e);
      }

      // Get JTBDestination
      JTBDestination jtbDestination = getJTBDestination(jtbConnection, destinationName);

      // Get JTBTemplate
      JTBMessageTemplate jtbMessageTemplate = getJTBMessageTemplate(templateName);

      try {
         Message m = jtbConnection.createJMSMessage(jtbMessageTemplate.getJtbMessageType());

         // Resolve variables
         byte[] payloadBytes = null;
         switch (jtbMessageTemplate.getJtbMessageType()) {
            case TEXT:
               String payload = variablesManager.replaceTemplateVariables(jtbMessageTemplate.getPayloadText());
               jtbMessageTemplate.setPayloadText(payload);
               break;

            case BYTES:
               payloadBytes = jtbMessageTemplate.getPayloadBytes();
               break;
            default:
               break;
         }

         // Send Message
         JTBMessage jtbMessage = jtbMessageTemplate.toJTBMessage(jtbDestination, m);
         jtbDestination.getJtbConnection().sendMessage(jtbMessage);

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

   public int emptyQueue(String sessionName, String queueName) throws ExecutionException, UnknownSessionException,
                                                               UnknownDestinationException, UnknownQueueException {
      log.debug("emptyQueue");

      // Get JTBConnection
      JTBConnection jtbConnection = getJTBConnection(sessionName);
      try {
         jtbConnection.connect();
      } catch (Exception e) {
         log.error("Exception when emptying queue '{}::{}'", sessionName, queueName, e);
         throw new ExecutionException(e);
      }

      JTBDestination jtbDestination = getJTBQueue(jtbConnection, queueName);

      try {
         return jtbConnection.emptyQueue(jtbDestination.getAsJTBQueue());
      } catch (JMSException e) {
         log.error("Exception when emptying queue '{}::{}'", sessionName, queueName, e);
         throw new ExecutionException(e);
      }
   }

   // ----------------------------
   // Services related to Scripts
   // ----------------------------
   public int executeScript(String scriptName, boolean simulation, int nbMessagesMax) throws Exception {
      log.debug("executeScript scriptName {} simulation? {} nbMessagesMax {}", scriptName, simulation, nbMessagesMax);
      return scriptExecutionEngine.executeScriptNoUI(scriptName, simulation, nbMessagesMax);
   }

   // ----------------------------
   // Helpers
   // ----------------------------
   private JTBConnection getJTBConnection(String sessionName) throws UnknownSessionException {
      if (sessionName == null) {
         throw new UnknownSessionException(UNSPECIFIED);
      }

      JTBSession jtbSession = cm.getJTBSessionByName(sessionName);
      if (jtbSession == null) {
         log.warn("Session '{}' does not exist", sessionName);
         throw new UnknownSessionException(sessionName);
      }
      return jtbSession.getJTBConnection(JTBSessionClientType.REST);
   }

   private JTBDestination getJTBDestination(JTBConnection jtbConnection,
                                            String destinationName) throws UnknownDestinationException {
      if (destinationName == null) {
         throw new UnknownDestinationException(UNSPECIFIED);
      }

      JTBDestination jtbDestination = jtbConnection.getJTBDestinationByName(destinationName);
      if (jtbDestination == null) {
         log.warn("Destination '{}' does not exist", destinationName);
         throw new UnknownDestinationException(destinationName);
      }
      return jtbDestination;
   }

   private JTBQueue getJTBQueue(JTBConnection jtbConnection, String queueName) throws UnknownDestinationException,
                                                                               UnknownQueueException {
      if (queueName == null) {
         throw new UnknownQueueException(UNSPECIFIED);
      }

      JTBDestination jtbDestination = getJTBDestination(jtbConnection, queueName);
      if (!(jtbDestination.isJTBQueue())) {
         log.warn("Destination '{}' is not a Queue", queueName);
         throw new UnknownQueueException(queueName);
      }
      return jtbDestination.getAsJTBQueue();
   }

   private JTBMessageTemplate getJTBMessageTemplate(String templateName) throws UnknownTemplateException, ExecutionException {
      if (templateName == null) {
         throw new UnknownTemplateException(UNSPECIFIED);
      }

      JTBMessageTemplate jtbMessageTemplate;
      try {
         jtbMessageTemplate = templatesManager.getTemplateFromNameFromSystemFolder(templateName);
         if (jtbMessageTemplate == null) {
            throw new UnknownTemplateException(templateName);
         }
         return jtbMessageTemplate;
      } catch (CoreException | JAXBException | IOException e) {
         throw new ExecutionException(e);
      }
   }
}

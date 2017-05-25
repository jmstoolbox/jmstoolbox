/*
 * Copyright (C) 2015-2017 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.dialog.MessageSendDialog;
import org.titou10.jtb.dialog.MessageTypePayloadDialog;
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.model.JTBObject;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBTopic;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.dnd.DNDData;
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.ui.navigator.NodeJTBTopic;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;
import org.titou10.jtb.variable.VariablesManager;
import org.titou10.jtb.visualizer.VisualizersManager;

/**
 * Manage the "Send Message" command
 * 
 * @author Denis Forveille
 * 
 */
public class MessageSendHandler {

   private static final Logger log = LoggerFactory.getLogger(MessageSendHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Inject
   private ConfigManager       cm;

   @Inject
   private VariablesManager    variablesManager;

   @Inject
   private VisualizersManager  visualizersManager;

   // This can be called in 3 contexts :
   // - right click on a session = QUEUE : -> use selection
   // - right click on message browser = MESSAGE : -> use tabJTBQueue
   // - drop an external file onto a Queue in the message browser or destination name: -`use DNDData.getSourceExternalFileName

   @Execute
   public void execute(Shell shell,
                       @Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional JTBObject selection,
                       @Named(Constants.CURRENT_TAB_JTBDESTINATION) @Optional JTBDestination jtbDestination) {
      log.debug("execute");

      switch (context) {
         case Constants.COMMAND_CONTEXT_PARAM_QUEUE:
            if (selection instanceof NodeJTBQueue) {
               NodeJTBQueue nodeJTBQueue = (NodeJTBQueue) selection;
               jtbDestination = (JTBQueue) nodeJTBQueue.getBusinessObject();
            } else {
               NodeJTBTopic nodeJTBTopic = (NodeJTBTopic) selection;
               jtbDestination = (JTBTopic) nodeJTBTopic.getBusinessObject();
            }
            break;

         case Constants.COMMAND_CONTEXT_PARAM_MESSAGE:
            break;

         case Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP:
            jtbDestination = DNDData.getTargetJTBDestination();
            log.debug("'Send message' initiated from Drag & Drop: {} Destination: {}", DNDData.getDrag(), jtbDestination);

            switch (DNDData.getDrag()) {

               case JTB_MESSAGES:
                  List<JTBMessage> jtbMessages = DNDData.getSourceJTBMessages();

                  try {
                     // Post Messages
                     JTBMessage newMessage;
                     for (JTBMessage jtbMessage : jtbMessages) {
                        // Create new Message to destination from old Message
                        newMessage = new JTBMessage(jtbDestination, jtbMessage.getJmsMessage());
                        jtbDestination.getJtbConnection().sendMessage(newMessage, jtbDestination);
                     }
                     // Refresh List if the destination is browsable
                     if ((jtbDestination.isJTBQueue()) && (!jtbDestination.getAsJTBQueue().isBrowsable())) {
                        return;
                     }
                     eventBroker.post(Constants.EVENT_REFRESH_QUEUE_MESSAGES, jtbDestination);
                     return;
                  } catch (JMSException e) {
                     jtbStatusReporter.showError("Problem occurred while sending the messages", e, jtbDestination.getName());
                     return;
                  }

               case TEMPLATES_FILENAMES:
                  List<String> fileNames = DNDData.getSourceTemplatesFileNames();

                  for (String fileName : fileNames) {

                     // Ask for the type of payload
                     MessageTypePayloadDialog dialog = new MessageTypePayloadDialog(shell, fileName);
                     if (dialog.open() != Window.OK) {
                        return;
                     }

                     JTBMessageType type = dialog.getJtbMessageType();
                     JTBMessageTemplate template99 = new JTBMessageTemplate();
                     try {
                        switch (type) {
                           case BYTES:
                              template99.setJtbMessageType(JTBMessageType.BYTES);
                              template99.setPayloadBytes(Files.readAllBytes(Paths.get(fileName)));
                              break;

                           default:
                              template99.setJtbMessageType(JTBMessageType.TEXT);
                              template99.setPayloadText(new String(Files.readAllBytes(Paths.get(fileName))));
                              break;
                        }
                     } catch (IOException e1) {
                        jtbStatusReporter.showError("A problem occurred while reading the source file",
                                                    e1,
                                                    jtbDestination.getName());
                        return;
                     }

                     MessageSendDialog dialog2 = new MessageSendDialog(shell,
                                                                       jtbStatusReporter,
                                                                       cm,
                                                                       variablesManager,
                                                                       visualizersManager,
                                                                       template99,
                                                                       jtbDestination);
                     if (dialog2.open() != Window.OK) {
                        return;
                     }

                     template99 = dialog2.getTemplate();

                     JTBConnection jtbConnection = jtbDestination.getJtbConnection();

                     try {

                        // Send Message
                        Message m = jtbConnection.createJMSMessage(template99.getJtbMessageType());
                        JTBMessage jtbMessage = template99.toJTBMessage(jtbDestination, m);
                        jtbDestination.getJtbConnection().sendMessage(jtbMessage);

                     } catch (JMSException e) {
                        jtbStatusReporter.showError("Problem while sending the message", e, jtbDestination.getName());
                        return;
                     }
                  }

                  // Refresh List if the destination is browsable
                  if ((jtbDestination.isJTBQueue()) && (!jtbDestination.getAsJTBQueue().isBrowsable())) {
                     return;
                  }
                  eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, jtbDestination);

                  return;

               default:
                  break;
            }

            break;

         default:
            log.error("Invalid value : {}", context);
            return;
      }

      // Create temporary template
      JTBMessageTemplate template = new JTBMessageTemplate();
      MessageSendDialog dialog = new MessageSendDialog(shell,
                                                       jtbStatusReporter,
                                                       cm,
                                                       variablesManager,
                                                       visualizersManager,
                                                       template,
                                                       jtbDestination);
      if (dialog.open() != Window.OK) {
         return;
      }

      template = dialog.getTemplate();

      JTBConnection jtbConnection = jtbDestination.getJtbConnection();

      try {

         // Send Message
         Message m = jtbConnection.createJMSMessage(template.getJtbMessageType());
         JTBMessage jtbMessage = template.toJTBMessage(jtbDestination, m);
         jtbDestination.getJtbConnection().sendMessage(jtbMessage);

         // Refresh List if the destination is browsable
         if ((jtbDestination.isJTBQueue()) && (!jtbDestination.getAsJTBQueue().isBrowsable())) {
            return;
         }
         eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, jtbDestination);

      } catch (JMSException e) {
         jtbStatusReporter.showError("Problem while sending the message", e, jtbDestination.getName());
         return;
      }
   }

   @CanExecute
   public boolean canExecute(@Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                             @Named(IServiceConstants.ACTIVE_SELECTION) @Optional JTBObject selection,
                             @Named(Constants.CURRENT_TAB_JTBDESTINATION) @Optional JTBDestination jtbDestination,
                             @Optional MMenuItem menuItem) {

      switch (context) {
         case Constants.COMMAND_CONTEXT_PARAM_QUEUE:
            // Show menu on Queues and Topics only
            if (selection instanceof NodeJTBQueue) {
               return Utils.enableMenu(menuItem);
            }
            if (selection instanceof NodeJTBTopic) {
               return Utils.enableMenu(menuItem);
            }
            return Utils.disableMenu(menuItem);

         case Constants.COMMAND_CONTEXT_PARAM_MESSAGE:
            // Always show menu
            return Utils.enableMenu(menuItem);

         case Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP:
            // Always show menu
            return Utils.enableMenu(menuItem);

         default:
            log.error("Invalid value : {}", context);
            return Utils.disableMenu(menuItem);
      }
   }
}

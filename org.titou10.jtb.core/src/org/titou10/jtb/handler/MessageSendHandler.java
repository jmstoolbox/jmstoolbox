/*
 * Copyright (C) 2015 Denis Forveille titou10.titou10@gmail.com
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
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBObject;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBTopic;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.ui.navigator.NodeJTBTopic;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Send Message" command
 * 
 * @author Denis Forveille
 * 
 */
public class MessageSendHandler {

   private static final Logger log = LoggerFactory.getLogger(MessageSendHandler.class);

   @Inject
   private IEventBroker eventBroker;

   @Inject
   private ConfigManager cm;

   @Inject
   private JTBStatusReporter jtbStatusReporter;

   // This can be called in two contexts depending on parameter "queueOrMessage":
   // - right click on a session = QUEUE : -> use selection
   // - right click on message browser = MESSAGE : -> use tabJTBQueue

   @Execute
   public void execute(Shell shell,
                       @Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional JTBObject selection,
                       @Named(Constants.CURRENT_TAB_JTBQUEUE) @Optional JTBQueue tabJTBQueue) {
      log.debug("execute");

      JTBDestination jtbDestination;

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
            jtbDestination = tabJTBQueue;
            break;

         default:
            log.error("Invalid value : {}", context);
            return;
      }

      // Create temporary template
      JTBMessageTemplate template = new JTBMessageTemplate();
      MessageSendDialog dialog = new MessageSendDialog(shell, cm, template, jtbDestination);
      if (dialog.open() != Window.OK) {
         return;
      }

      template = dialog.getTemplate();

      JTBSession jtbSession = jtbDestination.getJtbSession();

      try {

         Message m = jtbSession.createJMSMessage(template.getJtbMessageType());
         template.toJMSMessage(m);

         // Send Message
         JTBMessage jtbMessage = new JTBMessage(jtbDestination, m);
         jtbDestination.getJtbSession().sendMessage(jtbMessage);

         // Refresh List
         eventBroker.send(Constants.EVENT_REFRESH_MESSAGES, jtbDestination);

      } catch (JMSException e) {
         jtbStatusReporter.showError("Probleme while sending the message", e, jtbDestination.getName());
         return;
      }
   }

   @CanExecute
   public boolean canExecute(@Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                             @Named(IServiceConstants.ACTIVE_SELECTION) @Optional JTBObject selection,
                             @Named(Constants.CURRENT_TAB_JTBQUEUE) @Optional JTBQueue tabJTBQueue,
                             @Optional MMenuItem menuItem) {
      log.debug("canExecute context={} selection={} tabJTBQueue={}", context, selection, tabJTBQueue);

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

         default:
            log.error("Invalid value : {}", context);
            return Utils.disableMenu(menuItem);
      }
   }
}

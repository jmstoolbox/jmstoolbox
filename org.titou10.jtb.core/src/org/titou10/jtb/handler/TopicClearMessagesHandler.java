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
package org.titou10.jtb.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBObject;
import org.titou10.jtb.jms.model.JTBTopic;
import org.titou10.jtb.ui.navigator.NodeJTBTopic;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Empty Queue" dialog and command
 * 
 * @author Denis Forveille
 * 
 */
public class TopicClearMessagesHandler {

   private static final Logger log = LoggerFactory.getLogger(TopicClearMessagesHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   // This can be called in two contexts depending on parameter "queueOrMessage":
   // - right click on a session = TOPIC : -> use selection
   // - right click on message browser = MESSAGE : -> use tabJTBTopic

   @Execute
   public void execute(Shell shell,
                       @Named(Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM) String context,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional JTBObject selection,
                       @Named(Constants.CURRENT_TAB_JTBDESTINATION) @Optional JTBDestination jtbDestination) {
      log.debug("execute");

      JTBTopic jtbTopic;
      switch (context) {
         case Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM_TOPIC:
            NodeJTBTopic nodeJTBTopic = (NodeJTBTopic) selection;
            jtbTopic = (JTBTopic) nodeJTBTopic.getBusinessObject();
            break;
         case Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM_MSG:
            jtbTopic = jtbDestination.getAsJTBTopic();
            break;
         default:
            log.error("Invalid value : {}", context);
            return;
      }
      String msg = "Are you sure to clear all messages captured for topic '" + jtbTopic.getName() + "' ?";

      if (!(MessageDialog.openConfirm(shell, "Confirmation", msg))) {
         return;
      }

      // Rafraichissement de la liste
      eventBroker.send(Constants.EVENT_TOPIC_CLEAR_MESSAGES, jtbTopic);
   }

   @CanExecute
   public boolean canExecute(@Named(Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM) String context,
                             @Named(IServiceConstants.ACTIVE_SELECTION) @Optional JTBObject selection,
                             @Named(Constants.CURRENT_TAB_JTBDESTINATION) @Optional JTBDestination jtbDestination,
                             @Optional MMenuItem menuItem) {

      // log.debug("canExecute {} {}", context, selection);

      switch (context) {
         case Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM_TOPIC:
            // Show menu on Topic only
            if (selection instanceof NodeJTBTopic) {
               return Utils.enableMenu(menuItem);
            }
            if (selection instanceof List) {
               return Utils.enableMenu(menuItem);
            }

            return Utils.disableMenu(menuItem);

         case Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM_MSG:
            // Show menu on Topics Only
            if (jtbDestination instanceof JTBTopic) {
               return Utils.enableMenu(menuItem);
            } else {
               return Utils.disableMenu(menuItem);
            }

         default:
            log.error("Invalid value : {}", context);
            return Utils.disableMenu(menuItem);
      }
   }
}

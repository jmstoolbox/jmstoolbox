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

import java.util.List;

import javax.jms.JMSException;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBObject;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Empty Queue" dialog and command
 * 
 * @author Denis Forveille
 * 
 */
public class QueueEmptyHandler {

   private static final Logger log = LoggerFactory.getLogger(QueueEmptyHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   // This can be called in two contexts depending on parameter "queueOrMessage":
   // - right click on a session = QUEUE : -> use selection
   // - right click on message browser = MESSAGE : -> use tabJTBQueue

   @Execute
   public void execute(Shell shell,
                       @Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional JTBObject selection,
                       @Named(Constants.CURRENT_TAB_JTBDESTINATION) @Optional JTBDestination jtbDestination) {
      log.debug("execute");

      JTBQueue jtbQueue;
      switch (context) {
         case Constants.COMMAND_CONTEXT_PARAM_QUEUE:
            NodeJTBQueue nodeJTBQueue = (NodeJTBQueue) selection;
            jtbQueue = (JTBQueue) nodeJTBQueue.getBusinessObject();
            break;
         case Constants.COMMAND_CONTEXT_PARAM_MESSAGE:
            jtbQueue = jtbDestination.getAsJTBQueue();
            break;
         default:
            log.error("Invalid value : {}", context);
            return;
      }
      String msg = "Are you sure to remove all messages from queue '" + jtbQueue.getName() + "' ?";

      if (!(MessageDialog.openConfirm(shell, "Confirmation", msg))) {
         return;
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

         @Override
         public void run() {
            try {
               jtbQueue.getJtbConnection().emptyQueue(jtbQueue);
            } catch (JMSException e) {
               jtbStatusReporter.showError("Problem occurred while pruning the queue", e, jtbQueue.getName());
            }
         }
      });

      // Rafraichissement de la liste
      eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, jtbQueue);
   }

   @CanExecute
   public boolean canExecute(@Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                             @Named(IServiceConstants.ACTIVE_SELECTION) @Optional JTBObject selection,
                             @Named(Constants.CURRENT_TAB_JTBDESTINATION) @Optional JTBDestination jtbDestination,
                             @Optional MMenuItem menuItem) {

      switch (context) {
         case Constants.COMMAND_CONTEXT_PARAM_QUEUE:
            // Show menu on Queues that can be browsed only
            if (selection instanceof NodeJTBQueue nodeJTBQueue) {
               JTBQueue jtbQueue = (JTBQueue) nodeJTBQueue.getBusinessObject();
               if (jtbQueue.isBrowsable()) {
                  return Utils.enableMenu(menuItem);
               } else {
                  return Utils.disableMenu(menuItem);
               }
            }
            if (selection instanceof List) {
               return Utils.enableMenu(menuItem);
            }

            return Utils.disableMenu(menuItem);

         case Constants.COMMAND_CONTEXT_PARAM_MESSAGE:
            // Show menu on Queues that can be browsed only
            if (jtbDestination.isJTBQueue()) {
               if (jtbDestination.getAsJTBQueue().isBrowsable()) {
                  return Utils.enableMenu(menuItem);
               }
            }
            return Utils.disableMenu(menuItem);

         default:
            log.error("Invalid value : {}", context);
            return Utils.disableMenu(menuItem);
      }
   }
}

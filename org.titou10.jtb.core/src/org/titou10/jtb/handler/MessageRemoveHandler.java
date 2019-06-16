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

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;

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
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Remove Message" dialog and command
 * 
 * @author Denis Forveille
 * 
 */
public class MessageRemoveHandler {

   private static final Logger log = LoggerFactory.getLogger(MessageRemoveHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<JTBMessage> selection) {
      log.debug("execute");

      String msg;

      try {
         JTBMessage jtbMessage1 = selection.get(0);

         // Confirmation Dialog
         if (Utils.containsOneElement(selection)) {
            msg = "Please confirm the removing of message with id : \n" + jtbMessage1.getJmsMessage().getJMSMessageID();
         } else {
            msg = "Are you sure to remove those " + selection.size() + " messages?";
         }
         if (!(MessageDialog.openConfirm(shell, "Confirmation", msg))) {
            return;
         }

         // All messages are from the same Queue...
         JTBDestination jtbDestination = jtbMessage1.getJtbDestination();

         // Remove Messages
         BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            @Override
            public void run() {
               try {
                  for (JTBMessage jtbMessage : selection) {
                     jtbDestination.getJtbConnection().removeMessage(jtbMessage);
                  }
               } catch (JMSException e) {
                  jtbStatusReporter.showError("Exception occurred when removing messages", e, "");
               }
            }
         });

         // Refresh List of Message
         eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, jtbDestination);

      } catch (JMSException e) {
         jtbStatusReporter.showError("Exception occurred when removing messages", e, "");
         return;
      }
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<JTBMessage> selection,
                             @Named(Constants.CURRENT_TAB_JTBDESTINATION) JTBDestination jtbDestination,
                             @Optional MMenuItem menuItem) {

      if (Utils.isNullorEmpty(selection)) {
         return Utils.disableMenu(menuItem);
      }

      // Removing a message is only valid on JTBQueue
      if (!(jtbDestination.isJTBQueue())) {
         return Utils.disableMenu(menuItem);
      }

      // DF Strange but in some scenarios, selection may not be a list of JTBMessage
      // drag and drop a template on the table viewer, send message then right clic on the empty part of the viewer
      // selection is a List<IFile>...
      // Sound like a bug in eclipse 4.4.2
      if (!(selection.get(0) instanceof JTBMessage)) {
         return Utils.disableMenu(menuItem);
      }

      JTBMessage selected = selection.get(0);

      // Enable menu only if the selected messages has a JMSMessageID
      try {
         if (Utils.isEmpty(selected.getJmsMessage().getJMSMessageID())) {
            return Utils.disableMenu(menuItem);
         }
      } catch (JMSException e) {
         // DF: Impossible...
         log.error("Exception occurred when testing JMSMessageId to enable remove message menu !", e);
      }

      // Enable menu only if the selected messages are from the active tab
      if (selected.getJtbDestination().getName().equals(jtbDestination.getName())) {
         return Utils.enableMenu(menuItem);
      }

      return Utils.disableMenu(menuItem);
   }
}

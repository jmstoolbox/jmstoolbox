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

import java.util.List;

import javax.inject.Named;
import javax.jms.JMSException;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.dialog.MessageEditDialog;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "View Message" dialog and command
 * 
 * @author Denis Forveille
 * 
 */
@SuppressWarnings("restriction")
public class MessageViewHandler {

   private static final Logger log = LoggerFactory.getLogger(MessageViewHandler.class);

   @Execute
   public void execute(Shell shell,
                       ECommandService commandService,
                       EHandlerService handlerService,
                       IEventBroker eventBroker,
                       ConfigManager cm,
                       JTBStatusReporter jtbStatusReporter,
                       @Named(IServiceConstants.ACTIVE_SELECTION) List<JTBMessage> selection) {
      log.debug("execute. Message = {}", selection);

      // Can only be called with one message selected...
      JTBMessage jtbMessage = selection.get(0);
      JTBDestination jtbDestination = jtbMessage.getJtbDestination();

      try {
         MessageEditDialog d = new MessageEditDialog(shell, cm, jtbMessage);
         int res = d.open();
         switch (res) {
            case MessageEditDialog.BUTTON_SAVE_TEMPLATE:
               log.debug("Save as template pressed");

               try {
                  boolean res2 = Utils.createNewTemplate(shell,
                                                         d.getTemplate(),
                                                         cm.getTemplateFolder(),
                                                         cm.getTemplateFolder(),
                                                         jtbDestination.getName());
                  if (res2) {
                     eventBroker.post(Constants.EVENT_TEMPLATES, null); // Refresh Template Browser asynchronously
                  }
               } catch (Exception e) {
                  jtbStatusReporter.showError("An error occurred when saving template", e, "");
               }
               return;

            case MessageEditDialog.BUTTON_REMOVE:
               log.debug("Remove pressed");

               // Call the "Message Remove" Command
               ParameterizedCommand myCommand3 = commandService.createCommand(Constants.COMMAND_MESSAGE_REMOVE, null);
               handlerService.executeHandler(myCommand3);
               return;
            default:
               return;
         }
      } catch (JMSException e) {
         jtbStatusReporter.showError("Problem while editing Message", e, jtbDestination.getName());
         return;
      }
   }

   @CanExecute
   public boolean canExecute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<JTBMessage> selection,
                             @Named(Constants.CURRENT_TAB_JTBQUEUE) JTBQueue tabJTBQueue,
                             @Optional MMenuItem menuItem) {
      log.debug("canExecute={}", selection);

      if ((selection == null) || (selection.size() != 1)) {
         return Utils.disableMenu(menuItem);
      }

      // !!! It happens that selection items are not JTBMessage...at least in v4.4.2
      // To reproduce
      // drag and drop a message from templates to queue
      // then right click in queue area ->fail
      if (!(selection.get(0) instanceof JTBMessage)) {
         return Utils.disableMenu(menuItem);
      }

      // Enable menu only if the selected message is from the active tab
      JTBMessage selected = selection.get(0);
      if (selected.getJtbDestination().getName().equals(tabJTBQueue.getName())) {
         return Utils.enableMenu(menuItem);
      }

      return Utils.disableMenu(menuItem);

   }
}

/*
 * Copyright (C) 2018 Denis Forveille titou10.titou10@gmail.com
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

import jakarta.inject.Inject;
import jakarta.inject.Named;

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
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBTopic;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Topic Remove MEssage" command
 * 
 * @author Denis Forveille
 * 
 */
public class TopicRemoveMessagesHandler {

   private static final Logger log = LoggerFactory.getLogger(TopicRemoveMessagesHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Execute
   public void execute(Shell shell,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<JTBMessage> selection,
                       @Named(Constants.CURRENT_TAB_JTBDESTINATION) @Optional JTBDestination jtbDestination) {
      log.debug("execute");

      String msg;
      if (selection.size() == 1) {
         msg = "Are you sure to remove this captured messages from topic '" + jtbDestination.getName() + "' ?";
      } else {
         msg = "Are you sure to remove those captured messages from topic '" + jtbDestination.getName() + "' ?";
      }

      if (!(MessageDialog.openConfirm(shell, "Confirmation", msg))) {
         return;
      }

      // Rafraichissement de la liste
      eventBroker.send(Constants.EVENT_TOPIC_REMOVE_MESSAGES, selection);
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<JTBMessage> selection,
                             @Named(Constants.CURRENT_TAB_JTBDESTINATION) @Optional JTBDestination jtbDestination,
                             @Optional MMenuItem menuItem) {

      // Show menu on Topics Only
      if (jtbDestination instanceof JTBTopic) {
         if (Utils.isNotEmpty(selection)) {
            return Utils.enableMenu(menuItem);
         }
      }

      return Utils.disableMenu(menuItem);
   }
}

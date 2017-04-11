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
import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.TextMessage;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Message Export Payload" command
 * 
 * @author Denis Forveille
 * 
 */
public class MessageExportPayloadHandler {

   private static final Logger log = LoggerFactory.getLogger(MessageExportPayloadHandler.class);

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<JTBMessage> selection) {
      log.debug("execute");

      // Show the "save as" dialog
      try {
         for (JTBMessage jtbMessage : selection) {
            switch (jtbMessage.getJtbMessageType()) {
               case TEXT:
                  Utils.writePayloadToOS((TextMessage) jtbMessage.getJmsMessage(), shell);
                  break;

               case BYTES:
                  Utils.writePayloadToOS((BytesMessage) jtbMessage.getJmsMessage(), shell);
                  break;

               case MAP:
                  Utils.writePayloadToOS((MapMessage) jtbMessage.getJmsMessage(), shell);
                  break;

               default:
                  // No export for other types of messages
                  return;
            }
         }
      } catch (Exception e) {
         jtbStatusReporter.showError("An error occurred when exporting the payload", e, "");
         return;
      }
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<JTBMessage> selection,
                             @Named(Constants.CURRENT_TAB_JTBDESTINATION) JTBDestination jtbDestination,
                             @Optional MMenuItem menuItem) {

      // Show the menu only if some message is selected
      if (Utils.isNullorEmpty(selection)) {
         return Utils.disableMenu(menuItem);
      }

      // Enable menu only if the selected message is from the active tab
      JTBMessage selected = selection.get(0);
      if (selected.getJtbDestination().getName().equals(jtbDestination.getName())) {

         // Enable menu only for TEXT, BYTES and MAP Messages
         switch (selected.getJtbMessageType()) {
            case TEXT:
            case BYTES:
            case MAP:
               return Utils.enableMenu(menuItem);
            default:
               return Utils.disableMenu(menuItem);
         }
      }

      return Utils.disableMenu(menuItem);
   }
}

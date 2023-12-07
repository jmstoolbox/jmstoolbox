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
package org.titou10.jtb.handler;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Message Export as Template in Batch" command
 * 
 * @author Denis Forveille
 * 
 */
public class MessageExportTemplateBatchHandler {

   private static final Logger log = LoggerFactory.getLogger(MessageExportTemplateBatchHandler.class);

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Inject
   private TemplatesManager    templatesManager;

   @Execute
   public void execute(Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<JTBMessage> selection) {
      log.debug("execute");

      try {
         Integer nb = templatesManager.writeTemplateInBatchToOS(shell, selection);
         if (nb != null) {
            MessageDialog.openInformation(shell, "Success", nb + " messages successfully exported as templates");
         }
      } catch (Exception e) {
         jtbStatusReporter.showError("An error occurred when exporting the template", e, "");
         return;
      }
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<JTBMessage> selection,
                             @Named(Constants.CURRENT_TAB_JTBDESTINATION) JTBDestination jtbDestination,
                             @Optional MMenuItem menuItem) {

      // Show the menu only if more than one message is selected
      if (Utils.isNullorEmpty(selection) || (selection.size() <= 1)) {
         return Utils.disableMenu(menuItem);
      }

      // Enable menu only if the selected message is from the active tab
      JTBMessage firstJtbMessage = selection.get(0);
      if (firstJtbMessage.getJtbDestination().getName().equals(jtbDestination.getName())) {

         // All selected messages must be of type TEXT, BYTES or MAP
         for (JTBMessage jtbMessage : selection) {
            switch (jtbMessage.getJtbMessageType()) {
               case TEXT:
               case BYTES:
               case MAP:
                  continue;
               default:
                  return Utils.disableMenu(menuItem);
            }
         }

         return Utils.enableMenu(menuItem);
      }

      return Utils.disableMenu(menuItem);
   }
}

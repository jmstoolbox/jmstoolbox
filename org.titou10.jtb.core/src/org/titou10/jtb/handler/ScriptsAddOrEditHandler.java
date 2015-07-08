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

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.dialog.ScriptsAddOrEditDialog;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Add or Edit Scripts" command
 * 
 * @author Denis Forveille
 * 
 */
public class ScriptsAddOrEditHandler {

   private static final Logger log = LoggerFactory.getLogger(ScriptsAddOrEditHandler.class);

   @Execute
   public void execute(Shell shell,
                       IEventBroker eventBroker,
                       JTBStatusReporter jtbStatusReporter,
                       ConfigManager cm,
                       @Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<Object> selection,
                       @Named(Constants.COMMAND_SCRIPTS_ADDEDIT_PARAM) String mode) {
      log.debug("execute. script={} mode={}", selection, mode);

      if (mode == null) {
         return;
      }

      // Script selected? folder selected? nothing selected?
      Script script;
      if ((selection == null) || (selection.isEmpty())) {
         // No Selection
         script = new Script();
      } else {
         if (selection.get(0) instanceof Directory) {
            // Directory selected. create new script and set parent
            script = new Script();
            script.setParent((Directory) selection.get(0));
         } else {
            // Edit script
            script = (Script) selection.get(0);
         }
      }

      ScriptsAddOrEditDialog dialog = new ScriptsAddOrEditDialog(shell, cm, script);
      int res = dialog.open();
      switch (res) {
         case IDialogConstants.OK_ID:
            log.debug("Save Script pressed");
            try {
               cm.writeScriptFile();
               // Refresh Script Browser asynchronously
               eventBroker.post(Constants.EVENT_TEMPLATES, null);
            } catch (Exception e) {
               jtbStatusReporter.showError("Problem while saving Script", e, script.getName());
               return;
            }
            return;

         case ScriptsAddOrEditDialog.BUTTON_EXECUTE_SCRIPT:
            log.debug("Execute Script pressed");
            return;

         default:
            return;
      }

   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<Object> selection,
                             @Named(Constants.COMMAND_SCRIPTS_ADDEDIT_PARAM) String mode,
                             @Optional MMenuItem menuItem,
                             MApplication app,
                             EModelService modelService,
                             EPartService partService) {
      log.debug("canExecute={} mode={}", selection, mode);

      switch (mode) {
         case Constants.COMMAND_SCRIPTS_ADDEDIT_ADD:
            return Utils.enableMenu(menuItem);

         case Constants.COMMAND_SCRIPTS_ADDEDIT_EDIT:

            // Show only if maximum one script is selected
            if ((selection != null) && (selection.size() == 1)) {
               if (selection.get(0) instanceof Script) {
                  return Utils.enableMenu(menuItem);
               }
            }
            break;
      }

      return Utils.disableMenu(menuItem);
   }

}

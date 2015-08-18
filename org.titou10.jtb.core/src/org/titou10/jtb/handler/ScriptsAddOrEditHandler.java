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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.dialog.ScriptNewDialog;
import org.titou10.jtb.script.ScriptsUtils;
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

   @Inject
   private EPartService partService;

   @Inject
   private IEventBroker eventBroker;

   @Inject
   private EModelService modelService;

   @Inject
   private ConfigManager cm;

   @Inject
   private JTBStatusReporter jtbStatusReporter;

   @Execute
   public void execute(Shell shell,
                       MApplication app,
                       MWindow window,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<Object> selection,
                       @Named(Constants.COMMAND_SCRIPTS_ADDEDIT_PARAM) String mode) {
      log.debug("execute. script={} mode={}", selection, mode);

      if (mode == null) {
         return;
      }

      // Selected object
      Object sel = null;
      if ((selection != null) && (!(selection.isEmpty()))) {
         sel = selection.get(0);
      }

      Script script;
      Directory selectedDirectory = cm.getScripts().getDirectory().get(0); // Top parent by default

      switch (mode) {
         case Constants.COMMAND_SCRIPTS_ADDEDIT_ADD:

            script = new Script();

            // Determine directory from where the "New Script" command has been activated, if any
            if (sel instanceof Directory) {
               selectedDirectory = (Directory) sel;
            } else {
               if (sel instanceof Script) {
                  selectedDirectory = ((Script) sel).getParent();
               }
            }

            // Ask for location and name
            ScriptNewDialog dialogSave = new ScriptNewDialog(shell, cm.getScripts(), selectedDirectory);
            if (dialogSave.open() != Window.OK) {
               return;
            }
            script.setName(dialogSave.getSelectedScriptName());
            script.setParent(dialogSave.getSelectedDirectory());

            // Add the new script to the Configuration
            dialogSave.getSelectedDirectory().getScript().add(script);

            // Write file with scripts
            try {
               cm.writeScriptFile();
            } catch (Exception e) {
               jtbStatusReporter.showError("Problem while saving Script", e, script.getName());
               return;
            }

            // Refresh Script Browser
            eventBroker.post(Constants.EVENT_REFRESH_SCRIPTS_BROWSER, null);

            break;

         case Constants.COMMAND_SCRIPTS_ADDEDIT_EDIT:

            script = (Script) sel;
            selectedDirectory = (Directory) script.getParent();
            break;

         default:
            throw new IllegalStateException("Impossible");
      }

      // Reuse or create a part per Script
      String scriptFullName = ScriptsUtils.getFullNameDots(script);

      String partName = Constants.PART_SCRIPT_PREFIX + scriptFullName;
      MPart part = (MPart) modelService.find(partName, app);
      if (part == null) {

         // First clone current script, in order to not directly work on the script...
         Script workingScript = ScriptsUtils.cloneScript(script, script.getName(), script.getParent());

         // Save Selected Script in Window Context
         window.getContext().set(Constants.CURRENT_WORKING_SCRIPT, workingScript);

         // Create part from Part Descriptor
         part = partService.createPart(Constants.PARTDESCRITOR_SCRIPT);
         part.setLabel(ScriptsUtils.getFullName(workingScript));
         part.setElementId(partName);

         MPartStack stack = (MPartStack) modelService.find(Constants.PARTSTACK_SCRIPTT, app);
         stack.getChildren().add(part);
      } else {
         log.debug("{} already exist", partName);
      }

      // Show Part
      partService.showPart(part, PartState.CREATE);
      partService.activate(part, true);
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<Object> selection,
                             @Named(Constants.COMMAND_SCRIPTS_ADDEDIT_PARAM) String mode,
                             @Optional MMenuItem menuItem) {
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

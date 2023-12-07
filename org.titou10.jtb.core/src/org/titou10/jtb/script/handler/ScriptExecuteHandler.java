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
package org.titou10.jtb.script.handler;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.script.ScriptExecutionEngine;
import org.titou10.jtb.script.dialog.ScriptExecutionConfirmationDialog;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.util.Constants;

/**
 * Manage the "Script Execute" command
 * 
 * @author Denis Forveille
 * 
 */
public class ScriptExecuteHandler {

   private static final Logger   log = LoggerFactory.getLogger(ScriptExecuteHandler.class);

   @Inject
   private ScriptExecutionEngine scriptExecutionEngine;

   @Execute
   public void execute(Shell parentShell, MWindow window, @Named(Constants.COMMAND_SCRIPT_EXECUTE_PARAM) String mode) {
      log.debug("execute. mode={}", mode);

      Script script = (Script) window.getContext().get(Constants.CURRENT_WORKING_SCRIPT);

      boolean simulation;
      switch (mode) {
         case Constants.COMMAND_SCRIPT_EXECUTE_EXECUTE:
            simulation = false;
            break;

         case Constants.COMMAND_SCRIPT_EXECUTE_SIMULATE:
            simulation = true;
            break;

         default:
            return;
      }

      // Confirmation
      ScriptExecutionConfirmationDialog dialog = new ScriptExecutionConfirmationDialog(parentShell, simulation);
      if (dialog.open() != Window.OK) {
         return;
      }

      scriptExecutionEngine.executeScript(script, simulation, dialog.isDoShowPostLogs(), dialog.getMaxMessages());

   }

   @CanExecute
   public boolean canExecute(MWindow window) {

      // Display the "Execute" buttons only if a Script is Selected, either "new" or "old"
      Script script = (Script) window.getContext().get(Constants.CURRENT_WORKING_SCRIPT);
      if (script == null) {
         return false;
      } else {
         return true;
      }
   }

}

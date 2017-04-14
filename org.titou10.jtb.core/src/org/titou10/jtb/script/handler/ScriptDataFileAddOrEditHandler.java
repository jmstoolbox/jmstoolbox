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
package org.titou10.jtb.script.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.script.ScriptsUtils;
import org.titou10.jtb.script.dialog.ScriptNewDataFileDialog;
import org.titou10.jtb.script.gen.DataFile;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.util.Constants;

/**
 * Manage the "Script New Data File" command
 * 
 * @author Denis Forveille
 * 
 */
public class ScriptDataFileAddOrEditHandler {

   private static final Logger log = LoggerFactory.getLogger(ScriptDataFileAddOrEditHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Execute
   public void execute(Shell shell,
                       MWindow window,
                       @Named(IServiceConstants.ACTIVE_PART) MPart part,
                       @Named(Constants.COMMAND_SCRIPT_NEWDF_PARAM) String mode,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional DataFile selection) {
      log.debug("execute. mode={}", mode);

      Script script = (Script) window.getContext().get(Constants.CURRENT_WORKING_SCRIPT);

      DataFile dataFile = null;
      switch (mode) {
         case Constants.COMMAND_SCRIPT_NEWDF_EDIT:
            dataFile = ScriptsUtils.cloneDataFile(selection);
            break;

         case Constants.COMMAND_SCRIPT_NEWDF_ADD:
            dataFile = new DataFile();
            dataFile.setDelimiter(",");
            dataFile.setScriptLevel(false);
            break;

         default:
            throw new IllegalArgumentException(mode + " value is invalid");

      }

      ScriptNewDataFileDialog d1 = new ScriptNewDataFileDialog(shell, dataFile, script, selection);
      if (d1.open() != Window.OK) {
         return;
      }
      dataFile = d1.getDataFile();

      switch (mode) {
         case Constants.COMMAND_SCRIPT_NEWDF_EDIT:
            int index = script.getDataFile().indexOf(selection);
            if (index > -1) {
               script.getDataFile().set(index, dataFile);
            } else {
               script.getDataFile().add(dataFile);
            }
            break;

         case Constants.COMMAND_SCRIPT_NEWDF_ADD:
            script.getDataFile().add(dataFile);
            break;

         default:
            // Impossible
            break;
      }

      // Indicate that script is dirty
      part.setDirty(true);

      // Refresh Script Browser
      eventBroker.post(Constants.EVENT_REFRESH_SCRIPT_EDIT, "X");

   }

   @CanExecute
   public boolean canExecute(MWindow window) {

      // Display the "Add Data File" menus/buttons only if a Script is Selected
      Script script = (Script) window.getContext().get(Constants.CURRENT_WORKING_SCRIPT);
      if (script == null) {
         return false;
      } else {
         return true;
      }
   }
}

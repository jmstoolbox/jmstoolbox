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

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;

/**
 * Manage the "Export Scripts" command
 * 
 * @author Denis Forveille
 * 
 */
public class ScriptsExportHandler {

   private static final Logger log = LoggerFactory.getLogger(ScriptsExportHandler.class);

   @Execute
   public void execute(Shell shell, ConfigManager cm, JTBStatusReporter jtbStatusReporter) {
      log.debug("execute.");

      FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
      fileDialog.setText("Select a Scripts File to export");
      fileDialog.setFileName(Constants.JTB_SCRIPT_FILE_NAME);
      fileDialog.setFilterExtensions(new String[] { Constants.SCRIPT_FILE_EXTENSION_FILTER });
      fileDialog.setOverwrite(true);

      String scriptsFileName = fileDialog.open();
      if (scriptsFileName == null) {
         return;
      }

      try {
         cm.exportScripts(scriptsFileName);
         MessageDialog.openInformation(shell, "Export successful", "The Scripts file has been successfully exported.");
      } catch (IOException | CoreException e) {
         jtbStatusReporter.showError("A problem occurred when exporting the Scripts file", e, "");
         return;
      }
   }
}

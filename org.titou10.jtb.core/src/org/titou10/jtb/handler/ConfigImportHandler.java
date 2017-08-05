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

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.IWorkbench;
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
 * Manage the "Import Configuration" command
 * 
 * @author Denis Forveille
 * 
 */
public class ConfigImportHandler {

   private static final Logger log = LoggerFactory.getLogger(ConfigImportHandler.class);

   @Inject
   private ConfigManager       cm;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell, IWorkbench workbench) {
      log.debug("execute.");

      FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
      fileDialog.setText("Select the Configuration File to import");
      fileDialog.setFileName(Constants.JTB_CONFIG_FILE_NAME);
      fileDialog.setFilterExtensions(new String[] { Constants.JTB_CONFIG_FILE_EXTENSION });

      String configFileName = fileDialog.open();
      if (configFileName == null) {
         return;
      }

      try {
         boolean res = cm.configurationImport(configFileName);
         if (res) {
            MessageDialog.openInformation(shell,
                                          "Restart Warning",
                                          "The configuration has been successfully imported. \nThe application will now restart.");
            workbench.restart();
         }
      } catch (Exception e) {
         jtbStatusReporter.showError("A problem occurred when importing the configuration file", e, "");
         return;
      }
   }
}

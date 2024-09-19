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
package org.titou10.jtb.ie.handler;

import java.io.IOException;
import java.util.EnumSet;

import jakarta.xml.bind.JAXBException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.ie.ImportExportType;
import org.titou10.jtb.ie.dialog.ConfigImportDialog;
import org.titou10.jtb.ui.JTBStatusReporter;

import jakarta.inject.Inject;

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
      log.debug("execute");

      ConfigImportDialog dialog = new ConfigImportDialog(shell);
      if (dialog.open() != Window.OK) {
         return;
      }

      EnumSet<ImportExportType> importTypes = dialog.getImportTypes();
      String zipFileName = dialog.getFileName();

      try {

         Boolean restartRequired = cm.importConfig(importTypes, zipFileName);

         if (restartRequired == null) {
            MessageDialog
                     .openWarning(shell,
                                  "Nothing done !",
                                  "Nothing has been imported has the zip file does not contain any JMSToolBox configuration files");
            return;
         }

         if (restartRequired) {
            MessageDialog.openInformation(shell,
                                          "Restart Warning",
                                          "The configuration has been successfully imported. \nThe application will now restart.");
            workbench.restart();
         } else {
            MessageDialog.openInformation(shell, "Import succesful", "The configuration has been successfully imported.");
         }

      } catch (IOException | CoreException | JAXBException e) {
         jtbStatusReporter.showError("A problem occurred when importing the configuration", e, "");
         return;
      }
   }
}

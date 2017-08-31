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
package org.titou10.jtb.cs.handler;

import java.io.IOException;

import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.cs.ColumnsSetsManager;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;

/**
 * Manage the "Export Columns Sets" command
 * 
 * @author Denis Forveille
 * 
 */
public class ColumnsSetsExportHandler {
   private static final Logger log = LoggerFactory.getLogger(ColumnsSetsExportHandler.class);

   @Inject
   private ColumnsSetsManager  csManager;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell) {
      log.debug("execute.");

      FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
      fileDialog.setText("Select a file to export columns sets");
      fileDialog.setFileName(Constants.JTB_COLUMNSSETS_CONFIG_FILE_NAME);
      fileDialog.setFilterExtensions(new String[] { Constants.JTB_COLUMNSSETS_CONFIG_FILE_EXTENSION });
      fileDialog.setOverwrite(true);

      String columnsSetsFileName = fileDialog.open();
      if (columnsSetsFileName == null) {
         return;
      }

      try {
         csManager.exportConfig(columnsSetsFileName);
         MessageDialog.openInformation(shell, "Export successful", "The columns sets file has been successfully exported.");
      } catch (IOException | CoreException e) {
         jtbStatusReporter.showError("A problem occurred when exporting the columns sets file", e, "");
         return;
      }
   }

}

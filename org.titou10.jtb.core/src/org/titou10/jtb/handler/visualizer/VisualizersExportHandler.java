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
package org.titou10.jtb.handler.visualizer;

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
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.visualizer.VisualizersManager;

/**
 * Manage the "Export Visualizers" command
 * 
 * @author Denis Forveille
 * 
 */
public class VisualizersExportHandler {

   private static final Logger log = LoggerFactory.getLogger(VisualizersExportHandler.class);

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Inject
   private VisualizersManager  visualizersManager;

   @Execute
   public void execute(Shell shell) {
      log.debug("execute.");

      FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
      fileDialog.setText("Select a file to export visualizers");
      fileDialog.setFileName(Constants.JTB_VISUALIZER_FILE_NAME);
      fileDialog.setFilterExtensions(new String[] { Constants.VISUALIZER_FILE_EXTENSION_FILTER });
      fileDialog.setOverwrite(true);

      String visualizerFileName = fileDialog.open();
      if (visualizerFileName == null) {
         return;
      }

      try {
         visualizersManager.exportVisualizer(visualizerFileName);
         MessageDialog.openInformation(shell, "Export successful", "The visualizer file has been successfully exported.");
      } catch (IOException | CoreException e) {
         jtbStatusReporter.showError("A problem occurred when exporting the visualizer file", e, "");
         return;
      }
   }
}

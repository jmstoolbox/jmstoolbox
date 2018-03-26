/*

 * Copyright (C) 2018 Denis Forveille titou10.titou10@gmail.com
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;

/**
 * Manage the "Export log files" command
 * 
 * @author Denis Forveille
 * 
 */
public class LogExportHandler {

   private static final Logger log = LoggerFactory.getLogger(LogExportHandler.class);

   @Inject
   private ConfigManager       cm;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell) {
      log.debug("execute");

      String now = new SimpleDateFormat("YYYYmmdd_HHMMSS").format(new Date());

      // -------------------
      // JMSToolBox log file
      // -------------------

      IPath jtbLogFileName = cm.getJtbProject().getLocation().append(Constants.JTB_LOG_FILE_NAME);

      FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
      fileDialog.setText("Specify a name for the JMSToolBox log file");
      fileDialog.setFileName("JTB_" + now + "_" + Constants.JTB_LOG_FILE_NAME);
      fileDialog.setOverwrite(true);

      if (fileDialog.open() == null) {
         return;
      }

      // Build file name
      StringBuffer sb = new StringBuffer(256);
      sb.append(fileDialog.getFilterPath());
      sb.append(File.separator);
      sb.append(fileDialog.getFileName());
      String choosenFileName = sb.toString();
      log.debug("choosenFileName={}", choosenFileName);

      java.nio.file.Path destPath = Paths.get(choosenFileName);

      try {
         Files.copy(Paths.get(jtbLogFileName.toOSString()), destPath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
         jtbStatusReporter.showError("A problem occurred when exporting the JMSToolBox log file", e, "");
         return;
      }

      // -------------------
      // eclipse log file
      // -------------------

      IPath eclipseLogFilePath = Platform.getLogFileLocation();

      fileDialog = new FileDialog(shell, SWT.SAVE);
      fileDialog.setText("Specify a name for the eclipse log file");
      fileDialog.setFileName("JTB_" + now + "_eclipse.log");
      fileDialog.setOverwrite(true);

      if (fileDialog.open() == null) {
         return;
      }

      // Build file name
      sb.setLength(0);
      sb.append(fileDialog.getFilterPath());
      sb.append(File.separator);
      sb.append(fileDialog.getFileName());
      choosenFileName = sb.toString();
      log.debug("choosenFileName={}", choosenFileName);

      destPath = Paths.get(choosenFileName);

      try {
         Files.copy(Paths.get(eclipseLogFilePath.toOSString()), destPath, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
         jtbStatusReporter.showError("A problem occurred when exporting the eclipse log file", e, "");
         return;
      }

   }
}

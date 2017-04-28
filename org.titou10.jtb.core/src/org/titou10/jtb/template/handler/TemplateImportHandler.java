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
package org.titou10.jtb.template.handler;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;

import javax.inject.Inject;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.template.TemplatesUtils;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;

/**
 * Manage the "Import Template" command
 * 
 * @author Denis Forveille
 * 
 */
public class TemplateImportHandler {

   private static final Logger log = LoggerFactory.getLogger(TemplateImportHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private ConfigManager       cm;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell) {
      log.debug("execute.");

      // TODO Offer option to manage replace/overwrite option
      // TODO Offer possibility to select individual templates
      // TODO Offer possibility to select destination folder under "Templates"
      // TODO Verify that the imported files are real templates

      StringBuilder sb = new StringBuilder(512);
      sb.append("!!! WARNING !!!");
      sb.append("\n");
      sb.append("In this version, the whole content of the selected folder will be imported at the root of the \"Templates\" folder");
      sb.append("\n");
      sb.append("An error will occur if a Template with the same name already exist at destination");

      DirectoryDialog dirDialog = new DirectoryDialog(shell, SWT.OPEN);
      dirDialog.setText("Select a source folder for the Templates to import");
      dirDialog.setMessage(sb.toString());

      String sourceFolderName = dirDialog.open();
      if (sourceFolderName == null) {
         return;
      }

      try {
         IFolder templatesFolder = cm.getTemplateFolder();
         Path templatesFolderPath = templatesFolder.getRawLocation().toFile().toPath();

         TemplatesUtils.importTemplates(templatesFolderPath, sourceFolderName);

         MessageDialog.openInformation(shell,
                                       "Import successful",
                                       "The templates have successfully been imported from " + sourceFolderName);

         // Refresh file system
         templatesFolder.refreshLocal(IResource.DEPTH_INFINITE, null);

         // Refresh Template Browser asynchronously
         eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);

      } catch (FileAlreadyExistsException e) {
         MessageDialog.openError(shell, "Import unsuccessful", "A file with name '" + e.getMessage() + "' already exists");
         return;
      } catch (Exception e) {
         jtbStatusReporter.showError("A problem occurred when importing the templates", e, "");
         return;
      }
   }

}

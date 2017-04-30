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

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.template.TemplatesUtils;
import org.titou10.jtb.template.dialog.TemplateChooserDialog;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Export Templates" command
 * 
 * @author Denis Forveille
 * 
 */
public class TemplateExportHandler {

   private static final Logger log = LoggerFactory.getLogger(TemplateExportHandler.class);

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Inject
   private ConfigManager       cm;

   @Execute
   public void execute(Shell shell) {
      log.debug("execute.");

      // Choose templates to export
      List<IResource> selectedTemplates = chooseTemplatesToExport(shell, jtbStatusReporter, cm);
      if (Utils.isNullorEmpty(selectedTemplates)) {
         return;
      }

      // Eliminate "duplicates", ie selected item where parent is also selected
      List<IResource> templatesToExport = new ArrayList<>(selectedTemplates.size());
      for (IResource iResource : selectedTemplates) {
         boolean add = true;
         for (IResource parent : selectedTemplates) {
            if (iResource == parent) {
               continue;
            }
            if (iResource.getFullPath().toString().startsWith(parent.getFullPath().toString())) {
               add = false;
               break;
            }
         }
         if (add) {
            templatesToExport.add(iResource);
         }
      }

      // TODO Offer option to manage replace/overwrite option

      StringBuilder sb = new StringBuilder(512);
      sb.append("!!! WARNING !!!");
      sb.append("\n");
      sb.append("An error will occur if a file with the same name already exist at destination");

      DirectoryDialog dirDialog = new DirectoryDialog(shell, SWT.OPEN);
      dirDialog.setText("Select a destination folder for the Templates");
      dirDialog.setMessage(sb.toString());

      String targetFolderName = dirDialog.open();
      if (targetFolderName == null) {
         return;
      }

      try {
         TemplatesUtils.exportTemplates(templatesToExport, targetFolderName);

         MessageDialog.openInformation(shell,
                                       "Export successful",
                                       "The templates have successfully been exported to " + targetFolderName);
      } catch (FileAlreadyExistsException e) {
         MessageDialog.openError(shell,
                                 "Export unsuccessful",
                                 "A file with name '" + e.getMessage() + "' already exists in destination folder");
         return;
      } catch (IOException e) {
         jtbStatusReporter.showError("A problem occurred when exporting the templates", e, "");
         return;
      }
   }

   private List<IResource> chooseTemplatesToExport(Shell shell, JTBStatusReporter jtbStatusReporter, ConfigManager cm) {

      // First Show a list of templates
      IResource[] files;
      try {
         files = cm.getTemplateFolder().members();
      } catch (CoreException e) {
         jtbStatusReporter.showError("Problem occurred while reading the template folder", e, "");
         return null;
      }

      Arrays.sort(files, (IResource o1, IResource o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

      TemplateChooserDialog dialog1 = new TemplateChooserDialog(shell, true, cm.getTemplateFolder());
      if (dialog1.open() != Window.OK) {
         return null;
      }

      return dialog1.getSelectedResources();
   }

}

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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Delete/Rename/Duplicate Template" command
 * 
 * @author Denis Forveille
 * 
 */
public class TemplateRenameDuplicateDeleteHandler {

   private static final Logger log = LoggerFactory.getLogger(TemplateRenameDuplicateDeleteHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Inject
   private TemplatesManager    templatesManager;

   @Execute
   public void execute(Shell shell,

                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<IFileStore> selection,
                       @Named(Constants.COMMAND_TEMPLATE_RDD_PARAM) String mode) {
      log.debug("execute.  mode={}", mode);

      switch (mode) {
         case Constants.COMMAND_TEMPLATE_RDD_DUPLICATE:
            // Available only with 1 IFileStore selected
            IFileStore iFile1 = (IFileStore) selection.get(0);

            // Ask for new name and check if it is OK...
            IFileStore newPath1 = askForNewName(shell, iFile1);
            if (newPath1 == null) {
               return;
            }

            // Copy file
            log.debug("Duplicating file '{}' to '{}'", iFile1, newPath1);
            try {
               iFile1.copy(newPath1, EFS.NONE, new NullProgressMonitor());

               // Refresh Template Browser asynchronously
               eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);
            } catch (CoreException e) {
               jtbStatusReporter.showError("Error while duplicating this template", e, "");
               return;
            }
            break;

         case Constants.COMMAND_TEMPLATE_RDD_DELETE:

            // Confirmation Dialog
            String msg;
            if (selection.size() == 1) {
               IFileStore s = selection.get(0);
               msg = "Please confirm the deletion of '" + s.getName() + "'\n";
            } else {
               msg = "Are you sure to delete those " + selection.size() + " elements ?";
            }
            if (!(MessageDialog.openConfirm(shell, "Confirmation", msg))) {
               return;
            }

            for (IFileStore iFileStore : selection) {
               log.debug("Delete file {}", selection);
               try {
                  iFileStore.delete(EFS.NONE, new NullProgressMonitor());
               } catch (CoreException e) {
                  jtbStatusReporter.showError("Problem when deleting element", e, "");
                  return;
               }
            }
            // Refresh Template Browser asynchronously
            eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);
            break;

         case Constants.COMMAND_TEMPLATE_RDD_RENAME:
            // Available only with 1 IFileStore selected
            IFileStore iResource2 = selection.get(0);

            // Ask for new name and check if it is OK...
            IFileStore newPath2 = askForNewName(shell, iResource2);
            if (newPath2 == null) {
               return;
            }

            // Rename file
            log.debug("Renaming file '{}' to '{}'", iResource2, newPath2);
            try {
               iResource2.move(newPath2, EFS.NONE, new NullProgressMonitor());

               // Refresh Template Browser asynchronously
               eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);
            } catch (CoreException e) {
               jtbStatusReporter.showError("Error while renaming this template", e, "");
               return;
            }
            break;

         default:
            break;
      }
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<IFileStore> selection,
                             @Named(Constants.COMMAND_TEMPLATE_RDD_PARAM) String mode,
                             @Optional MMenuItem menuItem) {

      if (selection == null) {
         return Utils.disableMenu(menuItem);
      }

      // No menu displayed if it includes the Template folder
      for (IFileStore fileStore : selection) {
         if (templatesManager.isSystemTemplateDirectoryFileStore(fileStore)) {
            return Utils.disableMenu(menuItem);
         }
      }

      switch (mode) {
         case Constants.COMMAND_TEMPLATE_RDD_DELETE:
            return Utils.enableMenu(menuItem);

         case Constants.COMMAND_TEMPLATE_RDD_DUPLICATE:
            if (selection.size() > 1) {
               return Utils.disableMenu(menuItem);
            } else {
               if (selection.get(0).fetchInfo().isDirectory()) {
                  return Utils.disableMenu(menuItem);
               } else {
                  return Utils.enableMenu(menuItem);
               }
            }

         case Constants.COMMAND_TEMPLATE_RDD_RENAME:
            if (selection.size() > 1) {
               return Utils.disableMenu(menuItem);
            } else {
               return Utils.enableMenu(menuItem);
            }

         default:
            return Utils.disableMenu(menuItem);
      }

   }

   // --------
   // Helpers
   // --------

   private IFileStore askForNewName(Shell shell, IFileStore oldResource) {

      String oldName = oldResource.getName();

      String title = null;
      if (oldResource.fetchInfo().isDirectory()) {
         title = "Please enter a name for the Folder";
      } else {
         title = "Please enter a name for the Template";
      }

      // Ask for new name
      InputDialog inputDialog = new InputDialog(shell, title, "New Name:", oldName, null);
      if (inputDialog.open() != Window.OK) {
         return null;
      }
      String newName = inputDialog.getValue();

      // Do nothing if new name is empty
      if (Utils.isEmpty(newName)) {
         return null;
      }
      newName = newName.trim();
      // Do nothing if new name is the same as the current file name
      if (newName.equals(oldName)) {
         return null;
      }

      IFileStore newFileStore = EFS.getLocalFileSystem()
               .getStore(URIUtil.toPath(oldResource.toURI()).removeLastSegments(1).append(newName));

      // Check for duplicates
      if (newFileStore.fetchInfo().exists()) {
         MessageDialog.openInformation(shell, "Resource already exist", "A resource with this name already exist.");
         return null;
      }

      return newFileStore;
   }

}

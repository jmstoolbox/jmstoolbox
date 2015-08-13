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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Temlate New Folder" command
 * 
 * @author Denis Forveille
 * 
 */
public class TemplateNewFolderHandler {

   private static final Logger log = LoggerFactory.getLogger(TemplateNewFolderHandler.class);

   @Inject
   private IEventBroker eventBroker;

   @Inject
   private JTBStatusReporter jtbStatusReporter;

   @Execute
   public void execute(Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<IResource> selection) {
      log.debug("execute .selection={}", selection);

      IResource sel = selection.get(0);

      IFolder parentFolder;
      // Find parent
      if (sel instanceof IFolder) {
         parentFolder = (IFolder) sel;
      } else {
         parentFolder = (IFolder) sel.getParent();
      }
      log.debug("Parent={}", parentFolder);

      // Ask for folder name
      InputDialog inputDialog = new InputDialog(shell, "Please enter the name of the new folder", "Folder Name:", "", null);
      if (inputDialog.open() != Window.OK) {
         return;
      }
      String folderName = inputDialog.getValue();

      // Do nothing if new name is empty
      if ((folderName == null) || (folderName.trim().isEmpty())) {
         return;
      }

      folderName = folderName.trim();

      IPath newFolderPath = parentFolder.getFullPath().append(folderName);

      // Check for duplicate
      IFolder newFolder = ResourcesPlugin.getWorkspace().getRoot().getFolder(newFolderPath);
      if (newFolder.exists()) {
         MessageDialog.openInformation(shell, "Folder already exist", "A folder with this name already exist.");
         return;
      }

      // Create the folder
      try {
         newFolder.create(true, true, null);
      } catch (CoreException e) {
         jtbStatusReporter.showError("Problem when creating folder", e, "");
         return;
      }

      // Refresh Template Browser asynchronously
      eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);

   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<IResource> selection,
                             @Optional MMenuItem menuItem) {
      log.debug("canExecute={}", selection);

      // Only one selection is authorized to display the menu
      if ((selection != null) && (selection.size() != 1)) {
         return Utils.disableMenu(menuItem);
      }
      return Utils.enableMenu(menuItem);
   }

}

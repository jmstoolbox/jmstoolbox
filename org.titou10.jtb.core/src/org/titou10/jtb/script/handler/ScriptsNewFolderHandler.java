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
package org.titou10.jtb.script.handler;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.xml.bind.JAXBException;

import org.eclipse.core.runtime.CoreException;
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
import org.titou10.jtb.script.ScriptsManager;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Scripts New Folder" command
 * 
 * @author Denis Forveille
 * 
 */
public class ScriptsNewFolderHandler {

   private static final Logger log = LoggerFactory.getLogger(ScriptsNewFolderHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private ScriptsManager      scriptsManager;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell, @Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<Object> selection) {
      log.debug("execute .selection={}", selection);

      // Can be either a Directory, or a Script
      Object sel = selection.get(0);

      // Find parent directory
      Directory parentDirectory;
      if (sel instanceof Directory) {
         parentDirectory = (Directory) sel;
      } else {
         parentDirectory = ((Script) sel).getParent();
      }

      // Ask for folder name
      InputDialog inputDialog = new InputDialog(shell, "Please enter the name of the new folder", "Folder Name:", "", null);
      if (inputDialog.open() != Window.OK) {
         return;
      }
      String newDirectoryName = inputDialog.getValue();

      // Do nothing if new name is empty
      if (Utils.isEmpty(newDirectoryName)) {
         return;
      }

      newDirectoryName = newDirectoryName.trim();

      // Check for duplicate
      for (Directory d : parentDirectory.getDirectory()) {
         if (d.getName().equals(newDirectoryName)) {
            MessageDialog.openInformation(shell, "Folder already exist", "A folder with this name already exist.");
            return;
         }
      }

      // Add the folder
      Directory newDir = new Directory();
      newDir.setName(newDirectoryName);
      newDir.setParent(parentDirectory);
      parentDirectory.getDirectory().add(newDir);

      // Save Scripts
      try {
         scriptsManager.writeConfig();
      } catch (JAXBException | CoreException e) {
         jtbStatusReporter.showError("Problem when creating folder", e, "");
         return;
      }

      // Refresh Template Browser asynchronously
      eventBroker.post(Constants.EVENT_REFRESH_SCRIPTS_BROWSER, newDir);

   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<Object> selection,
                             @Optional MMenuItem menuItem) {

      // Only one selection is authorized to display the menu
      if ((selection != null) && (selection.size() != 1)) {
         return Utils.disableMenu(menuItem);
      }
      return Utils.enableMenu(menuItem);
   }

}

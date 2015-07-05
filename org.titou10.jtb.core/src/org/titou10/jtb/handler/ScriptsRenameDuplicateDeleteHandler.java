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

import javax.inject.Named;
import javax.xml.bind.JAXBException;

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
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.script.ScriptsUtils;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Delete/Rename/Duplicate Script" command
 * 
 * @author Denis Forveille
 * 
 */
public class ScriptsRenameDuplicateDeleteHandler {

   private static final Logger log = LoggerFactory.getLogger(ScriptsRenameDuplicateDeleteHandler.class);

   @Execute
   public void execute(Shell shell,
                       IEventBroker eventBroker,
                       ConfigManager cm,
                       JTBStatusReporter jtbStatusReporter,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<Object> selection,
                       @Named(Constants.COMMAND_SCRIPTS_RDD_PARAM) String mode) {
      log.debug("execute.  mode={}", mode);

      switch (mode) {
         case Constants.COMMAND_SCRIPTS_RDD_DUPLICATE:

            // Available only with 1 Script of 1 Directory selected
            Directory parentDirectory;

            Directory oldDir = null;
            Script oldScript = null;

            if (selection.get(0) instanceof Directory) {
               oldDir = (Directory) selection.get(0);
               parentDirectory = oldDir.getParent();
            } else {
               oldScript = (Script) selection.get(0);
               parentDirectory = oldScript.getParent();
            }

            // Ask for new name and check if it is OK...
            String newName = askForNewName(shell, oldDir, oldScript);
            if (newName == null) {
               return;
            }

            // Duplicate Scripts/Directory
            if (oldScript == null) {
               parentDirectory.getDirectory().add(ScriptsUtils.cloneDirectory(oldDir, newName, parentDirectory));
            } else {
               parentDirectory.getScript().add(ScriptsUtils.cloneScript(oldScript, newName, parentDirectory));
            }

            // Write scripts
            try {
               cm.writeScriptFile();
            } catch (JAXBException | CoreException e) {
               jtbStatusReporter.showError("Problem when saving Script file", e, "");
               return;
            }

            // Refresh Scripts Browser asynchronously
            eventBroker.post(Constants.EVENT_SCRIPTS, null);

            break;

         case Constants.COMMAND_SCRIPTS_RDD_DELETE:

            // Confirmation Dialog
            String msg;
            if (selection.size() == 1) {
               String n;
               if (selection.get(0) instanceof Directory) {
                  Directory d = (Directory) selection.get(0);
                  n = d.getName();
               } else {
                  Script s = (Script) selection.get(0);
                  n = s.getName();
               }

               msg = "Please confirm the deletion of '" + n + "'\n";
            } else {
               msg = "Are you sure to delete those " + selection.size() + " elements ?";
            }
            if (!(MessageDialog.openConfirm(shell, "Confirmation", msg))) {
               return;
            }

            // Remove Scripts and Folders
            for (Object o : selection) {
               if (o instanceof Directory) {
                  Directory d = (Directory) o;
                  Directory parentDir = ((Directory) o).getParent();
                  parentDir.getDirectory().remove(d);
               } else {
                  Script s = (Script) o;
                  Directory parentDir = ((Script) o).getParent();
                  parentDir.getScript().remove(s);
               }
            }

            // Write scripts
            try {
               cm.writeScriptFile();
            } catch (JAXBException | CoreException e) {
               jtbStatusReporter.showError("Problem when saving Script file", e, "");
               return;
            }

            // Refresh Scripts Browser asynchronously
            eventBroker.post(Constants.EVENT_SCRIPTS, null);

            break;

         case Constants.COMMAND_SCRIPTS_RDD_RENAME:

            // Available only with 1 Script of 1 Directory selected
            String oldName2;
            Directory oldDir2 = null;
            Script oldScript2 = null;
            if (selection.get(0) instanceof Directory) {
               oldDir2 = (Directory) selection.get(0);
               oldName2 = oldDir2.getName();
            } else {
               oldScript2 = (Script) selection.get(0);
               oldName2 = oldScript2.getName();
            }

            // Ask for new name and check if it is OK...
            String newName2 = askForNewName(shell, oldDir2, oldScript2);
            if (newName2 == null) {
               return;
            }

            // Change Name
            if (oldScript2 == null) {
               log.debug("Renaming Directory '{}' to '{}'", oldName2, newName2);
               oldDir2.setName(newName2);
            } else {
               log.debug("Renaming Script '{}' to '{}'", oldName2, newName2);
               oldScript2.setName(newName2);
            }

            // Write scripts
            try {
               cm.writeScriptFile();
            } catch (JAXBException | CoreException e) {
               jtbStatusReporter.showError("Problem when saving Script file", e, "");
               return;
            }

            // Refresh Scripts Browser asynchronously
            eventBroker.post(Constants.EVENT_SCRIPTS, null);

         default:
            break;
      }
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<Object> selection,
                             @Named(Constants.COMMAND_SCRIPTS_RDD_PARAM) String mode,
                             @Optional MMenuItem menuItem) {
      log.debug("canExecute={} mode={}", selection, mode);

      if (selection == null) {
         return Utils.disableMenu(menuItem);
      }

      // No menu displayed if it includes the Scripts folder
      for (Object o : selection) {
         if (o instanceof Directory) {
            Directory dir = (Directory) o;
            if (dir.getName().equals(Constants.SCRIPTS_FOLDER)) {
               return Utils.disableMenu(menuItem);
            }
         }
      }

      switch (mode) {
         case Constants.COMMAND_SCRIPTS_RDD_DELETE:
            return Utils.enableMenu(menuItem);

         case Constants.COMMAND_SCRIPTS_RDD_DUPLICATE:
            if (selection.size() > 1) {
               return Utils.disableMenu(menuItem);
            } else {
               return Utils.enableMenu(menuItem);
            }

         case Constants.COMMAND_SCRIPTS_RDD_RENAME:
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

   private String askForNewName(Shell shell, Directory oldDir, Script oldScript) {

      String oldName;
      Directory dirParent;
      String title;
      if (oldScript == null) {
         oldName = oldDir.getName();
         dirParent = oldDir.getParent();
         title = "Please enter a name for the Folder";
      } else {
         oldName = oldScript.getName();
         dirParent = oldScript.getParent();
         title = "Please enter a name for the Script";
      }

      // Ask for new name
      InputDialog inputDialog = new InputDialog(shell, title, "New Name:", oldName + "_new", null);
      if (inputDialog.open() != Window.OK) {
         return null;
      }
      String newName = inputDialog.getValue();

      // Do nothing if new name is empty
      if ((newName == null) || (newName.trim().isEmpty())) {
         return null;
      }
      newName = newName.trim();
      // Do nothing if new name is the same as the current file name
      if ((newName == null) || (newName.trim().isEmpty())) {
         return null;
      }
      if (newName.equals(oldName)) {
         return null;
      }

      // Check for duplicates
      if (oldScript == null) {
         for (Directory d : dirParent.getDirectory()) {
            if (d.getName().equals(newName)) {
               MessageDialog.openInformation(shell, "Folder already exist", "A Folder with this name already exist.");
               return null;
            }
         }
      } else {
         for (Script s : dirParent.getScript()) {
            if (s.getName().equals(newName)) {
               MessageDialog.openInformation(shell, "Script already exist", "A Script with this name already exist.");
               return null;
            }
         }
      }
      return newName;
   }

}

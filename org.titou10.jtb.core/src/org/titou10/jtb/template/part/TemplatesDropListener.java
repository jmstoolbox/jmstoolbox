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
package org.titou10.jtb.template.part;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.ui.dnd.DNDData;
import org.titou10.jtb.ui.dnd.DNDData.DNDElement;
import org.titou10.jtb.ui.dnd.TransferJTBMessage;
import org.titou10.jtb.ui.dnd.TransferTemplate;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Template Browser : Drop Listener
 * 
 * @author Denis Forveille
 *
 */
@SuppressWarnings("restriction")
public class TemplatesDropListener extends ViewerDropAdapter {

   private static final Logger log = LoggerFactory.getLogger(TemplatesDropListener.class);

   private TemplatesManager    templatesManager;
   private Shell               shell;
   private ECommandService     commandService;
   private EHandlerService     handlerService;

   TemplatesDropListener(TemplatesManager templatesManager,
                         TreeViewer treeViewer,
                         Shell shell,
                         ECommandService commandService,
                         EHandlerService handlerService) {
      super(treeViewer);
      this.templatesManager = templatesManager;
      this.shell = shell;
      this.commandService = commandService;
      this.handlerService = handlerService;

      this.setFeedbackEnabled(false); // Disable "in between" visual clues
   }

   @Override
   public boolean validateDrop(Object target, int operation, TransferData transferData) {

      if (TransferTemplate.getInstance().isSupportedType(transferData)) {
         return true;
      }

      if (TransferJTBMessage.getInstance().isSupportedType(transferData)) {
         return true;
      }

      // Files dropped from OS
      // Check if the files selected are all JTB Templates
      if (FileTransfer.getInstance().isSupportedType(transferData)) {
         String[] fileNames = (String[]) FileTransfer.getInstance().nativeToJava(transferData);
         for (String fileName : fileNames) {
            try {
               if (!templatesManager.isFileStoreATemplate(fileName)) {
                  log.debug("File '{}' is not a jtb template. Reject drop", fileName);
                  return false;
               }
            } catch (IOException e) {
               log.error("IOException occurred when determining file nature for {}", fileName, e);
               return false;
            }
         }
         return true;
      }

      return false;
   }

   @Override
   public void drop(DropTargetEvent event) {
      // Store the element where the Template of TemplateFolder has beeen dropped
      Object target = determineTarget(event);
      log.debug("The drop was done on element: {}", target);

      IFileStore targetFileStore = (IFileStore) target;

      if (targetFileStore.fetchInfo().isDirectory()) {
         DNDData.dropOnTemplateFileStoreFolder(targetFileStore);
      } else {
         DNDData.dropOnTemplateFileStoreFile(targetFileStore);
      }

      // External file(s) drop on JTBDestination determined by the "FileTransfer" kind
      if (FileTransfer.getInstance().isSupportedType(event.dataTypes[0])) {

         String[] fileNames = (String[]) event.data;
         if ((fileNames == null) || (fileNames.length == 0)) {
            return;
         }

         DNDData.dragTemplatesFileNames(Arrays.asList(fileNames));
      }

      super.drop(event);
   }

   @Override
   public boolean performDrop(Object data) {
      log.debug("performDrop: {}", DNDData.getDrag());

      switch (DNDData.getDrag()) {

         // Templates come from the Template Browser
         case TEMPLATE_FILESTORES:
            moveOrCopyTemplatesFromBrowser();
            return true;

         case JTB_MESSAGES:// Messages from the Message Browser
         case TEMPLATES_FILENAMES: // Templates come from the OS

            // Call "Save as Template" Command
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(Constants.COMMAND_CONTEXT_PARAM, Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP);
            ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_MESSAGE_SAVE_TEMPLATE, parameters);
            handlerService.executeHandler(myCommand);
            return true;

         default:
            log.warn("Drag & Drop operation not implemented? : {}", DNDData.getDrag());
            return false;
      }
   }

   private void moveOrCopyTemplatesFromBrowser() {

      IFileStore targetFileStore = DNDData.getTargetTemplateFileStore();
      List<IFileStore> fileStores = DNDData.getSourceTemplatesFileStores();

      IFileStore destFolder;
      if (DNDData.getDrop() == DNDElement.DROP_ON_TEMPLATE_FILESTORE_FILE) {
         destFolder = targetFileStore.getParent();
      } else {
         destFolder = targetFileStore;
      }

      // IFileStore targetFolder = DNDData.getTargetTemplateFileStore();
      // IFileStore targetFile = DNDData.getTargetTemplateFileStore();

      for (IFileStore sourceFileStore : fileStores) {
         log.debug("sourceFileStore={} targetFileStore={}", sourceFileStore, targetFileStore);

         if (sourceFileStore.fetchInfo().isDirectory()) {

            // Check if source and target share the same directory,If so, do nothing...
            if (sourceFileStore.getParent().equals(destFolder)) {
               log.debug("Do nothing, both have the same Directory");
               continue;
            }

            // Check if destFolder has for ancestor sourceTemplateFolder.. in this case do nothing
            boolean areRelated = Utils.isFileStoreGrandChildOfParent(sourceFileStore, destFolder);
            if (areRelated) {
               log.warn("D&D cancelled, destFolder has for ancestor sourceTemplateFolder");
               continue;
            }

            // Compute new path
            IFileStore newFolderFileStore = templatesManager.appendFilenameToFileStore(destFolder, sourceFileStore.getName());
            log.debug("newFolderFileStore={}", newFolderFileStore);

            // Check existence of new path
            if (newFolderFileStore.fetchInfo().exists()) {
               MessageDialog.openInformation(shell, "Folder already exist", "A folder with this name already exist.");
               return;
            }

            // Perform the move or copy
            try {
               if (getCurrentOperation() == DND.DROP_MOVE) {
                  sourceFileStore.move(newFolderFileStore, EFS.OVERWRITE, new NullProgressMonitor());
               } else {
                  sourceFileStore.copy(newFolderFileStore, EFS.OVERWRITE, new NullProgressMonitor());
               }
            } catch (CoreException e) {
               log.error("Exception occurred during drag & drop", e);
               return;
            }

         } else {

            // Check if source and target share the same folder,If so, do nothing...
            if (sourceFileStore.getParent().equals(destFolder)) {
               log.debug("Do nothing, both have the same folder");
               continue;
            }

            // Compute new FileStore
            IFileStore newFileStore = templatesManager.appendFilenameToFileStore(destFolder, sourceFileStore.getName());
            log.debug("newFileStore={}", newFileStore);

            // Check existence of new path
            if (newFileStore.fetchInfo().exists()) {
               MessageDialog.openInformation(shell, "File already exist", "A template with this name already exist.");
               continue;
            }

            // Perform the move or copy
            try {
               if (getCurrentOperation() == DND.DROP_MOVE) {
                  sourceFileStore.move(newFileStore, EFS.OVERWRITE, new NullProgressMonitor());
               } else {
                  sourceFileStore.copy(newFileStore, EFS.OVERWRITE, new NullProgressMonitor());
               }
            } catch (CoreException e) {
               log.error("Exception occurred during drag & drop", e);
               return;
            }

         }
      }

      // Refresh TreeViewer
      getViewer().refresh();
   }

}

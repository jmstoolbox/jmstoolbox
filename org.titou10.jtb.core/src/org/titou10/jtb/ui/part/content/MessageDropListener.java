/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.ui.part.content;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.ui.dnd.DNDData;
import org.titou10.jtb.ui.dnd.TransferJTBMessage;
import org.titou10.jtb.ui.dnd.TransferTemplate;
import org.titou10.jtb.util.Constants;

/**
 * Handle drop of Message and templates on the content browser
 *
 * @author Denis Forveille
 *
 */
final class MessageDropListener extends ViewerDropAdapter {

   private static final Logger log                     = LoggerFactory.getLogger(MessageDropListener.class);

   private ECommandService     commandService;
   private EHandlerService     handlerService;
   private TemplatesManager    templatesManager;

   private JTBDestination      jtbDestination;

   boolean                     containsJTBTemplates    = false;
   boolean                     containsNonJTBTemplates = false;

   MessageDropListener(ECommandService commandService,
                       EHandlerService handlerService,
                       TemplatesManager templatesManager,
                       TableViewer tableViewer,
                       JTBDestination jtbDestination) {
      super(tableViewer);

      this.commandService = commandService;
      this.handlerService = handlerService;

      this.templatesManager = templatesManager;

      this.jtbDestination = jtbDestination;
      this.setFeedbackEnabled(false); // Disable "in between" visual clues
   }

   @Override
   public boolean validateDrop(Object target, int operation, TransferData transferData) {

      if (TransferTemplate.getInstance().isSupportedType(transferData)) {
         // If the selection includes a Folder, forbid the drop
         List<IFileStore> fileStores = DNDData.getSourceTemplatesFileStores();
         for (IFileStore fileStore : fileStores) {
            if (fileStore.fetchInfo().isDirectory()) {
               return false;
            }
         }
         return true;
      }

      if (TransferJTBMessage.getInstance().isSupportedType(transferData)) {
         return true;
      }

      // Files dropped from OS
      // Check if the files selected are all JTB Templates or all non JTB Messages
      if (FileTransfer.getInstance().isSupportedType(transferData)) {

         containsJTBTemplates = false;
         containsNonJTBTemplates = false;

         try {
            String[] fileNames = (String[]) FileTransfer.getInstance().nativeToJava(transferData);

            // On linux, fileName are not yet set...
            if (fileNames != null) {
               for (String fileName : fileNames) {
                  // Directories are not supported
                  File f = new File(fileName);
                  if (f.isDirectory()) {
                     return false;
                  }

                  if (templatesManager.isFileStoreATemplate(fileName)) {
                     containsJTBTemplates = true;
                  } else {
                     containsNonJTBTemplates = true;
                  }
               }
            }
         } catch (IOException e) {
            log.error("IOException occurred when determining file nature of a file", e);
            return false;
         }

         if (containsJTBTemplates && containsNonJTBTemplates) {
            log.debug("Cannot mix JTBTemplates and non JTBTemplates during drop");
            return false;
         }

         return true;
      }

      return false;
   }

   @Override
   public void dragEnter(DropTargetEvent event) {
      log.debug("dragEnter : {}", event);

      // Choose TransferJTBMessage if supported
      for (int i = 0; i < event.dataTypes.length; i++) {
         if (TransferJTBMessage.getInstance().isSupportedType(event.dataTypes[i])) {
            event.currentDataType = event.dataTypes[i];
            break;
         }
      }
   }

   @Override
   public void drop(DropTargetEvent event) {
      log.debug("drop : {}", event);

      // Stores the JTBDestination where the drop occurred
      log.debug("The drop was done on element: {}", jtbDestination);

      DNDData.dropOnJTBDestination(jtbDestination);

      // If source=OS/files, store the filenames
      for (TransferData td : event.dataTypes) {
         if (FileTransfer.getInstance().isSupportedType(td) && event.data != null) {

            String[] fileNames = (String[]) event.data;

            for (String fileName : fileNames) {
               // Directories are not supported
               File f = new File(fileName);
               if (f.isDirectory()) {
                  log.debug("Dropping directories are not supported");
                  return;
               }

            }
            DNDData.dragTemplatesFileNames(Arrays.asList(fileNames));
            log.debug("fileNames: {}", (Object[]) fileNames);
            break;
         }
      }

      super.drop(event);
   }

   @Override
   public boolean performDrop(Object data) {
      log.debug("performDrop : {}", DNDData.getDrag());

      switch (DNDData.getDrag()) {
         case TEMPLATE_FILESTORES: // From Template Browser

            Map<String, Object> parameters1 = new HashMap<>();
            parameters1.put(Constants.COMMAND_CONTEXT_PARAM, Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP);

            ParameterizedCommand myCommand1 = commandService.createCommand(Constants.COMMAND_MESSAGE_SEND_TEMPLATE, parameters1);
            handlerService.executeHandler(myCommand1);

            return true;

         case JTB_MESSAGES: // From the Message Browser
         case TEMPLATES_FILENAMES: // From the OS

            Map<String, Object> parameters2 = new HashMap<>();
            parameters2.put(Constants.COMMAND_CONTEXT_PARAM, Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP);

            ParameterizedCommand myCommand2 = commandService.createCommand(Constants.COMMAND_MESSAGE_SEND, parameters2);
            handlerService.executeHandler(myCommand2);

            return true;

         default:
            log.warn("Drag & Drop operation not implemented? : {}", DNDData.getDrag());
            return false;
      }
   }

}

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
package org.titou10.jtb.ui.part;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.ui.navigator.NodeJTBTopic;
import org.titou10.jtb.util.Constants;

public class JTBSessionBrowserDropListener extends ViewerDropAdapter {

   private static final Logger log = LoggerFactory.getLogger(JTBSessionBrowserDropListener.class);

   private ECommandService     commandService;
   private EHandlerService     handlerService;
   private TemplatesManager    templatesManager;

   public JTBSessionBrowserDropListener(ECommandService commandService,
                                        EHandlerService handlerService,
                                        TemplatesManager templatesManager,
                                        TreeViewer treeViewer) {
      super(treeViewer);

      this.commandService = commandService;
      this.handlerService = handlerService;

      this.templatesManager = templatesManager;

      this.setFeedbackEnabled(false); // Disable "in between" visual clues
   }

   @Override
   public boolean validateDrop(Object target, int operation, TransferData transferData) {
      log.debug("target: {}", target);

      // Object dropped on nothing
      if (target == null) {
         return false;
      }

      // Object dropped on something different than a NodeJTBQueue or NodeJTBTopic
      if (!((target instanceof NodeJTBQueue) || (target instanceof NodeJTBTopic))) {
         return false;
      }

      if (TransferTemplate.getInstance().isSupportedType(transferData)) {
         return true;
      }

      if (TransferJTBMessage.getInstance().isSupportedType(transferData)) {
         return true;
      }

      // Files dropped from OS
      // Can only be files representing JTB Templates
      // Check if the files selected are all JTB Templates or all non JTB Messages
      if (FileTransfer.getInstance().isSupportedType(transferData)) {

         try {
            String[] fileNames = (String[]) FileTransfer.getInstance().nativeToJava(transferData);

            // On linux, fileName are not yet set...
            if (fileNames != null) {
               for (String fileName : fileNames) {
                  if (!templatesManager.isFileStoreATemplate(fileName)) {
                     return false;
                  }
               }
            }
         } catch (IOException e) {
            log.error("IOException occurred when determining file nature of a file", e);
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

      // Store the JTBDestination where the drop occurred
      Object target = determineTarget(event);

      JTBDestination jtbDestination;
      if (target instanceof NodeJTBQueue) {
         NodeJTBQueue nodeJTBQueue = (NodeJTBQueue) target;
         jtbDestination = (JTBDestination) nodeJTBQueue.getBusinessObject();
      } else {
         NodeJTBTopic nodeJTBTopic = (NodeJTBTopic) target;
         jtbDestination = (JTBDestination) nodeJTBTopic.getBusinessObject();
      }

      log.debug("The drop was done on element: {}", jtbDestination);

      DNDData.dropOnJTBDestination(jtbDestination);

      // // External file(s) drop on JTBDestination, Set drag
      // if (FileTransfer.getInstance().isSupportedType(event.dataTypes[0])) {
      //
      // String[] fileNames = (String[]) event.data;
      //
      // // Check again for Linux
      // try {
      // for (String fileName : fileNames) {
      // if (!templatesManager.isFileStoreATemplate(fileName)) {
      // log.debug("File '{}' is not a valid JTB Template. cancel drop", fileName);
      // return;
      // }
      // }
      // } catch (IOException e) {
      // log.error("IOException occurred when determining file nature of a file", e);
      // return;
      // }
      //
      // // Build list of fileStores
      // List<IFileStore> fileStores = new ArrayList<>(fileNames.length);
      // for (String fileName : fileNames) {
      // fileStores.add(Utils.getFileStoreFromFilename(fileName));
      // }
      //
      // DNDData.dragTemplatesFileStores(fileStores);
      // }

      super.drop(event);
   }

   @Override
   public boolean performDrop(Object data) {
      log.debug("performDrop : {}", DNDData.getDrag());

      ParameterizedCommand myCommand;
      Map<String, Object> parameters = new HashMap<>();
      parameters.put(Constants.COMMAND_CONTEXT_PARAM, Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP);

      switch (DNDData.getDrag()) {

         // Drag & Drop of a JTBMessage from the Message Browser
         case JTB_MESSAGES:

            // Call "Message Copy or Move Handler" Command
            myCommand = commandService.createCommand(Constants.COMMAND_MESSAGE_SEND, parameters);
            handlerService.executeHandler(myCommand);

            return true;

         case TEMPLATE_FILESTORES:
            // Drag & Drop of a JTBMessageTemplate to a JTBDestination

            // Call "Send Message From Template" Command
            myCommand = commandService.createCommand(Constants.COMMAND_MESSAGE_SEND_TEMPLATE, parameters);
            handlerService.executeHandler(myCommand);
            return true;

         default:
            log.warn("Drag & Drop operation not implemented? : {}", DNDData.getDrag());
            return false;
      }
   }

}

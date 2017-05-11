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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ParameterizedCommand;
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
@SuppressWarnings("restriction")
final class MessageDropListener extends ViewerDropAdapter {

   private static final Logger log = LoggerFactory.getLogger(MessageDragListener.class);

   private ECommandService     commandService;
   private EHandlerService     handlerService;
   private TemplatesManager    templatesManager;

   private JTBDestination      jtbDestination;

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
   public void drop(DropTargetEvent event) {

      // Store the JTBDestination where the drop occurred
      log.debug("The drop was done on element: {}", jtbDestination);
      DNDData.dropOnJTBDestination(jtbDestination);

      // FIXME DF
      // // External file(s) drop on JTBDestination. Set drag
      // if (FileTransfer.getInstance().isSupportedType(event.dataTypes[0])) {
      // String[] filenames = (String[]) event.data;
      // if (filenames.length == 1) {
      // String fileName = filenames[0];
      //
      // try {
      // // Is this file a Template?
      // if (templatesManager.isFileStoreATemplate(fileName)) {
      // // Yes Drag Template
      // DNDData.dragTemplateExternal(fileName);
      // } else {
      // // No, ordinary file
      // DNDData.dragExternalFileName(fileName);
      // }
      // } catch (IOException e) {
      // log.error("Exception occured when determining kind of source file", e);
      // return;
      // }
      // } else {
      // return;
      // }
      // }

      super.drop(event);
   }

   @Override
   public boolean performDrop(Object data) {
      log.debug("performDrop : {}", DNDData.getDrag());

      switch (DNDData.getDrag()) {
         // case JTBMESSAGE:
         case JTBMESSAGE_MULTI:
         case TEMPLATE:
            // case TEMPLATE_EXTERNAL:

            // Templates from the Template Browser
         case TEMPLATE_FILESTORES:
            Map<String, Object> parameters1 = new HashMap<>();
            parameters1.put(Constants.COMMAND_CONTEXT_PARAM, Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP);

            ParameterizedCommand myCommand1 = commandService.createCommand(Constants.COMMAND_MESSAGE_SEND_TEMPLATE, parameters1);
            handlerService.executeHandler(myCommand1);

            return true;

         // case EXTERNAL_FILE_NAME:
         // Map<String, Object> parameters2 = new HashMap<>();
         // parameters2.put(Constants.COMMAND_CONTEXT_PARAM, Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP);
         //
         // ParameterizedCommand myCommand2 = commandService.createCommand(Constants.COMMAND_MESSAGE_SEND, parameters2);
         // handlerService.executeHandler(myCommand2);
         //
         // return true;

         default:
            log.warn("Drag & Drop operation not implemented? : {}", DNDData.getDrag());
            return false;
      }
   }

   @Override
   public boolean validateDrop(Object target, int operation, TransferData transferData) {
      return ((TransferTemplate.getInstance().isSupportedType(transferData))
              || (TransferJTBMessage.getInstance().isSupportedType(transferData))
              || (FileTransfer.getInstance().isSupportedType(transferData)));
   }
}

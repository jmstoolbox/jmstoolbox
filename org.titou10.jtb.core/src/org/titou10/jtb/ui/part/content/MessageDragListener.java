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
import java.util.ArrayList;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.TextMessage;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.ui.dnd.DNDData;
import org.titou10.jtb.ui.dnd.TransferJTBMessage;
import org.titou10.jtb.util.Utils;

/**
 * Handle dragging of Message from the content browser
 * 
 * @author Denis Forveille
 *
 */
final class MessageDragListener extends DragSourceAdapter {

   private static final Logger log = LoggerFactory.getLogger(MessageDragListener.class);

   private final TableViewer   tableViewer;
   private List<String>        fileNames;

   MessageDragListener(TableViewer tableViewer) {
      this.tableViewer = tableViewer;
   }

   @Override
   @SuppressWarnings("unchecked")
   public void dragStart(DragSourceEvent event) {
      log.debug("dragStart {}", event);

      IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
      switch (selection.size()) {
         case 0:
            event.doit = false;
            break;

         case 1:
            JTBMessage jtbMessage = (JTBMessage) selection.getFirstElement();
            if (jtbMessage.getJtbMessageType() == JTBMessageType.STREAM) {
               log.warn("STREAM Messages can not be dragged to templates or another Queue");
               event.doit = false;
               return;
            }

            DNDData.dragJTBMessage(jtbMessage);
            break;

         default:
            List<JTBMessage> jtbMessages = (List<JTBMessage>) selection.toList();
            for (JTBMessage jtbMessage2 : jtbMessages) {
               if (jtbMessage2.getJtbMessageType() == JTBMessageType.STREAM) {
                  log.warn("STREAM Messages can not be dragged to templates or another Queue");
                  event.doit = false;
                  return;
               }
            }
            DNDData.dragJTBMessageMulti(jtbMessages);
            break;
      }
   }

   @Override
   public void dragFinished(DragSourceEvent event) {
      log.debug("dragFinished {}", event);
      if (fileNames != null) {
         for (String string : fileNames) {
            File f = new File(string);
            f.delete();
         }
      }
   }

   @Override
   public void dragSetData(DragSourceEvent event) {
      if (TransferJTBMessage.getInstance().isSupportedType(event.dataType)) {
         // log.debug("dragSetData : TransferJTBMessage {}", event);
         return;
      }

      if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
         // log.debug("dragSetData : FileTransfer {}", event);

         String fileName;
         List<String> fileNames = new ArrayList<>();
         try {
            for (JTBMessage jtbMessage : DNDData.getSourceJTBMessages()) {

               switch (jtbMessage.getJtbMessageType()) {
                  case TEXT:
                     fileName = Utils.writePayloadToOS((TextMessage) jtbMessage.getJmsMessage());
                     fileNames.add(fileName);
                     break;

                  case BYTES:
                     fileName = Utils.writePayloadToOS((BytesMessage) jtbMessage.getJmsMessage());
                     fileNames.add(fileName);
                     break;

                  case MAP:
                     fileName = Utils.writePayloadToOS((MapMessage) jtbMessage.getJmsMessage());
                     fileNames.add(fileName);
                     break;

                  default:
                     break;
               }
            }
         } catch (IOException | JMSException e) {
            log.error("Exception occurred while creating temp file", e);
            event.doit = false;
            return;
         }

         if (fileNames.isEmpty()) {
            event.doit = false;
            return;
         }

         event.data = fileNames.toArray(new String[fileNames.size()]);
      }
   }
}

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
 * Handle dragging of Message from the Message browser
 * 
 * @author Denis Forveille
 *
 */
final class MessageDragListener extends DragSourceAdapter {

   private static final Logger log = LoggerFactory.getLogger(MessageDragListener.class);

   private final TableViewer   tableViewer;
   private List<String>        tempFileNames;

   MessageDragListener(TableViewer tableViewer) {
      this.tableViewer = tableViewer;
   }

   @Override
   @SuppressWarnings("unchecked")
   public void dragStart(DragSourceEvent event) {
      log.debug("dragStart {}", event);

      IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
      if (selection.isEmpty()) {
         event.doit = false;
         return;
      }

      List<JTBMessage> jtbMessages = (List<JTBMessage>) selection.toList();
      for (JTBMessage jtbMessage2 : jtbMessages) {
         if (jtbMessage2.getJtbMessageType() == JTBMessageType.STREAM) {
            log.warn("STREAM Messages can not be dragged to templates or another Queue");
            event.doit = false;
            return;
         }
      }
      DNDData.dragJTBMessages(jtbMessages);
   }

   @Override
   public void dragFinished(DragSourceEvent event) {
      log.debug("dragFinished {}", event);

      // Delete temp files created when drop to OS
      // Only on wWindows
      // On Linux thie method is calle before the OS pops up an eventual dialog asking to replace the file
      if (Utils.isWindows()) {
         if (tempFileNames != null) {
            for (String string : tempFileNames) {
               File f = new File(string);
               f.delete();
            }
         }
      }
   }

   @Override
   public void dragSetData(DragSourceEvent event) {
      if (TransferJTBMessage.getInstance().isSupportedType(event.dataType)) {
         // log.debug("dragSetData : TransferJTBMessage {}", event);
         return;
      }

      // Messages going outside JMSToolBox
      if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
         // log.debug("dragSetData : FileTransfer {}", event);

         String fileName;
         tempFileNames = new ArrayList<>(DNDData.getSourceJTBMessages().size());
         try {
            for (JTBMessage jtbMessage : DNDData.getSourceJTBMessages()) {

               switch (jtbMessage.getJtbMessageType()) {
                  case TEXT:
                     fileName = Utils.writePayloadToOS((TextMessage) jtbMessage.getJmsMessage());
                     tempFileNames.add(fileName);
                     break;

                  case BYTES:
                     fileName = Utils.writePayloadToOS((BytesMessage) jtbMessage.getJmsMessage());
                     tempFileNames.add(fileName);
                     break;

                  case MAP:
                     fileName = Utils.writePayloadToOS((MapMessage) jtbMessage.getJmsMessage());
                     tempFileNames.add(fileName);
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

         if (tempFileNames.isEmpty()) {
            event.doit = false;
            return;
         }

         event.data = tempFileNames.toArray(new String[tempFileNames.size()]);
      }
   }
}

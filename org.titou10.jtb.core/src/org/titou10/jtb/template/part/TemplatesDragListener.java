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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.ui.dnd.DNDData;
import org.titou10.jtb.ui.dnd.DNDData.DNDElement;
import org.titou10.jtb.ui.dnd.TransferTemplate;

/**
 * Template Browser : Drag Listener
 * 
 * @author Denis Forveille
 *
 */
public class TemplatesDragListener extends DragSourceAdapter {

   private static final Logger log = LoggerFactory.getLogger(TemplatesDragListener.class);

   private TemplatesManager    templatesManager;

   private final TreeViewer    treeViewer;
   private List<String>        tempFileNames;

   TemplatesDragListener(TemplatesManager templatesManager, TreeViewer treeViewer) {
      this.templatesManager = templatesManager;
      this.treeViewer = treeViewer;
   }

   @Override
   @SuppressWarnings("unchecked")
   public void dragStart(DragSourceEvent event) {
      log.debug("Start Drag from Template Browser");

      IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();

      if ((selection == null) || (selection.isEmpty())) {
         event.doit = false;
         return;
      }

      List<IFileStore> selectedFileStores = (List<IFileStore>) selection.toList();

      // Only one directory can be selected,
      // If a directory is in the selection, it must be alone
      int nbDir = 0;
      int nbFiles = 0;
      for (IFileStore iFileStore : selectedFileStores) {
         if (iFileStore.fetchInfo().isDirectory()) {
            nbDir++;
         } else {
            nbFiles++;
         }
      }
      if ((nbDir > 1) || ((nbDir == 1) && (nbFiles > 0))) {
         event.doit = false;
         return;
      }

      DNDData.dragTemplatesFileStores(selectedFileStores);
   }

   @Override
   public void dragFinished(DragSourceEvent event) {
      // log.debug("dragFinished {}", event);
      // Delete temps files created when drop to OS
      if (tempFileNames != null) {
         for (String fileName : tempFileNames) {
            File f = new File(fileName);
            f.delete();
         }
      }
   }

   @Override
   public void dragSetData(DragSourceEvent event) {

      if (TransferTemplate.getInstance().isSupportedType(event.dataType)) {
         // log.debug("dragSetData : TransferTemplate {}", event);
         return;
      }

      if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
         // log.debug("dragSetData : FileTransfer {}", event);

         if (DNDData.getDrag() != DNDElement.TEMPLATE_FILESTORES) {
            event.doit = false;
            return;
         }

         // Store file names in event.data and tempFileNames in case they are sent outside JTB (ie to the OS..)
         tempFileNames = new ArrayList<>(DNDData.getSourceTemplatesFileStores().size());

         try {
            for (IFileStore ifs : DNDData.getSourceTemplatesFileStores()) {
               tempFileNames.add(templatesManager.writeTemplateToTemp(ifs));
            }
         } catch (CoreException | IOException e) {
            log.error("Exception occurred while creating temp file", e);
            event.doit = false;
            return;
         }

         event.data = tempFileNames.toArray(new String[0]);
      }
   }

}

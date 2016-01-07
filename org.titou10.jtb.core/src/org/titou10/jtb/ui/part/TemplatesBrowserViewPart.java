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
package org.titou10.jtb.ui.part;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.template.TemplateTreeContentProvider;
import org.titou10.jtb.template.TemplateTreeLabelProvider;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.DNDData;
import org.titou10.jtb.util.DNDData.DNDElement;

/**
 * Manage the Template Browser
 * 
 * @author Denis Forveille
 *
 */
@SuppressWarnings("restriction")
public class TemplatesBrowserViewPart {

   private static final Logger log = LoggerFactory.getLogger(TemplatesBrowserViewPart.class);

   @Inject
   private ECommandService     commandService;

   @Inject
   private EMenuService        menuService;

   @Inject
   private EHandlerService     handlerService;

   @Inject
   private ESelectionService   selectionService;

   @Inject
   private ConfigManager       cm;

   // JFaces components
   private TreeViewer          treeViewer;

   @Inject
   @Optional
   public void refresh(@UIEventTopic(Constants.EVENT_REFRESH_TEMPLATES_BROWSER) String x) {
      log.debug("UIEvent refresh Templates");
      try {
         cm.getTemplateFolder().refreshLocal(IResource.DEPTH_INFINITE, null);
      } catch (CoreException e) {
         log.error("CoreException whene refreshing template folder", e);
      }
      treeViewer.refresh();
   }

   @PostConstruct
   public void createControls(Shell shell, Composite parent) {
      treeViewer = new TreeViewer(parent, SWT.MULTI);
      treeViewer.setContentProvider(new TemplateTreeContentProvider(false));
      treeViewer.setLabelProvider(new TemplateTreeLabelProvider());

      // Drag and Drop
      int operations = DND.DROP_MOVE | DND.DROP_COPY;
      Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };
      treeViewer.addDragSupport(operations, transferTypes, new TemplateDragListener(treeViewer));
      treeViewer.addDropSupport(operations, transferTypes, new TemplateDropListener(treeViewer, shell));

      Tree tree = treeViewer.getTree();

      // Manage selections
      treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            // Store selected Message
            selectionService.setSelection(buildListIResourcesSelected((IStructuredSelection) event.getSelection()));
         }
      });

      // Add a Double Clic Listener
      treeViewer.addDoubleClickListener(new IDoubleClickListener() {

         @Override
         public void doubleClick(DoubleClickEvent event) {
            ITreeSelection sel = (ITreeSelection) event.getSelection();
            Object selected = sel.getFirstElement();
            if (selected instanceof IFile) {

               // Call Template "Add or Edit" Command
               Map<String, Object> parameters = new HashMap<>();
               parameters.put(Constants.COMMAND_TEMPLATE_ADDEDIT_PARAM, Constants.COMMAND_TEMPLATE_ADDEDIT_EDIT);
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_TEMPLATE_ADDEDIT, parameters);
               handlerService.executeHandler(myCommand);
            }
         }
      });

      // Remove a Template of Folder from the list
      tree.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.DEL) {
               IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }

               // Call "Tempate Delete" Command
               Map<String, Object> parameters = new HashMap<>();
               parameters.put(Constants.COMMAND_TEMPLATE_RDD_PARAM, Constants.COMMAND_TEMPLATE_RDD_DELETE);
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_TEMPLATE_RDD, parameters);
               handlerService.executeHandler(myCommand);
            }
         }
      });

      // Populate tree with the content of the "Templates" folder
      treeViewer.setInput(new Object[] { cm.getTemplateFolder() });
      treeViewer.expandToLevel(2); // Expand first level

      // Attach the Popup Menu
      menuService.registerContextMenu(tree, Constants.TEMPLATES_POPUP_MENU);
   }

   // ---------------
   // Helper Classes
   // ---------------

   private List<IResource> buildListIResourcesSelected(IStructuredSelection selection) {
      List<IResource> l = new ArrayList<IResource>(selection.size());
      for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
         IResource ir = (IResource) iterator.next();
         l.add(ir);
      }
      return l;
   }

   // -----------------------
   // Providers and Listeners
   // -----------------------

   private class TemplateDragListener extends DragSourceAdapter {
      private final TreeViewer treeViewer;

      public TemplateDragListener(TreeViewer treeViewer) {
         this.treeViewer = treeViewer;
      }

      @Override
      public void dragStart(DragSourceEvent event) {
         log.debug("Start Drag");

         // Only allow one template at a time (for now...)
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
         if ((selection == null) || (selection.size() != 1)) {
            event.doit = false;
            return;
         }

         if (selection.getFirstElement() instanceof IFolder) {
            DNDData.dragTemplateFolder((IFolder) selection.getFirstElement());
         }
         if (selection.getFirstElement() instanceof IFile) {
            DNDData.dragTemplate((IFile) selection.getFirstElement());
         }
      }

      @Override
      public void dragSetData(DragSourceEvent event) {
         if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
            event.data = "unused";
         }
      }
   }

   private class TemplateDropListener extends ViewerDropAdapter {

      private Shell shell;

      public TemplateDropListener(TreeViewer treeViewer, Shell shell) {
         super(treeViewer);
         this.shell = shell;
         this.setFeedbackEnabled(false); // Disable "in between" visual clues
      }

      @Override
      public void drop(DropTargetEvent event) {
         // Store the element where the Template of TemplateFolder has beeen dropped
         Object target = determineTarget(event);
         log.debug("The drop was done on element: {}", target);

         if (target instanceof IFolder) {
            DNDData.dropOnTemplateIFolder((IFolder) target);
         }
         if (target instanceof IFile) {
            DNDData.dropOnTemplateIFile((IFile) target);
         }

         super.drop(event);
      }

      @Override
      public boolean performDrop(Object data) {
         log.debug("performDrop: {}", DNDData.getDrag());

         IFolder targetFolder = DNDData.getTargetTemplateIFolder();
         IFile targetFile = DNDData.getTargetTemplateIFile();
         IFolder destFolder;

         switch (DNDData.getDrag()) {
            case TEMPLATE:
               // Get back the sourceTemplate
               IFile sourceTemplate = DNDData.getSourceTemplateIFile();

               log.debug("sourceTemplate={} targetFolder={} targetFile={}", sourceTemplate, targetFolder, targetFile);

               // Check if source and target share the same folder,If so, do nothing...
               if (DNDData.getDrop() == DNDElement.TEMPLATE_FOLDER) {
                  destFolder = targetFolder;
               } else {
                  destFolder = (IFolder) targetFile.getParent();
               }
               if (sourceTemplate.getParent().equals(destFolder)) {
                  log.debug("Do nothing, both have the same folder");
                  return false;
               }

               // Compute new path
               IPath newFilePath = destFolder.getFullPath().append(sourceTemplate.getName());
               log.debug("newFilePath={}", newFilePath);

               // Check existence of new path
               IFile newFile = ResourcesPlugin.getWorkspace().getRoot().getFile(newFilePath);
               if (newFile.exists()) {
                  MessageDialog.openInformation(shell, "File already exist", "A template with this name already exist.");
                  return false;
               }

               // Perform the move or copy
               try {
                  if (getCurrentOperation() == DND.DROP_MOVE) {
                     sourceTemplate.move(newFilePath, true, null);
                  } else {
                     sourceTemplate.copy(newFilePath, true, null);
                  }
               } catch (CoreException e) {
                  log.error("Exception occurred during drag & drop", e);
                  return false;
               }

               // Refresh TreeViewer
               getViewer().refresh();

               return true;

            case TEMPLATE_FOLDER:
               // Get back the sourceTemplateFolder
               IFolder sourceTemplateFolder = DNDData.getSourceTemplateIFolder();

               log.debug("sourceTemplateFolder={} targetFolder={} targetFile={}", sourceTemplateFolder, targetFolder, targetFile);

               // Check if source and target share the same folder,If so, do nothing...
               if (DNDData.getDrop() == DNDElement.TEMPLATE_FOLDER) {
                  destFolder = targetFolder;
               } else {
                  destFolder = (IFolder) targetFile.getParent();
               }

               // Check if source and target share the same directory,If so, do nothing...
               if (sourceTemplateFolder.getParent().getFullPath().equals(destFolder.getFullPath())) {
                  log.debug("Do nothing, both have the same Directory");
                  return false;
               }

               // Check if destFolder has for ancestor sourceTemplateFolder.. in this case do nothing
               IContainer x = destFolder;
               while (x instanceof IFolder) {
                  if (x.getFullPath().equals(sourceTemplateFolder.getFullPath())) {
                     log.warn("D&D cancelled, destFolder has for ancestor sourceTemplateFolder");
                     return false;
                  }
                  x = x.getParent();
               }

               // Compute new path
               IPath newFolderPath = destFolder.getFullPath().append(sourceTemplateFolder.getName());
               log.debug("newFolderPath={}", newFolderPath);

               // Check existence of new path
               IFile newFolder = ResourcesPlugin.getWorkspace().getRoot().getFile(newFolderPath);
               if (newFolder.exists()) {
                  MessageDialog.openInformation(shell, "Folder already exist", "A folder with this name already exist.");
                  return false;
               }

               // Perform the move or copy
               try {
                  if (getCurrentOperation() == DND.DROP_MOVE) {
                     sourceTemplateFolder.move(newFolderPath, true, null);
                  } else {
                     sourceTemplateFolder.copy(newFolderPath, true, null);
                  }
               } catch (CoreException e) {
                  log.error("Exception occurred during drag & drop", e);
                  return false;
               }

               // Refresh TreeViewer
               getViewer().refresh();

               return true;

            case JTBMESSAGE:

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

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferData) {
         return TextTransfer.getInstance().isSupportedType(transferData);
      }
   }

}

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
import javax.xml.bind.JAXBException;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
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
import org.titou10.jtb.script.ScriptsTreeContentProvider;
import org.titou10.jtb.script.ScriptsTreeLabelProvider;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.DNDData;
import org.titou10.jtb.util.DNDData.DNDElement;

/**
 * Manage the Scripts Browser
 * 
 * @author Denis Forveille
 *
 */
@SuppressWarnings("restriction")
public class ScriptsBrowserViewPart {

   private static final Logger log = LoggerFactory.getLogger(ScriptsBrowserViewPart.class);

   @Inject
   private ConfigManager       cm;

   @Inject
   private ECommandService     commandService;

   @Inject
   private EHandlerService     handlerService;

   @Inject
   private EMenuService        menuService;

   @Inject
   private ESelectionService   selectionService;

   // JFaces components
   private TreeViewer          treeViewer;

   @Inject
   @Optional
   public void refresh(@UIEventTopic(Constants.EVENT_REFRESH_SCRIPTS_BROWSER) String x) {
      log.debug("UIEvent refresh Scripts");
      treeViewer.refresh();
      // treeViewer.expandAll();
   }

   @PostConstruct
   public void createControls(Shell shell, Composite parent, final MWindow window) {
      treeViewer = new TreeViewer(parent, SWT.MULTI);
      treeViewer.setContentProvider(new ScriptsTreeContentProvider(false));
      treeViewer.setLabelProvider(new ScriptsTreeLabelProvider());

      // Drag and Drop
      int operations = DND.DROP_MOVE | DND.DROP_COPY;
      Transfer[] transferTypes = new Transfer[] { TextTransfer.getInstance() };
      treeViewer.addDragSupport(operations, transferTypes, new TemplateDragListener(treeViewer));
      treeViewer.addDropSupport(operations, transferTypes, new TemplateDropListener(treeViewer));

      Tree tree = treeViewer.getTree();

      // Manage selections
      treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            List<Object> sel = buildListObjectSelected((IStructuredSelection) event.getSelection());
            selectionService.setSelection(sel);
         }
      });

      // Add a Double Clic Listener
      treeViewer.addDoubleClickListener(new IDoubleClickListener() {

         @Override
         public void doubleClick(DoubleClickEvent event) {
            ITreeSelection sel = (ITreeSelection) event.getSelection();
            Object selected = sel.getFirstElement();
            if (selected instanceof Script) {
               // Call Script "Add or Edit" Command
               Map<String, Object> parameters = new HashMap<>();
               parameters.put(Constants.COMMAND_SCRIPTS_ADDEDIT_PARAM, Constants.COMMAND_SCRIPTS_ADDEDIT_EDIT);
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_SCRIPTS_ADDEDIT, parameters);
               handlerService.executeHandler(myCommand);
            }
         }
      });

      // Remove a Script or from the list
      tree.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.DEL) {
               IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }

               // Call Tempate Add or Edit Command
               Map<String, Object> parameters = new HashMap<>();
               parameters.put(Constants.COMMAND_SCRIPTS_RDD_PARAM, Constants.COMMAND_SCRIPTS_RDD_DELETE);
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_SCRIPTS_RDD, parameters);
               handlerService.executeHandler(myCommand);
            }
         }
      });

      // Populate tree with the scripts
      treeViewer.setInput(cm.getScripts().getDirectory());
      treeViewer.expandToLevel(2); // Expand first level

      // Attach the Popup Menu
      menuService.registerContextMenu(tree, Constants.SCRIPTS_POPUP_MENU);
   }

   // ---------------
   // Helper Classes
   // ---------------
   private List<Object> buildListObjectSelected(IStructuredSelection selection) {
      List<Object> l = new ArrayList<Object>(selection.size());
      for (Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
         l.add(iterator.next());
      }
      return l;
   }

   // -----------------------
   // Providers and Listeners
   // -----------------------

   private class TemplateDragListener extends DragSourceAdapter {
      private final TreeViewer treeViewer;

      private Directory        sourceDirectory;
      private Script           sourceScript;

      public TemplateDragListener(TreeViewer treeViewer) {
         this.treeViewer = treeViewer;
      }

      @Override
      public void dragStart(DragSourceEvent event) {
         log.debug("Start Drag");

         // Only allow one Script or Directory at a time (for now...)
         IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
         if ((selection == null) || (selection.size() != 1)) {
            event.doit = false;
            return;
         }

         if (selection.getFirstElement() instanceof Directory) {
            sourceDirectory = (Directory) selection.getFirstElement();
            sourceScript = null;
         }

         if (selection.getFirstElement() instanceof Script) {
            sourceDirectory = null;
            sourceScript = (Script) selection.getFirstElement();
         }
      }

      @Override
      public void dragSetData(DragSourceEvent event) {
         if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
            event.data = "unused";

            if (sourceScript == null) {
               DNDData.setDrag(DNDElement.DIRECTORY);
            } else {
               DNDData.setDrag(DNDElement.SCRIPT);
            }

            DNDData.setSourceDirectory(sourceDirectory);
            DNDData.setSourceScript(sourceScript);
         }
      }
   }

   private class TemplateDropListener extends ViewerDropAdapter {

      private TreeViewer treeViewer;
      private Directory  targetDirectory;
      private Script     targetScript;

      public TemplateDropListener(TreeViewer treeViewer) {
         super(treeViewer);
         this.treeViewer = treeViewer;

         this.setFeedbackEnabled(false); // Disable "in between" visual clues
      }

      @Override
      public void drop(DropTargetEvent event) {

         // Store the place where the file has beeen dropped
         Object t = determineTarget(event);
         log.debug("The drop was done on element: {}", t);
         if (t instanceof Directory) {
            targetDirectory = (Directory) t;
            targetScript = null;
            DNDData.setTargetDirectory(targetDirectory);
            DNDData.setDrop(DNDElement.DIRECTORY);
         }
         if (t instanceof Script) {
            targetDirectory = null;
            targetScript = (Script) t;
            DNDData.setDrop(DNDElement.SCRIPT);
         }
         DNDData.setTargetDirectory(targetDirectory);
         DNDData.setTargetScript(targetScript);

         super.drop(event);
      }

      @Override
      public boolean performDrop(Object data) {
         log.debug("performDrop: {}", DNDData.getDrag());

         switch (DNDData.getDrag()) {
            case DIRECTORY:
               // Get back the sourceDirectory
               Directory sourceDirectory = DNDData.getSourceDirectory();

               Directory newDirectory = null;
               if (DNDData.getDrop() == DNDElement.DIRECTORY) {
                  // Drop Directory on Directory
                  newDirectory = targetDirectory;
               } else {
                  // Drop Directory on Script
                  newDirectory = targetScript.getParent();
               }

               // Check if source and target share the same directory,If so, do nothing...
               if (sourceDirectory.getParent() == newDirectory) {
                  log.debug("Do nothing, both have the same Directory");
                  return false;
               }
               // Check if newDirectory has for ancestor sourceDirectory.. in this case do nothing
               Directory x = newDirectory.getParent();
               while (x != null) {
                  if (x == sourceDirectory) {
                     log.warn("D&D cancelled, newDirectory has for ancestor sourceDirectory");
                     return false;
                  }
                  x = x.getParent();
               }

               // Remove from initial Directory
               sourceDirectory.getParent().getDirectory().remove(sourceDirectory);

               newDirectory.getDirectory().add(sourceDirectory);
               sourceDirectory.setParent(newDirectory);

               // Save config
               try {
                  cm.scriptsWriteFile();
               } catch (JAXBException | CoreException e) {
                  // TODO What to do here?
                  log.error("Exception when writing Script config while using D&D");
                  return false;
               }

               // TODO Close open tabs

               // Refresh TreeViewer
               treeViewer.refresh();

               return true;

            case SCRIPT:
               // Get back the sourceScript
               Script sourceScript = DNDData.getSourceScript();

               newDirectory = null;
               if (DNDData.getDrop() == DNDElement.DIRECTORY) {
                  newDirectory = targetDirectory;
               } else {
                  newDirectory = targetScript.getParent();
               }

               // Check if source and target share the same directory, If so, do nothing...
               if (sourceScript.getParent() == newDirectory) {
                  log.debug("Do nothing, both have the same Directory");
                  return false;
               }

               // Remove from initial Directory
               sourceScript.getParent().getScript().remove(sourceScript);

               // Move Script to new Directory
               newDirectory.getScript().add(sourceScript);
               sourceScript.setParent(newDirectory);

               // Save config
               try {
                  cm.scriptsWriteFile();
               } catch (JAXBException | CoreException e) {
                  // TODO What to do here?
                  log.error("Exception when writing Script config while using D&D");
                  return false;
               }

               // TODO Close open tabs

               // Refresh TreeViewer
               treeViewer.refresh();

               return true;

            default:
               log.error("Drag & Drop operation not implemented? : {}", DNDData.getDrag());
               return false;
         }

      }

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferData) {
         return TextTransfer.getInstance().isSupportedType(transferData);
      }
   }

}

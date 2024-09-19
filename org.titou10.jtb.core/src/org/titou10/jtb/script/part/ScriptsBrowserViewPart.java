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
package org.titou10.jtb.script.part;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.script.ScriptsManager;
import org.titou10.jtb.script.ScriptsTreeContentProvider;
import org.titou10.jtb.script.ScriptsTreeLabelProvider;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.ui.dnd.DNDData;
import org.titou10.jtb.ui.dnd.DNDData.DNDElement;
import org.titou10.jtb.ui.dnd.TransferScript;
import org.titou10.jtb.util.Constants;

/**
 * Manage the Scripts Browser
 *
 * @author Denis Forveille
 *
 */
public class ScriptsBrowserViewPart {

   private static final Logger log = LoggerFactory.getLogger(ScriptsBrowserViewPart.class);

   @Inject
   private ScriptsManager      scriptsManager;

   @Inject
   private ECommandService     commandService;

   @Inject
   private EHandlerService     handlerService;

   @Inject
   private EMenuService        menuService;

   @Inject
   private ESelectionService   selectionService;

   @Inject
   private EModelService       modelService;

   @Inject
   private EPartService        partService;

   @Inject
   private MApplication        app;

   // JFaces components
   private TreeViewer          treeViewer;

   @Inject
   @Optional
   public void refresh(@UIEventTopic(Constants.EVENT_REFRESH_SCRIPTS_BROWSER) Directory d) {
      log.debug("UIEvent refresh Scripts");
      treeViewer.setInput(scriptsManager.getScripts().getDirectory());
      treeViewer.refresh();
      if (d != null) {
         treeViewer.expandToLevel(d, 1);
      }
   }

   @PostConstruct
   public void createControls(Composite parent) {
      treeViewer = new TreeViewer(parent, SWT.MULTI);
      treeViewer.setContentProvider(new ScriptsTreeContentProvider(false));
      treeViewer.setLabelProvider(new ScriptsTreeLabelProvider());

      // Drag and Drop
      int operations = DND.DROP_MOVE | DND.DROP_COPY;
      Transfer[] transferTypes = new Transfer[] { TransferScript.getInstance() };
      treeViewer.addDragSupport(operations, transferTypes, new TemplateDragListener(treeViewer));
      treeViewer.addDropSupport(operations, transferTypes, new TemplateDropListener(treeViewer));

      Tree tree = treeViewer.getTree();

      // Manage selections
      treeViewer.addSelectionChangedListener(event -> {
         List<Object> sel = buildListObjectSelected((IStructuredSelection) event.getSelection());
         selectionService.setSelection(sel);
      });

      // Add a Double Clic Listener
      treeViewer.addDoubleClickListener(event -> {
         ITreeSelection sel = (ITreeSelection) event.getSelection();
         Object selected = sel.getFirstElement();
         if (selected instanceof Script) {
            // Call Script "Add or Edit" Command
            Map<String, Object> parameters = new HashMap<>();
            parameters.put(Constants.COMMAND_SCRIPTS_ADDEDIT_PARAM, Constants.COMMAND_SCRIPTS_ADDEDIT_EDIT);
            ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_SCRIPTS_ADDEDIT, parameters);
            handlerService.executeHandler(myCommand);
         }
      });

      // Remove a Script or from the list
      tree.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
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
      }));

      // Populate tree with the scripts
      treeViewer.setInput(scriptsManager.getScripts().getDirectory());
      treeViewer.expandToLevel(2); // Expand first level

      // Attach the Popup Menu
      menuService.registerContextMenu(tree, Constants.SCRIPTS_POPUP_MENU);
   }

   // ---------------
   // Helper Classes
   // ---------------
   private List<Object> buildListObjectSelected(IStructuredSelection selection) {
      List<Object> l = new ArrayList<>(selection.size());
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
            DNDData.dragDirectory((Directory) selection.getFirstElement());
         }
         if (selection.getFirstElement() instanceof Script) {
            DNDData.dragScript((Script) selection.getFirstElement());
         }
      }

   }

   private class TemplateDropListener extends ViewerDropAdapter {

      public TemplateDropListener(TreeViewer treeViewer) {
         super(treeViewer);
         this.setFeedbackEnabled(false); // Disable "in between" visual clues
      }

      @Override
      public void drop(DropTargetEvent event) {

         // Store the element where the Script or Directory has beeen dropped
         Object target = determineTarget(event);
         log.debug("The drop was done on element: {}", target.getClass().getName());
         if (target instanceof Directory) {
            DNDData.dropOnDirectory((Directory) target);
         }
         if (target instanceof Script) {
            DNDData.dropOnScript((Script) target);
         }

         super.drop(event);
      }

      @Override
      public boolean performDrop(Object data) {
         log.debug("performDrop: {}", DNDData.getDrag());

         Directory targetDirectory = DNDData.getTargetDirectory();
         Script targetScript = DNDData.getTargetScript();

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

               // Copy or Move Directory
               if (getCurrentOperation() == DND.DROP_MOVE) {
                  // Close the tabs of the folder moved if some scripts from that folder are opened
                  final String directoryFullName = Constants.PART_SCRIPT_PREFIX + scriptsManager.getFullNameDots(sourceDirectory);
                  Selector s = new Selector() {
                     @Override
                     public boolean select(MApplicationElement element) {
                        if (element.getElementId().startsWith(directoryFullName)) {
                           return true;
                        } else {
                           return false;
                        }
                     }
                  };
                  List<MPart> parts = modelService.findElements(app, MPart.class, EModelService.ANYWHERE, s);
                  for (MPart mPart : parts) {
                     partService.hidePart(mPart, true);
                  }

                  // Move Directory
                  sourceDirectory.getParent().getDirectory().remove(sourceDirectory);
                  newDirectory.getDirectory().add(sourceDirectory);
                  sourceDirectory.setParent(newDirectory);
               } else {
                  Directory d = scriptsManager.cloneDirectory(sourceDirectory, sourceDirectory.getName(), newDirectory);
                  newDirectory.getDirectory().add(d);
               }

               // Save config
               try {
                  scriptsManager.writeConfig();
               } catch (JAXBException | CoreException e) {
                  // TODO What to do here?
                  log.error("Exception when writing Script config while using D&D");
                  return false;
               }

               // Refresh TreeViewer
               getViewer().refresh();

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

               // Copy or Move Script
               if (getCurrentOperation() == DND.DROP_MOVE) {
                  // Close the tab viever if it is opened for this script
                  String scriptFullName = scriptsManager.getFullNameDots(sourceScript);
                  String partName = Constants.PART_SCRIPT_PREFIX + scriptFullName;
                  MPart part = (MPart) modelService.find(partName, app);
                  if (part != null) {
                     partService.hidePart(part, true);
                  }

                  // Move Script
                  sourceScript.getParent().getScript().remove(sourceScript);
                  newDirectory.getScript().add(sourceScript);
                  sourceScript.setParent(newDirectory);
               } else {
                  Script newScript = scriptsManager.cloneScript(sourceScript, sourceScript.getName(), newDirectory);
                  newDirectory.getScript().add(newScript);
               }

               // Save config
               try {
                  scriptsManager.writeConfig();
               } catch (JAXBException | CoreException e) {
                  // TODO What to do here?
                  log.error("Exception when writing Script config while using Drag & Drop");
                  return false;
               }

               // Refresh TreeViewer
               getViewer().refresh();

               return true;

            default:
               log.warn("Drag & Drop operation not implemented? : {}", DNDData.getDrag());
               return false;
         }

      }

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferData) {
         return TransferScript.getInstance().isSupportedType(transferData);
      }
   }

}

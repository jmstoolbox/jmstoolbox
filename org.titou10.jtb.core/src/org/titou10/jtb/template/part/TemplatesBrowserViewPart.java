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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.template.TemplateTreeContentProvider;
import org.titou10.jtb.template.TemplateTreeLabelProvider;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.ui.dnd.TransferJTBMessage;
import org.titou10.jtb.ui.dnd.TransferTemplate;
import org.titou10.jtb.util.Constants;

/**
 * Manage the Template Browser
 *
 * @author Denis Forveille
 *
 */
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
   private TemplatesManager    templatesManager;

   // JFaces components
   private TreeViewer          treeViewer;

   @Inject
   @Optional
   public void refresh(@UIEventTopic(Constants.EVENT_REFRESH_TEMPLATES_BROWSER) String noUse) {
      log.debug("UIEvent refresh Templates");

      TreePath[] savedState = treeViewer.getExpandedTreePaths();

      try {
         templatesManager.reload();
      } catch (CoreException e) {
         log.error("Exception occurred when reloading templtaes", e);
         return;
      }
      treeViewer.setInput(templatesManager.getTemplateRootDirsFileStores());

      treeViewer.refresh();
      treeViewer.setExpandedTreePaths(savedState);
   }

   @PostConstruct
   public void createControls(Shell shell, Composite parent) {
      treeViewer = new TreeViewer(parent, SWT.MULTI);
      treeViewer.setContentProvider(new TemplateTreeContentProvider(false));
      treeViewer.setLabelProvider(new TemplateTreeLabelProvider(templatesManager));

      // Drag and Drop
      int operations = DND.DROP_MOVE | DND.DROP_COPY;
      Transfer[] transferTypesDrag = new Transfer[] { TransferTemplate.getInstance(), FileTransfer.getInstance() };
      Transfer[] transferTypesDrop = new Transfer[] { TransferTemplate.getInstance(), TransferJTBMessage.getInstance(),
                                                      FileTransfer.getInstance() };
      treeViewer.addDragSupport(operations, transferTypesDrag, new TemplatesDragListener(templatesManager, treeViewer));
      treeViewer.addDropSupport(operations,
                                transferTypesDrop,
                                new TemplatesDropListener(templatesManager, treeViewer, shell, commandService, handlerService));

      Tree tree = treeViewer.getTree();

      // Manage selections
      treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         @Override
         @SuppressWarnings("unchecked")
         public void selectionChanged(SelectionChangedEvent event) {
            // Store selected Message
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            List<IFileStore> fileStoresSelected = new ArrayList<IFileStore>(selection.toList());
            selectionService.setSelection(fileStoresSelected);
         }
      });

      // Add a Double Clic Listener
      treeViewer.addDoubleClickListener(new IDoubleClickListener() {

         @Override
         public void doubleClick(DoubleClickEvent event) {
            ITreeSelection sel = (ITreeSelection) event.getSelection();
            IFileStore selected = (IFileStore) sel.getFirstElement();
            if (!selected.fetchInfo().isDirectory()) {

               // Call Template "Add or Edit" Command
               Map<String, Object> parameters = new HashMap<>();
               parameters.put(Constants.COMMAND_TEMPLATE_ADDEDIT_PARAM, Constants.COMMAND_TEMPLATE_ADDEDIT_EDIT);
               ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_TEMPLATE_ADDEDIT, parameters);
               handlerService.executeHandler(myCommand);
            }
         }
      });

      // Remove a Template of Folder from the list
      tree.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
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
      }));

      // Populate tree with the content of the "Templates" folder
      treeViewer.setInput(templatesManager.getTemplateRootDirsFileStores());
      treeViewer.expandToLevel(2); // Expand first level

      // Attach the Popup Menu
      menuService.registerContextMenu(tree, Constants.TEMPLATES_POPUP_MENU);
   }

}

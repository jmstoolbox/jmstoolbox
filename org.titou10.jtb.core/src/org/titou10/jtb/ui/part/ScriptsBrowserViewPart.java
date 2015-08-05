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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
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
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.util.Constants;

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
   private ConfigManager cm;

   @Inject
   private ECommandService commandService;

   @Inject
   private EHandlerService handlerService;

   // JFaces components
   private TreeViewer treeViewer;

   @Inject
   @Optional
   public void refresh(@UIEventTopic(Constants.EVENT_REFRESH_SCRIPTS_BROWSER) String x) {
      log.debug("UIEvent refresh Scripts");
      treeViewer.refresh();
      // treeViewer.expandAll();
   }

   @PostConstruct
   public void createControls(Shell shell, Composite parent, EMenuService menuService, final ESelectionService selectionService) {
      treeViewer = new TreeViewer(parent, SWT.MULTI);
      treeViewer.setContentProvider(new ScriptsTreeContentProvider(false));
      treeViewer.setLabelProvider(new ScriptsTreeLabelProvider());

      Tree tree = treeViewer.getTree();

      // Manage selections
      treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            selectionService.setSelection(buildListObjectSelected((IStructuredSelection) event.getSelection()));
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
}

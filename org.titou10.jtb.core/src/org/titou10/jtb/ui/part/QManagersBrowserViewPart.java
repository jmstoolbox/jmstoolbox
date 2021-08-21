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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.config.MetaQManager;
import org.titou10.jtb.util.Constants;

/**
 * Manage the View Part with Q Managers
 *
 * @author Denis Forveille
 *
 */
public class QManagersBrowserViewPart {

   private static final Logger log = LoggerFactory.getLogger(QManagersBrowserViewPart.class);

   @Inject
   private ConfigManager       cm;

   @PostConstruct
   public void postConstruct(Composite parent,
                             EMenuService menuService,
                             final ESelectionService selectionService,
                             final ECommandService commandService,
                             final EHandlerService handlerService) {
      log.info("createControls");

      parent.setLayout(new FillLayout(SWT.HORIZONTAL));

      ListViewer listViewer = new ListViewer(parent, SWT.BORDER | SWT.V_SCROLL);
      listViewer.setLabelProvider(new MyLabelProvider());

      // Manage selections
      listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         @Override
         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            selectionService.setSelection(selection.getFirstElement());
         }
      });

      listViewer.addDoubleClickListener(new IDoubleClickListener() {
         @Override
         public void doubleClick(DoubleClickEvent arg0) {
            // Call the View Message Command
            ParameterizedCommand myCommand = commandService.createCommand(Constants.COMMAND_QM_CONFIGURE, null);
            handlerService.executeHandler(myCommand);
         }
      });

      // Attach Popup Menu
      menuService.registerContextMenu(listViewer.getList(), Constants.QMANAGER_POPUP_MENU);

      // Populate fields
      listViewer.setContentProvider(ArrayContentProvider.getInstance());
      listViewer.setInput(cm.getInstalledPlugins());

   }

   // -------
   // Helpers
   // -------
   private class MyLabelProvider extends LabelProvider {
      @Override
      public String getText(Object element) {
         MetaQManager wqm = (MetaQManager) element;
         return wqm.getDisplayName();
      }
   }
}

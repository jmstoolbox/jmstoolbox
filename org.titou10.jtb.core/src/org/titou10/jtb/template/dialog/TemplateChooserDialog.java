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
package org.titou10.jtb.template.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.titou10.jtb.template.TemplateTreeContentProvider;
import org.titou10.jtb.template.TemplateTreeLabelProvider;
import org.titou10.jtb.template.TemplatesManager;

/**
 * 
 * Dialog to choose a Template from the available Templates
 * 
 * @author Denis Forveille
 *
 */
public class TemplateChooserDialog extends Dialog {

   private TemplatesManager templatesManager;

   private IFileStore       selectedTemplate;
   private List<IFileStore> selectedFileStores = new ArrayList<>();

   private boolean          multi;

   public TemplateChooserDialog(Shell parentShell, TemplatesManager templatesManager, boolean multi) {
      super(parentShell);
      setShellStyle(SWT.BORDER | SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
      this.templatesManager = templatesManager;
      this.multi = multi;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Select a Template");
   }

   @Override
   protected Point getInitialSize() {
      return new Point(600, 400);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(1, false));

      TreeViewer treeViewer;
      if (multi) {
         treeViewer = new TreeViewer(container, SWT.MULTI);
      } else {
         treeViewer = new TreeViewer(container, SWT.NONE);
      }
      treeViewer.setContentProvider(new TemplateTreeContentProvider(false));
      treeViewer.setLabelProvider(new TemplateTreeLabelProvider(templatesManager));
      treeViewer.setInput(templatesManager.getTemplateRootDirsFileStores());
      treeViewer.expandToLevel(2);

      Tree tree = treeViewer.getTree();
      tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

      // Manage selections
      treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         @SuppressWarnings("unchecked")
         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            if (multi) {
               selectedFileStores.clear();
               selectedFileStores.addAll(sel.toList());
            } else {
               selectedTemplate = (IFileStore) sel.getFirstElement();
            }
         }
      });

      // Add a Double Click Listener
      treeViewer.addDoubleClickListener(new IDoubleClickListener() {

         @Override
         public void doubleClick(DoubleClickEvent event) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            selectedTemplate = (IFileStore) sel.getFirstElement();
            selectedFileStores.clear();
            selectedFileStores.add(selectedTemplate);
            okPressed();
         }
      });

      return container;
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public IFileStore getSelectedTemplate() {
      return selectedTemplate;
   }

   public List<IFileStore> getSelectedFileStores() {
      return selectedFileStores;
   }
}

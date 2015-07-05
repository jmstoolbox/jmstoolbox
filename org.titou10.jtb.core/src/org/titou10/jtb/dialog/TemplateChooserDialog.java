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
package org.titou10.jtb.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
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
import org.titou10.jtb.util.TemplateTreeContentProvider;
import org.titou10.jtb.util.TemplateTreeLabelProvider;

/**
 * 
 * Dialog to choose a Template from the available Templates
 * 
 * @author Denis Forveille
 *
 */
public class TemplateChooserDialog extends Dialog {

   private IFolder         templateFolder;
   private IFile           selectedFile;
   private List<IResource> selectedResources = new ArrayList<>();

   private boolean         multi;

   public TemplateChooserDialog(Shell parentShell, boolean multi, IFolder templateFolder) {
      super(parentShell);
      setShellStyle(SWT.BORDER | SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
      this.multi = multi;
      this.templateFolder = templateFolder;
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
      treeViewer.setLabelProvider(new TemplateTreeLabelProvider());
      treeViewer.setInput(new Object[] { templateFolder });
      treeViewer.expandToLevel(2);

      Tree tree = treeViewer.getTree();
      tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

      // Manage selections
      treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         @SuppressWarnings("unchecked")
         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            if (multi) {
               selectedResources.clear();
               for (Iterator<IResource> it = sel.iterator(); it.hasNext();) {
                  selectedResources.add(it.next());
               }
            } else {
               IResource selected = (IResource) sel.getFirstElement();
               if (selected instanceof IFile) {
                  selectedFile = (IFile) selected;
               }
            }
         }
      });

      // Add a Double Click Listener
      treeViewer.addDoubleClickListener(new IDoubleClickListener() {

         @Override
         public void doubleClick(DoubleClickEvent event) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            IResource selected = (IResource) sel.getFirstElement();
            if (selected instanceof IFile) {
               selectedFile = (IFile) selected;

               selectedResources.clear();
               selectedResources.add(selected);
               okPressed();
            }
         }
      });

      return container;
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public IFile getSelectedFile() {
      return selectedFile;
   }

   public List<IResource> getSelectedResources() {
      return selectedResources;
   }

}

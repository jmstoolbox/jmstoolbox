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

import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.titou10.jtb.template.TemplateTreeContentProvider;
import org.titou10.jtb.template.TemplateTreeLabelProvider;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.util.Constants;

/**
 * 
 * Dialog to save a Template
 * 
 * @author Denis Forveille
 *
 */
public class TemplateSaveDialog extends Dialog {

   private TemplatesManager templatesManager;
   private List<IFileStore> templatesDirectories;

   private String           templateName;
   private Text             txtFileName;

   private IFileStore       selectedFolder;
   private String           selectedFileName;

   public TemplateSaveDialog(Shell parentShell,
                             TemplatesManager templatesManager,
                             List<IFileStore> templatesDirectories,
                             IFileStore selectedFolder,
                             String templateName) {
      super(parentShell);
      setShellStyle(SWT.BORDER | SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
      this.templatesManager = templatesManager;
      this.templatesDirectories = templatesDirectories;
      this.selectedFolder = selectedFolder;
      this.templateName = templateName;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Save Template");
   }

   @Override
   protected Point getInitialSize() {
      return new Point(600, 600);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(1, false));

      TreeViewer treeViewer = new TreeViewer(container, SWT.NONE);
      treeViewer.setContentProvider(new TemplateTreeContentProvider(true));
      treeViewer.setLabelProvider(new TemplateTreeLabelProvider(templatesManager));
      treeViewer.setInput(templatesDirectories.toArray());

      ISelection sel = new StructuredSelection(selectedFolder);
      treeViewer.setSelection(sel);
      treeViewer.expandToLevel(selectedFolder, 1);

      Tree tree = treeViewer.getTree();
      tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

      // Manage selections
      treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            selectedFolder = (IFileStore) sel.getFirstElement();
         }
      });

      Composite container2 = new Composite(container, SWT.NONE);
      container2.setLayout(new GridLayout(2, false));
      container2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Label lblNewLabel = new Label(container2, SWT.NONE);
      lblNewLabel.setText("Template Name: ");

      txtFileName = new Text(container2, SWT.BORDER);
      txtFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      txtFileName.setText(templateName);

      return container;
   }

   @Override
   protected void okPressed() {

      // User must enter a filename
      selectedFileName = txtFileName.getText().trim();
      if (selectedFileName.isEmpty()) {
         return;
      }

      super.okPressed();
   }

   public IFileStore getSelectedPath() {
      return templatesManager.appendFilenameToFileStore(selectedFolder, selectedFileName + Constants.JTB_TEMPLATE_FILE_EXTENSION);
   }
}

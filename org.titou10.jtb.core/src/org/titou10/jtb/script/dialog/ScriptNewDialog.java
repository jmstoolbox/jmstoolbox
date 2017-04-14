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
package org.titou10.jtb.script.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.titou10.jtb.script.ScriptsTreeContentProvider;
import org.titou10.jtb.script.ScriptsTreeLabelProvider;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Scripts;

/**
 * 
 * Dialog for new Script Creation
 * 
 * @author Denis Forveille
 *
 */
public class ScriptNewDialog extends Dialog {

   private Scripts   scripts;
   private Directory selectedDirectory;

   private String    selectedScriptName;

   private Text      txtFileName;

   public ScriptNewDialog(Shell parentShell, Scripts scripts, Directory selectedDirectory) {
      super(parentShell);
      setShellStyle(SWT.BORDER | SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
      this.scripts = scripts;
      this.selectedDirectory = selectedDirectory;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Save Script");
   }

   @Override
   protected Point getInitialSize() {
      return new Point(600, 400);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(1, false));

      TreeViewer treeViewer = new TreeViewer(container, SWT.NONE);
      treeViewer.setContentProvider(new ScriptsTreeContentProvider(true));
      treeViewer.setLabelProvider(new ScriptsTreeLabelProvider());
      treeViewer.setInput(scripts.getDirectory());

      if (selectedDirectory != null) {
         ISelection sel = new StructuredSelection(selectedDirectory);
         treeViewer.setSelection(sel);
         treeViewer.expandToLevel(selectedDirectory, 1);
      }

      Tree tree = treeViewer.getTree();
      tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

      // Manage selections
      treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
         public void selectionChanged(SelectionChangedEvent event) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            selectedDirectory = (Directory) sel.getFirstElement();
         }
      });

      Composite container2 = new Composite(container, SWT.NONE);
      container2.setLayout(new GridLayout(2, false));
      container2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Label lblNewLabel = new Label(container2, SWT.NONE);
      lblNewLabel.setText("Script Name: ");

      txtFileName = new Text(container2, SWT.BORDER);
      txtFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      txtFileName.setFocus();

      return container;
   }

   @Override
   protected void okPressed() {

      // User must enter a filename
      selectedScriptName = txtFileName.getText().trim();
      if (selectedScriptName.isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "The name of the script is mandatory");
         return;
      }

      // Check for duplicates
      for (Script s : selectedDirectory.getScript()) {
         if (s.getName().equals(selectedScriptName)) {
            MessageDialog.openError(getShell(), "Error", "A script with the same name already exist in this folder");
            return;
         }
      }

      super.okPressed();
   }

   // ----------------
   // Standard Getters
   // ----------------
   public String getSelectedScriptName() {
      return selectedScriptName;
   }

   public Directory getSelectedDirectory() {
      return selectedDirectory;
   }

}

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
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Ask for a new Variable of kind "List"
 * 
 * @author Denis Forveille
 *
 */
public class VariablesListDialog extends Dialog {

   private static final Logger log    = LoggerFactory.getLogger(VariablesListDialog.class);

   private List<String>        values = new ArrayList<>();

   public VariablesListDialog(Shell parentShell) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Add a new 'List' variable");
   }

   protected Point getInitialSize() {
      return new Point(584, 399);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(3, false));

      Label lblNewLabel = new Label(container, SWT.NONE);
      lblNewLabel.setText("Value:");

      final Text newValue = new Text(container, SWT.BORDER);
      newValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnAdd = new Button(container, SWT.NONE);
      btnAdd.setText("Add");

      // Table

      Label lblJars_1 = new Label(container, SWT.NONE);
      lblJars_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
      lblJars_1.setText("Values:");

      final ListViewer listViewer = new ListViewer(container, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
      org.eclipse.swt.widgets.List listValues = listViewer.getList();
      listValues.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
      listValues.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent e) {
            if (e.keyCode == SWT.DEL) {
               IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
               if (selection.isEmpty()) {
                  return;
               }
               String[] items = listViewer.getList().getSelection();
               for (String item : items) {
                  log.debug("Remove {} from the list", item);
                  values.remove(item);
               }
               listViewer.refresh();
            }
         }
      });

      btnAdd.addSelectionListener(new SelectionAdapter() {
         // Add file name to the list of Jars
         @Override
         public void widgetSelected(SelectionEvent e) {
            String value = newValue.getText();
            if ((value != null) && (value.length() > 0)) {
               log.debug("Adding value {} to the list", value);
               values.add(value);
               listViewer.refresh();
            }
         }
      });

      // Populate fields
      listViewer.setContentProvider(ArrayContentProvider.getInstance());
      listViewer.setInput(values);

      return container;
   }

   // ----------------
   // Standard Getters
   // ----------------

   public List<String> getValues() {
      return values;
   }

}

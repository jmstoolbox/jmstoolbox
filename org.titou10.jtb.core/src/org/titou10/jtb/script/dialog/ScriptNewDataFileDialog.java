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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.titou10.jtb.script.gen.DataFile;
import org.titou10.jtb.script.gen.Script;

/**
 * 
 * Dialog to create a new Data File in a Script
 * 
 * @author Denis Forveille
 *
 */
public class ScriptNewDataFileDialog extends Dialog {

   private DataFile dataFile;
   private Script   script;
   private DataFile originalDataFile;

   // private Button btnScriptLevel;
   private Text     textPrefix;
   private Text     textDelimiter;
   private Text     textVariableNames;
   private Text     textFileName;

   public ScriptNewDataFileDialog(Shell parentShell, DataFile dataFile, Script script, DataFile originalDataFile) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
      this.dataFile = dataFile;
      this.script = script;
      this.originalDataFile = originalDataFile;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText(script.getName() + ": Add/Edit a data file");
   }

   protected Point getInitialSize() {
      return new Point(600, 271);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(3, false));

      // // Script Level
      //
      // Label lblScriptLevel = new Label(container, SWT.NONE);
      // lblScriptLevel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      // lblScriptLevel.setText("Script level?");
      //
      // btnScriptLevel = new Button(container, SWT.CHECK);
      // btnScriptLevel.setEnabled(false);
      // new Label(container, SWT.NONE);

      // Variable Prefix

      Label lbl2 = new Label(container, SWT.NONE);
      lbl2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl2.setText("Prefix for var. name in templates");

      textPrefix = new Text(container, SWT.BORDER);
      textPrefix.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
      new Label(container, SWT.NONE);

      // Variable Names

      Label lbl6 = new Label(container, SWT.NONE);
      lbl6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl6.setText("List of var. names (separate with ',')");

      textVariableNames = new Text(container, SWT.BORDER);
      textVariableNames.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      new Label(container, SWT.NONE);

      // Delimiter

      Label lbl3 = new Label(container, SWT.NONE);
      lbl3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl3.setText("Data delimiter in data file");

      textDelimiter = new Text(container, SWT.BORDER);
      textDelimiter.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
      new Label(container, SWT.NONE);

      // File name

      Label lbl1 = new Label(container, SWT.SHADOW_NONE);
      lbl1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl1.setAlignment(SWT.CENTER);
      lbl1.setText("Data file name");

      textFileName = new Text(container, SWT.BORDER);
      textFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnChooseTemplate = new Button(container, SWT.NONE);
      btnChooseTemplate.setText("Select...");
      btnChooseTemplate.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
            fileDialog.setText("Select the data file");

            String selectedFileName = fileDialog.open();
            if (selectedFileName == null) {
               return;
            }

            textFileName.setText(selectedFileName);
         }
      });

      // Populate Fields
      if (dataFile.getVariablePrefix() != null) {
         textPrefix.setText(dataFile.getVariablePrefix());
      }
      if (dataFile.getDelimiter() != null) {
         textDelimiter.setText(dataFile.getDelimiter());
      }
      if (dataFile.getVariableNames() != null) {
         textVariableNames.setText(dataFile.getVariableNames());
      }
      if (dataFile.getFileName() != null) {
         textFileName.setText(dataFile.getFileName());
      }

      return container;
   }

   @Override
   protected void okPressed() {

      if (textPrefix.getText().trim().isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "The variable prefix is mandatory");
         return;
      }

      if (textDelimiter.getText().trim().isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "The delimiter is mandatory");
         return;
      }

      if (textVariableNames.getText().trim().isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "At least one variable name must be defined");
         return;
      }

      if (textFileName.getText().trim().isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "The file name is mandatory");
         return;
      }

      // variablePrefix must be unique per script..
      for (DataFile df : script.getDataFile()) {
         if (df.getVariablePrefix().equals(textPrefix.getText().trim())) {
            if (df != originalDataFile) {
               MessageDialog.openError(getShell(), "Error", "Another data file already exist with the same variable prefix");
               return;
            }
         }
      }

      // Populate fields

      // dataFile.setScriptLevel(btnScriptLevel.getSelection());
      dataFile.setVariablePrefix(textPrefix.getText().trim());
      dataFile.setDelimiter(textDelimiter.getText().trim());
      dataFile.setVariableNames(textVariableNames.getText().trim());
      dataFile.setFileName(textFileName.getText().trim());

      // In case of varialePrefix change, alert the user
      if (originalDataFile != null) {
         if (!(originalDataFile.getVariablePrefix().equals(dataFile.getVariablePrefix()))) {
            StringBuilder sb = new StringBuilder(256);
            sb.append("The variable prefix for this data file changed!");
            sb.append("\r");
            sb.append("You must (re)attach this data file to steps and adapt the message templates");
            sb.append("\r\r");
            sb.append("The new variable syntax for this data file is: ${");
            sb.append(dataFile.getVariablePrefix());
            sb.append(".<var name>}");

            MessageDialog.openWarning(getShell(), "Warning", sb.toString());
         }
      }
      super.okPressed();
   }

   // ----------------
   // Standard Getters
   // ----------------

   public DataFile getDataFile() {
      return dataFile;
   }

}

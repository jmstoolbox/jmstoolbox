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
package org.titou10.jtb.variable.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.titou10.jtb.variable.gen.Variable;

/**
 * 
 * Ask for a new Variable of kind "Int"
 * 
 * @author Denis Forveille
 *
 */
public class VariablesIntDialog extends Dialog {

   private Variable variable;

   private Integer  min;
   private Integer  max;

   private Spinner  minSpinner;
   private Spinner  maxSpinner;

   public VariablesIntDialog(Shell parentShell, Variable variable) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.variable = variable;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Add a new 'Int' variable");
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(2, false));

      Label lblNewLabel = new Label(container, SWT.NONE);
      lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      lblNewLabel.setAlignment(SWT.CENTER);
      lblNewLabel.setBounds(0, 0, 49, 13);
      lblNewLabel.setText("Minimum Value: ");

      minSpinner = new Spinner(container, SWT.BORDER);
      minSpinner.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      minSpinner.setMaximum(2000000);
      minSpinner.setPageIncrement(100);

      Label lblNewLabel_1 = new Label(container, SWT.NONE);
      lblNewLabel_1.setText("Maximum Value: ");

      maxSpinner = new Spinner(container, SWT.BORDER);
      maxSpinner.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      maxSpinner.setMaximum(2000000);
      maxSpinner.setSelection(9999);
      maxSpinner.setPageIncrement(100);

      if (variable != null) {
         minSpinner.setSelection(variable.getMin());
         maxSpinner.setSelection(variable.getMax());
      }

      return container;
   }

   @Override
   protected void okPressed() {
      min = minSpinner.getSelection();
      max = maxSpinner.getSelection();
      if (min > max) {
         MessageDialog.openError(getShell(), "Invalid values", "'Max' value must be greater or equal to 'min' value");
         return;
      }

      super.okPressed();
   }

   // ----------------
   // Standard Getters
   // ----------------
   public Integer getMin() {
      return min;
   }

   public Integer getMax() {
      return max;
   }

}

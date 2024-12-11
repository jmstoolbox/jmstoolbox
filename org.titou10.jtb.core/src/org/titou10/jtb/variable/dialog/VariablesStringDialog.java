/*
 * Copyright (C) 2015-2025 Denis Forveille titou10.titou10@gmail.com
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

import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.titou10.jtb.variable.gen.Variable;
import org.titou10.jtb.variable.gen.VariableStringKind;

/**
 * 
 * Ask for a new Variable of kind "String"
 * 
 * @author Denis Forveille
 *
 */
public class VariablesStringDialog extends Dialog {

   private Variable           variable;

   private Integer            length;
   private VariableStringKind kind;
   private String             characters;

   private Button             btnAlphanumeric;
   private Button             btnAlphabetic;
   private Button             btnNumeric;
   private Button             btnUUID;
   private Button             btnCustom;
   private Spinner            lengthSpinner;
   private Text               textCharacters;

   public VariablesStringDialog(Shell parentShell, Variable variable) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.variable = variable;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Add a new 'String' variable");
   }

   @Override
   protected Point getInitialSize() {
      Point p = super.getInitialSize();
      return new Point(600, p.y);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(2, false));

      Label lblNewLabel = new Label(container, SWT.NONE);
      lblNewLabel.setText("Length: ");

      lengthSpinner = new Spinner(container, SWT.BORDER);
      lengthSpinner.setMinimum(1);
      lengthSpinner.setMaximum(512);
      lengthSpinner.setSelection(16);
      lengthSpinner.setPageIncrement(10);

      Label lblNewLabel1 = new Label(container, SWT.NONE);
      lblNewLabel1.setText("Kind: ");

      Composite compositeKind = new Composite(container, SWT.NONE);
      compositeKind.setLayout(new RowLayout(SWT.HORIZONTAL));

      btnAlphanumeric = new Button(compositeKind, SWT.RADIO);
      btnAlphanumeric.setText(VariableStringKind.ALPHANUMERIC.name());
      btnAlphanumeric.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(VariableStringKind.ALPHANUMERIC);
      }));

      btnAlphabetic = new Button(compositeKind, SWT.RADIO);
      btnAlphabetic.setText(VariableStringKind.ALPHABETIC.name());
      btnAlphabetic.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(VariableStringKind.ALPHABETIC);
      }));

      btnNumeric = new Button(compositeKind, SWT.RADIO);
      btnNumeric.setText(VariableStringKind.NUMERIC.name());
      btnNumeric.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(VariableStringKind.NUMERIC);
      }));

      btnUUID = new Button(compositeKind, SWT.RADIO);
      btnUUID.setText(VariableStringKind.UUID.name());
      btnUUID.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(VariableStringKind.UUID);
      }));

      btnCustom = new Button(compositeKind, SWT.RADIO);
      btnCustom.setText(VariableStringKind.CUSTOM.name());
      btnCustom.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(VariableStringKind.CUSTOM);
      }));

      Label lblNewLabel_2 = new Label(container, SWT.NONE);
      lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_2.setText("Characters: ");

      textCharacters = new Text(container, SWT.BORDER);
      textCharacters.setEnabled(false);
      textCharacters.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      // Initial Selection

      if (variable == null) {
         enableDisableControls(VariableStringKind.ALPHANUMERIC);
      } else {
         enableDisableControls(variable.getStringKind());
         lengthSpinner.setSelection(variable.getStringLength());
         if (variable.getStringChars() != null) {
            textCharacters.setText(variable.getStringChars());
         }
      }

      return container;
   }

   @Override
   protected void okPressed() {
      length = lengthSpinner.getSelection();

      if (kind == VariableStringKind.CUSTOM) {

         // The characters must contains at least one char
         characters = textCharacters.getText().trim();
         if (characters.isEmpty()) {
            MessageDialog.openError(getShell(), "Error", "Please enter at least one character");
            return;
         }

         // Remove duplicate chars
         final SortedSet<Character> set = new TreeSet<>();
         for (int i = 0; i < characters.length(); i++) {
            set.add(characters.charAt(i));
         }
         final StringBuilder sb = new StringBuilder(set.size());
         for (final Character character : set) {
            sb.append(character);
         }
         characters = sb.toString();
      } else {
         characters = null;
      }

      super.okPressed();
   }

   private void enableDisableControls(VariableStringKind pKind) {
      this.kind = pKind;

      switch (pKind) {
         case ALPHABETIC:
            btnAlphabetic.setSelection(true);
            btnAlphanumeric.setSelection(false);
            btnNumeric.setSelection(false);
            btnUUID.setSelection(false);
            btnCustom.setSelection(false);
            lengthSpinner.setEnabled(true);

            textCharacters.setEnabled(false);
            textCharacters.setText("");
            characters = null;
            break;

         case ALPHANUMERIC:
            btnAlphabetic.setSelection(false);
            btnAlphanumeric.setSelection(true);
            btnNumeric.setSelection(false);
            btnUUID.setSelection(false);
            btnCustom.setSelection(false);
            lengthSpinner.setEnabled(true);

            textCharacters.setEnabled(false);
            textCharacters.setText("");
            characters = null;
            break;

         case NUMERIC:
            btnAlphabetic.setSelection(false);
            btnAlphanumeric.setSelection(false);
            btnNumeric.setSelection(true);
            btnUUID.setSelection(false);
            btnCustom.setSelection(false);
            lengthSpinner.setEnabled(true);

            textCharacters.setEnabled(false);
            textCharacters.setText("");
            characters = null;
            break;

         case UUID:
            btnAlphabetic.setSelection(false);
            btnAlphanumeric.setSelection(false);
            btnNumeric.setSelection(false);
            btnUUID.setSelection(true);
            btnCustom.setSelection(false);
            lengthSpinner.setEnabled(false);

            textCharacters.setEnabled(false);
            textCharacters.setText("");
            characters = null;
            break;
         case CUSTOM:
            btnAlphabetic.setSelection(false);
            btnAlphanumeric.setSelection(false);
            btnNumeric.setSelection(false);
            btnUUID.setSelection(false);
            btnCustom.setSelection(true);
            lengthSpinner.setEnabled(true);

            textCharacters.setEnabled(true);
            textCharacters.setText("");
            characters = null;
            break;
         default:
            break;
      }
   }

   // ----------------
   // Standard Getters
   // ----------------
   public Integer getLength() {
      return length;
   }

   public VariableStringKind getKind() {
      return kind;
   }

   public String getCharacters() {
      return characters;
   }

}

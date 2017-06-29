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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.titou10.jtb.variable.gen.Variable;
import org.titou10.jtb.variable.gen.VariableDateTimeKind;
import org.titou10.jtb.variable.gen.VariableDateTimeOffsetTU;

/**
 * 
 * Ask for a new Variable of kind "Date"
 * 
 * @author Denis Forveille
 *
 */
public class VariablesDateDialog extends Dialog {

   private Variable                 variable;

   private VariableDateTimeKind     kind;
   private String                   pattern = "yyyy-MM-dd-HH:mm:ss:SSS";
   private Calendar                 min;
   private Calendar                 max;
   private VariableDateTimeOffsetTU offsetTU;
   private Integer                  offset;

   private Button                   btnStandard;
   private Button                   btnRange;
   private Button                   btnOffset;
   private Text                     txtPattern;
   private DateTime                 dateMin;
   private DateTime                 dateMax;
   private Spinner                  spinnerOffset;
   private Combo                    comboOffsetTU;

   public VariablesDateDialog(Shell parentShell, Variable variable) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
      this.variable = variable;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Add a new 'Date' variable");
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

      // Pattern

      Label lblNewLabel = new Label(container, SWT.NONE);
      lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      lblNewLabel.setAlignment(SWT.CENTER);
      lblNewLabel.setBounds(0, 0, 49, 13);
      lblNewLabel.setText("Date/Time Pattern: ");

      txtPattern = new Text(container, SWT.BORDER);
      txtPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      txtPattern.setBounds(0, 0, 76, 19);
      txtPattern.setText(pattern);

      // Kind

      Label lblNewLabel_1 = new Label(container, SWT.NONE);
      lblNewLabel_1.setText("Kind: ");

      Composite compositeKind = new Composite(container, SWT.NONE);
      compositeKind.setLayout(new RowLayout(SWT.HORIZONTAL));

      btnStandard = new Button(compositeKind, SWT.RADIO);
      btnStandard.setText("Standard");
      btnStandard.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(VariableDateTimeKind.STANDARD);
      }));

      btnRange = new Button(compositeKind, SWT.RADIO);
      btnRange.setText("Range");
      btnRange.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(VariableDateTimeKind.RANGE);
      }));

      btnOffset = new Button(compositeKind, SWT.RADIO);
      btnOffset.setText("Offset");
      btnOffset.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(VariableDateTimeKind.OFFSET);
      }));

      // Calendars

      Label lblMinimun = new Label(container, SWT.NONE);
      lblMinimun.setText("Minimun:");

      dateMin = new DateTime(container, SWT.BORDER | SWT.DROP_DOWN);
      dateMin.setEnabled(false);

      Label lblMaximum = new Label(container, SWT.NONE);
      lblMaximum.setText("Maximum:");

      dateMax = new DateTime(container, SWT.BORDER | SWT.DROP_DOWN);
      dateMax.setEnabled(false);

      // Offsets

      String[] offsetTUNames = new String[VariableDateTimeOffsetTU.values().length];
      int i = 0;
      for (VariableDateTimeOffsetTU offsetTU : VariableDateTimeOffsetTU.values()) {
         offsetTUNames[i++] = offsetTU.name();
      }
      int sel = 4; // MINUTES

      Label lblOffset = new Label(container, SWT.NONE);
      lblOffset.setText("Offset:");

      spinnerOffset = new Spinner(container, SWT.BORDER);
      spinnerOffset.setMinimum(-99999);
      spinnerOffset.setMaximum(99999);
      spinnerOffset.setSelection(1);
      spinnerOffset.setEnabled(false);

      Label lblOffsetTU = new Label(container, SWT.NONE);
      lblOffsetTU.setText("Offset Unit:");

      comboOffsetTU = new Combo(container, SWT.NONE);
      comboOffsetTU.setEnabled(false);
      comboOffsetTU.setItems(offsetTUNames);
      comboOffsetTU.select(sel);
      offsetTU = VariableDateTimeOffsetTU.values()[sel];
      // Save the selected property Kind
      comboOffsetTU.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         String sel2 = comboOffsetTU.getItem(comboOffsetTU.getSelectionIndex());
         offsetTU = VariableDateTimeOffsetTU.valueOf(sel2);
      }));

      // Link
      Link link = new Link(container, SWT.NONE);
      link.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
      link.setText("<a>Help on date/time patterns</a>");
      link.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         Program.launch("http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html");
      }));

      // Initial Selection

      if (variable == null) {
         enableDisableControls(VariableDateTimeKind.STANDARD);
      } else {
         enableDisableControls(variable.getDateTimeKind());

         if (variable.getDateTimePattern() != null) {
            txtPattern.setText(variable.getDateTimePattern());
         }
         min = variable.getDateTimeMin() == null ? null : variable.getDateTimeMin().toGregorianCalendar();
         if (min != null) {
            dateMin.setDate(min.get(Calendar.YEAR), min.get(Calendar.MONTH), min.get(Calendar.DAY_OF_MONTH));
         }
         max = variable.getDateTimeMax() == null ? null : variable.getDateTimeMax().toGregorianCalendar();
         if (max != null) {
            dateMax.setDate(max.get(Calendar.YEAR), max.get(Calendar.MONTH), max.get(Calendar.DAY_OF_MONTH));
         }
         if (variable.getDateTimeOffsetTU() != null) {
            comboOffsetTU.select(variable.getDateTimeOffsetTU().ordinal());
         }
         if (variable.getDateTimeOffset() != null) {
            spinnerOffset.setSelection(variable.getDateTimeOffset());
         }
      }

      return container;
   }

   @Override
   protected void okPressed() {
      pattern = txtPattern.getText().trim();
      if (pattern.isEmpty()) {
         MessageDialog.openError(getShell(), "Pattern Mandatory", "The pattern is mandatory");
         return;
      }

      // Check is the pattern is valid
      try {
         new SimpleDateFormat(pattern);
      } catch (Exception e) {
         MessageDialog.openError(getShell(), "Invalid pattern", "[" + pattern + "] is not a valid pattern");
         return;
      }

      // Validate range
      switch (kind) {
         case RANGE:
            int minD = dateMin.getDay();
            int minM = dateMin.getMonth();
            int minY = dateMin.getYear();
            min = new GregorianCalendar(minY, minM, minD);

            int maxD = dateMax.getDay();
            int maxM = dateMax.getMonth();
            int maxY = dateMax.getYear();
            max = new GregorianCalendar(maxY, maxM, maxD);

            if (min.after(max)) {
               MessageDialog.openError(getShell(), "Invalid Range", "Maximum date must be after mimimum date");
               return;
            }

            break;

         case OFFSET:
            offset = spinnerOffset.getSelection();
            min = null;
            max = null;
            break;

         default:
            min = null;
            max = null;
            break;
      }

      super.okPressed();
   }

   private void enableDisableControls(VariableDateTimeKind pKind) {
      this.kind = pKind;

      switch (pKind) {
         case OFFSET:
            btnOffset.setSelection(true);
            btnRange.setSelection(false);
            btnStandard.setSelection(false);

            dateMin.setEnabled(false);
            dateMax.setEnabled(false);
            spinnerOffset.setEnabled(true);
            comboOffsetTU.setEnabled(true);
            min = null;
            max = null;
            break;

         case RANGE:
            btnOffset.setSelection(false);
            btnRange.setSelection(true);
            btnStandard.setSelection(false);

            dateMin.setEnabled(true);
            dateMax.setEnabled(true);
            spinnerOffset.setEnabled(false);
            comboOffsetTU.setEnabled(false);
            min = null;
            max = null;
            break;

         case STANDARD:
            btnOffset.setSelection(false);
            btnRange.setSelection(false);
            btnStandard.setSelection(true);

            dateMin.setEnabled(false);
            dateMax.setEnabled(false);
            spinnerOffset.setEnabled(false);
            comboOffsetTU.setEnabled(false);
            min = null;
            max = null;
            break;

         default:
            break;
      }
   }

   // ----------------
   // Standard Getters
   // ----------------

   public String getPattern() {
      return pattern;
   }

   public VariableDateTimeOffsetTU getOffsetTU() {
      return offsetTU;
   }

   public Integer getOffset() {
      return offset;
   }

   public Calendar getMin() {
      return min;
   }

   public Calendar getMax() {
      return max;
   }

   public VariableDateTimeKind getKind() {
      return kind;
   }

}

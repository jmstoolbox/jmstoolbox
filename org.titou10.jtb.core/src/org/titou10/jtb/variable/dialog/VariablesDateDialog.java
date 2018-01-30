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
import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.titou10.jtb.util.Constants;
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

   private String                   pattern = Constants.TS_FORMAT;
   private Text                     txtPattern;

   private Button                   btnStandard;

   private Button                   btnRange;
   private Calendar                 min;
   private Calendar                 max;
   private CDateTime                dateMin;
   private CDateTime                dateMax;

   private Button                   btnOffset;
   private VariableDateTimeOffsetTU offsetTU;
   private Integer                  offset;
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
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(3, false));

      // Pattern

      Label lblNewLabel = new Label(container, SWT.NONE);
      lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel.setText("Pattern: ");

      txtPattern = new Text(container, SWT.BORDER);
      txtPattern.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      txtPattern.setText(pattern);

      // Link
      Link link = new Link(container, SWT.NONE);
      link.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      link.setText("<a>Help on date/time patterns</a>");
      link.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         Program.launch("http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html");
      }));

      // Standard

      Composite compositeStandard = new Composite(container, SWT.NONE);
      compositeStandard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
      compositeStandard.setLayout(new RowLayout(SWT.HORIZONTAL));

      btnStandard = new Button(compositeStandard, SWT.RADIO);
      btnStandard.setText("<current date>");
      btnStandard.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(VariableDateTimeKind.STANDARD);
      }));

      // Range

      GridLayout rangeLayout = new GridLayout(6, false);
      Composite compositeRange = new Composite(container, SWT.NONE);
      compositeRange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
      compositeRange.setLayout(rangeLayout);

      btnRange = new Button(compositeRange, SWT.RADIO);
      btnRange.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(VariableDateTimeKind.RANGE);
      }));

      Label lblMinimun = new Label(compositeRange, SWT.NONE);
      lblMinimun.setText("<current date> between");

      dateMin = new CDateTime(compositeRange, CDT.BORDER | CDT.CLOCK_12_HOUR | CDT.DROP_DOWN | CDT.TAB_FIELDS);
      dateMin.setPattern(Constants.TS_FORMAT);

      // Compute component width, add 15% for the drop down icon on the right
      GC gc = new GC(dateMin);
      Point p = gc.textExtent(Constants.TS_FORMAT);
      int width = (int) (p.x * 1.15);

      GridData gdMin = new GridData(SWT.LEFT, SWT.CENTER, false, true);
      gdMin.widthHint = width;
      dateMin.setLayoutData(gdMin);

      Label lblMaximum = new Label(compositeRange, SWT.NONE);
      lblMaximum.setText("and");

      dateMax = new CDateTime(compositeRange, CDT.BORDER | CDT.CLOCK_12_HOUR | CDT.DROP_DOWN | CDT.TAB_FIELDS);
      dateMax.setPattern(Constants.TS_FORMAT);
      GridData gdMax = new GridData(SWT.LEFT, SWT.CENTER, false, true);
      gdMax.widthHint = width;
      dateMax.setLayoutData(gdMax);

      // Offsets

      RowLayout offsetLayout = new RowLayout(SWT.HORIZONTAL);
      offsetLayout.center = true;

      Composite compositeOffset = new Composite(container, SWT.NONE);
      compositeOffset.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
      compositeOffset.setLayout(offsetLayout);

      btnOffset = new Button(compositeOffset, SWT.RADIO);
      btnOffset.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(VariableDateTimeKind.OFFSET);
      }));

      Label lblOffset = new Label(compositeOffset, SWT.NONE);
      lblOffset.setText("<current date> +/-");

      spinnerOffset = new Spinner(compositeOffset, SWT.BORDER);
      spinnerOffset.setMinimum(-99999);
      spinnerOffset.setMaximum(99999);
      spinnerOffset.setSelection(1);

      String[] offsetTUNames = new String[VariableDateTimeOffsetTU.values().length];
      int i = 0;
      for (VariableDateTimeOffsetTU offsetTU : VariableDateTimeOffsetTU.values()) {
         offsetTUNames[i++] = offsetTU.name();
      }
      int sel = 4; // MINUTES
      comboOffsetTU = new Combo(compositeOffset, SWT.NONE);
      comboOffsetTU.setItems(offsetTUNames);
      comboOffsetTU.select(sel);
      offsetTU = VariableDateTimeOffsetTU.values()[sel];
      // Save the selected time unit
      comboOffsetTU.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         String sel2 = comboOffsetTU.getItem(comboOffsetTU.getSelectionIndex());
         offsetTU = VariableDateTimeOffsetTU.valueOf(sel2);
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
            dateMin.setSelection(new Date(min.getTimeInMillis()));
         }
         max = variable.getDateTimeMax() == null ? null : variable.getDateTimeMax().toGregorianCalendar();
         if (max != null) {
            dateMax.setSelection(new Date(max.getTimeInMillis()));
         }
         if (variable.getDateTimeOffsetTU() != null) {
            comboOffsetTU.select(variable.getDateTimeOffsetTU().ordinal());
         }
         if (variable.getDateTimeOffset() != null) {
            spinnerOffset.setSelection(variable.getDateTimeOffset());
         }
      }

      dateMin.setSelection(new Date());
      dateMax.setSelection(new Date());

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
            min = new GregorianCalendar();
            if (dateMin.getSelection() == null) {
               MessageDialog.openError(getShell(), "Invalid Date", "Start date is mandatory");
               return;
            }
            min.setTimeInMillis(dateMin.getSelection().getTime());

            max = new GregorianCalendar();
            if (dateMax.getSelection() == null) {
               MessageDialog.openError(getShell(), "Invalid Date", "End date is mandatory");
               return;
            }
            max.setTimeInMillis(dateMax.getSelection().getTime());

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

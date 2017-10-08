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
package org.titou10.jtb.dialog;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.titou10.jtb.util.Utils;
import org.titou10.jtb.variable.gen.VariableDateTimeOffsetTU;

/**
 * 
 * Build a selector clause for the JMS properties of kind Timestamp
 * 
 * @author Denis Forveille
 *
 */
public class PropertyBuildSelectorDialog extends Dialog {

   private static final String[] OPERATOR_NAMES = new String[] { "=", ">", ">=", "<", "<=", "<>" };

   private enum SelectorKind {
                              STANDARD,
                              RANGE,
                              OFFSET
   }

   private String                   propertyName;
   private long                     timestamp;
   private String                   selector;

   private SelectorKind             kind;

   private Button                   btnStandard;
   private Calendar                 standard;
   private DateTime                 dateStandard;
   private String                   operator;

   private Button                   btnRange;
   private Calendar                 min;
   private Calendar                 max;
   private DateTime                 dateMin;
   private DateTime                 dateMax;

   private Button                   btnOffset;
   private VariableDateTimeOffsetTU offsetTU;
   private Integer                  offset;
   private Spinner                  spinnerOffset;
   private Combo                    comboOffsetTU;
   private Combo                    comboOperators;

   public PropertyBuildSelectorDialog(Shell parentShell, String propertyName, long timestamp) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
      this.propertyName = propertyName;
      this.timestamp = timestamp;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Build a selector for property '" + propertyName + "'");
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(1, false));

      // Operator + Date

      RowLayout standardLayout = new RowLayout(SWT.HORIZONTAL);
      standardLayout.center = true;
      Composite compositeStandard = new Composite(container, SWT.NONE);
      compositeStandard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      compositeStandard.setLayout(standardLayout);

      btnStandard = new Button(compositeStandard, SWT.RADIO);
      btnStandard.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(SelectorKind.STANDARD);
      }));

      Label lblOperator = new Label(compositeStandard, SWT.NONE);
      lblOperator.setText(propertyName);

      int sel = 0;
      comboOperators = new Combo(compositeStandard, SWT.NONE);
      comboOperators.setItems(OPERATOR_NAMES);
      comboOperators.select(sel);
      offsetTU = VariableDateTimeOffsetTU.values()[sel];
      comboOperators.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         operator = OPERATOR_NAMES[comboOperators.getSelectionIndex()];
      }));

      dateStandard = new DateTime(compositeStandard, SWT.BORDER | SWT.DROP_DOWN | SWT.CALENDAR);

      // Range

      RowLayout rangeLayout = new RowLayout(SWT.HORIZONTAL);
      rangeLayout.center = true;

      Composite compositeRange = new Composite(container, SWT.NONE);
      compositeRange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      compositeRange.setLayout(rangeLayout);

      btnRange = new Button(compositeRange, SWT.RADIO);
      btnRange.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(SelectorKind.RANGE);
      }));

      Label lblMinimun = new Label(compositeRange, SWT.NONE);
      lblMinimun.setText(propertyName + " between ");
      dateMin = new DateTime(compositeRange, SWT.BORDER | SWT.DROP_DOWN);

      Label lblMaximum = new Label(compositeRange, SWT.NONE);
      lblMaximum.setText("and");
      dateMax = new DateTime(compositeRange, SWT.BORDER | SWT.DROP_DOWN);

      // Offsets

      RowLayout offsetLayout = new RowLayout(SWT.HORIZONTAL);
      offsetLayout.center = true;

      Composite compositeOffset = new Composite(container, SWT.NONE);
      compositeOffset.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      compositeOffset.setLayout(offsetLayout);

      btnOffset = new Button(compositeOffset, SWT.RADIO);
      btnOffset.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(SelectorKind.OFFSET);
      }));

      Label lblOffset = new Label(compositeOffset, SWT.NONE);
      lblOffset.setText(propertyName + " = " + Utils.formatTimestamp(timestamp, false) + "±");

      spinnerOffset = new Spinner(compositeOffset, SWT.BORDER);
      spinnerOffset.setMinimum(-99999);
      spinnerOffset.setMaximum(99999);
      spinnerOffset.setSelection(1);

      String[] offsetTUNames = new String[VariableDateTimeOffsetTU.values().length];
      int i = 0;
      for (VariableDateTimeOffsetTU offsetTU : VariableDateTimeOffsetTU.values()) {
         offsetTUNames[i++] = offsetTU.name();
      }
      int selTU = 3; // HOURS
      comboOffsetTU = new Combo(compositeOffset, SWT.NONE);
      comboOffsetTU.setItems(offsetTUNames);
      comboOffsetTU.select(selTU);
      offsetTU = VariableDateTimeOffsetTU.values()[selTU];
      // Save the selected Time Unit
      comboOffsetTU.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         String sel2 = comboOffsetTU.getItem(comboOffsetTU.getSelectionIndex());
         offsetTU = VariableDateTimeOffsetTU.valueOf(sel2);
      }));

      // Initial Selection
      enableDisableControls(SelectorKind.STANDARD);

      if (timestamp > 0) {
         Calendar c = new GregorianCalendar();
         c.setTimeInMillis(timestamp);
         dateStandard.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
         dateMin.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
         dateMax.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
      }
      operator = OPERATOR_NAMES[0];

      return container;
   }

   @Override
   protected void okPressed() {

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
            int standardD = dateStandard.getDay();
            int standardM = dateStandard.getMonth();
            int standardY = dateStandard.getYear();

            long ts = new GregorianCalendar(standardY, standardM, standardD).getTimeInMillis();
            selector = propertyName + " " + operator + " " + ts;
            break;
      }

      super.okPressed();
   }

   private void enableDisableControls(SelectorKind pKind) {
      this.kind = pKind;

      switch (pKind) {
         case OFFSET:
            btnStandard.setSelection(false);
            dateStandard.setEnabled(false);
            comboOperators.setEnabled(false);
            standard = null;

            btnRange.setSelection(false);
            dateMin.setEnabled(false);
            dateMax.setEnabled(false);
            min = null;
            max = null;

            btnOffset.setSelection(true);
            spinnerOffset.setEnabled(true);
            comboOffsetTU.setEnabled(true);
            break;

         case RANGE:
            btnStandard.setSelection(false);
            dateStandard.setEnabled(false);
            comboOperators.setEnabled(false);
            standard = null;

            btnRange.setSelection(true);
            dateMin.setEnabled(true);
            dateMax.setEnabled(true);
            min = null;
            max = null;

            btnOffset.setSelection(false);
            spinnerOffset.setEnabled(false);
            comboOffsetTU.setEnabled(false);
            break;

         case STANDARD:
            btnStandard.setSelection(true);
            dateStandard.setEnabled(true);
            comboOperators.setEnabled(true);

            btnRange.setSelection(false);
            dateMin.setEnabled(false);
            dateMax.setEnabled(false);
            min = null;
            max = null;

            btnOffset.setSelection(false);
            spinnerOffset.setEnabled(false);
            comboOffsetTU.setEnabled(false);
            break;

         default:
            break;
      }
   }

   // ----------------
   // Standard Getters
   // ----------------

   public String getSelector() {
      return selector;
   }

}

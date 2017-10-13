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

import java.util.Date;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.titou10.jtb.util.Constants;

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
                              RANGE
   }

   private String       propertyName;
   private long         timestamp;
   private String       selector;

   private SelectorKind kind;

   private Button       btnStandard;
   private CDateTime    dateStandard;
   private String       operator;
   private Combo        comboOperators;

   private Button       btnRange;
   private CDateTime    dateMin;
   private CDateTime    dateMax;

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

      GridLayout standardLayout = new GridLayout(4, false);
      Composite compositeStandard = new Composite(container, SWT.NONE);
      compositeStandard.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, true, 1, 1));
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
      comboOperators.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         operator = OPERATOR_NAMES[comboOperators.getSelectionIndex()];
      }));

      dateStandard = new CDateTime(compositeStandard, CDT.BORDER | CDT.CLOCK_12_HOUR | CDT.DROP_DOWN | CDT.TAB_FIELDS);
      dateStandard.setPattern(Constants.TS_FORMAT);

      // Compute component width, add 15% for the drop down icon on the right
      GC gc = new GC(dateStandard);
      Point p = gc.textExtent(Constants.TS_FORMAT);
      int width = (int) (p.x * 1.15);

      GridData gdStandard = new GridData(SWT.LEFT, SWT.CENTER, false, true);
      gdStandard.widthHint = width;
      dateStandard.setLayoutData(gdStandard);

      // Range

      GridLayout rangeLayout = new GridLayout(5, false);
      Composite compositeRange = new Composite(container, SWT.NONE);
      compositeRange.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      compositeRange.setLayout(rangeLayout);

      btnRange = new Button(compositeRange, SWT.RADIO);
      btnRange.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         enableDisableControls(SelectorKind.RANGE);
      }));

      Label lblMinimun = new Label(compositeRange, SWT.NONE);
      lblMinimun.setText(propertyName + " between ");
      dateMin = new CDateTime(compositeRange, CDT.BORDER | CDT.CLOCK_12_HOUR | CDT.DROP_DOWN | CDT.TAB_FIELDS);
      dateMin.setPattern(Constants.TS_FORMAT);
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

      // Initial Selection
      enableDisableControls(SelectorKind.STANDARD);

      Date d = new Date();
      if (timestamp > 0) {
         d = new Date(timestamp);
      }
      dateStandard.setSelection(d);
      dateMin.setSelection(d);
      dateMax.setSelection(d);
      operator = OPERATOR_NAMES[0];

      return container;
   }

   @Override
   protected void okPressed() {

      // Compute Selector
      switch (kind) {
         case RANGE:
            if (dateMin.getSelection() == null) {
               MessageDialog.openError(getShell(), "Invalid Date", "Start date is mandatory");
               return;
            }
            if (dateMax.getSelection() == null) {
               MessageDialog.openError(getShell(), "Invalid Date", "End date is mandatory");
               return;
            }

            long start = dateMin.getSelection().getTime();
            long end = dateMax.getSelection().getTime();
            if (start > end) {
               MessageDialog.openError(getShell(), "Invalid Range", "Start date must be anterior to end date");
               return;
            }
            selector = propertyName + " between " + start + " and " + end;
            break;

         default:
            if (dateStandard.getSelection() == null) {
               MessageDialog.openError(getShell(), "Invalid Date", "Please enter a date");
               return;
            }
            selector = propertyName + " " + operator + " " + dateStandard.getSelection().getTime();
            break;
      }

      super.okPressed();
   }

   private void enableDisableControls(SelectorKind pKind) {
      this.kind = pKind;

      switch (pKind) {
         case RANGE:
            btnStandard.setSelection(false);
            dateStandard.setEnabled(false);
            comboOperators.setEnabled(false);

            btnRange.setSelection(true);
            dateMin.setEnabled(true);
            dateMax.setEnabled(true);

            break;
         case STANDARD:
            btnStandard.setSelection(true);
            dateStandard.setEnabled(true);
            comboOperators.setEnabled(true);

            btnRange.setSelection(false);
            dateMin.setEnabled(false);
            dateMax.setEnabled(false);

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

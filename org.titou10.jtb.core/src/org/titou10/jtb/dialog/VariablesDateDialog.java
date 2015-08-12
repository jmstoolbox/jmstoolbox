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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.titou10.jtb.variable.gen.VariableDateTimeKind;

/**
 * 
 * Ask for a new Variable of kind "Date"
 * 
 * @author Denis Forveille
 *
 */
public class VariablesDateDialog extends Dialog {

   private VariableDateTimeKind kind;
   private String               pattern = "yyyy-MM-dd-HH:mm:ss:SSS";
   private Calendar             min;
   private Calendar             max;

   private Text                 txtPattern;
   private DateTime             dateMin;
   private DateTime             dateMax;

   public VariablesDateDialog(Shell parentShell) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Add a new 'Date' variable");
   }

   protected Point getInitialSize() {
      return new Point(649, 456);
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

      Button btnStandard = new Button(compositeKind, SWT.RADIO);
      btnStandard.setText("Standard");
      btnStandard.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            kind = VariableDateTimeKind.STANDARD;
            dateMin.setEnabled(false);
            dateMax.setEnabled(false);
            min = null;
            max = null;
         }
      });

      Button btnRange = new Button(compositeKind, SWT.RADIO);
      btnRange.setText("Range");
      btnRange.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            kind = VariableDateTimeKind.RANGE;
            dateMin.setEnabled(true);
            dateMax.setEnabled(true);
            min = null;
            max = null;
         }
      });

      // Calendars

      Label lblMinimun = new Label(container, SWT.NONE);
      lblMinimun.setText("Minimun:");

      dateMin = new DateTime(container, SWT.BORDER | SWT.DROP_DOWN);
      dateMin.setEnabled(false);

      Label lblMaximum = new Label(container, SWT.NONE);
      lblMaximum.setText("Maximum:");

      dateMax = new DateTime(container, SWT.BORDER | SWT.DROP_DOWN);
      dateMax.setEnabled(false);

      // Link
      Link link = new Link(container, SWT.NONE);
      link.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 2, 1));
      link.setText("<a>Help on date/time patterns</a>");
      link.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            Program.launch("http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html");
         }
      });

      // Initial Selection
      btnStandard.setSelection(true);
      kind = VariableDateTimeKind.STANDARD;

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
      if (kind == VariableDateTimeKind.RANGE) {
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
      } else {
         min = null;
         max = null;
      }

      super.okPressed();
   }

   // ----------------
   // Standard Getters
   // ----------------

   public String getPattern() {
      return pattern;
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

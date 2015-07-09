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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * Dialog to create a new Step in a Script
 * 
 * @author Denis Forveille
 *
 */
public class ScriptsNewStepDialog extends Dialog {

   private String  templateName;
   private String  sessionName;
   private String  destinationName;
   private Integer delay;
   private Integer iterations;

   private Text    txtTemplateName;
   private Text    txtSessionName;
   private Text    txtDestinationName;
   private Spinner delaySpinner;
   private Spinner iterationsSpinner;

   public ScriptsNewStepDialog(Shell parentShell) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Add a new Step");
   }

   protected Point getInitialSize() {
      return new Point(649, 456);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(3, false));

      // Pattern

      Label lblNewLabel = new Label(container, SWT.NONE);
      lblNewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      lblNewLabel.setAlignment(SWT.CENTER);
      lblNewLabel.setBounds(0, 0, 49, 13);
      lblNewLabel.setText("Template Name");

      txtTemplateName = new Text(container, SWT.BORDER);
      txtTemplateName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
      txtTemplateName.setBounds(0, 0, 76, 19);

      // Kind

      Label lblNewLabel_1 = new Label(container, SWT.NONE);
      lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_1.setText("Session Name");

      txtSessionName = new Text(container, SWT.BORDER);
      txtSessionName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

      Label lblNewLabel_2 = new Label(container, SWT.NONE);
      lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_2.setText("Destination Name");

      txtDestinationName = new Text(container, SWT.BORDER);
      txtDestinationName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

      Label lblNewLabel_3 = new Label(container, SWT.NONE);
      lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_3.setText("Pause");

      delaySpinner = new Spinner(container, SWT.BORDER);
      delaySpinner.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      delaySpinner.setMaximum(600);

      Label lblNewLabel_4 = new Label(container, SWT.NONE);
      lblNewLabel_4.setText("second(s) after this step");

      Label lblNewLabel_5 = new Label(container, SWT.NONE);
      lblNewLabel_5.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));
      lblNewLabel_5.setText("Repeat this step");

      iterationsSpinner = new Spinner(container, SWT.BORDER);
      iterationsSpinner.setMinimum(1);
      iterationsSpinner.setSelection(1);

      Label lblNewLabel_6 = new Label(container, SWT.NONE);
      lblNewLabel_6.setText("time(s)");

      return container;
   }

   @Override
   protected void okPressed() {

      templateName = txtTemplateName.getText().trim();
      if (templateName.isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "The name of the template is mandatory");
         return;
      }

      sessionName = txtSessionName.getText().trim();
      if (sessionName.isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "The name of the session is mandatory");
         return;
      }

      destinationName = txtDestinationName.getText().trim();
      if (destinationName.isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "The name of the destination is mandatory");
         return;
      }

      delay = delaySpinner.getSelection();
      iterations = iterationsSpinner.getSelection();

      super.okPressed();
   }

   // ----------------
   // Standard Getters
   // ----------------

   public String getDestinationName() {
      return destinationName;
   }

   public String getTemplateName() {
      return templateName;
   }

   public String getSessionName() {
      return sessionName;
   }

   public Integer getDelay() {
      return delay;
   }

   public Integer getIterations() {
      return iterations;
   }

}

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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.titou10.jtb.config.ConfigManager;

/**
 * 
 * Dialog to create a new Step in a Script
 * 
 * @author Denis Forveille
 *
 */
public class ScriptsNewStepDialog extends Dialog {

   private ConfigManager cm;

   private String  templateName    = "";
   private String  sessionName     = "";
   private String  destinationName = "";
   private Integer delay;
   private Integer iterations;

   private Text    txtDestinationName;
   private Spinner delaySpinner;
   private Spinner iterationsSpinner;

   public ScriptsNewStepDialog(Shell parentShell, ConfigManager cm) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.APPLICATION_MODAL);
      this.cm = cm;
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

      // Template

      Label lbl1 = new Label(container, SWT.NONE);
      lbl1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl1.setAlignment(SWT.CENTER);
      lbl1.setBounds(0, 0, 49, 13);
      lbl1.setText("Template Name");

      final Label lblTemplateName = new Label(container, SWT.NONE);
      lblTemplateName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnChooseTemplate = new Button(container, SWT.NONE);
      btnChooseTemplate.setText("Choose...");
      btnChooseTemplate.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            // Dialog to choose a template
            TemplateChooserDialog dialog1 = new TemplateChooserDialog(getShell(), false, cm.getTemplateFolder());
            if (dialog1.open() == Window.OK) {

               IFile template = dialog1.getSelectedFile();
               templateName = "/" + template.getProjectRelativePath().removeFirstSegments(1).toPortableString();
               lblTemplateName.setText(templateName);
            }
         }
      });

      // Session

      Label lbl2 = new Label(container, SWT.NONE);
      lbl2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl2.setText("Session Name");

      final Label lblSessionName = new Label(container, SWT.NONE);
      lblSessionName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnChooseSession = new Button(container, SWT.NONE);
      btnChooseSession.setText("Choose...");
      btnChooseSession.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            // Dialog to choose a template
            SessionChooserDialog dialog1 = new SessionChooserDialog(getShell());
            if (dialog1.open() == Window.OK) {

               IFile template = dialog1.getSelectedFile();
               sessionName = "/" + template.getProjectRelativePath().removeFirstSegments(1).toPortableString();
               lblSessionName.setText(sessionName);
            }
         }
      });

      // Destination

      Label lblNewLabel_2 = new Label(container, SWT.NONE);
      lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_2.setText("Destination Name");

      txtDestinationName = new Text(container, SWT.BORDER);
      txtDestinationName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
      new Label(container, SWT.NONE);

      Label lblNewLabel_3 = new Label(container, SWT.NONE);
      lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_3.setText("Pause");

      Composite composite = new Composite(container, SWT.NONE);
      composite.setLayout(new FillLayout(SWT.HORIZONTAL));

      delaySpinner = new Spinner(composite, SWT.BORDER);
      delaySpinner.setMaximum(600);

      Label lblNewLabel_4 = new Label(composite, SWT.NONE);
      lblNewLabel_4.setText("second(s) after this step");
      new Label(container, SWT.NONE);

      Label lblNewLabel_5 = new Label(container, SWT.NONE);
      lblNewLabel_5.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));
      lblNewLabel_5.setText("Repeat this step");

      Composite composite_1 = new Composite(container, SWT.NONE);
      composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));

      iterationsSpinner = new Spinner(composite_1, SWT.BORDER);
      iterationsSpinner.setMinimum(1);
      iterationsSpinner.setSelection(1);

      Label lblNewLabel_6 = new Label(composite_1, SWT.NONE);
      lblNewLabel_6.setText("time(s)");
      new Label(container, SWT.NONE);

      return container;
   }

   @Override
   protected void okPressed() {

      if (templateName.isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "The name of the template is mandatory");
         return;
      }

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

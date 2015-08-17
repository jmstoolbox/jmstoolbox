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
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.script.gen.Step;

/**
 * 
 * Dialog to create a new Step in a Script
 * 
 * @author Denis Forveille
 *
 */
public class ScriptNewStepDialog extends Dialog {

   private ConfigManager cm;
   private Step          step;

   private String  templateName;
   private String  sessionName;
   private String  destinationName;
   private Integer delay;
   private Integer iterations;

   private Text    txtDestinationName;
   private Spinner delaySpinner;
   private Spinner iterationsSpinner;
   private Label   lblTemplateName;
   private Label   lblSessionName;

   public ScriptNewStepDialog(Shell parentShell, ConfigManager cm, Step step) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
      this.cm = cm;
      this.step = step;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Step");
   }

   protected Point getInitialSize() {
      return new Point(649, 456);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(3, false));

      // Template

      Label lbl1 = new Label(container, SWT.SHADOW_NONE);
      lbl1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl1.setAlignment(SWT.CENTER);
      lbl1.setBounds(0, 0, 49, 13);
      lbl1.setText("Template Name");

      Button btnChooseTemplate = new Button(container, SWT.NONE);
      btnChooseTemplate.setText("Choose...");
      btnChooseTemplate.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            // Dialog to choose a template
            TemplateChooserDialog dialog1 = new TemplateChooserDialog(getShell(), false, cm.getTemplateFolder());
            if (dialog1.open() == Window.OK) {

               IFile template = dialog1.getSelectedFile();
               if (template != null) {
                  templateName = template.getProjectRelativePath().removeFirstSegments(1).toPortableString();
                  lblTemplateName.setText(templateName);
               }
            }
         }
      });

      lblTemplateName = new Label(container, SWT.BORDER | SWT.SHADOW_NONE);
      lblTemplateName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      // Session

      Label lbl2 = new Label(container, SWT.NONE);
      lbl2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl2.setText("Session Name");

      Button btnChooseSession = new Button(container, SWT.NONE);
      btnChooseSession.setText("Choose...");
      btnChooseSession.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            // Dialog to choose a template
            SessionChooserDialog dialog1 = new SessionChooserDialog(getShell(), cm);
            if (dialog1.open() == Window.OK) {

               JTBSession jtbSession = dialog1.getSelectedJTBSession();
               if (jtbSession != null) {
                  sessionName = jtbSession.getName();
                  lblSessionName.setText(sessionName);
               }
            }
         }
      });

      lblSessionName = new Label(container, SWT.BORDER | SWT.SHADOW_NONE);
      lblSessionName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      // Destination

      Label lblNewLabel_2 = new Label(container, SWT.NONE);
      lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_2.setText("Destination Name");

      txtDestinationName = new Text(container, SWT.BORDER);
      txtDestinationName.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));

      // Pause

      Label lblNewLabel_3 = new Label(container, SWT.NONE);
      lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_3.setText("Pause");

      Composite composite = new Composite(container, SWT.NONE);
      composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      GridLayout gl_composite = new GridLayout(2, false);
      gl_composite.marginWidth = 0;
      composite.setLayout(gl_composite);

      delaySpinner = new Spinner(composite, SWT.BORDER);
      delaySpinner.setMaximum(600);

      Label lblNewLabel_4 = new Label(composite, SWT.NONE);
      lblNewLabel_4.setText(" second(s) after this step");

      // Repeat

      Label lblNewLabel_5 = new Label(container, SWT.NONE);
      lblNewLabel_5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblNewLabel_5.setText("Repeat this step");

      Composite composite_1 = new Composite(container, SWT.NONE);
      composite_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      GridLayout gl_composite_1 = new GridLayout(2, false);
      gl_composite_1.marginWidth = 0;
      composite_1.setLayout(gl_composite_1);

      iterationsSpinner = new Spinner(composite_1, SWT.BORDER);
      iterationsSpinner.setMinimum(1);
      iterationsSpinner.setSelection(1);

      Label lblNewLabel_6 = new Label(composite_1, SWT.NONE);
      lblNewLabel_6.setText(" time(s)");
      new Label(container, SWT.NONE);

      // Populate Fields
      templateName = step.getTemplateName();
      sessionName = step.getSessionName();
      destinationName = step.getDestinationName();
      delay = step.getPauseSecsAfter();
      iterations = step.getIterations();

      lblTemplateName.setText(templateName);
      lblSessionName.setText(sessionName);
      txtDestinationName.setText(destinationName);
      delaySpinner.setSelection(delay);
      iterationsSpinner.setSelection(iterations);
      new Label(container, SWT.NONE);
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

      // Populate fields

      step.setTemplateName(templateName);
      step.setSessionName(sessionName);
      step.setDestinationName(destinationName);
      step.setPauseSecsAfter(delaySpinner.getSelection());
      step.setIterations(iterationsSpinner.getSelection());

      super.okPressed();
   }

   // ----------------
   // Standard Getters
   // ----------------
   public Step getStep() {
      return step;
   }

}

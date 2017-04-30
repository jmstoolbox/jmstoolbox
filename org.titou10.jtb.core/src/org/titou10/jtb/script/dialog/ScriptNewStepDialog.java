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
package org.titou10.jtb.script.dialog;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.script.ScriptsManager;
import org.titou10.jtb.script.gen.DataFile;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.template.dialog.TemplateChooserDialog;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Utils;

/**
 * 
 * Dialog to create a new Step in a Script
 * 
 * @author Denis Forveille
 *
 */
public class ScriptNewStepDialog extends Dialog {

   private static final Logger log = LoggerFactory.getLogger(ScriptNewStepDialog.class);

   private JTBStatusReporter   jtbStatusReporter;

   private ConfigManager       cm;
   private ScriptsManager      scriptsManager;
   private Step                step;
   private Script              script;

   private Boolean             isFolder;
   private String              templateName;
   private String              sessionName;
   private String              destinationName;
   private String              variablePrefix;
   private String              payloadDirectory;
   private Integer             delay;
   private Integer             iterations;

   private Label               lblTemplateName;
   private Label               lblSessionName;
   private Label               lblDestinationName;
   private Label               lblDataFile;
   private Label               lblPayloadDirectory;
   private Spinner             delaySpinner;
   private Spinner             iterationsSpinner;

   private Button              btnChooseDestination;

   public ScriptNewStepDialog(Shell parentShell,
                              JTBStatusReporter jtbStatusReporter,
                              ConfigManager cm,
                              ScriptsManager scriptsManager,
                              Step step,
                              Script script) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
      this.jtbStatusReporter = jtbStatusReporter;
      this.cm = cm;
      this.scriptsManager = scriptsManager;
      this.step = step;
      this.script = script;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText(script.getName() + ": Add/Edit a step");
   }

   @Override
   protected Point getInitialSize() {
      Point p = super.getInitialSize();
      return new Point(600, p.y);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      final Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(3, false));

      // Template

      final Label lbl1 = new Label(container, SWT.SHADOW_NONE | SWT.CENTER);
      lbl1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl1.setText("Template:");

      lblTemplateName = new Label(container, SWT.BORDER | SWT.SHADOW_NONE);
      lblTemplateName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnChooseTemplate = new Button(container, SWT.NONE);
      btnChooseTemplate.setText("Select...");
      btnChooseTemplate.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            // Dialog to choose a template
            TemplateChooserDialog dialog1 = new TemplateChooserDialog(getShell(), false, true, cm.getTemplateFolder());
            if (dialog1.open() == Window.OK) {

               IResource template = dialog1.getSelectedResource();
               if (template != null) {

                  templateName = "/" + template.getProjectRelativePath().removeFirstSegments(1).toPortableString();

                  if (template instanceof IFile) {
                     isFolder = false;
                  } else {
                     isFolder = true;
                  }
                  lblTemplateName.setText(scriptsManager.getTemplateDisplayName(isFolder, templateName));
               }
            }
         }
      });

      // Session

      Label lbl2 = new Label(container, SWT.NONE);
      lbl2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl2.setText("Session:");

      lblSessionName = new Label(container, SWT.BORDER | SWT.SHADOW_NONE);
      lblSessionName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnChooseSession = new Button(container, SWT.NONE);
      btnChooseSession.setText("Select...");
      btnChooseSession.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            // Dialog to choose a Session
            SessionChooserDialog dialog1 = new SessionChooserDialog(getShell(), cm);
            if (dialog1.open() == Window.OK) {

               JTBSession jtbSession = dialog1.getSelectedJTBSession();
               if (jtbSession != null) {
                  // Reset Destination if session name changed
                  if (!(sessionName.equals(jtbSession.getName()))) {
                     destinationName = "";
                     lblDestinationName.setText(destinationName);
                  }
                  sessionName = jtbSession.getName();
                  lblSessionName.setText(sessionName);

                  btnChooseDestination.setEnabled(true);
               }
            }
         }
      });

      // Destination

      Label lbl3 = new Label(container, SWT.NONE);
      lbl3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl3.setText("Destination:");

      lblDestinationName = new Label(container, SWT.BORDER | SWT.SHADOW_NONE);
      lblDestinationName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      btnChooseDestination = new Button(container, SWT.NONE);
      btnChooseDestination.setText("Select...");
      btnChooseDestination.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            // Connect to session, get list of destinations
            final JTBSession jtbSession = cm.getJTBSessionByName(sessionName);
            final JTBConnection jtbConnection = jtbSession.getJTBConnection(JTBSessionClientType.SCRIPT);
            if (!(jtbConnection.isConnected())) {

               BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
                  @Override
                  public void run() {
                     try {
                        jtbConnection.connectOrDisconnect();
                     } catch (Throwable e) {
                        jtbStatusReporter.showError("Connect unsuccessful", e, jtbSession.getName());
                        return;
                     }
                  }
               });
            }
            // Retest to check is the connect was successfull...
            if (!(jtbConnection.isConnected())) {
               return;
            }

            // Dialog to choose a destination

            DestinationChooserDialog dialog1 = new DestinationChooserDialog(getShell(), jtbSession);
            if (dialog1.open() == Window.OK) {

               JTBDestination jtbDestination = dialog1.getSelectedJTBDestination();
               if (jtbDestination != null) {
                  destinationName = jtbDestination.getName();
                  lblDestinationName.setText(destinationName);
               }
            }
         }
      });

      // DataFile (We store datafile variable prefix...)

      Label lbl4 = new Label(container, SWT.SHADOW_NONE);
      lbl4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl4.setAlignment(SWT.CENTER);
      lbl4.setText("Loop on data file:");

      // Composite with label and clear button
      Composite dataFileComposite = new Composite(container, SWT.NONE);
      dataFileComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      GridLayout gl1 = new GridLayout(2, false);
      gl1.marginWidth = 0;
      dataFileComposite.setLayout(gl1);

      lblDataFile = new Label(dataFileComposite, SWT.BORDER | SWT.SHADOW_NONE);
      lblDataFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      Button btnClear = new Button(dataFileComposite, SWT.NONE);
      btnClear.setToolTipText("Clear data file");
      btnClear.setImage(SWTResourceManager.getImage(this.getClass(), "icons/cross-script.png"));
      btnClear.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            variablePrefix = null;
            lblDataFile.setText("");
         }
      });

      Button btnChooseDataFile = new Button(container, SWT.NONE);
      btnChooseDataFile.setText("Select...");
      btnChooseDataFile.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            // Dialog to choose a Data File
            DataFileChooserDialog dialog1 = new DataFileChooserDialog(getShell(), scriptsManager, script.getDataFile());
            if (dialog1.open() == Window.OK) {

               DataFile dataFile = dialog1.getSelectedDataFile();
               if (dataFile != null) {
                  variablePrefix = dataFile.getVariablePrefix();

                  log.debug("Data File Selected : [{}]", dataFile.getVariablePrefix());
                  lblDataFile.setText(scriptsManager.buildDataFileDislayName(dataFile));

                  // Clear payloadDirectory
                  payloadDirectory = null;
                  lblPayloadDirectory.setText("");
               }
            }
         }
      });
      if (script.getDataFile().isEmpty()) {
         btnChooseDataFile.setEnabled(false);
      }

      // Payload Directory

      Label lbl9 = new Label(container, SWT.SHADOW_NONE);
      lbl9.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl9.setAlignment(SWT.CENTER);
      lbl9.setText("Dir. with payloads:");

      // Composite with label and clear button
      Composite payloadDirectoryComposite = new Composite(container, SWT.NONE);
      payloadDirectoryComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      GridLayout gl3 = new GridLayout(2, false);
      gl3.marginWidth = 0;
      payloadDirectoryComposite.setLayout(gl3);

      lblPayloadDirectory = new Label(payloadDirectoryComposite, SWT.BORDER | SWT.SHADOW_NONE);
      lblPayloadDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      Button btnClearPayloadDirectory = new Button(payloadDirectoryComposite, SWT.NONE);
      btnClearPayloadDirectory.setToolTipText("Clear data file");
      btnClearPayloadDirectory.setImage(SWTResourceManager.getImage(this.getClass(), "icons/cross-script.png"));
      btnClearPayloadDirectory.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            payloadDirectory = null;
            lblPayloadDirectory.setText("");
         }
      });

      Button btnChoosePayloadDirectory = new Button(container, SWT.NONE);
      btnChoosePayloadDirectory.setText("Select...");
      btnChoosePayloadDirectory.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            DirectoryDialog directoryDialog = new DirectoryDialog(getShell(), SWT.OPEN);
            directoryDialog.setText("Select the directory with payloads");

            String selectedDirectoryName = directoryDialog.open();
            if (selectedDirectoryName == null) {
               return;
            }
            log.debug("Payload Directory selected: {}", payloadDirectory);
            payloadDirectory = selectedDirectoryName;
            lblPayloadDirectory.setText(payloadDirectory);

            // Clear dataFile
            variablePrefix = null;
            lblDataFile.setText("");
         }
      });

      // Repeat

      Label lbl5 = new Label(container, SWT.NONE);
      lbl5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl5.setText("Repeat this step");

      Composite repeatComposite = new Composite(container, SWT.NONE);
      repeatComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      GridLayout gl2 = new GridLayout(2, false);
      gl2.marginWidth = 0;
      repeatComposite.setLayout(gl2);

      iterationsSpinner = new Spinner(repeatComposite, SWT.BORDER);
      iterationsSpinner.setMinimum(1);
      iterationsSpinner.setMaximum(9999);
      iterationsSpinner.setPageIncrement(10);
      iterationsSpinner.setTextLimit(4);
      iterationsSpinner.setSelection(1);

      Label lbl6 = new Label(repeatComposite, SWT.NONE);
      lbl6.setText(" time(s)");

      // Pause

      Label lbl7 = new Label(container, SWT.NONE);
      lbl7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl7.setText("Pause for");

      Composite composite = new Composite(container, SWT.NONE);
      composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      GridLayout gl_composite = new GridLayout(2, false);
      gl_composite.marginWidth = 0;
      composite.setLayout(gl_composite);

      delaySpinner = new Spinner(composite, SWT.BORDER);
      delaySpinner.setMaximum(600);

      Label lbl8 = new Label(composite, SWT.NONE);
      lbl8.setText(" second(s) after this step");

      // Populate Fields
      isFolder = step.isFolder();
      templateName = step.getTemplateName();
      sessionName = step.getSessionName();
      destinationName = step.getDestinationName();
      variablePrefix = step.getVariablePrefix();
      payloadDirectory = step.getPayloadDirectory();
      delay = step.getPauseSecsAfter();
      iterations = step.getIterations();

      lblTemplateName.setText(scriptsManager.getTemplateDisplayName(isFolder, templateName));
      lblSessionName.setText(sessionName);
      lblDestinationName.setText(destinationName);
      if (variablePrefix != null) {
         DataFile dataFile = scriptsManager.findDataFileByVariablePrefix(script, variablePrefix);
         lblDataFile.setText(scriptsManager.buildDataFileDislayName(dataFile));
      }
      if (payloadDirectory != null) {
         lblPayloadDirectory.setText(payloadDirectory);
      }
      delaySpinner.setSelection(delay);
      iterationsSpinner.setSelection(iterations);

      if (Utils.isNotEmpty(sessionName)) {
         btnChooseDestination.setEnabled(true);
      } else {
         btnChooseDestination.setEnabled(false);
      }

      return container;
   }

   @Override
   protected void okPressed() {

      if (templateName.isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "A template is mandatory");
         return;
      }

      if (sessionName.isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "A session is mandatory");
         return;
      }

      if (destinationName.isEmpty()) {
         MessageDialog.openError(getShell(), "Error", "A destination is mandatory");
         return;
      }

      // Populate fields

      step.setTemplateName(templateName);
      step.setFolder(isFolder);
      step.setSessionName(sessionName);
      step.setDestinationName(destinationName);
      step.setVariablePrefix(variablePrefix);
      step.setPayloadDirectory(payloadDirectory);
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

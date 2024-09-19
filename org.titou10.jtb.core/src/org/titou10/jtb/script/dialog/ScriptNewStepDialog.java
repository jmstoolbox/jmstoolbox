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
package org.titou10.jtb.script.dialog;

import java.io.IOException;

import jakarta.xml.bind.JAXBException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
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
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.script.ScriptsManager;
import org.titou10.jtb.script.gen.DataFile;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.sessiontype.SessionTypeManager;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.template.TemplatesManager.TemplateNameStructure;
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

   private static final Logger   log = LoggerFactory.getLogger(ScriptNewStepDialog.class);

   private JTBStatusReporter     jtbStatusReporter;
   private TemplatesManager      templatesManager;

   private ConfigManager         cm;
   private ScriptsManager        scriptsManager;
   private SessionTypeManager    sessionTypeManager;
   private Step                  step;
   private Script                script;

   private TemplateNameStructure tns;
   // private String templateName;
   // private String templateDirectory;
   private String                sessionName;
   private String                destinationName;
   private String                variablePrefix;
   private String                payloadDirectory;
   private Integer               delay;
   private Integer               iterations;

   private Label                 lblTemplateName;
   private Label                 lblSessionName;
   private Label                 lblDestinationName;
   private Label                 lblDataFile;
   private Label                 lblPayloadDirectory;
   private Spinner               delaySpinner;
   private Spinner               iterationsSpinner;

   private Button                btnChooseDestination;

   public ScriptNewStepDialog(Shell parentShell,
                              JTBStatusReporter jtbStatusReporter,
                              ConfigManager cm,
                              TemplatesManager templatesManager,
                              ScriptsManager scriptsManager,
                              SessionTypeManager sessionTypeManager,
                              Step step,
                              Script script) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
      this.jtbStatusReporter = jtbStatusReporter;
      this.cm = cm;
      this.templatesManager = templatesManager;
      this.scriptsManager = scriptsManager;
      this.sessionTypeManager = sessionTypeManager;
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
      return new Point(700, p.y);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      final Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(4, false));

      // Template

      final Label lbl1 = new Label(container, SWT.SHADOW_NONE | SWT.CENTER);
      lbl1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
      lbl1.setText("Template:");

      lblTemplateName = new Label(container, SWT.BORDER | SWT.SHADOW_NONE);
      lblTemplateName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnChooseTemplate = new Button(container, SWT.NONE);
      btnChooseTemplate.setText("Select...");
      btnChooseTemplate.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         // Dialog to choose a template
         TemplateChooserDialog dialog1 = new TemplateChooserDialog(getShell(), templatesManager, false);
         if (dialog1.open() != Window.OK) {
            return;
         }

         IFileStore template = dialog1.getSelectedTemplate();
         if (template != null) {
            String templateFileName = URIUtil.toPath(template.toURI()).toPortableString();
            tns = templatesManager.buildTemplateNameStructure(templateFileName);
            lblTemplateName.setText(tns.getSyntheticName());
         }
      }));

      // Session

      Label lbl2 = new Label(container, SWT.NONE);
      lbl2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
      lbl2.setText("Session:");

      lblSessionName = new Label(container, SWT.BORDER | SWT.SHADOW_NONE);
      lblSessionName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnChooseSession = new Button(container, SWT.NONE);
      btnChooseSession.setText("Select...");
      btnChooseSession.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         // Dialog to choose a Session
         SessionChooserDialog dialog1 = new SessionChooserDialog(getShell(), cm);
         if (dialog1.open() == Window.OK) {

            JTBSession jtbSession = dialog1.getSelectedJTBSession();
            if (jtbSession != null) {
               // Reset Destination if session name changed
               if (!(jtbSession.getName().equals(sessionName))) {
                  destinationName = "";
                  lblDestinationName.setText(destinationName);
               }
               sessionName = jtbSession.getName();
               lblSessionName.setText(sessionName);

               btnChooseDestination.setEnabled(true);
            }
         }
      }));

      // Destination

      Label lbl3 = new Label(container, SWT.NONE);
      lbl3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
      lbl3.setText("Destination:");

      lblDestinationName = new Label(container, SWT.BORDER | SWT.SHADOW_NONE);
      lblDestinationName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      btnChooseDestination = new Button(container, SWT.NONE);
      btnChooseDestination.setText("Select...");
      btnChooseDestination.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         // Connect to session, get list of destinations
         final JTBSession jtbSession = cm.getJTBSessionByName(sessionName);
         if (jtbSession == null) {
            jtbStatusReporter.showError("It seems session '" + sessionName + "' does not exist", null);
            return;
         }

         final JTBConnection jtbConnection = jtbSession.getJTBConnection(JTBSessionClientType.SCRIPT);
         if (!(jtbConnection.isConnected())) {

            BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
               @Override
               public void run() {
                  try {
                     jtbConnection.connect();
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

         DestinationChooserDialog dialog1 = new DestinationChooserDialog(getShell(), sessionTypeManager, jtbSession);
         if (dialog1.open() == Window.OK) {

            JTBDestination jtbDestination = dialog1.getSelectedJTBDestination();
            if (jtbDestination != null) {
               destinationName = jtbDestination.getName();
               lblDestinationName.setText(destinationName);
            }
         }
      }));

      // Bracket to show choice between DataFile and Payload Directory

      Label lbl99 = new Label(container, SWT.SHADOW_NONE | SWT.RIGHT);
      lbl99.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2));
      lbl99.setText("[");

      FontData currentFontData = lbl99.getFont().getFontData()[0];
      int currentFontHeight = currentFontData.getHeight();
      int size = currentFontHeight * 3;
      Font f = SWTResourceManager.getFont(currentFontData.getName(), size, SWT.NORMAL);
      lbl99.setFont(f);

      Image i = SWTResourceManager
               .getImage(this.getClass(), "icons/cross-script.png", currentFontHeight + 2, currentFontHeight + 2);

      // DataFile (We store datafile variable prefix...)

      Label lbl4 = new Label(container, SWT.SHADOW_NONE);
      lbl4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lbl4.setAlignment(SWT.CENTER);
      lbl4.setText("Loop on data file:");

      // Composite with label and clear button
      Composite dataFileComposite = new Composite(container, SWT.NONE);
      dataFileComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
      GridLayout gl1 = new GridLayout(2, false);
      gl1.marginWidth = 0;
      gl1.marginHeight = 0;
      dataFileComposite.setLayout(gl1);

      lblDataFile = new Label(dataFileComposite, SWT.BORDER | SWT.SHADOW_NONE);
      lblDataFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnClear = new Button(dataFileComposite, SWT.NONE);
      btnClear.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      btnClear.setToolTipText("Clear data file");
      btnClear.setImage(i);
      btnClear.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         variablePrefix = null;
         lblDataFile.setText("");
      }));

      Button btnChooseDataFile = new Button(container, SWT.NONE);
      btnChooseDataFile.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      btnChooseDataFile.setText("Select...");
      btnChooseDataFile.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
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
      }));
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
      payloadDirectoryComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
      GridLayout gl3 = new GridLayout(2, false);
      gl3.marginWidth = 0;
      gl3.marginHeight = 0;
      payloadDirectoryComposite.setLayout(gl3);

      lblPayloadDirectory = new Label(payloadDirectoryComposite, SWT.BORDER | SWT.SHADOW_NONE);
      lblPayloadDirectory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnClearPayloadDirectory = new Button(payloadDirectoryComposite, SWT.NONE);
      btnClearPayloadDirectory.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
      btnClearPayloadDirectory.setToolTipText("Clear data file");
      btnClearPayloadDirectory.setImage(i);
      btnClearPayloadDirectory.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         payloadDirectory = null;
         lblPayloadDirectory.setText("");
      }));

      Button btnChoosePayloadDirectory = new Button(container, SWT.NONE);
      btnChoosePayloadDirectory.setText("Select...");
      btnChoosePayloadDirectory.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
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
      }));

      // Repeat

      Label lbl5 = new Label(container, SWT.NONE);
      lbl5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
      lbl5.setText("Repeat this step");

      Composite repeatComposite = new Composite(container, SWT.NONE);
      repeatComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      GridLayout glRrepeatComposite = new GridLayout(2, false);
      glRrepeatComposite.marginWidth = 0;
      repeatComposite.setLayout(glRrepeatComposite);

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
      lbl7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
      lbl7.setText("Pause for");

      Composite pauseComposite = new Composite(container, SWT.NONE);
      pauseComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      GridLayout glPauseComposite = new GridLayout(2, false);
      glPauseComposite.marginWidth = 0;
      pauseComposite.setLayout(glPauseComposite);

      delaySpinner = new Spinner(pauseComposite, SWT.BORDER);
      delaySpinner.setMinimum(0);
      delaySpinner.setMaximum(9999);
      delaySpinner.setPageIncrement(10);
      delaySpinner.setTextLimit(4);
      delaySpinner.setSelection(0);

      Label lbl8 = new Label(pauseComposite, SWT.NONE);
      lbl8.setText(" second(s) after this step");

      // Populate Fields
      tns = templatesManager.buildTemplateNameStructure(step.getTemplateDirectory(), step.getTemplateName());
      sessionName = step.getSessionName();
      destinationName = step.getDestinationName();
      variablePrefix = step.getVariablePrefix();
      payloadDirectory = step.getPayloadDirectory();
      delay = step.getPauseSecsAfter();
      iterations = step.getIterations();

      lblTemplateName.setText(tns == null ? "" : tns.getSyntheticName());
      lblSessionName.setText(sessionName == null ? "" : sessionName);
      lblDestinationName.setText(destinationName == null ? "" : destinationName);
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

      if (tns == null) {
         MessageDialog.openError(getShell(), "Error", "A template is mandatory");
         return;
      }

      if (Utils.isEmpty(sessionName)) {
         MessageDialog.openError(getShell(), "Error", "A session is mandatory");
         return;
      }

      if (Utils.isEmpty(destinationName)) {
         MessageDialog.openError(getShell(), "Error", "A destination is mandatory");
         return;
      }

      // Payload directory is only valid with Templates of type Text or Bytes
      if (payloadDirectory != null) {
         try {
            JTBMessageTemplate template = templatesManager.getTemplateFromName(tns.getTemplateFullFileName());
            switch (template.getJtbMessageType()) {
               case BYTES:
               case TEXT:
                  break;

               default:
                  MessageDialog
                           .openError(getShell(),
                                      "Error",
                                      "Iterating on payloads stored in a directory can only be used with a template of type 'Text' or 'Bytes'");
                  return;
            }
         } catch (CoreException | JAXBException | IOException e) {
            MessageDialog.openError(getShell(), "Error", "An exception occurred while reading the template " + e.getMessage());
            return;
         }
      }

      // Populate fields

      step.setTemplateName(tns.getTemplateRelativeFileName());
      step.setTemplateDirectory(tns.getTemplateDirectoryName());
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

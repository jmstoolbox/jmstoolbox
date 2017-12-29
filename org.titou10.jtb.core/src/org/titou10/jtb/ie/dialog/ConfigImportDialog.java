/*
 * Copyright (C) 2017 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.ie.dialog;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.zip.ZipInputStream;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.titou10.jtb.ie.ImportExportType;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * 
 * Manage the Config Import Dialog
 * 
 * @author Denis Forveille
 *
 */
public class ConfigImportDialog extends Dialog {

   private EnumSet<ImportExportType> importTypes;
   private String                    fileName;

   private Button                    btnImportAll;
   private Button                    btnSessions;
   private Button                    btnTemplatesDirectory;
   private Button                    btnScripts;
   private Button                    btnColumnsSets;
   private Button                    btnVariables;
   private Button                    btnVisualizers;
   private Button                    btnPreferences;

   private Text                      textFileName;
   private Button                    btnBrowse;
   private Label                     label;

   public ConfigImportDialog(Shell parentShell) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      importTypes = EnumSet.noneOf(ImportExportType.class);
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Import Configuration");
   }

   @Override
   protected Point getInitialSize() {
      Point p = super.getInitialSize();
      return new Point(600, p.y);
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.OK_ID, "Import", true);
      createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", false);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(3, false));

      Label lblFromFile = new Label(container, SWT.NONE);
      lblFromFile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblFromFile.setText("From file:");

      textFileName = new Text(container, SWT.BORDER);
      textFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      btnBrowse = new Button(container, SWT.NONE);

      btnBrowse.setText("Browse...");

      btnBrowse.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
            fileDialog.setText("Specify a name for the file");
            fileDialog.setFilterExtensions(new String[] { Constants.JTB_EXPORT_CONFIG_FILE_EXTENSION });
            fileDialog.setFileName(Constants.JTB_EXPORT_CONFIG_FILE_NAME);

            String configFileName = fileDialog.open();
            if (configFileName == null) {
               return;
            }
            fileName = configFileName.trim();
            textFileName.setText(fileName);
         }
      });

      btnImportAll = new Button(container, SWT.CHECK);
      btnImportAll.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
      btnImportAll.setText("Import all");

      // Behavior

      btnImportAll.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            if (btnImportAll.getSelection()) {
               enableDisableComponents(false);
            } else {
               enableDisableComponents(true);
            }
         }
      });

      Group gComponents = new Group(container, SWT.SHADOW_ETCHED_IN);
      gComponents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
      gComponents.setText("Components to import");
      gComponents.setLayout(new GridLayout(3, false));

      btnColumnsSets = new Button(gComponents, SWT.CHECK);
      btnColumnsSets.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
      btnColumnsSets.setText("Columns Sets (Merge/Override)");

      btnSessions = new Button(gComponents, SWT.CHECK);
      btnSessions.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
      btnSessions.setText("Sessions configuration (Replace all, triggers a restart)");

      btnScripts = new Button(gComponents, SWT.CHECK);
      btnScripts.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
      btnScripts.setText("Scripts (Replace all)");

      btnTemplatesDirectory = new Button(gComponents, SWT.CHECK);
      btnTemplatesDirectory.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
      btnTemplatesDirectory.setText("Templates directories (Merge/Override)");

      btnVariables = new Button(gComponents, SWT.CHECK);
      btnVariables.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
      btnVariables.setText("Variables (Merge/Override)");

      btnVisualizers = new Button(gComponents, SWT.CHECK);
      btnVisualizers.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
      btnVisualizers.setText("Visualizers (Merge/Override)");

      label = new Label(gComponents, SWT.SEPARATOR | SWT.HORIZONTAL);
      label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

      btnPreferences = new Button(gComponents, SWT.CHECK);
      btnPreferences.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
      btnPreferences.setText("Preferences ((Merge/Override, triggers a restart)");

      // Initial state
      btnImportAll.setSelection(true);
      enableDisableComponents(false);

      return container;
   }

   private void enableDisableComponents(boolean state) {
      btnSessions.setEnabled(state);
      btnTemplatesDirectory.setEnabled(state);
      btnScripts.setEnabled(state);
      btnColumnsSets.setEnabled(state);
      btnVariables.setEnabled(state);
      btnVisualizers.setEnabled(state);
      btnPreferences.setEnabled(state);
   }

   @Override
   protected void okPressed() {

      fileName = textFileName.getText();
      if (Utils.isEmpty(fileName)) {
         MessageDialog.openError(getShell(), "Invalid File Name", "The file name is mandatory");
         return;
      }

      // Test if the file exists and is a zip file
      try (FileInputStream fis = new FileInputStream(fileName);
               ZipInputStream gis = new ZipInputStream(fis);) {} catch (IOException e) {
         MessageDialog.openError(getShell(), "Invalid File Name", "The file does not exists or is not a zip file");
         return;
      }

      if (btnImportAll.getSelection()) {
         importTypes = EnumSet.allOf(ImportExportType.class);
      } else {
         if (btnColumnsSets.getSelection()) {
            importTypes.add(ImportExportType.COLUMNS_SETS);
         }
         if (btnTemplatesDirectory.getSelection()) {
            importTypes.add(ImportExportType.DIRECTORY_TEMPLATES);
         }
         if (btnSessions.getSelection()) {
            importTypes.add(ImportExportType.SESSIONS);
         }
         if (btnScripts.getSelection()) {
            importTypes.add(ImportExportType.SCRIPTS);
         }
         if (btnVariables.getSelection()) {
            importTypes.add(ImportExportType.VARIABLES);
         }
         if (btnVisualizers.getSelection()) {
            importTypes.add(ImportExportType.VISUALIZERS);
         }
         if (btnPreferences.getSelection()) {
            importTypes.add(ImportExportType.PREFERENCES);
         }
      }
      if (importTypes.isEmpty()) {
         MessageDialog.openError(getShell(), "Nothing to import", "At least one component must be selected");
         return;
      }

      if (importTypes.contains(ImportExportType.SESSIONS) || importTypes.contains(ImportExportType.PREFERENCES)) {
         if (!(MessageDialog
                  .openQuestion(getShell(),
                                "Warning",
                                "The component choosen for import may cause JMSToolBox to restart.\n\nContinue anyway?"))) {
            return;
         }
      }

      super.okPressed();
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public EnumSet<ImportExportType> getImportTypes() {
      return importTypes;
   }

   public String getFileName() {
      return fileName;
   }

}

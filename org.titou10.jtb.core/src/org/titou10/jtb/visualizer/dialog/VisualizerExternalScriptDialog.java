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
package org.titou10.jtb.visualizer.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.titou10.jtb.visualizer.gen.Visualizer;
import org.titou10.jtb.visualizer.gen.VisualizerMessageType;

/**
 * 
 * Ask for a new Visualizer of kind "EXTERNAL_SCRIPT"
 * 
 * @author Denis Forveille
 *
 */
public class VisualizerExternalScriptDialog extends Dialog {

   private Visualizer                  visualizer;

   private boolean                     showScriptLogs;
   private String                      fileName;
   private List<VisualizerMessageType> listMessageType;

   private Text                        textFileName;
   private Button                      btnText;
   private Button                      btnBytes;
   private Button                      btnMap;

   private Button                      btnShowScriptLogs;

   public VisualizerExternalScriptDialog(Shell parentShell, Visualizer visualizer) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.visualizer = visualizer;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Add/Edit an 'External Script' visualizer");
   }

   protected Point getInitialSize() {
      return new Point(700, 200);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(3, false));

      Label lblNewLabel1 = new Label(container, SWT.NONE);
      lblNewLabel1.setText("Target JMS Messages: ");

      Composite compositeKind = new Composite(container, SWT.NONE);
      compositeKind.setLayout(new RowLayout(SWT.HORIZONTAL));
      compositeKind.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));

      btnText = new Button(compositeKind, SWT.CHECK);
      btnText.setText("TextMessage");

      btnBytes = new Button(compositeKind, SWT.CHECK);
      btnBytes.setText("BytesMessage");

      btnMap = new Button(compositeKind, SWT.CHECK);
      btnMap.setText("MapMessage");

      // Show Logs

      btnShowScriptLogs = new Button(container, SWT.CHECK);
      btnShowScriptLogs.setText("Show logs on execution? ");
      btnShowScriptLogs.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));

      // Script Name

      Label lblNewLabel = new Label(container, SWT.NONE);
      lblNewLabel.setText("Script file name: ");

      textFileName = new Text(container, SWT.BORDER);
      textFileName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Button btnBrowse = new Button(container, SWT.NONE);
      btnBrowse.setText("Browse...");
      btnBrowse.addSelectionListener(new SelectionAdapter() {
         // FileDialog to chose the name of a script
         @Override
         public void widgetSelected(SelectionEvent e) {
            FileDialog fileDialog = new FileDialog(getParentShell(), SWT.OPEN | SWT.MULTI);
            fileDialog.setText("Select script file");
            String sel = fileDialog.open();
            if (sel != null) {
               textFileName.setText(sel);
            }
         }
      });

      if (visualizer != null) {
         for (VisualizerMessageType visualizerMessageType : visualizer.getTargetMsgType()) {
            switch (visualizerMessageType) {
               case BYTES:
                  btnBytes.setSelection(true);
                  break;
               case MAP:
                  btnMap.setSelection(true);
                  break;
               case TEXT:
                  btnText.setSelection(true);
                  break;
            }
         }
         btnShowScriptLogs.setSelection(visualizer.isShowScriptLogs());
         textFileName.setText(visualizer.getFileName());
      }

      return container;
   }

   @Override
   protected void okPressed() {

      listMessageType = new ArrayList<>();
      if (btnBytes.getSelection()) {
         listMessageType.add(VisualizerMessageType.BYTES);
      }
      if (btnMap.getSelection()) {
         listMessageType.add(VisualizerMessageType.MAP);
      }
      if (btnText.getSelection()) {
         listMessageType.add(VisualizerMessageType.TEXT);
      }
      if (listMessageType.isEmpty()) {
         btnBytes.setFocus();
         MessageDialog.openError(getShell(), "Error", "A visualizer must be associated to at least one JMS Message Type");
         return;
      }

      fileName = textFileName.getText().trim();
      if (fileName.isEmpty()) {
         textFileName.setFocus();
         MessageDialog.openError(getShell(), "Error", "Please enter an external file name");
         return;
      }

      showScriptLogs = btnShowScriptLogs.getSelection();

      super.okPressed();
   }

   // ----------------
   // Standard Getters
   // ----------------
   public String getFileName() {
      return fileName;
   }

   public List<VisualizerMessageType> getListMessageType() {
      return listMessageType;
   }

   public boolean getShowScriptLogs() {
      return showScriptLogs;
   }

}

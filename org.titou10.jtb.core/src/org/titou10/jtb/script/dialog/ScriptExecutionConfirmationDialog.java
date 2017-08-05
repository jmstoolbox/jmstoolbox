/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * Confirmation Dialog before executing/simulating a script
 * 
 * @author Denis Forveille
 *
 */
public class ScriptExecutionConfirmationDialog extends Dialog {

   private static final String SIMULATION     = "simulation";
   private static final String SIMULATION_BTN = "Simulate";
   private static final String EXECUTION      = "execution";
   private static final String EXECUTION_BTN  = "Execute";

   private String              text;
   private String              textBtn;
   private int                 maxMessages    = 0;
   private boolean             doShowPostLogs = true;

   public ScriptExecutionConfirmationDialog(Shell parentShell, boolean simulation) {
      super(parentShell);
      setShellStyle(SWT.TITLE | SWT.PRIMARY_MODAL | SWT.CLOSE);
      if (simulation) {
         text = SIMULATION;
         textBtn = SIMULATION_BTN;
      } else {
         text = EXECUTION;
         textBtn = EXECUTION_BTN;
      }
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Script " + text + " confirmation");
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", true);
      createButton(parent, IDialogConstants.OK_ID, textBtn, false);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
      container.setLayout(new GridLayout(3, false));

      Label lbl = new Label(container, SWT.NONE);
      lbl.setText("Stop " + text + " after having posted");

      final Spinner spinnerMaxMessages = new Spinner(container, SWT.BORDER | SWT.RIGHT);
      spinnerMaxMessages.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      spinnerMaxMessages.setMinimum(0);
      spinnerMaxMessages.setMaximum(9999);
      spinnerMaxMessages.setIncrement(1);
      spinnerMaxMessages.setPageIncrement(50);
      spinnerMaxMessages.setTextLimit(4);
      spinnerMaxMessages.addModifyListener(new ModifyListener() {
         @Override
         public void modifyText(ModifyEvent e) {
            maxMessages = spinnerMaxMessages.getSelection();
         }
      });

      Label lb2 = new Label(container, SWT.NONE);
      lb2.setText("messages (0=process all)");

      // Log feedback

      Button btnDoShowLogs = new Button(container, SWT.CHECK);
      btnDoShowLogs.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
      btnDoShowLogs
               .setText("Show feedback for messages posted in log viewer (Disable if more than a few thousand messages are to be post)");
      btnDoShowLogs.setSelection(doShowPostLogs);
      btnDoShowLogs.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         Button b = (Button) e.getSource();
         doShowPostLogs = b.getSelection();
      }));

      return container;
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public int getMaxMessages() {
      return maxMessages;
   }

   public boolean isDoShowPostLogs() {
      return doShowPostLogs;
   }

}

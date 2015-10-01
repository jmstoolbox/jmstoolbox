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

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * Dialog that display session information
 * 
 * @author Denis Forveille
 *
 */
public class QueueInformationDialog extends Dialog {

   private String              queueName;
   private Map<String, Object> queueInformation;

   public QueueInformationDialog(Shell parentShell, String queueName, Map<String, Object> queueInformation) {
      super(parentShell);
      setShellStyle(SWT.TITLE | SWT.PRIMARY_MODAL);

      this.queueName = queueName;
      this.queueInformation = queueInformation;
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
      container.setLayout(new GridLayout(2, false));

      Label lbl = new Label(container, SWT.NONE);
      lbl.setText("Queue Name:");

      Label lblQueueName = new Label(container, SWT.NONE);
      lblQueueName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
      lblQueueName.setText(queueName);

      Label lblName;
      Label lblValue;
      String value;
      for (Entry<String, Object> e : queueInformation.entrySet()) {
         lblName = new Label(container, SWT.NONE);
         lblName.setText(e.getKey() + ": ");

         lblValue = new Label(container, SWT.NONE);
         if (e.getValue() != null) {
            value = e.getValue().toString();
         } else {
            value = "";
         }
         lblValue.setText(value);
      }

      return container;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Queue Information");
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      Button btnOk = createButton(parent, IDialogConstants.CANCEL_ID, "Done", false);
      btnOk.setText("OK");
   }

}

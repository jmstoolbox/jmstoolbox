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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * Dialog that display session information
 * 
 * @author Denis Forveille
 *
 */
public class SessionPropertiesDialog extends Dialog {

   private String   sessionName;
   private String   queueManager;
   private String   providerName;
   private String   providerVersion;
   private String   jmsVersion;
   private String[] jmsxPropertyNames;

   public SessionPropertiesDialog(Shell parentShell, String sessionName, String queueManager, String providerName,
                                  String providerVersion, String jmsVersion, String[] jmsxPropertyNames) {
      super(parentShell);
      setShellStyle(SWT.TITLE | SWT.APPLICATION_MODAL);

      this.sessionName = sessionName;
      this.queueManager = queueManager;
      this.providerName = providerName;
      this.providerVersion = providerVersion;
      this.jmsVersion = jmsVersion;
      this.jmsxPropertyNames = jmsxPropertyNames;
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
      container.setLayout(new GridLayout(2, false));

      Label lblNewLabel_3 = new Label(container, SWT.NONE);
      lblNewLabel_3.setText("Session Name:");

      Label lblSessionName = new Label(container, SWT.NONE);
      lblSessionName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
      lblSessionName.setText(sessionName);

      Label lblNewLabel_4 = new Label(container, SWT.NONE);
      lblNewLabel_4.setText("Queue Manager Name:");

      Label lblQueueManager = new Label(container, SWT.NONE);
      lblQueueManager.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
      lblQueueManager.setText(queueManager);

      Label lblNewLabel_6 = new Label(container, SWT.NONE);
      lblNewLabel_6.setText("Provider Name:");

      Label lblProviderName = new Label(container, SWT.NONE);
      lblProviderName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
      lblProviderName.setText(providerName);

      Label lblNewLabel_1 = new Label(container, SWT.NONE);
      lblNewLabel_1.setText("Provider Version:");

      Label lblProviderVersion = new Label(container, SWT.NONE);
      lblProviderVersion.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
      lblProviderVersion.setText(providerVersion);

      Label lblNewLabel_2 = new Label(container, SWT.NONE);
      lblNewLabel_2.setText("JMS Version:");

      Label lblJMSVersion = new Label(container, SWT.NONE);
      lblJMSVersion.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
      lblJMSVersion.setText(jmsVersion);

      Label lblNewLabel = new Label(container, SWT.NONE);
      lblNewLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
      lblNewLabel.setText("JMSX Properties:");

      List list = new org.eclipse.swt.widgets.List(container, SWT.NONE);
      list.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
      list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      list.setItems(jmsxPropertyNames);

      // Disable JMSX property list selections
      container.setEnabled(false);

      return container;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Session Information");
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      Button btnOk = createButton(parent, IDialogConstants.CANCEL_ID, "Done", false);
      btnOk.setText("OK");
   }

}

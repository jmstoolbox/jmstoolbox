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
import org.titou10.jtb.jms.model.JTBDestination;

/**
 * Display Destination information
 * 
 * @author Denis Forveille
 *
 */
public class DestinationInformationDialog extends Dialog {

   private JTBDestination      jtbDestination;
   private Map<String, Object> destinationInformation;

   public DestinationInformationDialog(Shell parentShell,
                                       JTBDestination jtbDestination,
                                       Map<String, Object> destinationInformation) {
      super(parentShell);
      setShellStyle(SWT.TITLE | SWT.PRIMARY_MODAL);

      this.jtbDestination = jtbDestination;
      this.destinationInformation = destinationInformation;
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
      container.setLayout(new GridLayout(2, false));

      Label lbl = new Label(container, SWT.NONE);
      lbl.setText("Destination Name:");

      Label lblQueueName = new Label(container, SWT.NONE);
      lblQueueName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
      lblQueueName.setText(jtbDestination.getName());

      if (jtbDestination.isJTBQueue()) {
         Label lbl2 = new Label(container, SWT.NONE);
         lbl2.setText("Is browsable?");

         Label lblIsBrowsable = new Label(container, SWT.NONE);
         lblIsBrowsable.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
         lblIsBrowsable.setText(String.valueOf(jtbDestination.getAsJTBQueue().isBrowsable()));
      }

      // Empty line
      new Label(container, SWT.NONE);
      new Label(container, SWT.NONE);

      Label lblName;
      Label lblValue;
      String value;
      for (Entry<String, Object> e : destinationInformation.entrySet()) {
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
      newShell.setText("Destination Information");
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      Button btnOk = createButton(parent, IDialogConstants.CANCEL_ID, "Done", false);
      btnOk.setText("OK");
   }

}

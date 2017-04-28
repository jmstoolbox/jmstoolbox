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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.titou10.jtb.jms.model.JTBMessageType;

/**
 * 
 * Ask the type of message after a file is dropped on the message browser
 * 
 * @author Denis Forveille
 *
 */
public class MessageTypePayloadDialog extends Dialog {

   private JTBMessageType jtbMessageType;

   private Button         btnText;
   private Button         btnBytes;

   public MessageTypePayloadDialog(Shell parentShell) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Choose the type of message to create");
   }

   protected Point getInitialSize() {
      return new Point(318, 104);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(2, false));

      Label lblNewLabel1 = new Label(container, SWT.NONE);
      lblNewLabel1.setText("Create a message of type ");

      Composite compositeKind = new Composite(container, SWT.NONE);
      compositeKind.setLayout(new RowLayout(SWT.HORIZONTAL));

      btnText = new Button(compositeKind, SWT.RADIO);
      btnText.setText("Text");
      btnText.setSelection(true);

      btnBytes = new Button(compositeKind, SWT.RADIO);
      btnBytes.setText("Bytes");

      return container;
   }

   @Override
   protected void okPressed() {
      if (btnBytes.getSelection()) {
         jtbMessageType = JTBMessageType.BYTES;
      }
      if (btnText.getSelection()) {
         jtbMessageType = JTBMessageType.TEXT;
      }
      super.okPressed();
   }

   // ----------------
   // Standard Getters
   // ----------------

   public JTBMessageType getJtbMessageType() {
      return jtbMessageType;
   }

}

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
package org.titou10.jtb.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.titou10.jtb.util.Utils;

/**
 * 
 * Capture the user credential
 * 
 * @author Denis Forveille
 *
 */
public class SessionConnectDialog extends Dialog {

   private String userID;
   private String password;

   private Text   txtUserID;
   private Text   txtPassword;

   public SessionConnectDialog(Shell parentShell, String userID, String password) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.userID = userID;
      this.password = password;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Connect");
   }

   @Override
   protected Point getInitialSize() {
      Point p = super.getInitialSize();
      return new Point(400, p.y + 20);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(2, false));

      Label lblUserID = new Label(container, SWT.NONE);
      lblUserID.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblUserID.setText("User ID");

      txtUserID = new Text(container, SWT.BORDER);
      txtUserID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Label lblPassword = new Label(container, SWT.NONE);
      lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblPassword.setText("Password");

      txtPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
      txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      new Label(container, SWT.NONE);
      new Label(container, SWT.NONE);

      Button btnCheckButton = new Button(container, SWT.CHECK);
      btnCheckButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      btnCheckButton.setText("Save User ID in session");

      Button btnCheckButton_1 = new Button(container, SWT.CHECK);
      btnCheckButton_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      btnCheckButton_1.setText("Save password in session");

      Button btnDoNotAskAgain = new Button(container, SWT.CHECK);
      btnDoNotAskAgain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 1));
      btnDoNotAskAgain.setText("Do not show this dialog again (Can be reactivated in session properties)");

      Composite composite = new Composite(container, SWT.NONE);
      composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      composite.setLayout(new RowLayout());

      Label lblNewLabel_1 = new Label(composite, SWT.NONE);
      lblNewLabel_1.setImage(SWTResourceManager.getImage(this.getClass(), "icons/error.png"));

      Label lblNewLabel = new Label(composite, SWT.WRAP);
      lblNewLabel
               .setText("If the userid/password are NOT saved in the session, the 'REST' and the 'scripts' features of JMSToolBox will not work");

      txtUserID.setText(this.userID);
      txtPassword.setText(this.password);

      if (Utils.isNotEmpty(this.userID) && (Utils.isEmpty(this.password))) {
         txtPassword.setFocus();
      } else {
         txtUserID.setFocus();
      }

      return container;
   }

   @Override
   protected void okPressed() {
      this.userID = txtUserID.getText();
      this.password = txtPassword.getText();
      super.okPressed();
   }

   // ----------------
   // Standard Getters
   // ----------------

   public String getUserID() {
      return userID;
   }

   public String getPassword() {
      return password;
   }
}

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.titou10.jtb.util.Utils;

/**
 * 
 * Capture the user credential
 * 
 * @author Denis Forveille
 *
 */
public class SessionConnectDialog extends Dialog {

   private String  userID;
   private String  password;
   private String  sessionName;

   private boolean rememberUserid;
   private boolean rememberPassword;
   private boolean doNotAskAgain;

   private Text    txtUserID;
   private Text    txtPassword;
   private Button  btnRememberUserid;
   private Button  btnRememberPassword;
   private Button  btnDoNotAskAgain;

   public SessionConnectDialog(Shell parentShell,
                               String sessionName,
                               String userID,
                               String password,
                               boolean rememberPassword,
                               boolean rememberUserid) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL);

      this.userID = userID;
      this.password = password;
      this.sessionName = sessionName;
      this.rememberUserid = rememberUserid;
      this.rememberPassword = rememberPassword;
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Connect to '" + sessionName + "'");
   }

   // @Override
   // protected Point getInitialSize() {
   // Point p = super.getInitialSize();
   // return new Point(400, p.y + 20);
   // }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(2, false));

      Label lblUserID = new Label(container, SWT.NONE);
      lblUserID.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblUserID.setText("Userid");

      txtUserID = new Text(container, SWT.BORDER);
      txtUserID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      Label lblPassword = new Label(container, SWT.NONE);
      lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
      lblPassword.setText("Password");

      txtPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
      txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

      new Label(container, SWT.NONE);
      new Label(container, SWT.NONE);

      new Label(container, SWT.NONE);
      btnRememberUserid = new Button(container, SWT.CHECK);
      btnRememberUserid.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
      btnRememberUserid.setText("Remember userid");

      new Label(container, SWT.NONE);
      btnRememberPassword = new Button(container, SWT.CHECK);
      btnRememberPassword.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
      btnRememberPassword.setText("Remember password");

      new Label(container, SWT.NONE);
      btnDoNotAskAgain = new Button(container, SWT.CHECK);
      btnDoNotAskAgain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      btnDoNotAskAgain.setText("Do not prompt again (Can be changed in session properties)");

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
      this.rememberUserid = btnRememberUserid.getSelection();
      this.rememberPassword = btnRememberPassword.getSelection();
      this.doNotAskAgain = btnDoNotAskAgain.getSelection();

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

   public boolean isRememberUserid() {
      return rememberUserid;
   }

   public boolean isRememberPassword() {
      return rememberPassword;
   }

   public boolean isDoNotAskAgain() {
      return doNotAskAgain;
   }
}

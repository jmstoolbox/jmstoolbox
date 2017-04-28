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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 
 * Display help text from QManager
 * 
 * @author Denis Forveille
 *
 */
public class QManagerHelpDialog extends Dialog {

   private String helpText;

   public QManagerHelpDialog(Shell parentShell, String helpText) {
      super(parentShell);
      this.helpText = helpText;
   }

   @Override
   protected void setShellStyle(int newShellStyle) {
      super.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
      setBlockOnOpen(false);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      super.createDialogArea(parent);

      Composite container = (Composite) super.createDialogArea(parent);

      StyledText txt = new StyledText(container, SWT.NONE);
      txt.setText(helpText);
      txt.setEditable(false);
      txt.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));

      return container;
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Help");
   }
}

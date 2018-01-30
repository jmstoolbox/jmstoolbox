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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * Show the logs of a running Script
 * 
 * @author Denis Forveille
 *
 */
public class VisualizerShowLogDialog extends Dialog {

   private Text textLogs;

   public VisualizerShowLogDialog(Shell parentShell) {
      super(parentShell);
      setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.CLOSE | SWT.MODELESS | SWT.BORDER);
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Script logs");
   }

   @Override
   protected Point getInitialSize() {
      return new Point(700, 600);
   }

   // Display only a "Close" Button
   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, true);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setLayout(new GridLayout(2, false));

      textLogs = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
      textLogs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

      // Add key binding for CTRL-a -> select all
      textLogs.addListener(SWT.KeyUp, new Listener() {
         public void handleEvent(Event event) {
            if (event.stateMask == SWT.MOD1 && event.keyCode == 'a') {
               ((Text) event.widget).selectAll();
            }
         }
      });

      return container;
   }

   // ----------------
   // Standard Getters
   // ----------------
   public Text getTextLogs() {
      return textLogs;
   }

}

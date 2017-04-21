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
package org.titou10.jtb.ui.part.content;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.wb.swt.SWTResourceManager;
import org.titou10.jtb.util.Constants;

/**
 * Popup to capture the delay for auto refresh message browser
 * 
 * 
 * @author Denis Forveille
 * 
 */
final class AutoRefreshPopup extends Dialog {

   private int     delay;
   private Spinner spinnerAutoRefreshDelay;

   public AutoRefreshPopup(Shell parentShell, int delay) {
      super(parentShell);
      setShellStyle(SWT.PRIMARY_MODAL);

      this.delay = delay;
   }

   @Override
   protected Control createDialogArea(Composite parent) {

      Composite ttComposite = new Composite(parent, SWT.BORDER_SOLID);
      ttComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
      ttComposite.setLayout(new GridLayout(5, false));

      Label lbl1 = new Label(ttComposite, SWT.CENTER);
      lbl1.setText("Auto refresh every");

      spinnerAutoRefreshDelay = new Spinner(ttComposite, SWT.BORDER);
      spinnerAutoRefreshDelay.setMinimum(Constants.MINIMUM_AUTO_REFRESH);
      spinnerAutoRefreshDelay.setMaximum(600);
      spinnerAutoRefreshDelay.setIncrement(1);
      spinnerAutoRefreshDelay.setPageIncrement(5);
      spinnerAutoRefreshDelay.setTextLimit(3);
      spinnerAutoRefreshDelay.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
      spinnerAutoRefreshDelay.setSelection(delay);

      Label lbl2 = new Label(ttComposite, SWT.CENTER);
      lbl2.setText("seconds");

      final Button applyButton = new Button(ttComposite, SWT.PUSH);
      applyButton.setImage(SWTResourceManager.getImage(this.getClass(), "icons/accept.png"));
      applyButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent event) {
            okPressed();
         }
      });

      final Button cancelButton = new Button(ttComposite, SWT.PUSH);
      cancelButton.setImage(SWTResourceManager.getImage(this.getClass(), "icons/cancel.png"));
      cancelButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent event) {
            cancelPressed();
         }
      });

      return ttComposite;
   }

   @Override
   protected void okPressed() {
      delay = spinnerAutoRefreshDelay.getSelection();
      super.okPressed();
   }

   // Remove standard dialog buttons
   @Override
   protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
      return null;
   }

   // Remove the spaces required for standard dialog buttons
   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      GridLayout gl = (GridLayout) parent.getLayout();
      gl.marginHeight = 0;
      gl.marginTop = 0;
      gl.marginBottom = 0;
      gl.verticalSpacing = 0;
      super.createButtonsForButtonBar(parent);
   }

   // ----------------
   // Standard Getters
   // ----------------
   public int getDelay() {
      return delay;
   }

}

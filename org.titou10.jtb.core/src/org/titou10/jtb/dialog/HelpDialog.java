/* Copyright (C) 2015-2017 Denis Forveille titou10.titou10@gmail.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>. */
package org.titou10.jtb.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * Help Dialog
 * 
 * @author Denis Forveille
 *
 */
public class HelpDialog extends Dialog {

   private static final String LABEL    = "Please check the JMSToolBox WiKi online here:";
   private static final String WEB      = "https://sourceforge.net/p/jmstoolbox/wiki/Home/";
   private static final String WEB_LINK = "<a href=\"" + WEB + "\">" + WEB + "</a>";

   public HelpDialog(Shell parentShell) {
      super(parentShell);
      setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.PRIMARY_MODAL);
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Help");
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      container.setFont(SWTResourceManager.getFont("Tahoma", 12, SWT.NORMAL));
      GridLayout gl_container = new GridLayout(1, false);
      gl_container.marginWidth = 20;
      gl_container.verticalSpacing = 10;
      container.setLayout(gl_container);

      Label lbl = new Label(container, SWT.NONE);
      lbl.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
      lbl.setFont(SWTResourceManager.getFont("Tahoma", 9, SWT.NORMAL));
      lbl.setText(LABEL);

      Link webLink = new Link(container, SWT.NONE);
      webLink.setFont(SWTResourceManager.getFont("Tahoma", 9, SWT.NORMAL));
      webLink.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
      webLink.setText(WEB_LINK);
      webLink.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
         Program.launch(WEB);
      }));

      return container;
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.OK_ID, "OK", true);
   }

   @Override
   protected Control createButtonBar(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);

      // create a layout with spacing and margins appropriate for the font size.
      GridLayout layout = new GridLayout();
      layout.numColumns = 0; // this is incremented by createButton
      layout.makeColumnsEqualWidth = true;
      layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
      layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
      layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
      layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
      composite.setLayout(layout);

      // GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
      GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_CENTER);
      composite.setLayoutData(data);
      composite.setFont(parent.getFont());

      // Add the buttons to the button bar.
      createButtonsForButtonBar(composite);
      return composite;
   }

}

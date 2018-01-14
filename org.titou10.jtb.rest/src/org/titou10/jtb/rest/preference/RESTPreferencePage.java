/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.rest.preference;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.titou10.jtb.rest.util.Constants;

/**
 * Provides the preferences page contribution for the REST Connector
 * 
 * @author Denis Forveille
 *
 */
public final class RESTPreferencePage extends PreferencePage {

   private IPreferenceStore ps;

   private Spinner          spinnerPort;
   private Button           startRESTOnStartup;

   public RESTPreferencePage(IPreferenceStore ps) {
      super("REST Connector");
      this.ps = ps;
   }

   @Override
   protected Control createContents(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      composite.setLayout(new GridLayout(2, false));

      Label lbl1 = new Label(composite, SWT.LEFT);
      lbl1.setText("Listen on port  ");

      spinnerPort = new Spinner(composite, SWT.BORDER);
      spinnerPort.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
      spinnerPort.setMinimum(1);
      spinnerPort.setMaximum(65535);
      spinnerPort.setIncrement(1);
      spinnerPort.setPageIncrement(50);
      spinnerPort.setTextLimit(5);

      startRESTOnStartup = new Button(composite, SWT.CHECK);
      startRESTOnStartup.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      startRESTOnStartup.setText("Start the REST connector on JMSToolBox startup");

      // Set Values
      spinnerPort.setSelection(ps.getInt(Constants.PREF_REST_PORT));
      startRESTOnStartup.setSelection(ps.getBoolean(Constants.PREF_REST_AUTOSTART));

      return composite;
   }

   @Override
   public boolean performOk() {
      saveValues();
      return true;
   }

   @Override
   protected void performApply() {
      saveValues();
   }

   @Override
   protected void performDefaults() {
      spinnerPort.setSelection(ps.getDefaultInt(Constants.PREF_REST_PORT));
      startRESTOnStartup.setSelection(ps.getDefaultBoolean(Constants.PREF_REST_AUTOSTART));
   }

   // -------
   // Helpers
   // -------
   private void saveValues() {
      // Page is lazily loaded, so components may be null if the page has not been visited
      if (spinnerPort == null) {
         return;
      }

      ps.setValue(Constants.PREF_REST_PORT, spinnerPort.getSelection());
      ps.setValue(Constants.PREF_REST_AUTOSTART, startRESTOnStartup.getSelection());
   }
}

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
package org.titou10.jtb.rest;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.titou10.jtb.rest.util.Constants;

public final class RESTPreferencePage extends PreferencePage {

   private IPreferenceStore preferenceStore;

   private Spinner          spinnerPort;
   private Button           startRESTOnStartup;

   public RESTPreferencePage(PreferenceStore preferenceStore) {
      super("REST Connector");
      this.preferenceStore = preferenceStore;
   }

   @Override
   protected Control createContents(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      composite.setLayout(new GridLayout(2, false));

      Label lbl1 = new Label(composite, SWT.LEFT);
      lbl1.setText("Listen on port  ");
      spinnerPort = new Spinner(composite, SWT.BORDER);
      spinnerPort.setMinimum(1);
      spinnerPort.setMaximum(65535);
      spinnerPort.setIncrement(1);
      spinnerPort.setPageIncrement(50);
      spinnerPort.setTextLimit(5);
      GridData gd1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
      spinnerPort.setLayoutData(gd1);

      Label lbl6 = new Label(composite, SWT.LEFT);
      lbl6.setText("Start the REST connector on JMSToolBox startup? ");
      startRESTOnStartup = new Button(composite, SWT.CHECK);

      // Set Values
      spinnerPort.setSelection(preferenceStore.getInt(Constants.PREF_REST_PORT));
      startRESTOnStartup.setSelection(preferenceStore.getBoolean(Constants.PREF_REST_AUTOSTART));

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
      spinnerPort.setSelection(preferenceStore.getDefaultInt(Constants.PREF_REST_PORT));
      startRESTOnStartup.setSelection(preferenceStore.getDefaultBoolean(Constants.PREF_REST_AUTOSTART));
   }

   // -------
   // Helpers
   // -------
   private void saveValues() {
      preferenceStore.setValue(Constants.PREF_REST_PORT, spinnerPort.getSelection());
      preferenceStore.setValue(Constants.PREF_REST_AUTOSTART, startRESTOnStartup.getSelection());
   }

}

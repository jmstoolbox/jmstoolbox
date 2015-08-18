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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Display the preference dialog
 * 
 * @author Denis Forveille
 *
 */
public class PreferencesDialog extends PreferenceDialog {

   public PreferencesDialog(Shell parentShell, PreferenceManager manager, PreferenceStore preferenceStore) {
      super(parentShell, manager);
      setDefaultImage(Utils.getImage(this.getClass(), "icons/preferences/cog.png"));

      setPreferenceStore(preferenceStore);

      PreferenceNode one = new PreferenceNode(Constants.JTB_CONFIG_PROJECT, new PrefPageOne(preferenceStore));
      manager.addToRoot(one);
   }

   private final class PrefPageOne extends PreferencePage {

      private IPreferenceStore preferenceStore;

      private Spinner          spinnerAutoRefreshDelay;
      private Spinner          spinnerMaxMessages;
      private Button           systemObject;

      public PrefPageOne(PreferenceStore preferenceStore) {
         super(Constants.JTB_CONFIG_PROJECT);
         this.preferenceStore = preferenceStore;
      }

      @Override
      protected Control createContents(Composite parent) {
         Composite composite = new Composite(parent, SWT.NONE);
         composite.setLayout(new GridLayout(3, false));

         Label lbl1 = new Label(composite, SWT.LEFT);
         lbl1.setText("Limit messages displayed to ");
         spinnerMaxMessages = new Spinner(composite, SWT.BORDER);
         spinnerMaxMessages.setMinimum(0);
         spinnerMaxMessages.setMaximum(5000);
         spinnerMaxMessages.setIncrement(1);
         spinnerMaxMessages.setPageIncrement(50);
         GridData gd1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
         spinnerMaxMessages.setLayoutData(gd1);
         Label lbl2 = new Label(composite, SWT.LEFT);
         lbl2.setText("messages (0 = no limit)");

         Label lbl3 = new Label(composite, SWT.LEFT);
         lbl3.setText("Auto Refresh Delay: ");
         spinnerAutoRefreshDelay = new Spinner(composite, SWT.BORDER | SWT.RIGHT);
         spinnerAutoRefreshDelay.setMinimum(10);
         spinnerAutoRefreshDelay.setMaximum(120);
         spinnerAutoRefreshDelay.setIncrement(10);
         GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
         spinnerAutoRefreshDelay.setLayoutData(gd2);
         Label lbl4 = new Label(composite, SWT.LEFT);
         lbl4.setText("seconds");

         Label lbl5 = new Label(composite, SWT.LEFT);
         lbl5.setText("Show system objects? ");
         systemObject = new Button(composite, SWT.CHECK);
         new Label(composite, SWT.LEFT);

         // Set Values
         spinnerMaxMessages.setSelection(preferenceStore.getInt(Constants.PREF_MAX_MESSAGES));
         spinnerAutoRefreshDelay.setSelection(preferenceStore.getInt(Constants.PREF_AUTO_REFRESH_DELAY));
         systemObject.setSelection(preferenceStore.getBoolean(Constants.PREF_SHOW_SYSTEM_OBJECTS));

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
         spinnerMaxMessages.setSelection(preferenceStore.getDefaultInt(Constants.PREF_MAX_MESSAGES));
         spinnerAutoRefreshDelay.setSelection(preferenceStore.getDefaultInt(Constants.PREF_AUTO_REFRESH_DELAY));
         systemObject.setSelection(preferenceStore.getDefaultBoolean(Constants.PREF_SHOW_SYSTEM_OBJECTS));
      }

      // -------
      // Helpers
      // -------
      private void saveValues() {
         preferenceStore.setValue(Constants.PREF_MAX_MESSAGES, spinnerMaxMessages.getSelection());
         preferenceStore.setValue(Constants.PREF_AUTO_REFRESH_DELAY, spinnerAutoRefreshDelay.getSelection());
         preferenceStore.setValue(Constants.PREF_SHOW_SYSTEM_OBJECTS, systemObject.getSelection());
      }

   }
}

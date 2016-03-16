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
package org.titou10.jtb.dialog;

import org.eclipse.jface.dialogs.MessageDialog;
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

   private Shell   shell;
   private boolean oldTrustAllCertificates;
   private boolean needsRestart;

   public PreferencesDialog(Shell parentShell, PreferenceManager manager, PreferenceStore preferenceStore) {
      super(parentShell, manager);
      setDefaultImage(Utils.getImage(this.getClass(), "icons/preferences/cog.png"));

      this.shell = parentShell;

      setPreferenceStore(preferenceStore);

      PreferenceNode one = new PreferenceNode("P1", new PrefPageGeneral("General", preferenceStore));
      PreferenceNode two = new PreferenceNode("P2", new PrefPageREST("REST configuration", preferenceStore));
      manager.addToRoot(one);
      manager.addToRoot(two);

      oldTrustAllCertificates = preferenceStore.getBoolean(Constants.PREF_TRUST_ALL_CERTIFICATES);
      needsRestart = false;
   }

   public boolean isNeedsRestart() {
      return needsRestart;
   }

   private final class PrefPageREST extends PreferencePage {

      private IPreferenceStore preferenceStore;

      private Spinner          spinnerPort;
      private Button           startRESTOnStartup;

      public PrefPageREST(String title, PreferenceStore preferenceStore) {
         super(title);
         this.preferenceStore = preferenceStore;
      }

      @Override
      protected Control createContents(Composite parent) {
         Composite composite = new Composite(parent, SWT.NONE);
         composite.setLayout(new GridLayout(2, false));

         Label lbl1 = new Label(composite, SWT.LEFT);
         lbl1.setText("Listen on port  ");
         spinnerPort = new Spinner(composite, SWT.BORDER);
         spinnerPort.setMinimum(1024);
         spinnerPort.setMaximum(65535);
         spinnerPort.setIncrement(1);
         spinnerPort.setPageIncrement(50);
         spinnerPort.setTextLimit(5);
         GridData gd1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
         spinnerPort.setLayoutData(gd1);

         Label lbl6 = new Label(composite, SWT.LEFT);
         lbl6.setText("Start the REST listener when JMSToolBox starts? ");
         startRESTOnStartup = new Button(composite, SWT.CHECK);

         // Set Values
         spinnerPort.setSelection(preferenceStore.getInt(Constants.PREF_REST_PORT));
         startRESTOnStartup.setSelection(preferenceStore.getBoolean(Constants.PREF_REST_AUTOSTART));

         return composite;
      }

      @Override
      public boolean performOk() {
         saveValues();

         // Reboot required, confirm with user
         if (oldTrustAllCertificates == preferenceStore.getBoolean(Constants.PREF_TRUST_ALL_CERTIFICATES)) {
            needsRestart = false;
         } else {
            if (!(MessageDialog
                     .openConfirm(shell,
                                  "Confirmation",
                                  "The 'Trust all certificates' option changed. This requires to restarts the application. Continue?"))) {
               needsRestart = false;
               return false;
            }
            needsRestart = true;
         }

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

   private final class PrefPageGeneral extends PreferencePage {

      private IPreferenceStore preferenceStore;

      private Spinner          spinnerAutoRefreshDelay;
      private Spinner          spinnerMaxMessages;
      private Button           systemObject;
      private Button           trustAllCertificates;
      private Button           clearScriptLogsOnExecution;

      public PrefPageGeneral(String title, PreferenceStore preferenceStore) {
         super(title);
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
         spinnerMaxMessages.setMaximum(9999);
         spinnerMaxMessages.setIncrement(1);
         spinnerMaxMessages.setPageIncrement(50);
         spinnerMaxMessages.setTextLimit(4);
         GridData gd1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
         spinnerMaxMessages.setLayoutData(gd1);
         Label lbl2 = new Label(composite, SWT.LEFT);
         lbl2.setText("messages (0 = no limit)");

         Label lbl3 = new Label(composite, SWT.LEFT);
         lbl3.setText("Auto Refresh Delay: ");
         spinnerAutoRefreshDelay = new Spinner(composite, SWT.BORDER | SWT.RIGHT);
         spinnerAutoRefreshDelay.setMinimum(5);
         spinnerAutoRefreshDelay.setMaximum(600);
         spinnerAutoRefreshDelay.setIncrement(1);
         spinnerAutoRefreshDelay.setPageIncrement(5);
         spinnerAutoRefreshDelay.setTextLimit(3);
         GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
         spinnerAutoRefreshDelay.setLayoutData(gd2);
         Label lbl4 = new Label(composite, SWT.LEFT);
         lbl4.setText("seconds");

         Label lbl5 = new Label(composite, SWT.LEFT);
         lbl5.setText("Show system objects? ");
         systemObject = new Button(composite, SWT.CHECK);
         new Label(composite, SWT.LEFT);

         Label lbl6 = new Label(composite, SWT.LEFT);
         lbl6.setText("Trust all certificates? ");
         trustAllCertificates = new Button(composite, SWT.CHECK);
         Label lbl7 = new Label(composite, SWT.LEFT);
         lbl7.setText("!!! This option opens a security hole");

         Label lbl8 = new Label(composite, SWT.LEFT);
         lbl8.setText("Clear scripts logs before execution/simulation?");
         clearScriptLogsOnExecution = new Button(composite, SWT.CHECK);
         new Label(composite, SWT.LEFT);

         // Set Values
         spinnerMaxMessages.setSelection(preferenceStore.getInt(Constants.PREF_MAX_MESSAGES));
         spinnerAutoRefreshDelay.setSelection(preferenceStore.getInt(Constants.PREF_AUTO_REFRESH_DELAY));
         systemObject.setSelection(preferenceStore.getBoolean(Constants.PREF_SHOW_SYSTEM_OBJECTS));
         trustAllCertificates.setSelection(preferenceStore.getBoolean(Constants.PREF_TRUST_ALL_CERTIFICATES));
         clearScriptLogsOnExecution.setSelection(preferenceStore.getBoolean(Constants.PREF_CLEAR_LOGS_EXECUTION));

         return composite;
      }

      @Override
      public boolean performOk() {
         saveValues();

         // Reboot required, confirm with user
         if (oldTrustAllCertificates == preferenceStore.getBoolean(Constants.PREF_TRUST_ALL_CERTIFICATES)) {
            needsRestart = false;
         } else {
            if (!(MessageDialog
                     .openConfirm(shell,
                                  "Confirmation",
                                  "The 'Trust all certificates' option changed. This requires to restarts the application. Continue?"))) {
               needsRestart = false;
               return false;
            }
            needsRestart = true;
         }

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
         trustAllCertificates.setSelection(preferenceStore.getDefaultBoolean(Constants.PREF_TRUST_ALL_CERTIFICATES));
         clearScriptLogsOnExecution.setSelection(preferenceStore.getDefaultBoolean(Constants.PREF_CLEAR_LOGS_EXECUTION));
      }

      // -------
      // Helpers
      // -------
      private void saveValues() {
         preferenceStore.setValue(Constants.PREF_MAX_MESSAGES, spinnerMaxMessages.getSelection());
         preferenceStore.setValue(Constants.PREF_AUTO_REFRESH_DELAY, spinnerAutoRefreshDelay.getSelection());
         preferenceStore.setValue(Constants.PREF_SHOW_SYSTEM_OBJECTS, systemObject.getSelection());
         preferenceStore.setValue(Constants.PREF_TRUST_ALL_CERTIFICATES, trustAllCertificates.getSelection());
         preferenceStore.setValue(Constants.PREF_CLEAR_LOGS_EXECUTION, clearScriptLogsOnExecution.getSelection());
      }

   }

}

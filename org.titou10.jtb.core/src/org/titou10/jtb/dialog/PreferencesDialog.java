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

import java.io.IOException;

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
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Display the preference dialog
 * 
 * @author Denis Forveille
 *
 */
public class PreferencesDialog extends PreferenceDialog {

   private static final Logger log = LoggerFactory.getLogger(PreferencesDialog.class);

   private JTBStatusReporter   jtbStatusReporter;

   private Shell               shell;
   private boolean             oldTrustAllCertificates;
   private boolean             needsRestart;

   public PreferencesDialog(Shell parentShell, JTBStatusReporter jtbStatusReporter, PreferenceManager manager, ConfigManager cm) {
      super(parentShell, manager);
      setDefaultImage(Utils.getImage(this.getClass(), "icons/preferences/cog.png"));

      this.shell = parentShell;
      this.jtbStatusReporter = jtbStatusReporter;

      PreferenceStore preferenceStore = cm.getPreferenceStore();
      setPreferenceStore(preferenceStore);

      PreferenceNode one = new PreferenceNode("P1", new PrefPageGeneral("General", preferenceStore));
      manager.addToRoot(one);

      for (PreferencePage pp : cm.getPluginsPreferencePages()) {
         manager.addToRoot(new PreferenceNode(pp.getTitle(), pp));
      }

      oldTrustAllCertificates = preferenceStore.getBoolean(Constants.PREF_TRUST_ALL_CERTIFICATES);
      needsRestart = false;
   }

   public boolean isNeedsRestart() {
      return needsRestart;
   }

   private final class PrefPageGeneral extends PreferencePage {

      private IPreferenceStore preferenceStore;

      private Spinner          spinnerAutoRefreshDelay;
      private Spinner          spinnerMaxMessages;
      private Button           systemObject;
      private Button           trustAllCertificates;
      private Button           clearScriptLogsOnExecution;
      private Spinner          spinnerMaxMessagesTopic;
      private Text             textConnectionClientId;

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
         spinnerMaxMessages.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
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
         spinnerAutoRefreshDelay.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
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

         Label lbl9 = new Label(composite, SWT.LEFT);
         lbl9.setText("Number of messages to capture per Topic Subscription: ");
         spinnerMaxMessagesTopic = new Spinner(composite, SWT.BORDER);
         spinnerMaxMessagesTopic.setMinimum(0);
         spinnerMaxMessagesTopic.setMaximum(9999);
         spinnerMaxMessagesTopic.setIncrement(1);
         spinnerMaxMessagesTopic.setPageIncrement(50);
         spinnerMaxMessagesTopic.setTextLimit(4);
         spinnerMaxMessagesTopic.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
         Label lbl10 = new Label(composite, SWT.LEFT);
         lbl10.setText("(0 = no limit)");

         Label lbl11 = new Label(composite, SWT.LEFT);
         lbl11.setText("Connection Client ID prefix : ");
         textConnectionClientId = new Text(composite, SWT.BORDER);
         textConnectionClientId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
         new Label(composite, SWT.LEFT);

         // Set Values
         spinnerMaxMessages.setSelection(preferenceStore.getInt(Constants.PREF_MAX_MESSAGES));
         spinnerAutoRefreshDelay.setSelection(preferenceStore.getInt(Constants.PREF_AUTO_REFRESH_DELAY));
         systemObject.setSelection(preferenceStore.getBoolean(Constants.PREF_SHOW_SYSTEM_OBJECTS));
         trustAllCertificates.setSelection(preferenceStore.getBoolean(Constants.PREF_TRUST_ALL_CERTIFICATES));
         clearScriptLogsOnExecution.setSelection(preferenceStore.getBoolean(Constants.PREF_CLEAR_LOGS_EXECUTION));
         spinnerMaxMessagesTopic.setSelection(preferenceStore.getInt(Constants.PREF_MAX_MESSAGES_TOPIC));
         textConnectionClientId.setText(preferenceStore.getString(Constants.PREF_CONN_CLIENT_ID_PREFIX));

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
         spinnerMaxMessagesTopic.setSelection(preferenceStore.getDefaultInt(Constants.PREF_MAX_MESSAGES_TOPIC));
         textConnectionClientId.setText(preferenceStore.getDefaultString(Constants.PREF_CONN_CLIENT_ID_PREFIX));
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
         preferenceStore.setValue(Constants.PREF_MAX_MESSAGES_TOPIC, spinnerMaxMessagesTopic.getSelection());
         preferenceStore.setValue(Constants.PREF_CONN_CLIENT_ID_PREFIX, textConnectionClientId.getText());

         // Save the preferences
         try {
            ((PreferenceStore) getPreferenceStore()).save();
         } catch (IOException e) {
            String msg = "Exception occurred when saving preferences";
            log.error(msg, e);
            jtbStatusReporter.showError(msg, Utils.getCause(e), e.getMessage());

         }
      }

   }

}

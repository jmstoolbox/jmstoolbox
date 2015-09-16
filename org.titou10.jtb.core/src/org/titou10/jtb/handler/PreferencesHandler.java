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
package org.titou10.jtb.handler;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.dialog.PreferencesDialog;

/**
 * 
 * Manage the Preferences Dialog
 * 
 * @author Denis Forveille
 *
 */
public class PreferencesHandler {

   private static final Logger log = LoggerFactory.getLogger(PreferencesHandler.class);

   @Inject
   private ConfigManager cm;

   @Execute
   public void execute(Shell shell, IWorkbench workbench) {
      log.debug("execute.");

      PreferenceManager pm = new PreferenceManager();

      PreferenceStore ps = cm.getPreferenceStore();

      PreferencesDialog dialog = new PreferencesDialog(shell, pm, ps);
      dialog.open();

      if (dialog.isNeedsRestart()) {
         log.debug("Restarting as required");
         workbench.restart();
      }
   }

}

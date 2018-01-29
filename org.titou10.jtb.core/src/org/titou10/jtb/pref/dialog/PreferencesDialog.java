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
package org.titou10.jtb.pref.dialog;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.config.JTBPreferenceStore;
import org.titou10.jtb.cs.ColumnsSetsManager;
import org.titou10.jtb.sessiontype.SessionTypeManager;
import org.titou10.jtb.ui.JTBStatusReporter;

/**
 * Display the preference dialog
 * 
 * @author Denis Forveille
 *
 */
public class PreferencesDialog extends PreferenceDialog {

   private PageGeneral pageGeneral;

   public PreferencesDialog(Shell parentShell,
                            IEventBroker eventBroker,
                            JTBStatusReporter jtbStatusReporter,
                            JTBPreferenceStore ps,
                            ConfigManager cm,
                            ColumnsSetsManager csManager,
                            PreferenceManager pm,
                            SessionTypeManager sessionTypeManager) {
      super(parentShell, pm);
      setDefaultImage(SWTResourceManager.getImage(this.getClass(), "icons/preferences/cog.png"));

      setPreferenceStore(ps);

      pageGeneral = new PageGeneral(jtbStatusReporter, ps, csManager);

      PreferenceNode nodeGeneral = new PreferenceNode("General", pageGeneral);
      PreferenceNode nodeSessionType = new PreferenceNode("SessionType",
                                                          new PageSessionType(eventBroker, jtbStatusReporter, sessionTypeManager));

      pm.addToRoot(nodeGeneral);
      pm.addToRoot(nodeSessionType);

      for (PreferencePage pp : cm.getPluginsPreferencePages()) {
         pm.addToRoot(new PreferenceNode(pp.getTitle(), pp));
      }

   }

   public boolean isNeedsRestart() {
      return pageGeneral.isNeedsRestart();
   }

}

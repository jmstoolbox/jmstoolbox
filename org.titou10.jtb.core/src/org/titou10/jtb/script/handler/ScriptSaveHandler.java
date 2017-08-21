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
package org.titou10.jtb.script.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.script.ScriptsManager;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;

/**
 * Manage the "Script Save" command
 * 
 * @author Denis Forveille
 * 
 */
public class ScriptSaveHandler {

   private static final Logger log = LoggerFactory.getLogger(ScriptSaveHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private ScriptsManager      scriptsManager;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(MPart part, @Named(Constants.CURRENT_WORKING_SCRIPT) Script workingScript) {
      log.debug("execute");

      // Clone the workingScript for another
      Script scriptToSave = scriptsManager.cloneScript(workingScript, workingScript.getName(), workingScript.getParent());

      // Replace the script into the collection of scripts
      List<Script> scriptsInParentDir = workingScript.getParent().getScript();
      for (Script s : scriptsInParentDir) {
         if (s.getName().equals(scriptToSave.getName())) {
            scriptsInParentDir.remove(s);
            break;
         }
      }
      scriptsInParentDir.add(scriptToSave);

      // Refresh Script Browser with new instances
      eventBroker.post(Constants.EVENT_REFRESH_SCRIPTS_BROWSER, workingScript.getParent());

      try {
         scriptsManager.writeConfig();
         part.setDirty(false);
      } catch (Exception e) {
         jtbStatusReporter.showError("Problem while saving Script", e);
         return;
      }

   }

   @CanExecute
   public boolean canExecute(MWindow window) {

      // Display the Buttons only if a Script is Selected, either new or old
      Script script = (Script) window.getContext().get(Constants.CURRENT_WORKING_SCRIPT);
      if (script == null) {
         return false;
      } else {
         return true;
      }
   }

}

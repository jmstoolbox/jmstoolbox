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
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.dialog.ScriptNewPauseDialog;
import org.titou10.jtb.dialog.ScriptNewStepDialog;
import org.titou10.jtb.script.ScriptsUtils;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.util.Constants;

/**
 * Manage the "Script New Step" command
 * 
 * @author Denis Forveille
 * 
 */
public class ScriptNewStepHandler {

   private static final Logger log = LoggerFactory.getLogger(ScriptNewStepHandler.class);

   @Inject
   private IEventBroker eventBroker;

   @Inject
   private ConfigManager cm;

   @Execute
   public void execute(Shell shell,
                       MWindow window,
                       @Named(IServiceConstants.ACTIVE_PART) MPart part,
                       @Named(Constants.COMMAND_SCRIPT_NEWSTEP_PARAM) String mode) {
      log.debug("execute. mode={}", mode);

      Script script = (Script) window.getContext().get(Constants.CURRENT_WORKING_SCRIPT);

      // Show New Step Dialog
      Step s;
      switch (mode) {
         case Constants.COMMAND_SCRIPT_NEWSTEP_STEP:

            ScriptNewStepDialog d1 = new ScriptNewStepDialog(shell, cm);
            if (d1.open() != Window.OK) {
               return;
            }
            s = ScriptsUtils.buildStep(d1.getTemplateName(),
                                       d1.getSessionName(),
                                       d1.getDestinationName(),
                                       d1.getDelay(),
                                       d1.getIterations());
            script.getStep().add(s);

            // Indicate that script is dirty
            part.setDirty(true);

            break;

         case Constants.COMMAND_SCRIPT_NEWSTEP_PAUSE:

            ScriptNewPauseDialog d2 = new ScriptNewPauseDialog(shell);
            if (d2.open() != Window.OK) {
               return;
            }
            log.debug("delay : {} seconds", d2.getDelay());
            s = ScriptsUtils.buildPauseStep(d2.getDelay());
            script.getStep().add(s);

            // Indicate that script is dirty
            part.setDirty(true);

            break;

         default:
            break;
      }

      // Refresh Script Browser
      eventBroker.post(Constants.EVENT_REFRESH_SCRIPT_EDIT, script);

   }

   @CanExecute
   public boolean canExecute(MWindow window) {

      // Display the "New Step" buttons only if a Script is Selected, either "new" or "old"
      Script script = (Script) window.getContext().get(Constants.CURRENT_WORKING_SCRIPT);
      if (script == null) {
         return false;
      } else {
         return true;
      }
   }
}

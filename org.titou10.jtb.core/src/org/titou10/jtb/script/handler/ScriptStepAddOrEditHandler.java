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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.script.ScriptsManager;
import org.titou10.jtb.script.dialog.ScriptNewPauseDialog;
import org.titou10.jtb.script.dialog.ScriptNewStepDialog;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.script.gen.StepKind;
import org.titou10.jtb.sessiontype.SessionTypeManager;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;

/**
 * Manage the "Script New Step" command
 * 
 * @author Denis Forveille
 * 
 */
public class ScriptStepAddOrEditHandler {

   private static final Logger log = LoggerFactory.getLogger(ScriptStepAddOrEditHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private ConfigManager       cm;

   @Inject
   private TemplatesManager    templatesManager;

   @Inject
   private ScriptsManager      scriptsManager;

   @Inject
   private SessionTypeManager  sessionTypeManager;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell,
                       MWindow window,
                       @Named(IServiceConstants.ACTIVE_PART) MPart part,
                       @Named(Constants.COMMAND_SCRIPT_NEWSTEP_PARAM) String mode,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional Step selection) {
      log.debug("execute. mode={}", mode);

      Script script = (Script) window.getContext().get(Constants.CURRENT_WORKING_SCRIPT);

      Step step;
      switch (mode) {
         case Constants.COMMAND_SCRIPT_NEWSTEP_EDIT:
            step = scriptsManager.cloneStep(selection);
            break;

         case Constants.COMMAND_SCRIPT_NEWSTEP_STEP:
            step = scriptsManager.buildStep();
            break;

         case Constants.COMMAND_SCRIPT_NEWSTEP_PAUSE:
            step = scriptsManager.buildPauseStep(5);
            break;

         default:
            throw new IllegalArgumentException(mode + " value is invalid");

      }

      if (step.getKind() == StepKind.REGULAR) {
         ScriptNewStepDialog d1 = new ScriptNewStepDialog(shell,
                                                          jtbStatusReporter,
                                                          cm,
                                                          templatesManager,
                                                          scriptsManager,
                                                          sessionTypeManager,
                                                          step,
                                                          script);
         if (d1.open() != Window.OK) {
            return;
         }
         step = d1.getStep();
      } else {
         ScriptNewPauseDialog d2 = new ScriptNewPauseDialog(shell, step, script.getName());
         if (d2.open() != Window.OK) {
            return;
         }
         step = d2.getStep();
      }

      switch (mode) {
         case Constants.COMMAND_SCRIPT_NEWSTEP_EDIT:
            int index = script.getStep().indexOf(selection);
            if (index > -1) {
               script.getStep().set(index, step);
            } else {
               script.getStep().add(step);
            }
            break;

         case Constants.COMMAND_SCRIPT_NEWSTEP_STEP:
         case Constants.COMMAND_SCRIPT_NEWSTEP_PAUSE:
            script.getStep().add(step);
            break;

         default:
            // Impossible
            break;
      }

      // Indicate that script is dirty
      part.setDirty(true);

      // Refresh Script Browser
      eventBroker.post(Constants.EVENT_REFRESH_SCRIPT_EDIT, "X");

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

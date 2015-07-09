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
package org.titou10.jtb.script;

import java.util.List;
import java.util.Random;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.script.ScriptStepResult.ExectionActionCode;
import org.titou10.jtb.script.ScriptStepResult.ExectionReturnCode;
import org.titou10.jtb.script.gen.GlobalVariable;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.script.gen.StepKind;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.variable.gen.Variable;

/**
 * Engine for script execution
 * 
 * @author Denis Forveille
 *
 */
public class ScriptExecutionEngine {

   private static final Logger log = LoggerFactory.getLogger(ScriptExecutionEngine.class);

   private IEventBroker eventBroker;

   private ConfigManager cm;
   private Script        script;

   private List<JTBMessageTemplate> templates;
   private List<JTBSession>         sessions;
   private List<Variable>           variables;

   private List<ScriptStepResult> logResult;

   public ScriptExecutionEngine(IEventBroker eventBroker, ConfigManager cm, Script script, List<ScriptStepResult> logResult) {
      this.script = script;
      this.cm = cm;
      this.eventBroker = eventBroker;
      this.logResult = logResult;
   }

   public void executeScript(boolean simulation) {
      log.debug("executeScript '{}'. simulation? {}", script.getName(), simulation);

      Random r = new Random(System.nanoTime());

      // Point to global objects
      List<JTBSession> cmSession = cm.getJtbSessions();
      List<Variable> cmVariables = cm.getVariables();

      List<Step> steps = script.getStep();
      List<GlobalVariable> globalVariables = script.getGlobalVariable();

      // Check that global variables still exist
      loop: for (GlobalVariable globalVariable : globalVariables) {
         for (Variable v : cmVariables) {
            if (v.getName().equals(globalVariable.getName())) {
               // TODO Validate that default value is compatible with the variable definition

               // TODO Generate a value for the variable

               break loop;
            }
            log.warn("Global Variable '{}' does not exist", globalVariable.getName());
            // The current variable does not exist
            ScriptStepResult res = new ScriptStepResult(ExectionActionCode.VALIDATION,
                                                        ExectionReturnCode.FAIL,
                                                        globalVariable.getName() + " does not exist");
            logResult.add(res);
            return;
         }
      }

      // Generate global variables values
      // VariablesUtils.resolveVariable(r, variable);

      // Execute steps
      RuntimeStep rs;
      for (Step step : steps) {

         if (step.getKind() == StepKind.PAUSE) {
            ScriptStepResult res = ScriptStepResult.createStartPause();
            logResult.add(res);
            eventBroker.send(Constants.EVENT_REFRESH_EXECUTION_LOG, res);

            int delay = step.getPauseSecsAfter().intValue();
            log.debug("running pause step.delay : {} seconds", delay);
            try {
               Thread.sleep(delay * 1000);
            } catch (InterruptedException e) {
               // NOP
            }
            res.updateSuccess(" Waited " + delay + " seconds");
            eventBroker.send(Constants.EVENT_REFRESH_EXECUTION_LOG, res);

            continue;
         }

         // Build Runtime Object
         rs = new RuntimeStep();

         // Find template

         // Find session

         // Find destination

         // Resolve Variables

         // If simulation, just log the result
         if (simulation) {

         } else {
            // Execute `the step

         }

      }

   }

   // -------
   // Helpers
   // -------

   private class RuntimeStep {
      private JTBMessageTemplate template;
      private JTBSession         session;
      private JTBDestination     destination;
   }
}

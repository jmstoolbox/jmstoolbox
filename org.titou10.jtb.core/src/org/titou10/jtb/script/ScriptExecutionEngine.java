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

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.script.ScriptStepResult.ExectionActionCode;
import org.titou10.jtb.script.ScriptStepResult.ExectionReturnCode;
import org.titou10.jtb.script.gen.GlobalVariable;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.template.TemplatesUtils;
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

      // Do the work in another Thread in order to be able to refresh the progress log...
      // TODO Implement UI blocking or BusyIndicator or Cancelable ProgressMonitor..
      Runnable runnable = new Runnable() {
         public void run() {
            executeScriptInBackground(false);
         }
      };
      new Thread(runnable).start();

   }

   private void executeScriptInBackground(boolean simulation) {
      log.debug("executeScriptInBackground '{}'. simulation? {}", script.getName(), simulation);

      Random r = new Random(System.nanoTime());

      // Point to global objects
      List<JTBSession> cmSession = cm.getJtbSessions();
      List<Variable> cmVariables = cm.getVariables();

      List<Step> steps = script.getStep();
      List<GlobalVariable> globalVariables = script.getGlobalVariable();

      // Check that global variables still exist
      loop: for (GlobalVariable globalVariable : globalVariables)

      {
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
      for (Step step : steps)

      {

         switch (step.getKind()) {
            case PAUSE:
               ScriptStepResult res = ScriptStepResult.createStartPause();
               logResult.add(res);
               eventBroker.send(Constants.EVENT_REFRESH_EXECUTION_LOG, res);

               int delay = step.getPauseSecsAfter().intValue();
               log.debug("running pause step.delay : {} seconds", delay);
               if (!simulation) {
                  try {
                     Thread.sleep(delay * 1000);
                  } catch (InterruptedException e) {
                     // NOP
                  }
               }
               res.updateSuccess(" Waited " + delay + " seconds");
               eventBroker.send(Constants.EVENT_REFRESH_EXECUTION_LOG, res);

               continue;

            case REGULAR:

               // Build Runtime Object
               try {
                  rs = new RuntimeStep(step);
               } catch (Exception e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }

               // Resolve Variables

               // If simulation, just log the result
               if (simulation) {

               } else {
                  // Execute `the step

               }
               continue;
            default:
               break;
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

      private boolean sessionWasConnected;

      public RuntimeStep(Step step) throws Exception {
         // Find template
         List<IFile> x = TemplatesUtils.getAllTemplatesIFiles(cm.getTemplateFolder());
         for (IFile iFile : x) {

         }

         // Find session
         // TODO should this code be in a method in cm instead?
         List<JTBSession> sessions = cm.getJtbSessions();
         for (JTBSession jtbSession : sessions) {
            if (jtbSession.getName().equals(step.getSessionName())) {
               session = jtbSession;
               break;
            }
         }
         if (session.isConnected()) {
            sessionWasConnected = true;
         } else {
            sessionWasConnected = false;
            session.connectOrDisconnect();
         }

         // Find destination
         SortedSet<JTBQueue> destinations = session.getJtbQueues();
         for (Iterator<JTBQueue> iterator = destinations.iterator(); iterator.hasNext();) {
            JTBQueue jtbQueue = (JTBQueue) iterator.next();
            if (jtbQueue.getName().equals(step.getDestinationName())) {
               destination = jtbQueue;
               break;
            }

         }
         if (destination == null) {
            // TODO non trouvé
         }
      }

      public void close() throws Exception {
         if (!sessionWasConnected) {
            session.connectOrDisconnect();
         }
      }

      // ----------------
      // Standard Getters
      // ----------------
      public JTBMessageTemplate getTemplate() {
         return template;
      }

      public JTBSession getSession() {
         return session;
      }

      public JTBDestination getDestination() {
         return destination;
      }

   }
}

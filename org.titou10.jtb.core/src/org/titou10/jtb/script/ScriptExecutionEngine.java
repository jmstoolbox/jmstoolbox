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

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.script.ScriptStepResult.ExectionActionCode;
import org.titou10.jtb.script.ScriptStepResult.ExectionReturnCode;
import org.titou10.jtb.script.gen.GlobalVariable;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.script.gen.StepKind;
import org.titou10.jtb.template.TemplatesUtils;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.variable.VariablesUtils;
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

   public ScriptExecutionEngine(IEventBroker eventBroker, ConfigManager cm, Script script) {
      this.script = script;
      this.cm = cm;
      this.eventBroker = eventBroker;
   }

   public void executeScript(final boolean simulation) {
      log.debug("executeScript '{}'. simulation? {}", script.getName(), simulation);

      // TODO Implement UI blocking or BusyIndicator or Cancelable ProgressMonitor..

      // Do the work in another Thread in order to be able to refresh the progress log...
      Runnable runnable = new Runnable() {
         public void run() {
            executeScriptInBackground(simulation);
         }
      };
      new Thread(runnable).start();

   }

   private void executeScriptInBackground(boolean simulation) {
      log.debug("executeScriptInBackground '{}'. simulation? {}", script.getName(), simulation);

      Random r = new Random(System.nanoTime());

      List<Step> steps = script.getStep();
      List<GlobalVariable> globalVariables = script.getGlobalVariable();

      updateLog(ScriptStepResult.createScriptStart());

      // Create runtime objects from steps
      List<RuntimeStep> runtimeSteps = new ArrayList<>(steps.size());
      for (Step step : steps) {
         runtimeSteps.add(new RuntimeStep(step));
      }

      // Gather templates used in the script and validate their existence
      try {
         List<IFile> allTemplates = TemplatesUtils.getAllTemplatesIFiles(cm.getTemplateFolder());
         for (RuntimeStep runtimeStep : runtimeSteps) {
            Step step = runtimeStep.getStep();
            if (step.getKind() == StepKind.REGULAR) {
               String templateName = step.getTemplateName();
               JTBMessageTemplate t = null;
               for (IFile iFile : allTemplates) {
                  String iFileName = "/" + iFile.getProjectRelativePath().removeFirstSegments(1).toPortableString();
                  if (iFileName.equals(templateName)) {
                     t = TemplatesUtils.readTemplate(iFile);
                     break;
                  }
               }
               if (t == null) {
                  updateLog(ScriptStepResult.createValidationTemplateFail(templateName));
                  return;
               }
               runtimeStep.setJtbMessageTemplate(t);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
         return;
      }

      // Gather sessions used in the script and validate their existence
      Map<String, AbstractMap.SimpleEntry<JTBSession, Boolean>> jtbSessionsUsed = new HashMap<>(steps.size());
      for (RuntimeStep runtimeStep : runtimeSteps) {
         Step step = runtimeStep.getStep();
         if (step.getKind() == StepKind.REGULAR) {
            String sessionName = step.getSessionName();
            SimpleEntry<JTBSession, Boolean> e = jtbSessionsUsed.get(sessionName);
            if (e == null) {
               JTBSession jtbSession = cm.getJTBSessionByName(sessionName);
               if (jtbSession == null) {
                  updateLog(ScriptStepResult.createValidationSessionFail(sessionName));
                  return;
               }
               e = new AbstractMap.SimpleEntry<>(jtbSession, (Boolean) null);
               jtbSessionsUsed.put(sessionName, e);
               log.debug("Session with name '{}' added to the list of sessions used in the script", sessionName);
            }
            runtimeStep.setJtbSession(e.getKey());
         }
      }

      // Connect to sessions if they are not connected and remember the state
      // TODO Should we open a new distinct connection?
      for (Entry<String, SimpleEntry<JTBSession, Boolean>> e : jtbSessionsUsed.entrySet()) {
         Map.Entry<JTBSession, Boolean> eJTBSession = e.getValue();
         if (eJTBSession.getKey().isConnected()) {
            eJTBSession.setValue(true);
         } else {
            log.debug("Connecting to {}", eJTBSession.getKey());
            eJTBSession.setValue(false);
            try {
               updateLog(ScriptStepResult.createSessionConnectStart(eJTBSession.getKey().getName()));
               eJTBSession.getKey().connectOrDisconnect();
               updateLog(ScriptStepResult.createSessionConnectEnd());
            } catch (Exception e1) {
               updateLog(ScriptStepResult.createSessionConnectFail(eJTBSession.getKey().getName(), e1));
               return;
            }
         }
      }

      // Resolve Destination Name
      for (RuntimeStep runtimeStep : runtimeSteps) {
         Step step = runtimeStep.getStep();
         if (step.getKind() == StepKind.REGULAR) {
            JTBSession jtbSession = runtimeStep.getJtbSession();
            JTBDestination jtbDestination = jtbSession.getJTBDestinationByName(step.getDestinationName());
            if (jtbDestination == null) {
               updateLog(ScriptStepResult.createValidationDestinationFail(step.getDestinationName()));
               // TODO : Disconnect!
               return;
            }
            runtimeStep.setJtbDestination(jtbDestination);

         }
      }

      // Check that global variables still exist
      List<Variable> cmVariables = cm.getVariables();
      Map<String, String> globalVariablesValues = new HashMap<>(globalVariables.size());

      loop: for (GlobalVariable globalVariable : globalVariables) {
         for (Variable v : cmVariables) {
            if (v.getName().equals(globalVariable.getName())) {

               // Generate a value for the variable if no defaut is provides
               String val = globalVariable.getConstantValue();
               if (val == null) {
                  globalVariablesValues.put(v.getName(), VariablesUtils.resolveVariable(r, v));
               } else {
                  globalVariablesValues.put(v.getName(), val);
               }

               continue loop;
            }
         }

         log.warn("Global Variable '{}' does not exist", globalVariable.getName());
         // The current variable does not exist
         ScriptStepResult res = new ScriptStepResult(ExectionActionCode.VALIDATION,
                                                     ExectionReturnCode.FAIL,
                                                     globalVariable.getName() + " does not exist");
         updateLog(res);
         return;
      }

      // Execute steps
      for (RuntimeStep runtimeStep : runtimeSteps) {
         switch (runtimeStep.getStep().getKind()) {
            case PAUSE:
               executePause(simulation, runtimeStep);
               break;

            case REGULAR:

               // Parse template to replace variables names by global variables values
               JTBMessageTemplate t = runtimeStep.getJtbMessageTemplate();
               String payload = t.getPayloadText();
               if (payload != null) {
                  for (Entry<String, String> v : globalVariablesValues.entrySet()) {
                     payload = payload.replaceAll(VariablesUtils.buildVariableReplaceName(v.getKey()), v.getValue());
                  }
               }

               t.setPayloadText(payload);

               try {
                  executeRegular(simulation, runtimeStep);
               } catch (JMSException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
               break;

            default:
               break;
         }
      }

      // TODO Should be in a finally...
      // Disconnect session that have been opened by this script
      for (

      Entry<String, SimpleEntry<JTBSession, Boolean>> e : jtbSessionsUsed.entrySet())

      {
         Map.Entry<JTBSession, Boolean> eJTBSession = e.getValue();
         if (!eJTBSession.getValue()) {
            log.debug("Disconnecting from {}", e.getKey());
            try {
               eJTBSession.getKey().connectOrDisconnect();
            } catch (Exception e1) {
               // TODO Auto-generated catch block
               e1.printStackTrace();
            }
         }
      }

      updateLog(ScriptStepResult.createScriptEndend());
   }

   // -------
   // Helpers
   // -------

   private void executeRegular(boolean simulation, RuntimeStep runtimeStep) throws JMSException {

      log.debug("executeRegular. SImulation? {}", simulation);

      Step step = runtimeStep.getStep();
      JTBMessageTemplate xx = runtimeStep.getJtbMessageTemplate();
      JTBSession jtbSession = runtimeStep.getJtbSession();
      JTBDestination jtbDestination = runtimeStep.getJtbDestination();

      // Generate local values

      for (int i = 0; i < step.getIterations(); i++) {

         JTBMessageTemplate jtbMessageTemplate = JTBMessageTemplate.deepClone(xx);

         // Generate local variables for each iteration
         String oldPayloadText = jtbMessageTemplate.getPayloadText();
         jtbMessageTemplate.setPayloadText(VariablesUtils.replaceTemplateVariables(cm.getVariables(), oldPayloadText));

         // Create Message
         Message m = jtbSession.createJMSMessage(jtbMessageTemplate.getJtbMessageType());
         jtbMessageTemplate.toJMSMessage(m);

         updateLog(ScriptStepResult.createSendStart(jtbMessageTemplate));

         // Send Message
         if (!simulation) {
            JTBMessage jtbMessage = new JTBMessage(jtbDestination, m);
            jtbDestination.getJtbSession().sendMessage(jtbMessage);

            // Eventually pause after...
            Integer pause = step.getPauseSecsAfter();
            if ((pause != null) && (pause > 0)) {
               updateLog(ScriptStepResult.createPauseStart(pause));

               try {
                  Thread.sleep(step.getPauseSecsAfter() * 1000);
               } catch (InterruptedException e) {
                  // NOP
               }
            }
         }

         updateLog(ScriptStepResult.createSendEnd());
      }

   }

   private void executePause(boolean simulation, RuntimeStep runtimeStep) {
      Step step = runtimeStep.getStep();
      Integer delay = step.getPauseSecsAfter();

      updateLog(ScriptStepResult.createPauseStart(delay));

      log.debug("running pause step.delay : {} seconds", delay);
      if (!simulation) {
         try {
            Thread.sleep(delay * 1000);
         } catch (InterruptedException e) {
            // NOP
         }
      }

      updateLog(ScriptStepResult.createPauseSuccess(delay));

   }

   private void updateLog(ScriptStepResult ssr) {
      if (ssr.getData() != null) {
         log.debug(ssr.getData().toString());
      }
      eventBroker.send(Constants.EVENT_REFRESH_EXECUTION_LOG, ssr);
   }

   private class RuntimeStep {
      private Step               step;
      private JTBMessageTemplate jtbMessageTemplate;
      private JTBSession         jtbSession;
      private JTBDestination     jtbDestination;

      // -----------
      // Constructor
      // -----------
      public RuntimeStep(Step step) {
         this.step = step;
      }

      // ------------------------
      // Standard Getters/Setters
      // ------------------------
      public Step getStep() {
         return step;
      }

      public JTBDestination getJtbDestination() {
         return jtbDestination;
      }

      public void setJtbDestination(JTBDestination jtbDestination) {
         this.jtbDestination = jtbDestination;
      }

      public JTBMessageTemplate getJtbMessageTemplate() {
         return jtbMessageTemplate;
      }

      public void setJtbMessageTemplate(JTBMessageTemplate jtbMessageTemplate) {
         this.jtbMessageTemplate = jtbMessageTemplate;
      }

      public JTBSession getJtbSession() {
         return jtbSession;
      }

      public void setJtbSession(JTBSession jtbSession) {
         this.jtbSession = jtbSession;
      }

   }

}

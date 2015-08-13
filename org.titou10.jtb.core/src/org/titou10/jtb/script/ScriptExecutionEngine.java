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

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBSession;
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

      // TODO Replace with a ProgressMomitor..

      // BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
      // @Override
      // public void run() {
      // executeScriptInBackground(simulation);
      // }
      // });

      ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
      try {
         progressDialog.run(true, true, new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
               executeScriptInBackground(monitor, simulation);
               monitor.done();
            }
         });
      } catch (InterruptedException e) {
         log.info("Process has been cancelled by user");
         updateLog(ScriptStepResult.createScriptCancelled());
         return;
      } catch (InvocationTargetException e) {
         log.error("Exception occured ", e);
         return;
      }
   }

   // private void executeScriptInBackground(boolean simulation) {
   private void executeScriptInBackground(IProgressMonitor monitor, boolean simulation) throws InterruptedException {
      log.debug("executeScriptInBackground '{}'. simulation? {}", script.getName(), simulation);

      Random r = new Random(System.nanoTime());

      List<Step> steps = script.getStep();
      List<GlobalVariable> globalVariables = script.getGlobalVariable();

      updateLog(ScriptStepResult.createScriptStart());

      // Create runtime objects from steps
      int totalWork = 6;
      List<RuntimeStep> runtimeSteps = new ArrayList<>(steps.size());
      for (Step step : steps) {
         runtimeSteps.add(new RuntimeStep(step));
         totalWork += step.getIterations();
      }

      monitor.beginTask("Executing Script", totalWork);

      // Gather templates used in the script and validate their existence
      try {
         monitor.subTask("Validating Templates...");
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

         monitor.worked(1);
         if (monitor.isCanceled()) {
            monitor.done();
            throw new InterruptedException();
         }

      } catch (Exception e) {
         updateLog(ScriptStepResult.createValidationExceptionFail("A probleme occured while validating templates", e));
         return;
      }

      // Gather sessions used in the script and validate their existence
      monitor.subTask("Validating Sessions...");
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
      monitor.worked(1);
      if (monitor.isCanceled()) {
         monitor.done();
         throw new InterruptedException();
      }

      // Connect to sessions if they are not connected and remember the state
      // TODO Should we open a new distinct connection?
      monitor.subTask("Opening Sessions...");
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
               updateLog(ScriptStepResult.createSessionConnectSuccess());
            } catch (Exception e1) {
               updateLog(ScriptStepResult.createSessionConnectFail(eJTBSession.getKey().getName(), e1));
               return;
            }
         }
      }
      monitor.worked(1);
      if (monitor.isCanceled()) {
         monitor.done();
         throw new InterruptedException();
      }

      // Resolve Destination Name
      monitor.subTask("Validating Destinations...");
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
      monitor.worked(1);
      if (monitor.isCanceled()) {
         monitor.done();
         throw new InterruptedException();
      }

      // Check that global variables still exist
      monitor.subTask("Validating Global Variables...");

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
         updateLog(ScriptStepResult.createValidationVariableFail(globalVariable.getName()));
         return;
      }
      monitor.worked(1);
      if (monitor.isCanceled()) {
         monitor.done();
         throw new InterruptedException();
      }

      // Execute steps
      for (RuntimeStep runtimeStep : runtimeSteps) {
         switch (runtimeStep.getStep().getKind()) {
            case PAUSE:
               executePause(monitor, simulation, runtimeStep);
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
                  executeRegular(monitor, simulation, runtimeStep);
               } catch (JMSException e) {
                  updateLog(ScriptStepResult.createStepFail(runtimeStep.getJtbDestination().getName(), e));
                  return;
               }
               break;

            default:
               break;
         }
      }

      // TODO Should be in a finally...
      // Disconnect session that have been opened by this script
      monitor.subTask("Disconnecting Sessions... ");
      for (Entry<String, SimpleEntry<JTBSession, Boolean>> e : jtbSessionsUsed.entrySet()) {
         Map.Entry<JTBSession, Boolean> eJTBSession = e.getValue();
         if (!eJTBSession.getValue()) {
            String sesionName = eJTBSession.getKey().getName();

            log.debug("Disconnecting from {}", sesionName);
            updateLog(ScriptStepResult.createSessionDisconnectStart(sesionName));

            try {
               eJTBSession.getKey().connectOrDisconnect();
               updateLog(ScriptStepResult.createSessionDisconnectSuccess());
            } catch (Exception e1) {
               updateLog(ScriptStepResult.createSessionDisconnectFail(sesionName, e1));
            }
         }
      }
      monitor.worked(1);
      if (monitor.isCanceled()) {
         monitor.done();
         throw new InterruptedException();
      }

      updateLog(ScriptStepResult.createScriptSuccess());
   }

   // -------
   // Helpers
   // -------

   private void executeRegular(IProgressMonitor monitor, boolean simulation, RuntimeStep runtimeStep) throws JMSException,
                                                                                                      InterruptedException {
      log.debug("executeRegular. Simulation? {}", simulation);

      Step step = runtimeStep.getStep();
      JTBMessageTemplate xx = runtimeStep.getJtbMessageTemplate();
      JTBSession jtbSession = runtimeStep.getJtbSession();
      JTBDestination jtbDestination = runtimeStep.getJtbDestination();

      // Generate local values

      for (int i = 0; i < step.getIterations(); i++) {

         monitor.subTask(runtimeStep.toString());

         JTBMessageTemplate jtbMessageTemplate = JTBMessageTemplate.deepClone(xx);

         // Generate local variables for each iteration
         String oldPayloadText = jtbMessageTemplate.getPayloadText();
         jtbMessageTemplate.setPayloadText(VariablesUtils.replaceTemplateVariables(cm.getVariables(), oldPayloadText));

         // Create Message
         Message m = jtbSession.createJMSMessage(jtbMessageTemplate.getJtbMessageType());
         jtbMessageTemplate.toJMSMessage(m);

         updateLog(ScriptStepResult.createStepStart(jtbMessageTemplate));

         // Send Message
         if (!simulation) {
            JTBMessage jtbMessage = new JTBMessage(jtbDestination, m);
            jtbDestination.getJtbSession().sendMessage(jtbMessage);
         }

         updateLog(ScriptStepResult.createStepSuccess());

         // Eventually pause after...
         Integer pause = step.getPauseSecsAfter();
         if ((pause != null) && (pause > 0)) {
            updateLog(ScriptStepResult.createStepPauseStart(pause));

            if (!simulation) {
               try {
                  TimeUnit.SECONDS.sleep(step.getPauseSecsAfter());
               } catch (InterruptedException e) {
                  // NOP
               }
            }
            updateLog(ScriptStepResult.createStepPauseSuccess());
         }

         monitor.worked(1);
         if (monitor.isCanceled()) {
            monitor.done();
            throw new InterruptedException();
         }

      }
   }

   private void executePause(IProgressMonitor monitor, boolean simulation, RuntimeStep runtimeStep) throws InterruptedException {

      monitor.subTask(runtimeStep.toString());

      Step step = runtimeStep.getStep();
      Integer delay = step.getPauseSecsAfter();

      updateLog(ScriptStepResult.createPauseStart(delay));

      log.debug("running pause step.delay : {} seconds", delay);
      if (!simulation) {
         try {
            TimeUnit.SECONDS.sleep(delay);
         } catch (InterruptedException e) {
            // NOP
         }

         monitor.worked(1);
         if (monitor.isCanceled()) {
            monitor.done();
            throw new InterruptedException();
         }

      }
      updateLog(ScriptStepResult.createPauseSuccess());
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
      // toString
      // ------------------------

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder(256);

         if (step.getKind() == StepKind.REGULAR) {
            builder.append("[");
            builder.append(step.getTemplateName());
            builder.append("] -> ");
            builder.append(step.getSessionName());
            builder.append(" : ");
            builder.append(step.getDestinationName());
         } else {
            builder.append("Pause for");
            builder.append(step.getPauseSecsAfter());
            builder.append(" seconds");
         }

         return builder.toString();
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

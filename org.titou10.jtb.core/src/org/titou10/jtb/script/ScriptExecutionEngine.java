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
package org.titou10.jtb.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.script.ScriptStepResult.ExectionActionCode;
import org.titou10.jtb.script.gen.DataFile;
import org.titou10.jtb.script.gen.GlobalVariable;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.script.gen.StepKind;
import org.titou10.jtb.template.TemplatesUtils;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;
import org.titou10.jtb.variable.VariablesManager;
import org.titou10.jtb.variable.gen.Variable;

/**
 * Script Execution Engine
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class ScriptExecutionEngine {

   private static final Logger log                     = LoggerFactory.getLogger(ScriptExecutionEngine.class);

   private static final String VARIABLE_NAME_SEPARATOR = ",";
   private static final String MAX_MESSAGES_REACHED    = "MAX_MESSAGES_REACHED";
   private static final int    NB_TICKS_VALIDATION     = 7;
   private static final int    NB_TICKS_PER_STEP       = 100000;

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private ConfigManager       cm;

   @Inject
   private VariablesManager    variablesManager;

   @Inject
   private ScriptsManager      scriptsManager;

   private boolean             clearLogsBeforeExecution;
   private int                 nbMessagePost;
   private int                 nbMessageMax;
   private boolean             doShowPostLogs;

   public void executeScript(Script script, final boolean simulation, int nbMessageMax, boolean doShowPostLogs) {
      log.debug("executeScript '{}'. simulation? {}", script.getName(), simulation);

      this.clearLogsBeforeExecution = cm.getPreferenceStore().getBoolean(Constants.PREF_CLEAR_LOGS_EXECUTION);

      this.nbMessagePost = 0;
      this.nbMessageMax = nbMessageMax == 0 ? Integer.MAX_VALUE : nbMessageMax;
      this.doShowPostLogs = doShowPostLogs;

      ProgressMonitorDialog progressDialog = new ProgressMonitorDialogPrimaryModal(Display.getCurrent().getActiveShell());
      try {
         progressDialog.run(true, true, new IRunnableWithProgress() {

            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
               executeScriptInBackground(monitor, script, simulation);
               monitor.done();
            }
         });
      } catch (InterruptedException e) {
         String msg = e.getMessage();
         if ((msg != null) && (msg.equals(MAX_MESSAGES_REACHED))) {
            log.info("Max messages reached");
            updateLog(ScriptStepResult.createScriptMaxReached(nbMessagePost, simulation));
         } else {
            log.info("Process has been cancelled by user");
            updateLog(ScriptStepResult.createScriptCancelled(nbMessagePost, simulation));
         }
         return;
      } catch (InvocationTargetException e) {
         Throwable t = Utils.getCause(e);
         log.error("Exception occured ", t);
         updateLog(ScriptStepResult.createValidationExceptionFail(ExectionActionCode.SCRIPT, "An unexpected problem occured", t));
         return;
      }

      // BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
      // @Override
      // public void run() {
      // executeScriptInBackground(simulation);
      // }
      // });
   }

   public void executeScriptNoUI(String scriptName, final boolean simulation, int nbMessageMax) throws Exception {
      // FIXME: find the script corresponding to the name
      Script script = new Script();
      executeScriptInBackground(new NullProgressMonitor(), script, simulation);
   }

   private void executeScriptInBackground(IProgressMonitor monitor, Script script, boolean simulation) throws InterruptedException {
      log.debug("executeScriptInBackground '{}'. simulation? {}", script.getName(), simulation);

      // 100 ticks per step + 7 validation
      int nbTicksExecution = script.getStep().size() * NB_TICKS_PER_STEP;
      int nbTicksTotal = +nbTicksExecution + NB_TICKS_VALIDATION;
      SubMonitor subMonitor = SubMonitor.convert(monitor, nbTicksTotal);

      // Build and validate runtime steps

      SubMonitor subMonitorValidation = subMonitor.split(NB_TICKS_VALIDATION);
      subMonitorValidation.setWorkRemaining(NB_TICKS_VALIDATION);
      if (simulation) {
         subMonitorValidation.setTaskName("Validating Script (Simulation)");
      } else {
         subMonitorValidation.setTaskName("Validating Script...");
      }

      Map<String, String> globalVariablesValues = new HashMap<>(script.getGlobalVariable().size());
      List<RuntimeStep> runtimeSteps = validateAndBuildRuntimeSteps(subMonitorValidation,
                                                                    script,
                                                                    simulation,
                                                                    globalVariablesValues);
      if (runtimeSteps == null) {
         return;
      }

      // Clear logs is the option is set in preferences
      if (clearLogsBeforeExecution) {
         eventBroker.send(Constants.EVENT_CLEAR_EXECUTION_LOG, "noUse");
      }

      // Execute steps

      SubMonitor subMonitorExecution = subMonitor.split(nbTicksExecution);
      if (simulation) {
         subMonitorExecution.setTaskName("Executing Script (Simulation)");
      } else {
         subMonitorExecution.setTaskName("Executing Script...");
      }

      for (RuntimeStep runtimeStep : runtimeSteps) {

         subMonitorExecution.setWorkRemaining(nbTicksExecution);
         nbTicksExecution -= NB_TICKS_PER_STEP;
         subMonitorExecution.setTaskName(runtimeStep.toString());

         switch (runtimeStep.getStep().getKind()) {
            case PAUSE:
               executePause(subMonitorExecution, simulation, runtimeStep);
               break;

            case REGULAR:

               // Parse the template to replace variables names by global variables values
               JTBMessageTemplate t = runtimeStep.getJtbMessageTemplate();
               String payload = t.getPayloadText();
               if (payload != null) {
                  for (Entry<String, String> v : globalVariablesValues.entrySet()) {
                     payload = payload.replaceAll(variablesManager.buildVariableReplaceName(v.getKey()), v.getValue());
                  }
                  t.setPayloadText(payload);
               }

               try {
                  executeRegular(subMonitorExecution, simulation, runtimeStep);
               } catch (JMSException | IOException e) {
                  log.error("Exception occurred during step execution ", e);
                  updateLog(ScriptStepResult.createStepFail(runtimeStep.getJtbDestination().getName(), e));
                  return;
               }
               break;

            default:
               break;
         }
      }

      updateLog(ScriptStepResult.createScriptSuccess(nbMessagePost, simulation));
   }

   // -------
   // Helpers
   // -------

   private void executeRegular(SubMonitor subMonitor,
                               boolean simulation,
                               RuntimeStep runtimeStep) throws JMSException, InterruptedException, IOException {
      log.debug("executeRegular. Simulation? {}", simulation);

      Map<String, String> dataFileVariables = new HashMap<>();

      JTBMessageTemplate t = runtimeStep.getJtbMessageTemplate();

      DataFile dataFile = runtimeStep.getDataFile();
      List<File> payloadFiles = runtimeStep.getPayloadFiles();
      String templateName = runtimeStep.getTemplateName();

      if (dataFile == null) {
         if (payloadFiles == null) {
            executeRegular2(NB_TICKS_PER_STEP, subMonitor, simulation, runtimeStep, t, templateName, dataFileVariables);
            return;
         }

         // Payload Directory present. Iterate on files, replace the payload by the content of the file
         int nbTicks = NB_TICKS_PER_STEP / payloadFiles.size();
         log.debug("nbFiles: {} nbTicksPerFile: {}", payloadFiles.size(), nbTicks);
         for (File file : payloadFiles) {
            switch (t.getJtbMessageType()) {
               case TEXT:
                  t.setPayloadText(new String(Files.readAllBytes(file.toPath())));
                  break;

               case BYTES:
                  t.setPayloadBytes(Files.readAllBytes(file.toPath()));
                  break;

               default:
                  break;
            }
            executeRegular2(nbTicks, subMonitor, simulation, runtimeStep, t, templateName, dataFileVariables);
         }
         return;
      }

      // DataFile is present, load the lines..
      String[] varNames = runtimeStep.getVarNames();

      Charset charset;
      // DF may be null because the charset property is new in v4.0
      if ((dataFile.getCharset() == null) || (dataFile.getCharset().startsWith(Constants.CHARSET_DEFAULT_PREFIX))) {
         charset = Charset.defaultCharset();
      } else {
         // TODO DF: may fail is charset does not exist
         charset = Charset.forName(dataFile.getCharset());
      }

      // Count nbOfLines for progressMonitor
      int nbLines = 0;
      try (BufferedReader reader = new BufferedReader(new FileReader(dataFile.getFileName()));) {
         while (reader.readLine() != null)
            nbLines++;
      }
      int nbTicks = nbLines > NB_TICKS_PER_STEP ? 1 : NB_TICKS_PER_STEP / nbLines;
      log.debug("nbLines: {} nbTicksPerLine: {}", nbLines, nbTicks);

      try (BufferedReader reader = Files.newBufferedReader(Paths.get(dataFile.getFileName()), charset);) {
         String line = null;
         while ((line = reader.readLine()) != null) {
            dataFileVariables.clear();

            // Parse and setup line Variables
            String[] values = line.split(Pattern.quote(dataFile.getDelimiter()));
            String value;
            for (int i = 0; i < varNames.length; i++) {
               String varName = varNames[i];
               if (i < values.length) {
                  value = values[i];
               } else {
                  value = "";
               }
               dataFileVariables.put(varName, value);
            }

            // Execute Step
            executeRegular2(nbTicks, subMonitor, simulation, runtimeStep, t, templateName, dataFileVariables);
         }
      }
   }

   private void executeRegular2(int nbTicks,
                                SubMonitor subMonitor,
                                boolean simulation,
                                RuntimeStep runtimeStep,
                                JTBMessageTemplate t,
                                String templateName,
                                Map<String, String> dataFileVariables) throws JMSException, InterruptedException {

      Step step = runtimeStep.getStep();
      JTBConnection jtbConnection = runtimeStep.getJtbConnection();
      JTBDestination jtbDestination = runtimeStep.getJtbDestination();

      int nbTickWorkePerIteration = step.getIterations() > nbTicks ? 1 : nbTicks / step.getIterations();

      for (int i = 0; i < step.getIterations(); i++) {

         JTBMessageTemplate jtbMessageTemplate = JTBMessageTemplate.deepClone(t);

         // If we use a data file, replace the dataFileVariables
         if (!(dataFileVariables.isEmpty())) {
            jtbMessageTemplate.setPayloadText(variablesManager.replaceDataFileVariables(dataFileVariables,
                                                                                        jtbMessageTemplate.getPayloadText()));
         }

         // Generate local variables for each iteration
         jtbMessageTemplate.setPayloadText(variablesManager.replaceTemplateVariables(jtbMessageTemplate.getPayloadText()));

         if (doShowPostLogs) {
            updateLog(ScriptStepResult.createStepStart(jtbMessageTemplate, templateName));
         }

         // Send Message
         if (!simulation) {
            Message m = jtbConnection.createJMSMessage(jtbMessageTemplate.getJtbMessageType());
            JTBMessage jtbMessage = jtbMessageTemplate.toJTBMessage(jtbDestination, m);
            jtbDestination.getJtbConnection().sendMessage(jtbMessage);
         }

         if (doShowPostLogs) {
            updateLog(ScriptStepResult.createStepSuccess());
         }
         // Increment nb messages posted
         nbMessagePost++;
         if (nbMessagePost >= nbMessageMax) {
            throw new InterruptedException(MAX_MESSAGES_REACHED);
         }

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

         subMonitor.worked(nbTickWorkePerIteration);
         if (subMonitor.isCanceled()) {
            subMonitor.done();
            throw new InterruptedException();
         }

      }
   }

   private void executePause(SubMonitor subMonitor, boolean simulation, RuntimeStep runtimeStep) throws InterruptedException {

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

         subMonitor.worked(1);
         if (subMonitor.isCanceled()) {
            subMonitor.done();
            throw new InterruptedException();
         }

      }
      subMonitor.worked(NB_TICKS_PER_STEP);
      updateLog(ScriptStepResult.createPauseSuccess());
   }

   private void updateLog(ScriptStepResult ssr) {
      if (ssr.getData() != null) {
         log.debug(ssr.getData().toString());
      }
      eventBroker.send(Constants.EVENT_REFRESH_EXECUTION_LOG, ssr);
      // eventBroker.post(Constants.EVENT_REFRESH_EXECUTION_LOG, ssr);
   }

   private List<RuntimeStep> validateAndBuildRuntimeSteps(SubMonitor subMonitor,
                                                          Script script,
                                                          boolean simulation,
                                                          Map<String, String> globalVariablesValues) throws InterruptedException {
      log.debug("validateAndBuildRuntimeSteps '{}'. simulation? {}", script.getName(), simulation);

      Random r = new Random(System.nanoTime());

      List<Step> steps = script.getStep();
      List<GlobalVariable> globalVariables = script.getGlobalVariable();

      updateLog(ScriptStepResult.createScriptStart(simulation));

      // Create runtime objects from steps
      List<RuntimeStep> runtimeSteps = new ArrayList<>(steps.size());
      for (Step step : steps) {
         runtimeSteps.add(new RuntimeStep(step));
      }

      // Gather templates used in the script and validate their existence
      try {
         subMonitor.subTask("Validating Templates...");
         List<IFile> allTemplates = TemplatesUtils.getAllTemplatesIFiles(cm.getTemplateFolder());
         for (RuntimeStep runtimeStep : runtimeSteps) {
            Step step = runtimeStep.getStep();

            if (step.getKind() != StepKind.REGULAR) {
               continue;
            }

            // Validate and read Template
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
               return null;
            }
            runtimeStep.setJtbMessageTemplate(t, templateName);
         }

         subMonitor.worked(1);
         if (subMonitor.isCanceled()) {
            subMonitor.done();
            throw new InterruptedException();
         }

      } catch (Exception e) {
         updateLog(ScriptStepResult.createValidationExceptionFail(ExectionActionCode.TEMPLATE,
                                                                  "A problem occured while validating templates",
                                                                  e));
         return null;
      }

      // Gather sessions used in the script and validate their existence
      subMonitor.subTask("Validating Sessions...");
      Map<String, JTBSession> jtbSessionsUsed = new HashMap<>(steps.size());
      for (RuntimeStep runtimeStep : runtimeSteps) {
         Step step = runtimeStep.getStep();

         if (step.getKind() != StepKind.REGULAR) {
            continue;
         }

         String sessionName = step.getSessionName();
         JTBSession jtbSession = jtbSessionsUsed.get(sessionName);
         if (jtbSession == null) {
            jtbSession = cm.getJTBSessionByName(sessionName);
            if (jtbSession == null) {
               updateLog(ScriptStepResult.createValidationSessionFail(sessionName));
               return null;
            }
            jtbSessionsUsed.put(sessionName, jtbSession);
            log.debug("Session with name '{}' added to the list of sessions used in the script", sessionName);
         }
         runtimeStep.setJtbConnection(jtbSession.getJTBConnection(JTBSessionClientType.SCRIPT_EXEC));
      }
      subMonitor.worked(1);

      if (subMonitor.isCanceled()) {
         subMonitor.done();
         throw new InterruptedException();
      }

      // Check that global variables still exist
      subMonitor.subTask("Validating Global Variables...");
      List<Variable> cmVariables = variablesManager.getVariables();
      loop: for (GlobalVariable globalVariable : globalVariables) {
         for (Variable v : cmVariables) {
            if (v.getName().equals(globalVariable.getName())) {

               // Generate a value for the variable if no defaut is provides
               String val = globalVariable.getConstantValue();
               if (val == null) {
                  globalVariablesValues.put(v.getName(), variablesManager.resolveVariable(r, v));
               } else {
                  globalVariablesValues.put(v.getName(), val);
               }

               continue loop;
            }
         }

         // The current variable does not exist
         log.warn("Global Variable '{}' does not exist", globalVariable.getName());
         updateLog(ScriptStepResult.createValidationVariableFail(globalVariable.getName()));
         return null;
      }
      subMonitor.worked(1);
      if (subMonitor.isCanceled()) {
         subMonitor.done();
         throw new InterruptedException();
      }

      // Check that data file exist and parse variable names

      subMonitor.subTask("Validating Data Files...");
      for (RuntimeStep runtimeStep : runtimeSteps) {
         Step step = runtimeStep.getStep();
         if (step.getKind() != StepKind.REGULAR) {
            continue;
         }

         String variablePrefix = step.getVariablePrefix();
         if (variablePrefix == null) {
            continue;
         }

         DataFile dataFile = scriptsManager.findDataFileByVariablePrefix(script, variablePrefix);
         if (dataFile == null) {
            log.warn("Data File with variablePrefix '{}' does not exist", variablePrefix);
            updateLog(ScriptStepResult.createValidationDataFileFail2(variablePrefix));
            return null;
         }
         String fileName = dataFile.getFileName();

         File f = new File(fileName);
         if (!(f.exists())) {
            // The Data File does not exist
            log.warn("Data File with variablePrefix {} is associated with file Name '{}' does not exist", variablePrefix, fileName);
            updateLog(ScriptStepResult.createValidationDataFileFail(fileName));
            return null;
         }
         runtimeStep.setDataFile(dataFile);

         String[] varNames = dataFile.getVariableNames().split(VARIABLE_NAME_SEPARATOR);
         log.debug("Variable names {} found in Data File '{}'", varNames, fileName);
         for (int i = 0; i < varNames.length; i++) {
            String varName = varNames[i];
            varNames[i] = dataFile.getVariablePrefix() + "." + varName;
         }
         runtimeStep.setVarNames(varNames);

      }

      subMonitor.worked(1);
      if (subMonitor.isCanceled()) {
         subMonitor.done();
         throw new InterruptedException();
      }

      // Check that the payload directory still exist and gather files in the directory

      subMonitor.subTask("Validating Payload Directory...");
      for (RuntimeStep runtimeStep : runtimeSteps) {
         Step step = runtimeStep.getStep();
         if (step.getKind() != StepKind.REGULAR) {
            continue;
         }

         String payloadDirectory = step.getPayloadDirectory();
         if (payloadDirectory == null) {
            continue;
         }

         File f = new File(payloadDirectory);
         if (!(f.exists())) {
            // The Payload Directory does not exist
            log.warn("Payload Directory {} does not exist", payloadDirectory);
            updateLog(ScriptStepResult.createValidationPayloadDirectoryFail(payloadDirectory));
            return null;
         }

         File[] files = f.listFiles(file -> file.isFile());
         if (files.length == 0) {
            log.warn("Payload Directory {} does not contain any file", payloadDirectory);
            updateLog(ScriptStepResult.createValidationPayloadDirectoryFail2(payloadDirectory));
            return null;
         }

         runtimeStep.setPayloadFiles(Arrays.asList(files));
      }

      subMonitor.worked(1);
      if (subMonitor.isCanceled()) {
         subMonitor.done();
         throw new InterruptedException();
      }

      // Connect to sessions if they are not connected

      subMonitor.subTask("Opening Sessions...");
      for (Entry<String, JTBSession> e : jtbSessionsUsed.entrySet()) {
         String sessionName = e.getKey();
         JTBSession jtbSession = e.getValue();
         JTBConnection jtbConnection = jtbSession.getJTBConnection(JTBSessionClientType.SCRIPT_EXEC);

         updateLog(ScriptStepResult.createSessionConnectStart(sessionName));
         if (jtbConnection.isConnected()) {
            updateLog(ScriptStepResult.createSessionConnectSuccess());
         } else {
            log.debug("Connecting to {}", sessionName);
            try {
               jtbConnection.connectOrDisconnect();
               updateLog(ScriptStepResult.createSessionConnectSuccess());

               // Refresh Session Browser
               eventBroker.send(Constants.EVENT_REFRESH_SESSION_BROWSER, false);

            } catch (Exception e1) {
               updateLog(ScriptStepResult.createSessionConnectFail(sessionName, e1));
               return null;
            }
         }
      }
      subMonitor.worked(1);
      if (subMonitor.isCanceled()) {
         subMonitor.done();
         throw new InterruptedException();
      }

      // Resolve Destination Name

      subMonitor.subTask("Validating Destinations...");
      for (RuntimeStep runtimeStep : runtimeSteps) {
         Step step = runtimeStep.getStep();
         if (step.getKind() == StepKind.REGULAR) {
            JTBConnection jtbConnection = runtimeStep.getJtbConnection();
            JTBDestination jtbDestination = jtbConnection.getJTBDestinationByName(step.getDestinationName());
            if (jtbDestination == null) {
               updateLog(ScriptStepResult.createValidationDestinationFail(step.getDestinationName()));
               return null;
            }
            runtimeStep.setJtbDestination(jtbDestination);
         }
      }
      subMonitor.worked(1);
      if (subMonitor.isCanceled()) {
         subMonitor.done();
         throw new InterruptedException();
      }

      return runtimeSteps;
   }

   // --------------
   // Helper Classes
   // --------------

   /**
    * A PRIMARY_MODAL ProgressMonitorDialog
    * 
    * @author Denis Forveille
    *
    */
   private class ProgressMonitorDialogPrimaryModal extends ProgressMonitorDialog {
      public ProgressMonitorDialogPrimaryModal(Shell parent) {
         super(parent);
         setShellStyle(SWT.TITLE | SWT.PRIMARY_MODAL);
      }
   }
}

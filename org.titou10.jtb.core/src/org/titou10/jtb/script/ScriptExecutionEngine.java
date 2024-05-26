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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javax.jms.JMSException;
import javax.jms.Message;

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
import org.titou10.jtb.config.JTBPreferenceStore;
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
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.template.TemplatesManager.TemplateNameStructure;
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
   private JTBPreferenceStore  ps;

   @Inject
   private TemplatesManager    templatesManager;

   @Inject
   private VariablesManager    variablesManager;

   @Inject
   private ScriptsManager      scriptsManager;

   public void executeScript(Script script, final boolean simulation, boolean doShowPostLogs, int nbMessagesMax) {
      log.debug("executeScript '{}'. simulation? {}", script.getName(), simulation);

      boolean clearLogsBeforeExecution = ps.getBoolean(Constants.PREF_CLEAR_LOGS_EXECUTION);
      int msgMax = nbMessagesMax == 0 ? Integer.MAX_VALUE : nbMessagesMax;

      MyIRunnableWithProgress mirp = new MyIRunnableWithProgress(clearLogsBeforeExecution,
                                                                 simulation,
                                                                 doShowPostLogs,
                                                                 msgMax,
                                                                 script);

      ProgressMonitorDialog progressDialog = new ProgressMonitorDialogPrimaryModal(Display.getCurrent().getActiveShell());

      try {

         updateLog(doShowPostLogs, ScriptStepResult.createScriptStart(simulation));
         progressDialog.run(true, true, mirp);
         updateLog(doShowPostLogs, ScriptStepResult.createScriptSuccess(mirp.getNbMessagePost(), simulation));

      } catch (InterruptedException e) {
         String msg = e.getMessage();
         if ((msg != null) && (msg.equals(MAX_MESSAGES_REACHED))) {
            log.info("Max messages reached");
            updateLog(doShowPostLogs, ScriptStepResult.createScriptMaxReached(mirp.getNbMessagePost(), simulation));
         } else {
            log.info("Process has been cancelled by user");
            updateLog(doShowPostLogs, ScriptStepResult.createScriptCancelled(mirp.getNbMessagePost(), simulation));
         }
      } catch (InvocationTargetException e) {
         Throwable t = Utils.getCause(e);
         log.error("Exception occured ", t);
         if (!(t instanceof ScriptValidationException)) {
            updateLog(doShowPostLogs,
                      ScriptStepResult
                               .createValidationExceptionFail(ExectionActionCode.SCRIPT, "An unexpected problem occured", t));
         }
      }
   }

   public int executeScriptNoUI(String scriptName, final boolean simulation, int nbMessagesMax) throws Exception {
      log.info("executeScriptNoUI scriptName '{}' simulation? {} nbMessagesMax {}", scriptName, simulation, nbMessagesMax);

      int msgMax = nbMessagesMax == 0 ? Integer.MAX_VALUE : nbMessagesMax;

      // Get the script with that name
      String scriptNameForSearch = scriptName.startsWith("/") ? scriptName : "/" + scriptName;
      Script script = scriptsManager.getMapScripts().get(scriptNameForSearch);
      if (script == null) {
         throw new ScriptValidationException("No script with name '" + scriptName + "' found");
      }

      // Execute Script
      AtomicInteger nbMessagePost = new AtomicInteger(0);
      executeScriptInBackground(new NullProgressMonitor(), simulation, false, msgMax, nbMessagePost, script);
      return nbMessagePost.get();
   }

   // -------
   // Helpers
   // -------

   private void executeScriptInBackground(IProgressMonitor monitor,
                                          boolean simulation,
                                          boolean doShowPostLogs,
                                          int nbMessagesMax,
                                          AtomicInteger nbMessagePost,
                                          Script script) throws InterruptedException, InvocationTargetException,
                                                         ScriptValidationException {
      log.debug("executeScriptInBackground '{}'. simulation? {}", script.getName(), simulation);

      // NB_TICKS_PER_STEP ticks per step + NB_TICKS_VALIDATION for validation
      int nbTicksExecution = script.getStep().size() * NB_TICKS_PER_STEP;
      int nbTicksTotal = nbTicksExecution + NB_TICKS_VALIDATION;
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
                                                                    doShowPostLogs,
                                                                    globalVariablesValues);

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
         subMonitorExecution.subTask(runtimeStep.toString());

         Step step = runtimeStep.getStep();

         switch (step.getKind()) {
            case PAUSE:

               updateLog(doShowPostLogs, ScriptStepResult.createPauseStart(step.getPauseSecsAfter()));
               executePause(subMonitorExecution, simulation, runtimeStep);
               updateLog(doShowPostLogs, ScriptStepResult.createPauseSuccess());
               break;

            case REGULAR:

               updateLog(doShowPostLogs,
                         ScriptStepResult.createStepStart(runtimeStep.getTemplateName(),
                                                          runtimeStep.getJtbDestination().getName()));

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

                  executeRegular(subMonitorExecution, simulation, doShowPostLogs, nbMessagesMax, nbMessagePost, runtimeStep);

                  updateLog(doShowPostLogs, ScriptStepResult.createStepSuccess());

               } catch (JMSException | IOException e) {
                  log.error("Exception occurred during step execution ", e);
                  updateLog(doShowPostLogs, ScriptStepResult.createStepFail(runtimeStep.getJtbDestination().getName(), e));
                  throw new InvocationTargetException(e);
               }
               break;

            default:
               break;
         }
      }
   }

   private void executeRegular(SubMonitor subMonitor,
                               boolean simulation,
                               boolean doShowPostLogs,
                               int nbMessagesMax,
                               AtomicInteger nbMessagePost,
                               RuntimeStep runtimeStep) throws JMSException, InterruptedException, IOException {
      log.debug("executeRegular. Simulation? {}", simulation);

      Map<String, String> dataFileVariables = new HashMap<>();

      JTBMessageTemplate jtbMessageTemplate = runtimeStep.getJtbMessageTemplate();

      DataFile dataFile = runtimeStep.getDataFile();
      List<File> payloadFiles = runtimeStep.getPayloadFiles();
      String templateName = runtimeStep.getTemplateName();

      if (dataFile == null) {
         if (payloadFiles == null) {
            executeRegular2(subMonitor,
                            NB_TICKS_PER_STEP,
                            simulation,
                            doShowPostLogs,
                            nbMessagesMax,
                            nbMessagePost,
                            runtimeStep,
                            jtbMessageTemplate,
                            templateName,
                            dataFileVariables);
            return;
         }

         // Payload Directory present. Iterate on files, replace the payload by the content of the file
         int nbTicks = NB_TICKS_PER_STEP / payloadFiles.size();
         log.debug("nbFiles: {} nbTicksPerFile: {}", payloadFiles.size(), nbTicks);
         for (File file : payloadFiles) {
            switch (jtbMessageTemplate.getJtbMessageType()) {
               case TEXT:
                  jtbMessageTemplate.setPayloadText(Utils.readFileText(file.getAbsolutePath()));
                  break;

               case BYTES:
                  jtbMessageTemplate.setPayloadBytes(Utils.readFileBytes(file.getAbsolutePath()));
                  break;

               default:
                  break;
            }
            executeRegular2(subMonitor,
                            nbTicks,
                            simulation,
                            doShowPostLogs,
                            nbMessagesMax,
                            nbMessagePost,
                            runtimeStep,
                            jtbMessageTemplate,
                            templateName,
                            dataFileVariables);
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
            executeRegular2(subMonitor,
                            nbTicks,
                            simulation,
                            doShowPostLogs,
                            nbMessagesMax,
                            nbMessagePost,
                            runtimeStep,
                            jtbMessageTemplate,
                            templateName,
                            dataFileVariables);
         }
      }
   }

   private void executeRegular2(SubMonitor subMonitor,
                                int nbTicks,
                                boolean simulation,
                                boolean doShowPostLogs,
                                int nbMessagesMax,
                                AtomicInteger nbMessagePost,
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

         updateLog(doShowPostLogs, ScriptStepResult.createPostStart(jtbMessageTemplate, templateName));

         // Send Message
         if (!simulation) {
            Message m = jtbConnection.createJMSMessage(jtbMessageTemplate.getJtbMessageType());
            JTBMessage jtbMessage = jtbMessageTemplate.toJTBMessage(jtbDestination, m);
            jtbDestination.getJtbConnection().sendMessage(jtbMessage);
         }

         updateLog(doShowPostLogs, ScriptStepResult.createPostSuccess());

         // Increment nb messages posted
         nbMessagePost.set(nbMessagePost.get() + 1);
         if (nbMessagePost.get() >= nbMessagesMax) {
            throw new InterruptedException(MAX_MESSAGES_REACHED);
         }

         // Eventually pause after...
         Integer pause = step.getPauseSecsAfter();
         if ((pause != null) && (pause > 0)) {
            updateLog(doShowPostLogs, ScriptStepResult.createStepPauseStart(pause));

            if (!simulation) {
               try {
                  TimeUnit.SECONDS.sleep(step.getPauseSecsAfter());
               } catch (InterruptedException e) {
                  // NOP
               }
            }
            updateLog(doShowPostLogs, ScriptStepResult.createStepPauseSuccess());
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
   }

   private void updateLog(boolean doShowPostLogs, ScriptStepResult ssr) {
      if (ssr.getData() != null) {
         log.debug(ssr.getData().toString());
      }
      if (ssr.isAlwaysShow() || doShowPostLogs) {
         // eventBroker.post(Constants.EVENT_REFRESH_EXECUTION_LOG, ssr);
         eventBroker.send(Constants.EVENT_REFRESH_EXECUTION_LOG, ssr);
      }
   }

   private List<RuntimeStep> validateAndBuildRuntimeSteps(SubMonitor subMonitor,
                                                          Script script,
                                                          boolean simulation,
                                                          boolean doShowPostLogs,
                                                          Map<String, String> globalVariablesValues) throws InterruptedException,
                                                                                                     ScriptValidationException {
      log.debug("validateAndBuildRuntimeSteps '{}'. simulation? {}", script.getName(), simulation);

      Random r = new Random(System.nanoTime());

      List<Step> steps = script.getStep();
      List<GlobalVariable> globalVariables = script.getGlobalVariable();

      // Create runtime objects from steps
      List<RuntimeStep> runtimeSteps = new ArrayList<>(steps.size());
      for (Step step : steps) {
         runtimeSteps.add(new RuntimeStep(step));
      }

      // Gather templates used in the script and validate their existence
      try {
         subMonitor.subTask("Validating Templates...");
         for (RuntimeStep runtimeStep : runtimeSteps) {
            Step step = runtimeStep.getStep();

            if (step.getKind() != StepKind.REGULAR) {
               continue;
            }

            // Validate and read Template
            TemplateNameStructure tns = templatesManager.buildTemplateNameStructure(step.getTemplateDirectory(),
                                                                                    step.getTemplateName());
            if (templatesManager.isUnknownTemplateDirectory(tns)) {
               ScriptStepResult ssr = ScriptStepResult.createValidationDirectoryFail();
               updateLog(doShowPostLogs, ssr);
               throw new ScriptValidationException(ssr);
            }

            String templateName = tns.getTemplateFullFileName();
            JTBMessageTemplate t = templatesManager.readTemplate(templateName);
            if (t == null) {
               ScriptStepResult ssr = ScriptStepResult.createValidationTemplateFail(tns.getSyntheticName());
               updateLog(doShowPostLogs, ssr);
               throw new ScriptValidationException(ssr);
            }
            runtimeStep.setJtbMessageTemplate(t, tns.getSyntheticName());
         }

         subMonitor.worked(1);
         if (subMonitor.isCanceled()) {
            subMonitor.done();
            throw new InterruptedException();
         }

      } catch (

      ScriptValidationException ite) {
         throw ite;
      } catch (Exception e) {
         ScriptStepResult ssr = ScriptStepResult
                  .createValidationExceptionFail(ExectionActionCode.TEMPLATE, "A problem occured while validating templates", e);
         updateLog(doShowPostLogs, ssr);

         throw new ScriptValidationException(ssr);
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
               ScriptStepResult ssr = ScriptStepResult.createValidationSessionFail(sessionName);
               updateLog(doShowPostLogs, ssr);
               throw new ScriptValidationException(ssr);
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

         ScriptStepResult ssr = ScriptStepResult.createValidationVariableFail(globalVariable.getName());
         updateLog(doShowPostLogs, ssr);
         throw new ScriptValidationException(ssr);
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
            ScriptStepResult ssr = ScriptStepResult.createValidationDataFileFail2(variablePrefix);
            updateLog(doShowPostLogs, ssr);
            throw new ScriptValidationException(ssr);
         }
         String fileName = dataFile.getFileName();

         File f = new File(fileName);
         if (!(f.exists())) {
            // The Data File does not exist
            log.warn("Data File with variablePrefix {} is associated with file Name '{}' does not exist", variablePrefix, fileName);
            ScriptStepResult ssr = ScriptStepResult.createValidationDataFileFail(fileName);
            updateLog(doShowPostLogs, ssr);
            throw new ScriptValidationException(ssr);
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
            ScriptStepResult ssr = ScriptStepResult.createValidationPayloadDirectoryFail(payloadDirectory);
            updateLog(doShowPostLogs, ssr);
            throw new ScriptValidationException(ssr);
         }

         File[] files = f.listFiles(file -> file.isFile());
         if (files.length == 0) {
            log.warn("Payload Directory {} does not contain any file", payloadDirectory);
            ScriptStepResult ssr = ScriptStepResult.createValidationPayloadDirectoryFail2(payloadDirectory);
            updateLog(doShowPostLogs, ssr);
            throw new ScriptValidationException(ssr);
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

         updateLog(doShowPostLogs, ScriptStepResult.createSessionConnectStart(sessionName));
         if (jtbConnection.isConnected()) {
            updateLog(doShowPostLogs, ScriptStepResult.createSessionConnectSuccess());
         } else {
            log.debug("Connecting to {}", sessionName);
            try {
               jtbConnection.connect();
               updateLog(doShowPostLogs, ScriptStepResult.createSessionConnectSuccess());

               // Refresh Session Browser
               eventBroker.send(Constants.EVENT_REFRESH_SESSION_BROWSER, false);

            } catch (Exception e1) {
               ScriptStepResult ssr = ScriptStepResult.createSessionConnectFail(sessionName, e1);
               updateLog(doShowPostLogs, ssr);
               throw new ScriptValidationException(ssr);
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
               ScriptStepResult ssr = ScriptStepResult.createValidationDestinationFail(step.getDestinationName());
               updateLog(doShowPostLogs, ssr);
               throw new ScriptValidationException(ssr);
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

   private class MyIRunnableWithProgress implements IRunnableWithProgress {

      private AtomicInteger nbMessagePost = new AtomicInteger(0);

      final boolean         clearLogsBeforeExecution;
      final boolean         simulation;
      final int             nbMessagesMax;
      final boolean         doShowPostLogs;
      final private Script  script;

      public MyIRunnableWithProgress(boolean clearLogsBeforeExecution,
                                     boolean simulation,
                                     boolean doShowPostLogs,
                                     int nbMessagesMax,
                                     Script script) {
         this.clearLogsBeforeExecution = clearLogsBeforeExecution;
         this.simulation = simulation;
         this.doShowPostLogs = doShowPostLogs;
         this.nbMessagesMax = nbMessagesMax;
         this.script = script;
      }

      @Override
      public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

         // Clear logs is the option is set in preferences
         if (clearLogsBeforeExecution) {
            eventBroker.send(Constants.EVENT_CLEAR_EXECUTION_LOG, "noUse");
         }

         try {
            executeScriptInBackground(monitor, simulation, doShowPostLogs, nbMessagesMax, nbMessagePost, script);
         } catch (ScriptValidationException e) {
            throw new InvocationTargetException(e);
         }
         monitor.done();
      }

      public int getNbMessagePost() {
         return nbMessagePost.get();
      }
   }
}

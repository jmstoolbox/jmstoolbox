/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
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

import java.util.Calendar;

import org.titou10.jtb.jms.model.JTBMessageTemplate;

/**
 * Hold the result of an execution step
 * 
 * @author Denis Forveille
 *
 */
public final class ScriptStepResult {

   private static final String SCRIPT_RUNNING                  = "Started.";
   private static final String SCRIPT_TERMINATED               = "Terminated. %d messages posted.";
   private static final String SCRIPT_CANCELLED                = "Cancelled by user. %d messages posted.";
   private static final String SCRIPT_MAX_REACHED              = "Terminated (Maximum messages reached). %d messages posted.";
   private static final String SIMULATION_RUNNING              = "Simulation started.";
   private static final String SIMULATION_TERMINATED           = "Simulation terminated. %d posts simulated.";
   private static final String SIMULATION_CANCELLED            = "Simulation cancelled by user. %d posts simulated.";
   private static final String SIMULATION_MAX_REACHED          = "Simulation terminated (Maximum messages reached). %d posts simulated.";

   private static final String STEP_START                      = "Template '%s' to destination '%s'";
   private static final String STEP_TERMINATED                 = "Step Successful";
   private static final String STEP_FAILED                     = "Step to destination %s failed : %s";
   private static final String STEP_PAUSE_RUNNING              = "Pause for %d seconds after post ...";
   private static final String STEP_PAUSE_SUCCESS              = "Pause terminated.";

   private static final String PAUSE_RUNNING                   = "Pause for %d seconds ...";
   private static final String PAUSE_SUCCESS                   = "Pause terminated.";

   private static final String POST_TERMINATED                 = "Post Successful";

   private static final String SESSION_CONNECT                 = "Connecting to session '%s' ...";
   private static final String SESSION_CONNECT_SUCCESS         = "Connected.";
   private static final String SESSION_CONNECT_FAIL            = "Connection to session '%s' failed: %s";
   private static final String SESSION_DISCONNECT              = "Disconnecting from session '%s' ...";
   private static final String SESSION_DISCONNECT_SUCCESS      = "Disconnected.";
   private static final String SESSION_DISCONNECT_FAIL         = "Disconnection from session '%s' failed: %s";

   private static final String VALIDATION_TEMPLATE_FOLDER_FAIL = "Template Folder with name '%s' is unknown";
   private static final String VALIDATION_TEMPLATE_FAIL        = "Template with name '%s' is unknown";
   private static final String VALIDATION_DIRECTORY_FAIL       = "Directory is unknown to JMSToolBox";
   private static final String VALIDATION_SESSION_FAIL         = "Session with name '%s' is unknown";
   private static final String VALIDATION_DESTINATION_FAIL     = "Destination with name '%s' is unknown";
   private static final String VALIDATION_DATAFILE_FAIL        = "File with name '%s' does not exist";
   private static final String VALIDATION_DATAFILE2_FAIL       = "Data File with variable prefix '%s' is unknown";
   private static final String VALIDATION_PAYLOAD_DIR_FAIL     = "Payload directory '%s' does not exist";
   private static final String VALIDATION_PAYLOAD_DIR2_FAIL    = "Payload directory '%s' does not contain any file";
   private static final String VALIDATION_VARIABLE_FAIL        = "Global Variable '%s' does not exist";

   private static final String EXCEPTION_FAIL                  = "%s : %s";

   public enum ExectionActionCode {
                                   SCRIPT,
                                   STEP,
                                   POST,
                                   PAUSE,
                                   TEMPLATE,
                                   VARIABLE,
                                   DATAFILE,
                                   PAYLOAD_DIR,
                                   SESSION,
                                   DESTINATION,
                                   EXCEPTION;
   }

   public enum ExectionReturnCode {
                                   START,
                                   SUCCESS,
                                   FAILED,
                                   CANCELLED;
   }

   private Calendar           ts;
   private ExectionActionCode action;
   private ExectionReturnCode returnCode;
   private Object             data;
   private String             templateName;
   private boolean            alwaysShow;

   // ------------------------
   // Constructor
   // ------------------------
   public ScriptStepResult(ExectionActionCode action, ExectionReturnCode returnCode, Object data, boolean alwaysShow) {
      this(action, returnCode, data, null, alwaysShow);
   }

   public ScriptStepResult(ExectionActionCode action,
                           ExectionReturnCode returnCode,
                           Object data,
                           String templateName,
                           boolean alwaysShow) {
      this.action = action;
      this.returnCode = returnCode;
      this.data = data;
      this.ts = Calendar.getInstance();
      this.templateName = templateName;
      this.alwaysShow = alwaysShow;
   }

   // ------------------------
   // Factories
   // ------------------------

   // Script

   public static ScriptStepResult createScriptStart(boolean simulation) {
      if (simulation) {
         return new ScriptStepResult(ExectionActionCode.SCRIPT, ExectionReturnCode.START, SIMULATION_RUNNING, true);
      } else {
         return new ScriptStepResult(ExectionActionCode.SCRIPT, ExectionReturnCode.START, SCRIPT_RUNNING, true);
      }
   }

   public static ScriptStepResult createScriptSuccess(int nbMessagePost, boolean simulation) {
      if (simulation) {
         return new ScriptStepResult(ExectionActionCode.SCRIPT,
                                     ExectionReturnCode.SUCCESS,
                                     String.format(SIMULATION_TERMINATED, nbMessagePost),
                                     true);
      } else {
         return new ScriptStepResult(ExectionActionCode.SCRIPT,
                                     ExectionReturnCode.SUCCESS,
                                     String.format(SCRIPT_TERMINATED, nbMessagePost),
                                     true);
      }
   }

   public static ScriptStepResult createScriptCancelled(int nbMessagePost, boolean simulation) {
      if (simulation) {
         return new ScriptStepResult(ExectionActionCode.SCRIPT,
                                     ExectionReturnCode.CANCELLED,
                                     String.format(SIMULATION_CANCELLED, nbMessagePost),
                                     true);
      } else {
         return new ScriptStepResult(ExectionActionCode.SCRIPT,
                                     ExectionReturnCode.CANCELLED,
                                     String.format(SCRIPT_CANCELLED, nbMessagePost),
                                     true);
      }
   }

   public static ScriptStepResult createScriptMaxReached(int nbMessagePost, boolean simulation) {
      if (simulation) {
         return new ScriptStepResult(ExectionActionCode.SCRIPT,
                                     ExectionReturnCode.SUCCESS,
                                     String.format(SIMULATION_MAX_REACHED, nbMessagePost),
                                     true);
      } else {
         return new ScriptStepResult(ExectionActionCode.SCRIPT,
                                     ExectionReturnCode.SUCCESS,
                                     String.format(SCRIPT_MAX_REACHED, nbMessagePost),
                                     true);
      }
   }

   // Session

   public static ScriptStepResult createSessionConnectStart(String sessionName) {
      return new ScriptStepResult(ExectionActionCode.SESSION,
                                  ExectionReturnCode.START,
                                  String.format(SESSION_CONNECT, sessionName),
                                  true);
   }

   public static ScriptStepResult createSessionConnectSuccess() {
      return new ScriptStepResult(ExectionActionCode.SESSION, ExectionReturnCode.SUCCESS, SESSION_CONNECT_SUCCESS, true);
   }

   public static ScriptStepResult createSessionConnectFail(String sessionName, Exception e) {
      String ex = e.getClass().getCanonicalName() + ": " + e.getMessage();
      return new ScriptStepResult(ExectionActionCode.SESSION,
                                  ExectionReturnCode.FAILED,
                                  String.format(SESSION_CONNECT_FAIL, sessionName, ex),
                                  true);
   }

   public static ScriptStepResult createSessionDisconnectStart(String sessionName) {
      return new ScriptStepResult(ExectionActionCode.SESSION,
                                  ExectionReturnCode.START,
                                  String.format(SESSION_DISCONNECT, sessionName),
                                  true);
   }

   public static ScriptStepResult createSessionDisconnectSuccess() {
      return new ScriptStepResult(ExectionActionCode.SESSION, ExectionReturnCode.SUCCESS, SESSION_DISCONNECT_SUCCESS, true);
   }

   public static ScriptStepResult createSessionDisconnectFail(String sessionName, Exception e) {
      String ex = e.getClass().getCanonicalName() + ": " + e.getMessage();
      return new ScriptStepResult(ExectionActionCode.SESSION,
                                  ExectionReturnCode.FAILED,
                                  String.format(SESSION_DISCONNECT_FAIL, sessionName, ex),
                                  true);
   }

   // Step

   public static ScriptStepResult createStepStart(String templateName, String destinationName) {
      return new ScriptStepResult(ExectionActionCode.STEP,
                                  ExectionReturnCode.START,
                                  String.format(STEP_START, templateName, destinationName),
                                  true);
   }

   public static ScriptStepResult createStepSuccess() {
      return new ScriptStepResult(ExectionActionCode.STEP, ExectionReturnCode.SUCCESS, STEP_TERMINATED, true);
   }

   public static ScriptStepResult createStepFail(String destinationName, Exception e) {
      String ex = e.getClass().getCanonicalName() + ": " + e.getMessage();
      return new ScriptStepResult(ExectionActionCode.SCRIPT,
                                  ExectionReturnCode.FAILED,
                                  String.format(STEP_FAILED, destinationName, ex),
                                  true);
   }

   public static ScriptStepResult createStepPauseStart(Integer delay) {
      return new ScriptStepResult(ExectionActionCode.STEP,
                                  ExectionReturnCode.START,
                                  String.format(STEP_PAUSE_RUNNING, delay),
                                  true);
   }

   public static ScriptStepResult createStepPauseSuccess() {
      return new ScriptStepResult(ExectionActionCode.STEP, ExectionReturnCode.SUCCESS, String.format(STEP_PAUSE_SUCCESS), true);
   }

   // Post

   public static ScriptStepResult createPostStart(JTBMessageTemplate jtbMessageTemplate, String templateName) {
      return new ScriptStepResult(ExectionActionCode.POST, ExectionReturnCode.START, jtbMessageTemplate, templateName, false);
   }

   public static ScriptStepResult createPostSuccess() {
      return new ScriptStepResult(ExectionActionCode.POST, ExectionReturnCode.SUCCESS, POST_TERMINATED, false);
   }

   // Pause

   public static ScriptStepResult createPauseStart(Integer delay) {
      return new ScriptStepResult(ExectionActionCode.PAUSE, ExectionReturnCode.START, String.format(PAUSE_RUNNING, delay), true);
   }

   public static ScriptStepResult createPauseSuccess() {
      return new ScriptStepResult(ExectionActionCode.PAUSE, ExectionReturnCode.SUCCESS, String.format(PAUSE_SUCCESS), true);
   }

   // Validations

   public static ScriptStepResult createValidationTemplateFolderFail(String templateFolderName) {
      return new ScriptStepResult(ExectionActionCode.TEMPLATE,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_TEMPLATE_FOLDER_FAIL, templateFolderName),
                                  true);
   }

   public static ScriptStepResult createValidationTemplateFail(String templateName) {
      return new ScriptStepResult(ExectionActionCode.TEMPLATE,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_TEMPLATE_FAIL, templateName),
                                  true);
   }

   public static ScriptStepResult createValidationDirectoryFail() {
      return new ScriptStepResult(ExectionActionCode.TEMPLATE, ExectionReturnCode.FAILED, VALIDATION_DIRECTORY_FAIL, true);
   }

   public static ScriptStepResult createValidationSessionFail(String sessionName) {
      return new ScriptStepResult(ExectionActionCode.SESSION,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_SESSION_FAIL, sessionName),
                                  true);
   }

   public static ScriptStepResult createValidationDestinationFail(String destinationName) {
      return new ScriptStepResult(ExectionActionCode.DESTINATION,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_DESTINATION_FAIL, destinationName),
                                  true);
   }

   public static ScriptStepResult createValidationVariableFail(String variableName) {
      return new ScriptStepResult(ExectionActionCode.VARIABLE,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_VARIABLE_FAIL, variableName),
                                  true);
   }

   public static ScriptStepResult createValidationDataFileFail(String dataFileName) {
      return new ScriptStepResult(ExectionActionCode.DATAFILE,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_DATAFILE_FAIL, dataFileName),
                                  true);
   }

   public static ScriptStepResult createValidationDataFileFail2(String dataFileName) {
      return new ScriptStepResult(ExectionActionCode.DATAFILE,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_DATAFILE2_FAIL, dataFileName),
                                  true);
   }

   public static ScriptStepResult createValidationPayloadDirectoryFail(String dataFileName) {
      return new ScriptStepResult(ExectionActionCode.PAYLOAD_DIR,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_PAYLOAD_DIR_FAIL, dataFileName),
                                  true);
   }

   public static ScriptStepResult createValidationPayloadDirectoryFail2(String dataFileName) {
      return new ScriptStepResult(ExectionActionCode.PAYLOAD_DIR,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_PAYLOAD_DIR2_FAIL, dataFileName),
                                  true);
   }

   public static ScriptStepResult createValidationExceptionFail(ExectionActionCode executionCode, String message, Throwable t) {
      String ex = t.getClass().getCanonicalName() + ": " + t.getMessage();
      return new ScriptStepResult(executionCode, ExectionReturnCode.FAILED, String.format(EXCEPTION_FAIL, message, ex), true);
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public Calendar getTs() {
      return ts;
   }

   public void setTs(Calendar ts) {
      this.ts = ts;
   }

   public ExectionActionCode getAction() {
      return action;
   }

   public void setAction(ExectionActionCode action) {
      this.action = action;
   }

   public ExectionReturnCode getReturnCode() {
      return returnCode;
   }

   public void setReturnCode(ExectionReturnCode returnCode) {
      this.returnCode = returnCode;
   }

   public Object getData() {
      return data;
   }

   public void setData(Object data) {
      this.data = data;
   }

   public String getTemplateName() {
      return templateName;
   }

   public void setTemplateName(String templateName) {
      this.templateName = templateName;
   }

   public boolean isAlwaysShow() {
      return alwaysShow;
   }

}

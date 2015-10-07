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

import java.util.Calendar;

import org.titou10.jtb.jms.model.JTBMessageTemplate;

/**
 * Hold the result of an execution step
 * 
 * @author Denis Forveille
 *
 */
public class ScriptStepResult {

   private static final String SCRIPT_RUNNING                  = "Started.";
   private static final String SCRIPT_TERMINATED               = "Terminated.";
   private static final String SCRIPT_CANCELLED                = "Cancelled by user.";

   private static final String STEP_TERMINATED                 = "Post Successful";
   private static final String STEP_FAILED                     = "Post to destination %s failed : %s";
   private static final String STEP_PAUSE_RUNNING              = "Pause for %d seconds after post ...";
   private static final String STEP_PAUSE_SUCCESS              = "Pause terminated.";

   private static final String PAUSE_RUNNING                   = "Pause for %d seconds ...";
   private static final String PAUSE_SUCCESS                   = "Pause terminated.";

   private static final String SESSION_CONNECT                 = "Connecting to session '%s' ...";
   private static final String SESSION_CONNECT_SUCCESS         = "Connected.";
   private static final String SESSION_CONNECT_FAIL            = "Connection to session '%s' failed: %s";
   private static final String SESSION_DISCONNECT              = "Disconnecting from session '%s' ...";
   private static final String SESSION_DISCONNECT_SUCCESS      = "Disconnected.";
   private static final String SESSION_DISCONNECT_FAIL         = "Disconnection from session '%s' failed: %s";

   private static final String VALIDATION_TEMPLATE_FOLDER_FAIL = "Template Folder with name '%s' is unknown";
   private static final String VALIDATION_TEMPLATE_FAIL        = "Template with name '%s' is unknown";
   private static final String VALIDATION_SESSION_FAIL         = "Session with name '%s' is unknown";
   private static final String VALIDATION_DESTINATION_FAIL     = "Destination with name '%s' is unknown";
   private static final String VALIDATION_DATAFILE_FAIL        = "File with name '%s' does not exist";
   private static final String VALIDATION_DATAFILE2_FAIL       = "Data File with variable prefix '%s' is unknown";
   private static final String VALIDATION_VARIABLE_FAIL        = "Global Variable '%s' does not exist";

   private static final String EXCEPTION_FAIL                  = "%s : %s";

   public enum ExectionActionCode {
                                   SCRIPT,
                                   STEP,
                                   PAUSE,
                                   TEMPLATE,
                                   VARIABLE,
                                   DATAFILE,
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

   // ------------------------
   // Constructor
   // ------------------------
   public ScriptStepResult(ExectionActionCode action, ExectionReturnCode returnCode, Object data) {
      this.action = action;
      this.returnCode = returnCode;
      this.data = data;
      this.ts = Calendar.getInstance();
   }

   // ------------------------
   // Factories
   // ------------------------

   // Script

   public static ScriptStepResult createScriptStart() {
      return new ScriptStepResult(ExectionActionCode.SCRIPT, ExectionReturnCode.START, SCRIPT_RUNNING);
   }

   public static ScriptStepResult createScriptSuccess() {
      return new ScriptStepResult(ExectionActionCode.SCRIPT, ExectionReturnCode.SUCCESS, SCRIPT_TERMINATED);
   }

   public static ScriptStepResult createScriptCancelled() {
      return new ScriptStepResult(ExectionActionCode.SCRIPT, ExectionReturnCode.CANCELLED, SCRIPT_CANCELLED);
   }

   // Session

   public static ScriptStepResult createSessionConnectStart(String sessionName) {
      return new ScriptStepResult(ExectionActionCode.SESSION,
                                  ExectionReturnCode.START,
                                  String.format(SESSION_CONNECT, sessionName));
   }

   public static ScriptStepResult createSessionConnectSuccess() {
      return new ScriptStepResult(ExectionActionCode.SESSION, ExectionReturnCode.SUCCESS, SESSION_CONNECT_SUCCESS);
   }

   public static ScriptStepResult createSessionConnectFail(String sessionName, Exception e) {
      return new ScriptStepResult(ExectionActionCode.SESSION,
                                  ExectionReturnCode.FAILED,
                                  String.format(SESSION_CONNECT_FAIL, sessionName, e.getMessage()));
   }

   public static ScriptStepResult createSessionDisconnectStart(String sessionName) {
      return new ScriptStepResult(ExectionActionCode.SESSION,
                                  ExectionReturnCode.START,
                                  String.format(SESSION_DISCONNECT, sessionName));
   }

   public static ScriptStepResult createSessionDisconnectSuccess() {
      return new ScriptStepResult(ExectionActionCode.SESSION, ExectionReturnCode.SUCCESS, SESSION_DISCONNECT_SUCCESS);
   }

   public static ScriptStepResult createSessionDisconnectFail(String sessionName, Exception e) {
      return new ScriptStepResult(ExectionActionCode.SESSION,
                                  ExectionReturnCode.FAILED,
                                  String.format(SESSION_DISCONNECT_FAIL, sessionName, e.getMessage()));
   }

   // Step

   public static ScriptStepResult createStepStart(JTBMessageTemplate jtbMessageTemplate) {
      return new ScriptStepResult(ExectionActionCode.STEP, ExectionReturnCode.START, jtbMessageTemplate);
   }

   public static ScriptStepResult createStepSuccess() {
      return new ScriptStepResult(ExectionActionCode.STEP, ExectionReturnCode.SUCCESS, STEP_TERMINATED);
   }

   public static ScriptStepResult createStepFail(String destinationName, Exception e) {
      return new ScriptStepResult(ExectionActionCode.SCRIPT,
                                  ExectionReturnCode.FAILED,
                                  String.format(STEP_FAILED, destinationName, e.getMessage()));
   }

   public static ScriptStepResult createStepPauseStart(Integer delay) {
      return new ScriptStepResult(ExectionActionCode.STEP, ExectionReturnCode.START, String.format(STEP_PAUSE_RUNNING, delay));
   }

   public static ScriptStepResult createStepPauseSuccess() {
      return new ScriptStepResult(ExectionActionCode.STEP, ExectionReturnCode.SUCCESS, String.format(STEP_PAUSE_SUCCESS));
   }

   // Pause

   public static ScriptStepResult createPauseStart(Integer delay) {
      return new ScriptStepResult(ExectionActionCode.PAUSE, ExectionReturnCode.START, String.format(PAUSE_RUNNING, delay));
   }

   public static ScriptStepResult createPauseSuccess() {
      return new ScriptStepResult(ExectionActionCode.PAUSE, ExectionReturnCode.SUCCESS, String.format(PAUSE_SUCCESS));
   }

   // Validations

   public static ScriptStepResult createValidationTemplateFolderFail(String templateFolderName) {
      return new ScriptStepResult(ExectionActionCode.TEMPLATE,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_TEMPLATE_FOLDER_FAIL, templateFolderName));
   }

   public static ScriptStepResult createValidationTemplateFail(String templateName) {
      return new ScriptStepResult(ExectionActionCode.TEMPLATE,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_TEMPLATE_FAIL, templateName));
   }

   public static ScriptStepResult createValidationSessionFail(String sessionName) {
      return new ScriptStepResult(ExectionActionCode.SESSION,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_SESSION_FAIL, sessionName));
   }

   public static ScriptStepResult createValidationDestinationFail(String destinationName) {
      return new ScriptStepResult(ExectionActionCode.DESTINATION,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_DESTINATION_FAIL, destinationName));
   }

   public static ScriptStepResult createValidationVariableFail(String variableName) {
      return new ScriptStepResult(ExectionActionCode.VARIABLE,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_VARIABLE_FAIL, variableName));
   }

   public static ScriptStepResult createValidationDataFileFail(String dataFileName) {
      return new ScriptStepResult(ExectionActionCode.DATAFILE,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_DATAFILE_FAIL, dataFileName));
   }

   public static ScriptStepResult createValidationDataFileFail2(String dataFileName) {
      return new ScriptStepResult(ExectionActionCode.DATAFILE,
                                  ExectionReturnCode.FAILED,
                                  String.format(VALIDATION_DATAFILE2_FAIL, dataFileName));
   }

   public static ScriptStepResult createValidationExceptionFail(ExectionActionCode executionCode, String message, Throwable t) {
      return new ScriptStepResult(executionCode, ExectionReturnCode.FAILED, String.format(EXCEPTION_FAIL, message, t.getMessage()));
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

}

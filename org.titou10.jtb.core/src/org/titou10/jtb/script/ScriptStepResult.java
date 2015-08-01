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

   private static final String SCRIPT_RUNNING    = "Started.";
   private static final String SCRIPT_TERMINATED = "Terminated.";

   private static final String SEND_TERMINATED = "Post Successful";

   private static final String SESSION_CONNECT      = "Connecting to session '%s'";
   private static final String SESSION_CONNECTED    = "Connected.";
   private static final String SESSION_CONNECT_FAIL = "Connection to session '%s' failed: %s";

   private static final String PAUSE_RUNNING = "Waiting for %d seconds...";
   private static final String PAUSE_SUCCESS = "Pause of %d seconds terminated.";

   private static final String VALIDATION_TEMPLATE_FAIL    = "Template with name '%s' is unknown";
   private static final String VALIDATION_SESSION_FAIL     = "Session with name '%s' is unknown";
   private static final String VALIDATION_DESTINATION_FAIL = "Destination with name '%s' is unknown";

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

   public static ScriptStepResult createSendStart(JTBMessageTemplate jtbMessageTemplate) {
      return new ScriptStepResult(ExectionActionCode.STEP, ExectionReturnCode.RUNNING, jtbMessageTemplate);
   }

   public static ScriptStepResult createSendEnd() {
      return new ScriptStepResult(ExectionActionCode.STEP, ExectionReturnCode.SUCCESS, SEND_TERMINATED);
   }

   public static ScriptStepResult createSessionConnectStart(String sessionName) {
      return new ScriptStepResult(ExectionActionCode.SCRIPT,
                                  ExectionReturnCode.RUNNING,
                                  String.format(SESSION_CONNECT, sessionName));
   }

   public static ScriptStepResult createSessionConnectEnd() {
      return new ScriptStepResult(ExectionActionCode.SCRIPT, ExectionReturnCode.RUNNING, SESSION_CONNECTED);
   }

   public static ScriptStepResult createSessionConnectFail(String sessionName, Exception e) {
      return new ScriptStepResult(ExectionActionCode.SCRIPT,
                                  ExectionReturnCode.FAIL,
                                  String.format(SESSION_CONNECT_FAIL, sessionName, e.getMessage()));
   }

   public static ScriptStepResult createValidationTemplateFail(String templateName) {
      return new ScriptStepResult(ExectionActionCode.VALIDATION,
                                  ExectionReturnCode.FAIL,
                                  String.format(VALIDATION_TEMPLATE_FAIL, templateName));
   }

   public static ScriptStepResult createValidationSessionFail(String sessionName) {
      return new ScriptStepResult(ExectionActionCode.VALIDATION,
                                  ExectionReturnCode.FAIL,
                                  String.format(VALIDATION_SESSION_FAIL, sessionName));
   }

   public static ScriptStepResult createValidationDestinationFail(String destinationName) {
      return new ScriptStepResult(ExectionActionCode.VALIDATION,
                                  ExectionReturnCode.FAIL,
                                  String.format(VALIDATION_DESTINATION_FAIL, destinationName));
   }

   public static ScriptStepResult createScriptStart() {
      return new ScriptStepResult(ExectionActionCode.SCRIPT, ExectionReturnCode.RUNNING, SCRIPT_RUNNING);
   }

   public static ScriptStepResult createScriptEndend() {
      return new ScriptStepResult(ExectionActionCode.SCRIPT, ExectionReturnCode.SUCCESS, SCRIPT_TERMINATED);
   }

   public static ScriptStepResult createPauseStart(Integer delay) {
      return new ScriptStepResult(ExectionActionCode.PAUSE, ExectionReturnCode.RUNNING, String.format(PAUSE_RUNNING, delay));
   }

   public static ScriptStepResult createPauseSuccess(Integer delay) {
      return new ScriptStepResult(ExectionActionCode.PAUSE, ExectionReturnCode.SUCCESS, String.format(PAUSE_SUCCESS, delay));
   }

   public static ScriptStepResult createRegularStart() {
      return new ScriptStepResult(ExectionActionCode.STEP, ExectionReturnCode.RUNNING, null);
   }

   public void updateSuccess(Object data) {
      this.returnCode = ExectionReturnCode.SUCCESS;
      this.data = data;
   }

   public void updateFail(Object data) {
      this.returnCode = ExectionReturnCode.FAIL;
      this.data = data;
   }

   // ------------------------
   // Helper
   // ------------------------
   public enum ExectionActionCode {
                                   PAUSE,
                                   VALIDATION,
                                   SCRIPT,
                                   STEP;
   }

   public enum ExectionReturnCode {
                                   RUNNING,
                                   SUCCESS,
                                   FAIL;
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

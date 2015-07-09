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

/**
 * Hold the result of an execution step
 * 
 * @author Denis Forveille
 *
 */
public class ScriptStepResult {

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

   public static ScriptStepResult createStartPause() {
      return new ScriptStepResult(ExectionActionCode.PAUSE, ExectionReturnCode.RUNNING, null);
   }

   public static ScriptStepResult createStartRegukar() {
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

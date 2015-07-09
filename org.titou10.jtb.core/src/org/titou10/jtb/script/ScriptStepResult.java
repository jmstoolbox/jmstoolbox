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

import java.util.Date;

/**
 * Hold the result of an execution step
 * 
 * @author Denis Forveille
 *
 */
public class ScriptStepResult {

   private Date   ts;
   private String action;
   private String returnCode;
   private Object data;

   // ------------------------
   // Constructor
   // ------------------------
   public ScriptStepResult(String action, String returnCode, Object data) {
      this.action = action;
      this.returnCode = returnCode;
      this.data = data;
      this.ts = new Date();
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public Date getTs() {
      return ts;
   }

   public void setTs(Date ts) {
      this.ts = ts;
   }

   public String getAction() {
      return action;
   }

   public void setAction(String action) {
      this.action = action;
   }

   public String getReturnCode() {
      return returnCode;
   }

   public void setReturnCode(String returnCode) {
      this.returnCode = returnCode;
   }

   public Object getData() {
      return data;
   }

   public void setData(Object data) {
      this.data = data;
   }

}

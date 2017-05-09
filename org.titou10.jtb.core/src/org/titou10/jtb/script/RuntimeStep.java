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

import java.io.File;
import java.util.List;

import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.script.gen.DataFile;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.script.gen.StepKind;

/**
 * "Runtime" view of a step
 * 
 * @author Denis Forveille
 *
 */
public class RuntimeStep {
   private Step               step;

   private JTBMessageTemplate jtbMessageTemplate;
   private JTBConnection      jtbConnection;
   private JTBDestination     jtbDestination;

   private DataFile           dataFile;
   private List<File>         payloadFiles;
   private String[]           varNames;

   private String             templateName;

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
      StringBuilder sb = new StringBuilder(256);

      if (step.getKind() == StepKind.REGULAR) {
         sb.append("[");
         sb.append(step.getTemplateDirectory());
         sb.append("::");
         sb.append(step.getTemplateName());
         sb.append("] -> ");
         sb.append(step.getSessionName());
         if (step.getVariablePrefix() != null) {
            sb.append(". CSV file: '");
            sb.append(dataFile.getFileName());
            sb.append("'");
         }
         if (step.getPayloadDirectory() != null) {
            sb.append(". Payloads from directory '");
            sb.append(step.getPayloadDirectory());
            sb.append("'");
         }
      } else {
         sb.append("Pause for");
         sb.append(step.getPauseSecsAfter());
         sb.append(" seconds");
      }

      return sb.toString();
   }

   public void setJtbMessageTemplate(JTBMessageTemplate jtbMessageTemplate, String templateName) {
      this.jtbMessageTemplate = jtbMessageTemplate;
      this.templateName = templateName;
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public Step getStep() {
      return step;
   }

   public String[] getVarNames() {
      return varNames;
   }

   public void setVarNames(String[] varNames) {
      this.varNames = varNames;
   }

   public JTBDestination getJtbDestination() {
      return jtbDestination;
   }

   public void setJtbDestination(JTBDestination jtbDestination) {
      this.jtbDestination = jtbDestination;
   }

   public DataFile getDataFile() {
      return dataFile;
   }

   public void setDataFile(DataFile dataFile) {
      this.dataFile = dataFile;
   }

   public JTBConnection getJtbConnection() {
      return jtbConnection;
   }

   public void setJtbConnection(JTBConnection jtbConnection) {
      this.jtbConnection = jtbConnection;
   }

   public List<File> getPayloadFiles() {
      return payloadFiles;
   }

   public void setPayloadFiles(List<File> payloadFiles) {
      this.payloadFiles = payloadFiles;
   }

   public JTBMessageTemplate getJtbMessageTemplate() {
      return jtbMessageTemplate;
   }

   public String getTemplateName() {
      return templateName;
   }

}

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

import java.util.ArrayList;
import java.util.List;

import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBSession;
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
   private Step                     step;

   private List<JTBMessageTemplate> jtbMessageTemplates = new ArrayList<>();
   private List<String>             templateNames       = new ArrayList<>();

   private JTBSession               jtbSession;
   private JTBDestination           jtbDestination;

   private DataFile                 dataFile;
   private String[]                 varNames;

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
         if (jtbMessageTemplates.size() == 1) {
            builder.append(step.getTemplateName());
         } else {
            builder.append("Folder:");
            builder.append(step.getTemplateName());
         }
         builder.append("] -> ");
         builder.append(step.getSessionName());
      } else {
         builder.append("Pause for");
         builder.append(step.getPauseSecsAfter());
         builder.append(" seconds");
      }

      return builder.toString();
   }

   public void addJTBMessageTemplate(JTBMessageTemplate jtbMessageTemplate, String templateName) {
      jtbMessageTemplates.add(jtbMessageTemplate);
      templateNames.add(templateName);
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public Step getStep() {
      return step;
   }

   public List<JTBMessageTemplate> getJtbMessageTemplates() {
      return jtbMessageTemplates;
   }

   public void setJtbMessageTemplates(List<JTBMessageTemplate> jtbMessageTemplates) {
      this.jtbMessageTemplates = jtbMessageTemplates;
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

   public JTBSession getJtbSession() {
      return jtbSession;
   }

   public void setJtbSession(JTBSession jtbSession) {
      this.jtbSession = jtbSession;
   }

   public DataFile getDataFile() {
      return dataFile;
   }

   public void setDataFile(DataFile dataFile) {
      this.dataFile = dataFile;
   }

   public List<String> getTemplateNames() {
      return templateNames;
   }

   public void setTemplateNames(List<String> templateNames) {
      this.templateNames = templateNames;
   }

}

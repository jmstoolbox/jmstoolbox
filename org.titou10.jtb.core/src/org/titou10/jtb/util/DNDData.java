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
package org.titou10.jtb.util;

import java.lang.ref.WeakReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;

/**
 * 
 * Keep data related to Drag & Drop operations
 * 
 * @author Denis Forveille
 *
 */
public class DNDData {

   private static DNDElement                        drag;                         // Kind of object Dragged
   private static DNDElement                        drop;                         // Kind of drop target

   private static WeakReference<JTBMessage>         sourceJTBMessage;
   private static WeakReference<IFile>              sourceJTBMessageTemplateIFile;
   private static WeakReference<Directory>          sourceDirectory;
   private static WeakReference<Script>             sourceScript;
   private static WeakReference<Step>               sourceStep;
   private static WeakReference<JTBDestination>     targetJTBDestination;
   private static WeakReference<IResource>          targeTemplateIResource;
   private static WeakReference<Directory>          targetDirectory;
   private static WeakReference<Script>             targetScript;
   private static WeakReference<Step>               targetStep;

   private static WeakReference<JTBMessageTemplate> sourceJTBMessageTemplate;     // Link from script execution

   public enum DNDElement {
                           JTBMESSAGE,
                           JTBDESTINATION,
                           TEMPLATE,
                           JTBMESSAGETEMPLATE,
                           DIRECTORY,
                           SCRIPT,
                           STEP;
   }

   // ------------------
   // Get/Set References
   // ------------------

   public static Directory getSourceDirectory() {
      if (sourceDirectory != null) {
         return sourceDirectory.get();
      } else {
         return null;
      }
   }

   public static void setSourceDirectory(Directory directory) {
      DNDData.sourceDirectory = new WeakReference<>(directory);
   }

   public static Directory getTargetDirectory() {
      if (targetDirectory != null) {
         return targetDirectory.get();
      } else {
         return null;
      }
   }

   public static void setTargetDirectory(Directory directory) {
      DNDData.targetDirectory = new WeakReference<>(directory);
   }

   public static Script getTargetScript() {
      if (targetScript != null) {
         return targetScript.get();
      } else {
         return null;
      }
   }

   public static void setTargetScript(Script script) {
      DNDData.targetScript = new WeakReference<>(script);
   }

   public static Script getSourceScript() {
      if (sourceScript != null) {
         return sourceScript.get();
      } else {
         return null;
      }
   }

   public static void setSourceScript(Script script) {
      DNDData.sourceScript = new WeakReference<>(script);
   }

   public static Step getTargetStep() {
      if (targetStep != null) {
         return targetStep.get();
      } else {
         return null;
      }
   }

   public static void setTargetStep(Step step) {
      DNDData.targetStep = new WeakReference<>(step);
   }

   public static Step getSourceStep() {
      if (sourceStep != null) {
         return sourceStep.get();
      } else {
         return null;
      }
   }

   public static void setSourceStep(Step step) {
      DNDData.sourceStep = new WeakReference<>(step);
   }

   public static JTBMessageTemplate getSourceJTBMessageTemplate() {
      if (sourceJTBMessageTemplate != null) {
         return sourceJTBMessageTemplate.get();
      } else {
         return null;
      }
   }

   public static void setSourceJTBMessageTemplate(JTBMessageTemplate sourceJTBMessageTemplate) {
      DNDData.sourceJTBMessageTemplate = new WeakReference<>(sourceJTBMessageTemplate);
   }

   public static IResource getTargetTemplateIResource() {
      if (targeTemplateIResource != null) {
         return targeTemplateIResource.get();
      } else {
         return null;
      }
   }

   public static void setTargetTemplateIResource(IResource targeTemplateIResource) {
      DNDData.targeTemplateIResource = new WeakReference<>(targeTemplateIResource);
   }

   public static JTBMessage getSourceJTBMessage() {
      if (sourceJTBMessage != null) {
         return sourceJTBMessage.get();
      } else {
         return null;
      }
   }

   public static void setSourceJTBMessage(JTBMessage sourceJTBMessage) {
      DNDData.sourceJTBMessage = new WeakReference<>(sourceJTBMessage);
   }

   public static IFile getSourceJTBMessageTemplateIFile() {
      if (sourceJTBMessageTemplateIFile != null) {
         return sourceJTBMessageTemplateIFile.get();
      } else {
         return null;
      }
   }

   public static void setSourceJTBMessageTemplateIFile(IFile sourceJTBMessageTemplateIFile) {
      DNDData.sourceJTBMessageTemplateIFile = new WeakReference<>(sourceJTBMessageTemplateIFile);
   }

   public static JTBDestination getTargetJTBDestination() {
      if (targetJTBDestination != null) {
         return targetJTBDestination.get();
      } else {
         return null;
      }
   }

   public static void setTargetJTBDestination(JTBDestination targetJTBDestination) {
      DNDData.targetJTBDestination = new WeakReference<>(targetJTBDestination);
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public static DNDElement getDrag() {
      return drag;
   }

   public static void setDrag(DNDElement drag) {
      DNDData.drag = drag;
   }

   public static DNDElement getDrop() {
      return drop;
   }

   public static void setDrop(DNDElement drop) {
      DNDData.drop = drop;
   }

}

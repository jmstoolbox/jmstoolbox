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
import org.eclipse.core.resources.IFolder;
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

   private static DNDElement                        drag;                     // Kind of object Dragged
   private static DNDElement                        drop;                     // Kind of drop target

   private static WeakReference<JTBMessage>         sourceJTBMessage;
   private static WeakReference<IFile>              sourceTemplateIFile;
   private static WeakReference<IFolder>            sourceTemplateIFolder;
   private static WeakReference<Directory>          sourceDirectory;
   private static WeakReference<Script>             sourceScript;
   private static WeakReference<Step>               sourceStep;

   private static WeakReference<JTBDestination>     targetJTBDestination;
   private static WeakReference<IResource>          targeTemplateIResource;
   private static WeakReference<IFile>              targetTemplateIFile;
   private static WeakReference<IFolder>            targetTemplateIFolder;
   private static WeakReference<Directory>          targetDirectory;
   private static WeakReference<Script>             targetScript;
   private static WeakReference<Step>               targetStep;

   private static WeakReference<JTBMessageTemplate> sourceJTBMessageTemplate; // Link from script execution

   public enum DNDElement {
                           JTBMESSAGE,
                           JTBDESTINATION,
                           TEMPLATE,
                           TEMPLATE_FOLDER,
                           JTBMESSAGETEMPLATE,
                           DIRECTORY,
                           SCRIPT,
                           STEP;
   }

   public static void dragTemplate(IFile file) {
      sourceTemplateIFile = new WeakReference<>(file);
      drag = DNDElement.TEMPLATE;
   }

   public static void dragTemplateFolder(IFolder folder) {
      sourceTemplateIFolder = new WeakReference<>(folder);
      drag = DNDElement.TEMPLATE_FOLDER;
   }

   public static void dragScript(Script script) {
      sourceScript = new WeakReference<>(script);
      drag = DNDElement.SCRIPT;
   }

   public static void dragDirectory(Directory directory) {
      sourceDirectory = new WeakReference<>(directory);
      drag = DNDElement.DIRECTORY;
   }

   // -------------

   public static void dropOnTemplateIFile(IFile file) {
      targetTemplateIFile = new WeakReference<>(file);
      drop = DNDElement.TEMPLATE;
   }

   public static void dropOnTemplateIFolder(IFolder folder) {
      targetTemplateIFolder = new WeakReference<>(folder);
      drop = DNDElement.TEMPLATE_FOLDER;
   }

   public static void dropOnScript(Script script) {
      targetScript = new WeakReference<>(script);
      drop = DNDElement.SCRIPT;
   }

   public static void dropOnDirectory(Directory directory) {
      targetDirectory = new WeakReference<>(directory);
      drop = DNDElement.DIRECTORY;
   }

   // ------------------
   // Get/Set References
   // ------------------

   public static IFile getSourceTemplateIFile() {
      return (sourceTemplateIFile == null) ? null : sourceTemplateIFile.get();
   }

   public static IFile getTargetTemplateIFile() {
      return (targetTemplateIFile == null) ? null : targetTemplateIFile.get();
   }

   public static IFolder getSourceTemplateIFolder() {
      return (sourceTemplateIFolder == null) ? null : sourceTemplateIFolder.get();
   }

   public static IFolder getTargetTemplateIFolder() {
      return (targetTemplateIFolder == null) ? null : targetTemplateIFolder.get();
   }

   public static Directory getSourceDirectory() {
      return (sourceDirectory == null) ? null : sourceDirectory.get();
   }

   public static Directory getTargetDirectory() {
      return (targetDirectory == null) ? null : targetDirectory.get();
   }

   public static Script getSourceScript() {
      return (sourceScript == null) ? null : sourceScript.get();
   }

   public static Script getTargetScript() {
      return (targetScript == null) ? null : targetScript.get();
   }

   // -------------
   // -------------
   // -------------
   public static Step getTargetStep() {
      return (targetStep == null) ? null : targetStep.get();
   }

   public static void setTargetStep(Step step) {
      DNDData.targetStep = new WeakReference<>(step);
   }

   public static Step getSourceStep() {
      return (sourceStep == null) ? null : sourceStep.get();
   }

   public static void setSourceStep(Step step) {
      DNDData.sourceStep = new WeakReference<>(step);
   }

   public static JTBMessageTemplate getSourceJTBMessageTemplate() {
      return (sourceJTBMessageTemplate == null) ? null : sourceJTBMessageTemplate.get();
   }

   public static void setSourceJTBMessageTemplate(JTBMessageTemplate sourceJTBMessageTemplate) {
      DNDData.sourceJTBMessageTemplate = new WeakReference<>(sourceJTBMessageTemplate);
   }

   public static IResource getTargetTemplateIResource() {
      return (targeTemplateIResource == null) ? null : targeTemplateIResource.get();
   }

   public static void setTargetTemplateIResource(IResource targeTemplateIResource) {
      DNDData.targeTemplateIResource = new WeakReference<>(targeTemplateIResource);
   }

   public static JTBMessage getSourceJTBMessage() {
      return (sourceJTBMessage == null) ? null : sourceJTBMessage.get();
   }

   public static void setSourceJTBMessage(JTBMessage sourceJTBMessage) {
      DNDData.sourceJTBMessage = new WeakReference<>(sourceJTBMessage);
   }

   public static IFile getSourceJTBMessageTemplateIFile() {
      return (sourceTemplateIFile == null) ? null : sourceTemplateIFile.get();
   }

   public static void setSourceJTBMessageTemplateIFile(IFile sourceJTBMessageTemplateIFile) {
      DNDData.sourceTemplateIFile = new WeakReference<>(sourceJTBMessageTemplateIFile);
   }

   public static JTBDestination getTargetJTBDestination() {
      return (targetJTBDestination == null) ? null : targetJTBDestination.get();
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

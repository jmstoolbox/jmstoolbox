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
package org.titou10.jtb.ui.dnd;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
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

   // Kind of object Dragged
   private static DNDElement                        drag;
   // Kind of drop target
   private static DNDElement                        drop;

   private static List<WeakReference<JTBMessage>>   sourceJTBMessages;
   private static WeakReference<IFileStore>         sourceTemplateFileStore;
   private static WeakReference<String>             sourceTemplateExternal;
   private static WeakReference<IFileStore>         sourceTemplateFolderFileStore;
   private static WeakReference<Directory>          sourceDirectory;
   private static WeakReference<Script>             sourceScript;
   private static WeakReference<Step>               sourceStep;
   private static WeakReference<String>             sourceExternalFileName;

   private static WeakReference<JTBDestination>     targetJTBDestination;
   private static WeakReference<IFileStore>         targetTemplateFileStore;
   private static WeakReference<IFileStore>         targetTemplateFolderFileStore;
   private static WeakReference<Directory>          targetDirectory;
   private static WeakReference<Script>             targetScript;
   private static WeakReference<Step>               targetStep;

   private static WeakReference<List<IFileStore>>   sourceTemplatesFileStore;

   private static WeakReference<JTBMessageTemplate> selectedJTBMessageTemplate;   // Link from script execution

   public enum DNDElement {
                           JTBMESSAGE,
                           JTBMESSAGE_MULTI,
                           JTBDESTINATION,
                           TEMPLATE,
                           TEMPLATE_EXTERNAL,
                           TEMPLATE_FOLDER,
                           DIRECTORY,
                           SCRIPT,
                           STEP,
                           EXTERNAL_FILE_NAME,

                           TEMPLATES_FILESTORE;
   }

   // FileStores

   public static void dragTemplates(List<IFileStore> fileStores) {
      clearDrag();
      sourceTemplatesFileStore = new WeakReference<>(fileStores);
      drag = DNDElement.TEMPLATES_FILESTORE;
   }

   public static List<IFileStore> getSourceTemplatesFileStore() {
      return (sourceTemplatesFileStore == null) ? null : sourceTemplatesFileStore.get();
   }

   // Steps

   public static void dragStep(Step step) {
      clearDrag();
      sourceStep = new WeakReference<>(step);
      drag = DNDElement.STEP;
   }

   public static void dropOnStep(Step step) {
      clearDrop();
      targetStep = new WeakReference<>(step);
      drop = DNDElement.STEP;
   }

   // JTB Messages

   public static void dragJTBMessage(JTBMessage jtbMessage) {
      clearDrag();
      sourceJTBMessages = Collections.singletonList(new WeakReference<>(jtbMessage));
      drag = DNDElement.JTBMESSAGE;
   }

   public static void dragJTBMessageMulti(List<JTBMessage> jtbMessages) {
      clearDrag();
      sourceJTBMessages = new ArrayList<>(jtbMessages.size());
      for (JTBMessage jtbMessage : jtbMessages) {
         sourceJTBMessages.add(new WeakReference<JTBMessage>(jtbMessage));
      }
      drag = DNDElement.JTBMESSAGE_MULTI;
   }

   // JTB Destinations

   public static void dragExternalFileName(String externalFileName) {
      clearDrag();
      sourceExternalFileName = new WeakReference<>(externalFileName);
      drag = DNDElement.EXTERNAL_FILE_NAME;
   }

   public static void dropOnJTBDestination(JTBDestination jtbDestination) {
      clearDrop();
      targetJTBDestination = new WeakReference<>(jtbDestination);
      drop = DNDElement.JTBDESTINATION;
   }

   // Templates

   public static void dragTemplate(IFileStore fileStore) {
      clearDrag();
      sourceTemplateFileStore = new WeakReference<>(fileStore);
      drag = DNDElement.TEMPLATE;
   }

   public static void dragTemplateFolderFileStore(IFileStore folderFileStore) {
      clearDrag();
      sourceTemplateFolderFileStore = new WeakReference<>(folderFileStore);
      drag = DNDElement.TEMPLATE_FOLDER;
   }

   public static void dragTemplateExternal(String fileName) {
      clearDrag();
      sourceTemplateExternal = new WeakReference<>(fileName);
      drag = DNDElement.TEMPLATE_EXTERNAL;
   }

   public static void dropOnTemplateFileStore(IFileStore fileStore) {
      clearDrop();
      targetTemplateFileStore = new WeakReference<>(fileStore);
      drop = DNDElement.TEMPLATE;
   }

   public static void dropOnTemplateFolderFileStore(IFileStore folderFileStore) {
      clearDrop();
      targetTemplateFolderFileStore = new WeakReference<>(folderFileStore);
      drop = DNDElement.TEMPLATE_FOLDER;
   }

   // Scripts

   public static void dragScript(Script script) {
      clearDrag();
      sourceScript = new WeakReference<>(script);
      drag = DNDElement.SCRIPT;
   }

   public static void dragDirectory(Directory directory) {
      clearDrag();
      sourceDirectory = new WeakReference<>(directory);
      drag = DNDElement.DIRECTORY;
   }

   public static void dropOnScript(Script script) {
      clearDrop();
      targetScript = new WeakReference<>(script);
      drop = DNDElement.SCRIPT;
   }

   public static void dropOnDirectory(Directory directory) {
      clearDrop();
      targetDirectory = new WeakReference<>(directory);
      drop = DNDElement.DIRECTORY;
   }

   // ------------------
   // Convenient place to store "current" JTBMessageTemplate
   // ------------------
   public static void setSelectedJTBMessageTemplate(JTBMessageTemplate jtbMessageTemplate) {
      DNDData.selectedJTBMessageTemplate = new WeakReference<>(jtbMessageTemplate);
   }

   public static JTBMessageTemplate getSelectedJTBMessageTemplate() {
      return (selectedJTBMessageTemplate == null) ? null : selectedJTBMessageTemplate.get();
   }

   // ------------------
   // Get/Set References
   // ------------------

   public static String getSourceTemplateExternal() {
      return (sourceTemplateExternal == null) ? null : sourceTemplateExternal.get();
   }

   public static IFileStore getSourceTemplateFileStore() {
      return (sourceTemplateFileStore == null) ? null : sourceTemplateFileStore.get();
   }

   public static IFileStore getTargetTemplateFileStore() {
      return (targetTemplateFileStore == null) ? null : targetTemplateFileStore.get();
   }

   public static IFileStore getSourceTemplateFolderFileStore() {
      return (sourceTemplateFolderFileStore == null) ? null : sourceTemplateFolderFileStore.get();
   }

   public static IFileStore getTargetTemplateFolderFileStore() {
      return (targetTemplateFolderFileStore == null) ? null : targetTemplateFolderFileStore.get();
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

   public static Step getSourceStep() {
      return (sourceStep == null) ? null : sourceStep.get();
   }

   public static Step getTargetStep() {
      return (targetStep == null) ? null : targetStep.get();
   }

   // public static JTBMessage getSourceJTBMessage() {
   // return (sourceJTBMessage == null) ? null : sourceJTBMessage.get();
   // }

   public static String getSourceExternalFileName() {
      return (sourceExternalFileName == null) ? null : sourceExternalFileName.get();
   }

   public static List<JTBMessage> getSourceJTBMessages() {
      List<JTBMessage> res = new ArrayList<>(sourceJTBMessages.size());
      for (WeakReference<JTBMessage> wr : sourceJTBMessages) {
         if (wr.get() != null) {
            res.add(wr.get());
         }
      }
      return res;
   }

   public static IFileStore getSourceJTBMessageTemplateFileStore() {
      return (sourceTemplateFileStore == null) ? null : sourceTemplateFileStore.get();
   }

   public static JTBDestination getTargetJTBDestination() {
      return (targetJTBDestination == null) ? null : targetJTBDestination.get();
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public static DNDElement getDrag() {
      return drag;
   }

   public static DNDElement getDrop() {
      return drop;
   }

   // ------------------------
   // Helpers
   // ------------------------
   private static void clearDrag() {
      sourceJTBMessages = null;
      sourceTemplateFileStore = null;
      sourceTemplateFolderFileStore = null;
      sourceDirectory = null;
      sourceScript = null;
      sourceStep = null;
      sourceExternalFileName = null;
   }

   private static void clearDrop() {
      targetJTBDestination = null;
      targetTemplateFileStore = null;
      targetTemplateFolderFileStore = null;
      targetDirectory = null;
      targetScript = null;
      targetStep = null;
   }
}

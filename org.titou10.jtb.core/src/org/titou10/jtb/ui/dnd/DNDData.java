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
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.titou10.jtb.cs.gen.Column;
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
   private static List<WeakReference<IFileStore>>   sourceTemplatesFileStore;
   private static List<WeakReference<String>>       sourceTemplatesFileNames;

   private static WeakReference<JTBDestination>     targetJTBDestination;
   private static WeakReference<IFileStore>         targetTemplateFileStore;

   private static WeakReference<Directory>          sourceDirectory;
   private static WeakReference<Script>             sourceScript;
   private static WeakReference<Step>               sourceStep;
   private static WeakReference<Directory>          targetDirectory;
   private static WeakReference<Script>             targetScript;
   private static WeakReference<Step>               targetStep;

   private static WeakReference<Column>             sourceColumn;
   private static WeakReference<Column>             targetColumn;

   private static WeakReference<JTBMessageTemplate> selectedJTBMessageTemplate; // Link from script execution

   public enum DNDElement {
                           DIRECTORY,
                           SCRIPT,
                           STEP,

                           DROP_ON_TEMPLATE_FILESTORE_FILE,
                           DROP_ON_TEMPLATE_FILESTORE_FOLDER,
                           DROP_ON_JTB_DESTINATION,

                           JTB_MESSAGES,
                           TEMPLATE_FILESTORES,
                           TEMPLATES_FILENAMES,

                           CS_COLUMN;
   }

   // File Names

   public static void dragTemplatesFileNames(List<String> fileNames) {
      clearDrag();
      sourceTemplatesFileNames = new ArrayList<>(fileNames.size());
      for (String fileName : fileNames) {
         sourceTemplatesFileNames.add(new WeakReference<String>(fileName));
      }
      drag = DNDElement.TEMPLATES_FILENAMES;
   }

   public static List<String> getSourceTemplatesFileNames() {
      List<String> res = new ArrayList<>(sourceTemplatesFileNames.size());
      for (WeakReference<String> wr : sourceTemplatesFileNames) {
         if (wr.get() != null) {
            res.add(wr.get());
         }
      }
      return res;
   }

   // FileStores

   public static void dragTemplatesFileStores(List<IFileStore> fileStores) {
      clearDrag();
      sourceTemplatesFileStore = new ArrayList<>(fileStores.size());
      for (IFileStore fileStore : fileStores) {
         sourceTemplatesFileStore.add(new WeakReference<IFileStore>(fileStore));
      }
      drag = DNDElement.TEMPLATE_FILESTORES;
   }

   public static List<IFileStore> getSourceTemplatesFileStores() {
      List<IFileStore> res = new ArrayList<>(sourceTemplatesFileStore.size());
      for (WeakReference<IFileStore> wr : sourceTemplatesFileStore) {
         if (wr.get() != null) {
            res.add(wr.get());
         }
      }
      return res;
   }

   public static void dropOnTemplateFileStoreFile(IFileStore fileFileStore) {
      clearDrop();
      targetTemplateFileStore = new WeakReference<>(fileFileStore);
      drop = DNDElement.DROP_ON_TEMPLATE_FILESTORE_FILE;
   }

   public static void dropOnTemplateFileStoreFolder(IFileStore folderFileStore) {
      clearDrop();
      targetTemplateFileStore = new WeakReference<>(folderFileStore);
      drop = DNDElement.DROP_ON_TEMPLATE_FILESTORE_FOLDER;
   }

   // JTB Messages

   public static void dragJTBMessages(List<JTBMessage> jtbMessages) {
      clearDrag();
      sourceJTBMessages = new ArrayList<>(jtbMessages.size());
      for (JTBMessage jtbMessage : jtbMessages) {
         sourceJTBMessages.add(new WeakReference<JTBMessage>(jtbMessage));
      }
      drag = DNDElement.JTB_MESSAGES;
   }

   // JTB Destinations

   public static void dropOnJTBDestination(JTBDestination jtbDestination) {
      clearDrop();
      targetJTBDestination = new WeakReference<>(jtbDestination);
      drop = DNDElement.DROP_ON_JTB_DESTINATION;
   }

   public static JTBDestination getTargetJTBDestination() {
      return (targetJTBDestination == null) ? null : targetJTBDestination.get();
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

   // Columns Set Colum

   public static void dragColumn(Column column) {
      clearDrag();
      sourceColumn = new WeakReference<>(column);
      drag = DNDElement.CS_COLUMN;
   }

   public static void dropOnColumn(Column column) {
      clearDrop();
      targetColumn = new WeakReference<>(column);
      drop = DNDElement.CS_COLUMN;
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

   public static Column getSourceColumn() {
      return (sourceColumn == null) ? null : sourceColumn.get();
   }

   public static Column getTargetColumn() {
      return (targetColumn == null) ? null : targetColumn.get();
   }

   public static IFileStore getTargetTemplateFileStore() {
      return (targetTemplateFileStore == null) ? null : targetTemplateFileStore.get();
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
      sourceTemplatesFileNames = null;
      sourceTemplatesFileStore = null;

      sourceDirectory = null;
      sourceScript = null;
      sourceStep = null;

      sourceColumn = null;
   }

   private static void clearDrop() {
      targetJTBDestination = null;
      targetTemplateFileStore = null;

      targetDirectory = null;
      targetScript = null;
      targetStep = null;

      targetColumn = null;
   }
}

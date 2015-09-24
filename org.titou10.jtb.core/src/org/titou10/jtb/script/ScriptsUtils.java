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
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.script.gen.DataFile;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.GlobalVariable;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.script.gen.StepKind;

/**
 * Utility class to manage "Scripts"
 * 
 * @author Denis Forveille
 *
 */
public final class ScriptsUtils {

   private static final Logger log = LoggerFactory.getLogger(ScriptsUtils.class);

   public static String getFullNameDots(Directory directory) {

      // Build a list of directory names
      List<String> parts = new ArrayList<>();

      Directory d = directory;
      while (d != null) {
         // Do not use High level Directory
         if (d.getParent() != null) {
            parts.add(d.getName());
            parts.add(".");
         }
         d = d.getParent();
      }

      Collections.reverse(parts);

      StringBuilder sb = new StringBuilder(128);
      for (String string : parts) {
         sb.append(string);
      }

      return sb.toString().substring(1);
   }

   public static String getFullName(Script script) {

      // Build a list of directory names
      List<String> parts = new ArrayList<>();
      parts.add(script.getName());
      Directory d = script.getParent();
      while (d != null) {
         // Do not use High level Directory
         if (d.getParent() != null) {
            parts.add("/");
            parts.add(d.getName());
         }
         d = d.getParent();
      }

      Collections.reverse(parts);

      StringBuilder sb = new StringBuilder(128);
      for (String string : parts) {
         sb.append(string);
      }

      return sb.toString();
   }

   public static String getFullNameDots(Script script) {
      String name = getFullName(script);
      name = name.substring(1);
      return name.replaceAll("/", ".");
   }

   public static Directory cloneDirectory(Directory baseDirectory, String newName, Directory parentDirectory) {
      log.debug("Clone Directory from {} to {}", baseDirectory.getName(), newName);

      Directory newDir = new Directory();
      newDir.setName(newName);
      newDir.setParent(parentDirectory);

      // Clone subDirs
      List<Directory> dirs = new ArrayList<>(baseDirectory.getDirectory().size());
      for (Directory directory : baseDirectory.getDirectory()) {
         dirs.add(ScriptsUtils.cloneDirectory(directory, directory.getName(), directory.getParent()));
      }
      newDir.getDirectory().addAll(dirs);

      // Clone scripts
      List<Script> scripts = new ArrayList<>(baseDirectory.getScript().size());
      for (Script script : baseDirectory.getScript()) {
         scripts.add(ScriptsUtils.cloneScript(script, script.getName(), script.getParent()));
      }
      newDir.getScript().addAll(scripts);

      return newDir;
   }

   public static Script cloneScript(Script baseScript, String newName, Directory parentDirectory) {
      log.debug("Clone Script from {} to {}", baseScript.getName(), newName);

      Script newScript = new Script();
      newScript.setName(newName);
      newScript.setParent(parentDirectory);

      List<DataFile> dataFiles = new ArrayList<>(baseScript.getDataFile().size());
      for (DataFile dataFile : baseScript.getDataFile()) {
         dataFiles.add(cloneDataFile(dataFile));
      }
      newScript.getDataFile().addAll(dataFiles);

      GlobalVariable newGV;
      List<GlobalVariable> globalVariables = new ArrayList<>(baseScript.getGlobalVariable().size());
      for (GlobalVariable globalVariable : baseScript.getGlobalVariable()) {
         newGV = new GlobalVariable();
         newGV.setConstantValue(globalVariable.getConstantValue());
         newGV.setName(globalVariable.getName());
         globalVariables.add(newGV);
      }
      newScript.getGlobalVariable().addAll(globalVariables);

      Step newStep;
      List<Step> steps = new ArrayList<>(baseScript.getStep().size());
      for (Step step : baseScript.getStep()) {
         newStep = new Step();
         newStep.setTemplateName(step.getTemplateName());
         newStep.setSessionName(step.getSessionName());
         newStep.setDestinationName(step.getDestinationName());
         newStep.setKind(step.getKind());
         newStep.setIterations(step.getIterations());
         newStep.setPauseSecsAfter(step.getPauseSecsAfter());
         steps.add(newStep);
      }
      newScript.getStep().addAll(steps);

      return newScript;
   }

   public static Step cloneStep(Step baseStep) {

      Step step = new Step();

      step.setKind(baseStep.getKind());

      step.setTemplateName(baseStep.getTemplateName());
      step.setSessionName(baseStep.getSessionName());
      step.setDestinationName(baseStep.getDestinationName());
      step.setIterations(baseStep.getIterations());
      step.setPauseSecsAfter(baseStep.getPauseSecsAfter());

      return step;
   }

   public static DataFile cloneDataFile(DataFile baseDataFile) {

      DataFile dataFile = new DataFile();

      dataFile.setDelimiter(baseDataFile.getDelimiter());
      dataFile.setFileName(baseDataFile.getFileName());
      dataFile.setVariableNames(baseDataFile.getVariableNames());
      dataFile.setVariablePrefix(baseDataFile.getVariablePrefix());

      return dataFile;
   }

   public static Step buildPauseStep(Integer delay) {
      Step step = new Step();
      step.setKind(StepKind.PAUSE);
      step.setPauseSecsAfter(delay);
      return step;
   }

   public static Step buildStep(String templateName,
                                String sessionName,
                                String destinationName,
                                Integer delay,
                                Integer iterations) {
      Step step = new Step();
      step.setKind(StepKind.REGULAR);
      step.setTemplateName(templateName);
      step.setSessionName(sessionName);
      step.setDestinationName(destinationName);
      step.setPauseSecsAfter(delay);
      step.setIterations(iterations);
      return step;
   }

   private ScriptsUtils() {
      // Pure Utility Class
   }
}

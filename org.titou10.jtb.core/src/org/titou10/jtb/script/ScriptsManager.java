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
package org.titou10.jtb.script;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.script.gen.DataFile;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.GlobalVariable;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.script.gen.Scripts;
import org.titou10.jtb.script.gen.Step;
import org.titou10.jtb.script.gen.StepKind;
import org.titou10.jtb.util.Constants;

/**
 * Manage all things related to "Scripts"
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class ScriptsManager {

   private static final Logger log               = LoggerFactory.getLogger(ScriptsManager.class);

   private static final String EMPTY_SCRIPT_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><scripts><directory name=\"Scripts\"/></scripts>";
   private static final String ENC               = "UTF-8";

   @Inject
   private ConfigManager       cm;

   private JAXBContext         jcScripts;
   private IFile               scriptsIFile;
   private Scripts             scripts;

   @PostConstruct
   private void initialize() throws Exception {
      log.debug("Initializing ScriptsManager");

      scriptsIFile = cm.getJtbProject().getFile(Constants.JTB_SCRIPT_CONFIG_FILE_NAME);

      // Load and Parse Scripts config file
      jcScripts = JAXBContext.newInstance(Scripts.class);
      if (!(scriptsIFile.exists())) {
         log.warn("Scripts file '{}' does not exist. Creating an new empty one.", Constants.JTB_SCRIPT_CONFIG_FILE_NAME);
         try {
            this.scriptsIFile.create(new ByteArrayInputStream(EMPTY_SCRIPT_FILE.getBytes(ENC)), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }
      }
      scripts = parseScriptsFile(this.scriptsIFile.getContents());
      // Bug correction: in version < v1.2.0 the empty script file was incorectly created (without the "Scripts" directory)
      if (scripts.getDirectory().isEmpty()) {
         log.warn("Invalid empty Scripts file encountered. Creating a new empty one");
         scriptsIFile.delete(true, null);
         this.scriptsIFile.create(new ByteArrayInputStream(EMPTY_SCRIPT_FILE.getBytes(ENC)), false, null);
         scripts = parseScriptsFile(scriptsIFile.getContents());
      }

      log.debug("ScriptsManager initialized");
   }

   // ---------
   // Scripts
   // ---------

   public void importConfig(InputStream is) throws JAXBException, CoreException, FileNotFoundException {
      log.debug("importConfig");

      Scripts newScripts = parseScriptsFile(is);
      if (newScripts == null) {
         return;
      }

      // TODO Merge instead of replace
      scripts = newScripts;

      // Write the variable file
      writeConfig();
   }

   // Write Variables File
   public void writeConfig() throws JAXBException, CoreException {
      log.info("scriptsWriteFile file '{}'", Constants.JTB_SCRIPT_CONFIG_FILE_NAME);

      Marshaller m = jcScripts.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(scripts, sw);

      // TODO Add the logic to temporarily save the previous file in case of crash while saving

      try (InputStream is = new ByteArrayInputStream(sw.toString().getBytes(ENC))) {
         scriptsIFile.setContents(is, false, false, null);
      } catch (IOException e) {
         log.error("IOException", e);
         return;
      }
   }

   public Scripts getScripts() {
      return scripts;
   }

   public int getNbScripts() {
      return scriptsCount(scripts.getDirectory());
   }

   // --------
   // Map Script Name -> Script
   // --------
   public Map<String, Script> getMapScripts() {
      Directory rootDirectory = scripts.getDirectory().get(0);
      Map<String, Script> mapScripts = new HashMap<>();
      buildScriptsMap(mapScripts, rootDirectory, "");
      return mapScripts;
   }

   private Map<String, Script> buildScriptsMap(Map<String, Script> res, Directory baseDir, String prefix) {
      for (Script s : baseDir.getScript()) {
         res.put(prefix + "/" + s.getName(), s);
      }
      for (Directory d : baseDir.getDirectory()) {
         buildScriptsMap(res, d, prefix + "/" + d.getName());
      }
      return res;
   }

   // --------
   // Manage Displays
   // --------

   public String getFullNameDots(Directory directory) {

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

   public String getFullName(Script script) {

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

   public String getFullNameDots(Script script) {
      String name = getFullName(script);
      if (name.startsWith("/")) {
         name = name.substring(1);
      }
      return name.replaceAll("/", ".");
   }

   // --------
   // Builders
   // --------

   public Step buildPauseStep(Integer delay) {
      Step step = new Step();
      step.setKind(StepKind.PAUSE);
      step.setPauseSecsAfter(delay);
      return step;
   }

   public Step buildStep() {
      Step step = new Step();
      step.setKind(StepKind.REGULAR);
      step.setPauseSecsAfter(0);
      step.setIterations(1);
      return step;
   }

   public String buildDataFileDislayName(DataFile dataFile) {
      if (dataFile == null) {
         return "<unknown>";
      }

      StringBuilder sb = new StringBuilder(128);
      sb.append("[");
      sb.append(dataFile.getVariablePrefix());
      sb.append("] ");
      sb.append(dataFile.getFileName());
      return sb.toString();
   }

   public Directory cloneDirectory(Directory baseDirectory, String newName, Directory parentDirectory) {
      log.debug("Clone Directory from {} to {}", baseDirectory.getName(), newName);

      Directory newDir = new Directory();
      newDir.setName(newName);
      newDir.setParent(parentDirectory);

      // Clone subDirs
      List<Directory> dirs = new ArrayList<>(baseDirectory.getDirectory().size());
      for (Directory directory : baseDirectory.getDirectory()) {
         dirs.add(cloneDirectory(directory, directory.getName(), newDir));
      }
      newDir.getDirectory().addAll(dirs);

      // Clone scripts
      List<Script> scripts = new ArrayList<>(baseDirectory.getScript().size());
      for (Script script : baseDirectory.getScript()) {
         scripts.add(cloneScript(script, script.getName(), newDir));
      }
      newDir.getScript().addAll(scripts);

      return newDir;
   }

   public Script cloneScript(Script baseScript, String newName, Directory parentDirectory) {
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
         newStep.setTemplateDirectory(step.getTemplateDirectory());
         newStep.setSessionName(step.getSessionName());
         newStep.setDestinationName(step.getDestinationName());
         newStep.setVariablePrefix(step.getVariablePrefix());
         newStep.setPayloadDirectory(step.getPayloadDirectory());
         newStep.setKind(step.getKind());
         newStep.setIterations(step.getIterations());
         newStep.setPauseSecsAfter(step.getPauseSecsAfter());
         steps.add(newStep);
      }
      newScript.getStep().addAll(steps);

      return newScript;
   }

   public Step cloneStep(Step baseStep) {

      Step step = new Step();

      step.setKind(baseStep.getKind());

      step.setTemplateName(baseStep.getTemplateName());
      step.setSessionName(baseStep.getSessionName());
      step.setDestinationName(baseStep.getDestinationName());
      step.setVariablePrefix(baseStep.getVariablePrefix());
      step.setPayloadDirectory(baseStep.getPayloadDirectory());
      step.setIterations(baseStep.getIterations());
      step.setPauseSecsAfter(baseStep.getPauseSecsAfter());

      String templateDirectory = baseStep.getTemplateDirectory();
      step.setTemplateDirectory(templateDirectory == null || templateDirectory.isEmpty() ? Constants.JTB_TEMPLATE_CONFIG_FOLDER_NAME
               : templateDirectory);

      return step;
   }

   public DataFile cloneDataFile(DataFile baseDataFile) {

      DataFile dataFile = new DataFile();

      dataFile.setDelimiter(baseDataFile.getDelimiter());
      dataFile.setFileName(baseDataFile.getFileName());
      dataFile.setVariableNames(baseDataFile.getVariableNames());
      dataFile.setVariablePrefix(baseDataFile.getVariablePrefix());
      dataFile.setScriptLevel(baseDataFile.isScriptLevel());
      dataFile.setCharset(baseDataFile.getCharset());

      return dataFile;
   }

   // -------
   // Helpers
   // -------
   public DataFile findDataFileByVariablePrefix(Script script, String variablePrefix) {
      for (DataFile dataFile : script.getDataFile()) {
         if (dataFile.getVariablePrefix().equals(variablePrefix)) {
            return dataFile;
         }
      }
      return null;
   }

   private int scriptsCount(List<Directory> dirs) {
      int nb = 0;
      for (Directory directory : dirs) {
         nb += directory.getScript().size();
         nb += scriptsCount(directory.getDirectory());
      }
      return nb;
   }

   // Parse Script File
   private Scripts parseScriptsFile(InputStream is) throws JAXBException {
      log.debug("Parsing Script file '{}'", Constants.JTB_SCRIPT_CONFIG_FILE_NAME);

      Unmarshaller u = jcScripts.createUnmarshaller();
      u.setListener(new ScriptJAXBParentListener());
      return (Scripts) u.unmarshal(is);
   }

}

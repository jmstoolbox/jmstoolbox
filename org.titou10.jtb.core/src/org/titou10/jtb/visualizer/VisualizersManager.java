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
package org.titou10.jtb.visualizer;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Singleton;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.swt.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.visualizer.gen.Visualizer;
import org.titou10.jtb.visualizer.gen.VisualizerKind;
import org.titou10.jtb.visualizer.gen.VisualizerMessageType;
import org.titou10.jtb.visualizer.gen.Visualizers;

/**
 * Manage all things related to "Visualizers"
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class VisualizersManager {

   private static final Logger                      log                    = LoggerFactory.getLogger(VisualizersManager.class);

   private static final String                      EMPTY_VISUALIZER_FILE  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><visualizers></visualizers>";
   private static final String                      ENC                    = "UTF-8";
   private static final List<VisualizerMessageType> COL_TEXT               = Collections.singletonList(VisualizerMessageType.TEXT);
   private static final List<VisualizerMessageType> COL_BYTES              = Collections.singletonList(VisualizerMessageType.BYTES);
   private static final List<VisualizerMessageType> COL_ALL                = Arrays.asList(VisualizerMessageType.TEXT,
                                                                                           VisualizerMessageType.BYTES);
   private static final String                      JS_LANGUAGE            = "nashorn";
   private static final String                      JS_PARAM_VISUALIZER    = "jtb_visualizer";
   private static final String                      JS_PARAM_JMS_TYPE      = "jtb_jmsMessageType";
   private static final String                      JS_PARAM_PAYLOAD_TEXT  = "jtb_payloadText";
   private static final String                      JS_PARAM_PAYLOAD_BYTES = "jtb_payloadBytes";
   private static final String                      JS_PARAM_PAYLOAD_MAP   = "jtb_payloadMap";

   private JAXBContext                              jcVisualizers;
   private IFile                                    visualizersIFile;
   private Visualizers                              visualizersDef;

   private ScriptEngine                             scriptEngine;
   private Compilable                               compilingEngine;
   private VisualizerScriptsHook                    visualizerScriptsHook;

   private List<Visualizer>                         visualizers;
   private Map<JTBMessageType, String[]>            visualizersPerJTBMessageType;

   private Map<String, CompiledScript>              mapCompiledScripts;

   public int initialize(IFile vIFile) throws Exception {
      log.debug("Initializing VisualizersManager");

      visualizersIFile = vIFile;

      // Load and Parse Visualizers config file
      jcVisualizers = JAXBContext.newInstance(Visualizers.class);
      if (!(visualizersIFile.exists())) {
         log.warn("Visualizers file '{}' does not exist. Creating an new empty one.", Constants.JTB_VISUALIZER_FILE_NAME);
         try {
            this.visualizersIFile.create(new ByteArrayInputStream(EMPTY_VISUALIZER_FILE.getBytes(ENC)), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }
      }
      visualizersDef = parseVisualizersFile(this.visualizersIFile.getContents());

      // Initialize script engine
      mapCompiledScripts = new HashMap<>();
      scriptEngine = new ScriptEngineManager().getEngineByName(JS_LANGUAGE);
      compilingEngine = (Compilable) scriptEngine;
      visualizerScriptsHook = new VisualizerScriptsHook(this);

      // Build list of visualizers
      reload();

      log.debug("VisualizersManager initialized");
      return visualizers.size();
   }

   // ---------
   // Visualizers
   // ---------

   public boolean importVisualizers(String visualizerFileName) throws JAXBException, CoreException, FileNotFoundException {
      log.debug("importVisualizers : {}", visualizerFileName);

      // Try to parse the given file
      File f = new File(visualizerFileName);
      Visualizers newVisualizers = parseVisualizersFile(new FileInputStream(f));

      if (newVisualizers == null) {
         return false;
      }

      // Merge visualizers
      List<Visualizer> mergedVisualizers = new ArrayList<>(visualizersDef.getVisualizer());
      for (Visualizer v : newVisualizers.getVisualizer()) {
         // If a visualizer with the same name exist, replace it
         for (Visualizer temp : visualizersDef.getVisualizer()) {
            if (temp.getName().equals(v.getName())) {
               mergedVisualizers.remove(temp);
            }
         }
         mergedVisualizers.add(v);
      }
      visualizersDef.getVisualizer().clear();
      visualizersDef.getVisualizer().addAll(mergedVisualizers);

      // Write the visualizers file
      writeVisualizersFile();

      // load visualizers
      reload();

      return true;
   }

   public void exportVisualizers(String visualizerFileName) throws IOException, CoreException {
      log.debug("exportVisualizer : {}", visualizerFileName);
      Files.copy(visualizersIFile.getContents(), Paths.get(visualizerFileName), StandardCopyOption.REPLACE_EXISTING);
   }

   public boolean saveVisualizers() throws JAXBException, CoreException {
      log.debug("visualizerSave");

      visualizersDef.getVisualizer().clear();
      for (Visualizer v : visualizers) {
         if (v.isSystem()) {
            continue;
         }
         visualizersDef.getVisualizer().add(v);
      }
      writeVisualizersFile();

      reload();

      return true;
   }

   public void reload() {
      visualizers = new ArrayList<>();
      visualizers.addAll(visualizersDef.getVisualizer());
      visualizers.addAll(buildSystemVisualizers());

      Collections.sort(visualizers, (Visualizer o1, Visualizer o2) -> {
         // System variables first
         boolean sameSystem = o1.isSystem() == o2.isSystem();
         if (!(sameSystem)) {
            if (o1.isSystem()) {
               return -1;
            } else {
               return 1;
            }
         }

         return o1.getName().compareTo(o2.getName());
      });

      // Build a map of visualiser names per JTBMessageType
      Map<JTBMessageType, List<String>> map = new HashMap<>();

      for (Visualizer visualizer : visualizers) {
         for (VisualizerMessageType vmt : visualizer.getTargetMsgType()) {
            JTBMessageType jtbMT = JTBMessageType.valueOf(vmt.name());
            List<String> lv = map.get(jtbMT);
            if (lv == null) {
               lv = new ArrayList<>();
               map.put(jtbMT, lv);
            }
            lv.add(visualizer.getName());
         }
      }
      visualizersPerJTBMessageType = new HashMap<>();
      for (Entry<JTBMessageType, List<String>> e : map.entrySet()) {
         visualizersPerJTBMessageType.put(e.getKey(), e.getValue().toArray(new String[e.getValue().size()]));
      }

      mapCompiledScripts.clear();
   }

   // ---------------
   // Getters/Helpers
   // ---------------

   public List<Visualizer> getVisualisers() {
      return visualizers;
   }

   // For now, the user can not create all kinds
   public String[] getVisualizerKindsBuildable() {
      String[] vkNames = new String[3];
      vkNames[0] = VisualizerKind.OS_EXTENSION.name();
      vkNames[1] = VisualizerKind.EXTERNAL_SCRIPT.name();
      vkNames[2] = VisualizerKind.INLINE_SCRIPT.name();
      return vkNames;
   }

   public String buildDescription(Visualizer visualizer) {
      StringBuilder sb = new StringBuilder(128);

      switch (visualizer.getKind()) {
         case BUILTIN:
            break;

         case INLINE_SCRIPT:
            sb.append("Language='");
            sb.append(visualizer.getLanguage());
            sb.append("'");
            break;

         case OS_EXTENSION:
            sb.append("Delegates to OS extension '");
            sb.append(visualizer.getExtension());
            sb.append("'");
            break;

         case EXTERNAL_EXEC:
            sb.append("Command name='");
            sb.append(visualizer.getFileName());
            sb.append("'");
            break;

         case EXTERNAL_SCRIPT:
            sb.append("Language='");
            sb.append(visualizer.getLanguage());
            sb.append("'. Script name='");
            sb.append(visualizer.getFileName());
            sb.append("'");
            break;
      }

      return sb.toString();
   }

   // --------
   // Finders
   // --------

   public String[] getVizualisersNamesForMessageType(JTBMessageType jtbMessageType) {
      return visualizersPerJTBMessageType.get(jtbMessageType);
   }

   public int findIndexVisualizerForType(String[] visualizers, JTBMessageType jtbMessageType, byte[] payloadBytes) {

      switch (jtbMessageType) {
         case TEXT:
            // TextMessages: Text viewer
            return findPositionInArray(visualizers, "Text");

         case MAP:
            // TextMessages: Text viewer
            return findPositionInArray(visualizers, "Text");

         case BYTES:
            // Empty BytesMessages: Other
            if (payloadBytes == null) {
               return findPositionInArray(visualizers, "Other");
            }

            if (payloadBytes.length >= 4) {
               String pref = new String(Arrays.copyOfRange(payloadBytes, 0, 4));
               if (pref.equals("%PDF")) {
                  return findPositionInArray(visualizers, "PDF");
               }
            }
            if (payloadBytes.length >= 2) {
               String pref = new String(Arrays.copyOfRange(payloadBytes, 0, 2));
               if (pref.equals("PK")) {
                  return findPositionInArray(visualizers, "ZIP");
               }
            }
            break;

         default:
            break;
      }

      // All Other: unknown
      return findPositionInArray(visualizers, "Other");
   }

   // --------
   // Launchers
   // --------
   public CompiledScript compileScript(String source) throws ScriptException {
      Compilable compilingEngine = (Compilable) scriptEngine;
      return compilingEngine.compile(source);
   }

   public void launchVisualizer(String name,
                                JTBMessageType jtbMessageType,
                                String payloadText,
                                byte[] payloadBytes,
                                Map<String, Object> payloadMap) throws Exception {
      log.debug("launchVisualizer name: {} type={}", name, jtbMessageType);

      Visualizer visualizer = getVizualiserFromName(name);

      switch (visualizer.getKind()) {
         case OS_EXTENSION:
            switch (jtbMessageType) {
               case TEXT:
                  launchExternalExtension(visualizer.getExtension(), payloadText);
                  break;
               case BYTES:
                  launchExternalExtension(visualizer.getExtension(), payloadBytes);
                  break;
               case MAP:
                  launchExternalExtension(visualizer.getExtension(), payloadMap);
                  break;
               default:
                  break;
            }
            break;

         case EXTERNAL_SCRIPT:
         case INLINE_SCRIPT:
            executeScript(visualizer, jtbMessageType, payloadText, payloadBytes, payloadMap);
            break;

         case EXTERNAL_EXEC:
            break;

         case BUILTIN:
            break;

         default:
            break;
      }
   }

   private void executeScript(Visualizer visualizer,
                              JTBMessageType jtbMessageType,
                              String payloadText,
                              byte[] payloadBytes,
                              Map<String, Object> payloadMap) throws Exception {
      log.debug("launchScript");

      // Get and comiple the Script
      CompiledScript cs;
      if (visualizer.getKind() == VisualizerKind.EXTERNAL_SCRIPT) {
         FileReader dfr = new FileReader(visualizer.getFileName());
         cs = compilingEngine.compile(dfr);
      } else {
         // Allow caching only for inline scripts as external scripts may change without JTB knowing it
         cs = mapCompiledScripts.get(visualizer.getName());
         if (cs == null) {
            cs = compilingEngine.compile(visualizer.getSource());
            mapCompiledScripts.put(visualizer.getName(), cs);
         }
      }

      // Set parameters
      SimpleBindings global = new SimpleBindings();
      global.put(JS_PARAM_VISUALIZER, visualizerScriptsHook);
      global.put(JS_PARAM_JMS_TYPE, jtbMessageType.name());
      if (payloadText != null) {
         global.put(JS_PARAM_PAYLOAD_TEXT, payloadText);
      }
      if (payloadBytes != null) {
         global.put(JS_PARAM_PAYLOAD_BYTES, payloadBytes);
      }
      if ((payloadMap != null) && (!payloadMap.isEmpty())) {
         global.put(JS_PARAM_PAYLOAD_MAP, payloadMap);
      }

      // Call the script
      cs.eval(global);
   }

   public void launchExternalExtension(String extension, String payloadText) throws IOException {
      log.debug("launchExternalExtension");

      File temp = File.createTempFile("jmstoolbox_", extension);
      temp.deleteOnExit();

      if ((payloadText == null) || (payloadText.isEmpty())) {
         log.debug("launchVisualizer. No visualisation: payloadText is empty or null");
         return;
      }

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
         bw.write(payloadText);
      }

      executeExternalExtension(extension, temp);
   }

   public void launchExternalExtension(String extension, byte[] payloadBytes) throws IOException {
      log.debug("launchExternalExtension");

      File temp = File.createTempFile("jmstoolbox_", extension);
      temp.deleteOnExit();
      if ((payloadBytes == null) || (payloadBytes.length == 0)) {
         log.debug("launchVisualizer. No visualisation: payloadBytes is empty or null");
         return;
      }
      try (FileOutputStream fos = new FileOutputStream(temp)) {
         fos.write(payloadBytes);
      }
      executeExternalExtension(extension, temp);
   }

   public void launchExternalExtension(String extension, Map<String, Object> payloadMap) throws IOException {
      log.debug("launchExternalExtension");

      File temp = File.createTempFile("jmstoolbox_", extension);
      temp.deleteOnExit();

      if ((payloadMap == null) || (payloadMap.isEmpty())) {
         log.debug("launchVisualizer. No visualisation: payloadMap is empty or null");
         return;
      }
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
         for (Entry<String, Object> e : payloadMap.entrySet()) {
            bw.write(e.getKey() + " = " + e.getValue());
         }
      }
      executeExternalExtension(extension, temp);
   }

   private void executeExternalExtension(String extension, File contentFile) {
      log.debug("launchExternalExtension");

      if (extension == null) {
         log.debug("No extension specified. Let the OS decide");
         Program.launch(contentFile.getAbsolutePath());
         return;
      }

      Program p = Program.findProgram(extension);
      log.debug("Program found for extension='{}' : '{}'", extension, p);
      if (p == null) {
         Program.launch(contentFile.getAbsolutePath());
         return;
      } else {
         p.execute(contentFile.getAbsolutePath());
      }
   }
   // --------
   // Builders
   // --------

   private List<Visualizer> buildSystemVisualizers() {
      List<Visualizer> list = new ArrayList<>();

      list.add(buildOSExtension(true, "Text", ".txt", COL_TEXT));
      list.add(buildOSExtension(true, "xml ", ".xml", COL_TEXT));
      list.add(buildOSExtension(true, "Json ", ".json", COL_TEXT));
      list.add(buildOSExtension(true, "ZIP", ".zip", COL_BYTES));
      list.add(buildOSExtension(true, "PDF", ".pdf", COL_BYTES));
      list.add(buildOSExtension(true, "Other", ".unknown", COL_ALL));
      return list;
   }

   public Visualizer buildOSExtension(boolean system, String name, String extension, List<VisualizerMessageType> listMessageType) {
      Visualizer v = new Visualizer();
      v.setKind(VisualizerKind.OS_EXTENSION);
      v.setSystem(system);
      v.setName(name);
      v.setExtension(extension);
      v.getTargetMsgType().addAll(listMessageType);

      return v;
   }

   public Visualizer buildInlineScript(boolean system, String name, String source, List<VisualizerMessageType> listMessageType) {
      Visualizer v = new Visualizer();
      v.setKind(VisualizerKind.INLINE_SCRIPT);
      v.setSystem(system);
      v.setName(name);
      v.setLanguage(JS_LANGUAGE);
      v.setSource(source);
      v.getTargetMsgType().addAll(listMessageType);

      return v;
   }

   public Visualizer buildExternalScript(boolean system,
                                         String name,
                                         String fileName,
                                         List<VisualizerMessageType> listMessageType) {
      Visualizer v = new Visualizer();
      v.setKind(VisualizerKind.EXTERNAL_SCRIPT);
      v.setSystem(system);
      v.setName(name);
      v.setLanguage(JS_LANGUAGE);
      v.setFileName(fileName);
      v.getTargetMsgType().addAll(listMessageType);

      return v;
   }

   // -------
   // Helpers
   // -------

   // Parse Visualizers File into Visualizer Object
   private Visualizers parseVisualizersFile(InputStream is) throws JAXBException {
      log.debug("Parsing Visualizer file '{}'", Constants.JTB_VISUALIZER_FILE_NAME);

      Unmarshaller u = jcVisualizers.createUnmarshaller();
      return (Visualizers) u.unmarshal(is);
   }

   // Write Visualizers File
   private void writeVisualizersFile() throws JAXBException, CoreException {
      log.info("Writing Visualizers file '{}'", Constants.JTB_VISUALIZER_FILE_NAME);

      Marshaller m = jcVisualizers.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(visualizersDef, sw);

      // TODO Add the logic to temporarily save the previous file in case of crash while saving

      try {
         InputStream is = new ByteArrayInputStream(sw.toString().getBytes(ENC));
         visualizersIFile.setContents(is, false, false, null);
      } catch (UnsupportedEncodingException e) {
         // Impossible
         log.error("UnsupportedEncodingException", e);
         return;
      }
   }

   private Visualizer getVizualiserFromName(String name) {
      return visualizers.stream().filter(v -> v.getName().equals(name)).findFirst().orElse(null);
   }

   private int findPositionInArray(String[] visualizers, String name) {
      for (int i = 0; i < visualizers.length; i++) {
         if (visualizers[i].equals(name)) {
            return i;
         }
      }
      for (int i = 0; i < visualizers.length; i++) {
         if (visualizers[i].equals("Other")) {
            return i;
         }
      }
      return 0;
   }

}

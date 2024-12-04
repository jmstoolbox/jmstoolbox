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
package org.titou10.jtb.visualizer;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Matcher;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

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
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;
import org.titou10.jtb.visualizer.dialog.VisualizerShowLogDialog;
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

   private static final Logger                      log                          = LoggerFactory
            .getLogger(VisualizersManager.class);

   private static final String                      PAYLOAD_FILENAME_CHARS       = "p";
   public static final String                       PAYLOAD_FILENAME_PLACEHOLDER = "${" + PAYLOAD_FILENAME_CHARS + "}";
   private static final String                      PAYLOAD_FILENAME_REGEXP      = "\\$\\{" + PAYLOAD_FILENAME_CHARS + "\\}";
   private static final String                      JMS_MSG_TYPE_CHARS           = "t";
   public static final String                       JMS_MSG_TYPE_PLACEHOLDER     = "${" + JMS_MSG_TYPE_CHARS + "}";
   private static final String                      JMS_MSG_TYPE_REGEXP          = "\\$\\{" + JMS_MSG_TYPE_CHARS + "\\}";

   private static final String                      EMPTY_VISUALIZER_FILE        = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><visualizers></visualizers>";
   private static final String                      ENC                          = "UTF-8";

   private static final String                      JS_LANGUAGE                  = "JavaScript";
   private static final String                      JS_PARAM_VISUALIZER          = "jtb_visualizer";
   private static final String                      JS_PARAM_JMS_TYPE            = "jtb_jmsMessageType";
   private static final String                      JS_PARAM_PAYLOAD_TEXT        = "jtb_payloadText";
   private static final String                      JS_PARAM_PAYLOAD_BYTES       = "jtb_payloadBytes";
   private static final String                      JS_PARAM_PAYLOAD_MAP         = "jtb_payloadMap";

   private static final List<VisualizerMessageType> COL_TEXT                     = List.of(VisualizerMessageType.TEXT);
   private static final List<VisualizerMessageType> COL_BYTES                    = List.of(VisualizerMessageType.BYTES);
   private static final List<VisualizerMessageType> COL_ALL                      = List
            .of(VisualizerMessageType.TEXT, VisualizerMessageType.BYTES, VisualizerMessageType.MAP);
   private static final String[]                    VK_NAMES_USER                = { VisualizerKind.OS_EXTENSION.name(),
                                                                                     VisualizerKind.EXTERNAL_SCRIPT.name(),
                                                                                     VisualizerKind.INLINE_SCRIPT.name(),
                                                                                     VisualizerKind.EXTERNAL_COMMAND.name() };

   public static final VisualizerComparator         VISUALIZER_COMPARATOR        = new VisualizerComparator();

   @Inject
   private ConfigManager                            cm;

   private JAXBContext                              jcVisualizers;
   private IFile                                    visualizersIFile;
   private Visualizers                              visualizersDef;

   private ScriptEngine                             scriptEngine;
   private Compilable                               compilingEngine;
   private VisualizerScriptsHook                    visualizerScriptsHook;

   private List<Visualizer>                         visualizers;
   private Map<JTBMessageType, String[]>            visualizersPerJTBMessageType;

   private Map<String, CompiledScript>              mapCompiledScripts;

   @PostConstruct
   private void initialize() throws Exception {
      log.debug("Initializing VisualizersManager");

      visualizersIFile = cm.getJtbProject().getFile(Constants.JTB_VISUALIZER_CONFIG_FILE_NAME);

      // Load and Parse Visualizers config file
      jcVisualizers = JAXBContext.newInstance(Visualizers.class);
      if (!(visualizersIFile.exists())) {
         log.warn("Visualizers file '{}' does not exist. Creating a new empty one.", Constants.JTB_VISUALIZER_CONFIG_FILE_NAME);
         try {
            this.visualizersIFile.create(new ByteArrayInputStream(EMPTY_VISUALIZER_FILE.getBytes(ENC)), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }
      }
      visualizersDef = parseVisualizersFile(this.visualizersIFile.getContents());

      // Initialize script engine
      mapCompiledScripts = new HashMap<>();

      // https://github.com/oracle/graaljs/blob/master/docs/user/ScriptEngine.md
      scriptEngine = new ScriptEngineManager().getEngineByName(JS_LANGUAGE);
      Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
      bindings.put("polyglot.js.allowHostAccess", true);
      bindings.put("polyglot.js.allowHostClassLookup", (Predicate<String>) s -> true);

      compilingEngine = (Compilable) scriptEngine;
      visualizerScriptsHook = new VisualizerScriptsHook(this);

      // Build list of visualizers
      reloadConfig();

      log.debug("VisualizersManager initialized");
   }

   // ---------
   // Visualizers
   // ---------

   public void importConfig(InputStream is) throws JAXBException, CoreException, FileNotFoundException {
      log.debug("importConfig");

      Visualizers newVisualizers = parseVisualizersFile(is);

      if (newVisualizers == null) {
         return;
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
      reloadConfig();
   }

   public void saveConfig() throws JAXBException, CoreException {
      log.debug("saveConfig");

      visualizersDef.getVisualizer().clear();
      for (Visualizer v : visualizers) {
         if (v.isSystem()) {
            continue;
         }
         visualizersDef.getVisualizer().add(v);
      }
      writeVisualizersFile();

      reloadConfig();
   }

   public void reloadConfig() {
      visualizers = new ArrayList<>();
      visualizers.addAll(visualizersDef.getVisualizer());
      visualizers.addAll(buildSystemVisualizers());

      Collections.sort(visualizers, VISUALIZER_COMPARATOR);

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
      return VK_NAMES_USER;
   }

   public String buildDescription(Visualizer visualizer) {
      StringBuilder sb = new StringBuilder(128);

      switch (visualizer.getKind()) {
         case BUILTIN:
            break;

         case INLINE_SCRIPT:
            sb.append("Language: '");
            sb.append(visualizer.getLanguage());
            sb.append("'");
            break;

         case OS_EXTENSION:
            sb.append("Delegates to OS as extension '");
            sb.append(visualizer.getExtension());
            sb.append("'");
            break;

         case EXTERNAL_COMMAND:
            sb.append("Command name: '");
            sb.append(visualizer.getFileName());
            sb.append("'");
            break;

         case EXTERNAL_SCRIPT:
            sb.append("Language: '");
            sb.append(visualizer.getLanguage());
            sb.append("' Script file name: '");
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

   public void launchVisualizer(Shell shell,
                                String name,
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
            executeScript(shell, visualizer, jtbMessageType, payloadText, payloadBytes, payloadMap);
            break;

         case EXTERNAL_COMMAND:
            executeExternalCommand(visualizer, jtbMessageType, payloadText, payloadBytes, payloadMap);
            break;

         case BUILTIN:
            break;

         default:
            break;
      }
   }

   private void executeScript(Shell shell,
                              Visualizer visualizer,
                              JTBMessageType jtbMessageType,
                              String payloadText,
                              byte[] payloadBytes,
                              Map<String, Object> payloadMap) throws Exception {
      log.debug("executeScript");

      // Get and compile the Script
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
      if (Utils.isNotEmpty(payloadMap)) {
         global.put(JS_PARAM_PAYLOAD_MAP, payloadMap);
      }

      Writer sysout;
      if (visualizer.isShowScriptLogs()) {
         // Redirect output from a Log Viewer Dialog
         VisualizerShowLogDialog d = new VisualizerShowLogDialog(shell);
         d.setBlockOnOpen(false);
         d.open();
         Text textlog = d.getTextLogs();
         sysout = new VisualizersTextAreaWriter(textlog);
      } else {
         // Redirect output from the Script to JTB logs
         sysout = new VisualizersLogWriter(visualizer.getName());
      }
      scriptEngine.getContext().setWriter(sysout);
      scriptEngine.getContext().setErrorWriter(sysout);

      // Call the script
      try {
         cs.eval(global);
      } catch (Exception e) {
         e.printStackTrace(new PrintWriter(sysout, true));
         throw e;
      }
   }

   public void launchExternalExtension(String extension, String payloadText) throws IOException {
      log.debug("launchExternalExtension - Text");

      File temp = File.createTempFile("jmstoolbox_", extension);
      temp.deleteOnExit();

      if (Utils.isEmpty(payloadText)) {
         log.debug("launchVisualizer. No visualisation: payloadText is empty or null");
         return;
      }

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
         bw.write(payloadText);
      }

      executeExternalExtension(extension, temp);
   }

   public void launchExternalExtension(String extension, byte[] payloadBytes) throws IOException {
      log.debug("launchExternalExtension - Bytes");

      File temp = File.createTempFile("jmstoolbox_", extension);
      temp.deleteOnExit();
      if (Utils.isEmpty(payloadBytes)) {
         log.debug("launchVisualizer. No visualisation: payloadBytes is empty or null");
         return;
      }
      try (FileOutputStream fos = new FileOutputStream(temp)) {
         fos.write(payloadBytes);
      }
      executeExternalExtension(extension, temp);
   }

   public void launchExternalExtension(String extension, Map<String, Object> payloadMap) throws IOException {
      log.debug("launchExternalExtension - Map");

      File temp = File.createTempFile("jmstoolbox_", extension);
      temp.deleteOnExit();

      if (Utils.isEmpty(payloadMap)) {
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
      log.debug("executeExternalExtension");

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

   private void executeExternalCommand(Visualizer visualizer,
                                       JTBMessageType jtbMessageType,
                                       String payloadText,
                                       byte[] payloadBytes,
                                       Map<String, Object> payloadMap) throws Exception {
      log.debug("executeExternalCommand");

      // Create Temporay File with payload

      File temp = File.createTempFile("jmstoolbox_", ".tmp");
      temp.deleteOnExit();

      // TODO DF: redundant with methods above
      switch (jtbMessageType) {
         case TEXT:
            if (Utils.isEmpty(payloadText)) {
               log.debug("launchVisualizer. No visualisation: payloadText is empty or null");
               return;
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
               bw.write(payloadText);
            }
            break;

         case BYTES:
            if (Utils.isEmpty(payloadBytes)) {
               log.debug("launchVisualizer. No visualisation: payloadBytes is empty or null");
               return;
            }
            try (FileOutputStream fos = new FileOutputStream(temp)) {
               fos.write(payloadBytes);
            }
         case MAP:
            if (Utils.isEmpty(payloadMap)) {
               log.debug("launchVisualizer. No visualisation: payloadMap is empty or null");
               return;
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
               for (Entry<String, Object> e : payloadMap.entrySet()) {
                  bw.write(e.getKey() + " = " + e.getValue());
               }
            }
            break;
         default:
            break;
      }

      // Replace parameters
      String command = visualizer.getFileName();
      command = command.replaceAll(PAYLOAD_FILENAME_REGEXP, Matcher.quoteReplacement(temp.getAbsolutePath()));
      command = command.replaceAll(JMS_MSG_TYPE_REGEXP, jtbMessageType.name());

      // Execute command
      log.debug("Execute external command '{}'", command);
      // Java 21
      // Runtime rt = Runtime.getRuntime();
      // rt.exec(command);
      ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
      processBuilder.start();
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
      v.setShowScriptLogs(false);
      v.setName(name);
      v.setExtension(extension);
      v.getTargetMsgType().addAll(listMessageType);

      return v;
   }

   public Visualizer buildExternalCommand(boolean system,
                                          String name,
                                          String commandName,
                                          List<VisualizerMessageType> listMessageType) {
      Visualizer v = new Visualizer();
      v.setKind(VisualizerKind.EXTERNAL_COMMAND);
      v.setSystem(system);
      v.setName(name);
      v.setFileName(commandName);
      v.getTargetMsgType().addAll(listMessageType);

      return v;
   }

   public Visualizer buildInlineScript(boolean system,
                                       boolean showScriptLogs,
                                       String name,
                                       String source,
                                       List<VisualizerMessageType> listMessageType) {
      Visualizer v = new Visualizer();
      v.setKind(VisualizerKind.INLINE_SCRIPT);
      v.setSystem(system);
      v.setShowScriptLogs(showScriptLogs);
      v.setName(name);
      v.setLanguage(JS_LANGUAGE);
      v.setSource(source);
      v.getTargetMsgType().addAll(listMessageType);

      return v;
   }

   public Visualizer buildExternalScript(boolean system,
                                         boolean showScriptLogs,
                                         String name,
                                         String fileName,
                                         List<VisualizerMessageType> listMessageType) {
      Visualizer v = new Visualizer();
      v.setKind(VisualizerKind.EXTERNAL_SCRIPT);
      v.setSystem(system);
      v.setShowScriptLogs(showScriptLogs);
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
      log.debug("Parsing Visualizer file '{}'", Constants.JTB_VISUALIZER_CONFIG_FILE_NAME);

      Unmarshaller u = jcVisualizers.createUnmarshaller();
      return (Visualizers) u.unmarshal(is);
   }

   // Write Visualizers File
   private void writeVisualizersFile() throws JAXBException, CoreException {
      log.info("Writing Visualizers file '{}'", Constants.JTB_VISUALIZER_CONFIG_FILE_NAME);

      Marshaller m = jcVisualizers.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(visualizersDef, sw);

      // TODO Add the logic to temporarily save the previous file in case of crash while saving

      try (InputStream is = new ByteArrayInputStream(sw.toString().getBytes(ENC))) {
         visualizersIFile.setContents(is, false, false, null);
      } catch (IOException e) {
         log.error("IOException", e);
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

   public final static class VisualizerComparator implements Comparator<Visualizer> {

      @Override
      public int compare(Visualizer o1, Visualizer o2) {
         // System visualizers first
         boolean sameSystem = o1.isSystem() == o2.isSystem();
         if (!(sameSystem)) {
            if (o1.isSystem()) {
               return -1;
            } else {
               return 1;
            }
         }
         return o1.getName().compareTo(o2.getName());
      }
   }

}

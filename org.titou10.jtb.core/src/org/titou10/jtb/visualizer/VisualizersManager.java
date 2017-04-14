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
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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
import org.titou10.jtb.visualizer.gen.VisualizerMessageType;
import org.titou10.jtb.visualizer.gen.VisualizerSourceKind;
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

   private static final Logger                      log                   = LoggerFactory.getLogger(VisualizersManager.class);

   private static final String                      EMPTY_VISUALIZER_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><visualizers></visualizers>";
   private static final String                      ENC                   = "UTF-8";
   private static final List<VisualizerMessageType> COL_TEXT              = Collections.singletonList(VisualizerMessageType.TEXT);
   private static final List<VisualizerMessageType> COL_BYTES             = Collections.singletonList(VisualizerMessageType.BYTES);
   private static final List<VisualizerMessageType> COL_ALL               = Arrays.asList(VisualizerMessageType.TEXT,
                                                                                          VisualizerMessageType.BYTES);

   private JAXBContext                              jcVisualizers;
   private IFile                                    visualizersIFile;
   private Visualizers                              visualizersDef;

   private ScriptEngine                             scriptEngine;

   private List<Visualizer>                         visualizers;
   private Map<JTBMessageType, String[]>            visualizersPerJTBMessageType;

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

      // Build list of visualizers
      reload();

      // Initialize script engine
      scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");

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

      return true;
   }

   public void reload() {
      visualizers = new ArrayList<>();
      visualizers.addAll(visualizersDef.getVisualizer());
      visualizers.addAll(buildSystemVisualizers());

      // Build a map of visualiser names per JTBMessageType
      Map<JTBMessageType, List<String>> map = new HashMap<>();

      for (Visualizer visualizer : visualizers) {
         for (VisualizerMessageType vmt : visualizer.getTargetmessageType()) {
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
   }

   public List<Visualizer> getVisualisers() {
      return visualizers;
   }

   // --------
   // Finders
   // --------

   public String[] getVizualisersNamesForMessageType(JTBMessageType jtbMessageType) {
      return visualizersPerJTBMessageType.get(jtbMessageType);
   }

   public int findIndexVisualizerForType(String[] visualizers, JTBMessageType jtbMessageType, byte[] payloadBytes) {

      // TextMessages: Text viewer
      if (jtbMessageType == JTBMessageType.TEXT) {
         return findPositionInArray(visualizers, "Text");
      }

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

      // BytesMessages: Unknown Signature : Other
      return findPositionInArray(visualizers, "Other");
   }

   // --------
   // Launchers
   // --------
   public void launchVisualizer(String name,
                                JTBMessageType jtbMessageType,
                                String payloadText,
                                byte[] payloadBytes) throws IOException, ScriptException {
      log.debug("launchVisualizer name: {} type={}", name, jtbMessageType);

      Visualizer visualizer = getVizualiserFromName(visualizers, name);

      switch (visualizer.getSourceKind()) {
         case EXTERNAL_EXEC:
            break;

         case EXTERNAL_EXTENSION:
            launchExternalExtension(visualizer, jtbMessageType, payloadText, payloadBytes);
            break;

         case EXTERNAL_SCRIPT:
            FileReader dfr = new FileReader(visualizer.getFileName());
            scriptEngine.eval(dfr);
            break;

         case INTERNAL_BUILTIN:
            break;

         case INTERNAL_SCRIPT:
            scriptEngine.eval(visualizer.getSource());
            break;

         default:
            break;
      }
   }

   private static void launchExternalExtension(Visualizer visualizer,
                                               JTBMessageType jtbMessageType,
                                               String payloadText,
                                               byte[] payloadBytes) throws IOException {
      log.debug("launchOSExternal");

      String extension = visualizer.getExtension();

      File temp = File.createTempFile("jmstoolbox_", extension);
      temp.deleteOnExit();

      if (jtbMessageType == JTBMessageType.BYTES) {
         if ((payloadBytes == null) || (payloadBytes.length == 0)) {
            log.debug("launchVisualizer. No visualisation: payloadBytes is empty or null");
            return;
         }

         try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write(payloadBytes);
         }
      } else {
         if ((payloadText == null) || (payloadText.isEmpty())) {
            log.debug("launchVisualizer. No visualisation: payloadText is empty or null");
            return;
         }

         try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
            bw.write(payloadText);
         }
      }

      if (visualizer.getExtension() == null) {
         log.debug("No extension for name='{}'. Let the OS decide", visualizer.getName());
         Program.launch(temp.getAbsolutePath());
         return;
      }

      Program p = Program.findProgram(extension);
      log.debug("Program found for name='{}' extension='{}' : '{}'", visualizer.getName(), extension, p);
      if (p == null) {
         Program.launch(temp.getAbsolutePath());
         return;
      } else {
         p.execute(temp.getAbsolutePath());
      }

   }

   // --------
   // Builders
   // --------

   private List<Visualizer> buildSystemVisualizers() {
      List<Visualizer> list = new ArrayList<>();

      list.add(buildExternalExtension(true, "Text", ".txt", COL_TEXT));
      list.add(buildExternalExtension(true, "HTML ", ".html", COL_TEXT));
      list.add(buildExternalExtension(true, "ZIP", ".zip", COL_BYTES));
      list.add(buildExternalExtension(true, "PDF", ".pdf", COL_BYTES));
      list.add(buildExternalExtension(true, "Other", ",unknown", COL_ALL));

      list.add(buildInternalScript(true, "Custom JS Internal", "var x = 10;print (x);", COL_BYTES));
      list.add(buildExternalScript(true, "Custom JS External", "toto.js", COL_BYTES));

      return list;
   }

   public Visualizer buildExternalExtension(boolean system,
                                            String name,
                                            String extension,
                                            List<VisualizerMessageType> listMessageType) {
      Visualizer v = new Visualizer();
      v.setSourceKind(VisualizerSourceKind.EXTERNAL_EXTENSION);
      v.setSystem(system);
      v.setName(name);
      v.setExtension(extension);
      v.getTargetmessageType().addAll(listMessageType);

      return v;
   }

   public Visualizer buildInternalScript(boolean system, String name, String source, List<VisualizerMessageType> listMessageType) {
      Visualizer v = new Visualizer();
      v.setSourceKind(VisualizerSourceKind.INTERNAL_SCRIPT);
      v.setSystem(system);
      v.setName(name);
      v.setSource(source);
      v.getTargetmessageType().addAll(listMessageType);

      return v;
   }

   public Visualizer buildExternalScript(boolean system,
                                         String name,
                                         String fileName,
                                         List<VisualizerMessageType> listMessageType) {
      Visualizer v = new Visualizer();
      v.setSourceKind(VisualizerSourceKind.EXTERNAL_SCRIPT);
      v.setSystem(system);
      v.setName(name);
      v.setFileName(fileName);
      v.getTargetmessageType().addAll(listMessageType);

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

   private Visualizer getVizualiserFromName(List<Visualizer> visualizers, String name) {
      // Return the visualizer that corresponds to the name
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

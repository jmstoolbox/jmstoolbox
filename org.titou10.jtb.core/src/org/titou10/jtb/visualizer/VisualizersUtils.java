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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.swt.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.visualizer.gen.Visualizer;
import org.titou10.jtb.visualizer.gen.VisualizerMessageType;
import org.titou10.jtb.visualizer.gen.VisualizerSourceKind;

/**
 * Utility class to manage "Visualizers"
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class VisualizersUtils {

   private static final Logger log = LoggerFactory.getLogger(VisualizersUtils.class);

   public static List<Visualizer> getSystemVisualizers() {
      List<Visualizer> list = new ArrayList<>();

      list.add(buildVisualizerBuiltInExternal(true, "Text", ".txt", VisualizerMessageType.TEXT));
      list.add(buildVisualizerBuiltInExternal(true, "HTML ", ".html", VisualizerMessageType.TEXT));
      list.add(buildVisualizerBuiltInExternal(true, "ZIP", ".zip", VisualizerMessageType.BYTES));
      list.add(buildVisualizerBuiltInExternal(true, "PDF", ".pdf", VisualizerMessageType.BYTES));
      list.add(buildVisualizerBuiltInExternal(true, "Other", null, null));

      return list;
   }

   public static Visualizer buildVisualizerBuiltInExternal(boolean system,
                                                           String name,
                                                           String extension,
                                                           VisualizerMessageType messageType) {
      Visualizer v = new Visualizer();
      v.setSourceKind(VisualizerSourceKind.BUILTIN_EXTERNAL);
      v.setSystem(system);
      v.setName(name);
      v.setExtension(extension);
      v.setMessageType(messageType);

      return v;
   }

   public static Visualizer getVizualiserFromName(List<Visualizer> visualizers, String name) {
      // Return the visualizer that corresponds to tye name
      return visualizers.stream().filter(v -> v.getName().equals(name)).findFirst().orElse(null);
   }

   public static void launchVisualizer(List<Visualizer> visualizers,
                                       String name,
                                       JTBMessageType jtbMessageType,
                                       String payloadText,
                                       byte[] payloadBytes) throws IOException {
      log.debug("launchVisualizer name: {} type={}", name, jtbMessageType);

      if (name == null) {
         return;
      }

      // Program[] xx = Program.getPrograms();
      // for (Program string : xx) {
      // System.out.println("cc=" + string);
      // }

      Visualizer visualizer = getVizualiserFromName(visualizers, name);
      String extension = visualizer.getExtension() == null ? ".unknown" : visualizer.getExtension();

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
         log.debug("No extension for name='{}'. Let the OS decide", name);
         Program.launch(temp.getAbsolutePath());
         return;
      }

      Program p = Program.findProgram(extension);
      log.debug("Program found for name='{}' extension='{}' : '{}'", name, extension, p);
      if (p == null) {
         Program.launch(temp.getAbsolutePath());
         return;
      } else {
         p.execute(temp.getAbsolutePath());
      }

   }

   public static String[] getVizualisersNamesForMessageType(List<Visualizer> visualizers, JTBMessageType jtbMessageType) {

      VisualizerMessageType messageType;
      switch (jtbMessageType) {
         case BYTES:
            messageType = VisualizerMessageType.BYTES;
            break;

         case TEXT:
            messageType = VisualizerMessageType.TEXT;
            break;

         default:
            return null;
      }

      List<String> res = new ArrayList<>();
      for (Visualizer visualizer : visualizers) {
         if ((visualizer.getMessageType() == messageType) || (visualizer.getExtension() == null)) {
            res.add(visualizer.getName());
         }
      }
      return res.toArray(new String[res.size()]);

      // return visualizers.stream().filter(v -> v.getMessageType() == messageType || v.getExtension() == null).map(v ->
      // v.getName())
      // .toArray(size -> new String[size]);
   }

   public static int findIndexVisualizerForType(String[] visualizers, JTBMessageType jtbMessageType, byte[] payloadBytes) {

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

   private static int findPositionInArray(String[] visualizers, String name) {
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

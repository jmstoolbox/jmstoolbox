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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class exposed to external Scripts used by Visualizers
 * 
 * 
 * @author Denis Forveille
 *
 */
public class VisualizerScriptsHook {

   private static final Logger log = LoggerFactory.getLogger(VisualizersManager.class);

   private VisualizersManager  visualizersManager;

   VisualizerScriptsHook(VisualizersManager visualizersManager) {
      this.visualizersManager = visualizersManager;
   }

   // ---------------
   // Compression/Decompression Helpers
   // ---------------
   public byte[] compress(byte[] uncompressedBytes) throws IOException {
      log.debug("compress");

      if (uncompressedBytes == null) {
         return null;
      }
      if (uncompressedBytes.length == 0) {
         return new byte[0];
      }

      Deflater compresser = new Deflater();
      compresser.setInput(uncompressedBytes);
      compresser.finish();

      byte[] buffer = new byte[2048];
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream(uncompressedBytes.length);
      while (!compresser.finished()) {
         int count = compresser.deflate(buffer);
         outputStream.write(buffer, 0, count);
      }
      compresser.end();
      outputStream.close();
      return outputStream.toByteArray();
   }

   public byte[] decompress(byte[] compressedBytes) throws IOException, DataFormatException {
      log.debug("decompress");

      if (compressedBytes == null) {
         return null;
      }
      if (compressedBytes.length == 0) {
         return new byte[0];
      }

      Inflater decompresser = new Inflater();
      decompresser.setInput(compressedBytes);

      byte[] buffer = new byte[2048];
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedBytes.length);
      while (!decompresser.finished()) {
         int count = decompresser.inflate(buffer);
         outputStream.write(buffer, 0, count);
      }
      decompresser.end();
      outputStream.close();
      return outputStream.toByteArray();
   }

   // ---------------
   // Base64 Helpers
   // ---------------

   public byte[] decodeBase64(String text) {
      log.debug("decodeBase64 - String");
      if (text == null) {
         return null;
      }
      if (text.length() == 0) {
         return new byte[0];
      }
      return Base64.getDecoder().decode(text);
   }

   public byte[] decodeBase64(byte[] b) {
      log.debug("decodeBase64 - bytes");
      if (b == null) {
         return null;
      }
      if (b.length == 0) {
         return new byte[0];
      }
      return Base64.getDecoder().decode(b);
   }

   public byte[] encodeBase64(byte[] b) {
      log.debug("encodeBase64");

      if (b == null) {
         return null;
      }
      if (b.length == 0) {
         return new byte[0];
      }
      return Base64.getEncoder().encode(b);
   }

   public String encodeBase64ToString(byte[] b) {
      log.debug("encodeBase64ToString");

      if (b == null) {
         return null;
      }
      if (b.length == 0) {
         return "";
      }
      return Base64.getEncoder().encodeToString(b);
   }

   // ---------------
   // Show Content...
   // ---------------
   public void showContent(String extension, String text) throws IOException {
      log.debug("showContent - String");
      try {
         visualizersManager.launchExternalExtension(extension, text);
      } catch (IOException e) {
         log.error("Exception occured when trying to call external visualizer", e);
         throw e;
      }
   }

   public void showContent(String extension, byte[] bytes) throws IOException {
      log.debug("showText - Bytes");
      try {
         visualizersManager.launchExternalExtension(extension, bytes);
      } catch (IOException e) {
         log.error("Exception occured when trying to call external visualizer", e);
         throw e;
      }
   }

   public void showContent(String extension, Map<String, Object> map) throws IOException {
      log.debug("showText - Map");
      try {
         visualizersManager.launchExternalExtension(extension, map);
      } catch (IOException e) {
         log.error("Exception occured when trying to call external visualizer", e);
         throw e;
      }
   }
}

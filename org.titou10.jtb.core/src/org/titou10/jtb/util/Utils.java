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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that holds various utility methods
 * 
 * @author Denis Forveille
 *
 */
public final class Utils {

   private static final Logger log = LoggerFactory.getLogger(Utils.class);

   // ---------------------------
   // Validate JMS Property Names
   // ---------------------------

   // Check that a string is a valid JMS property name
   public static boolean isValidJMSPropertyName(String s) {
      // For templates, we dont have an active session to get meta data from...
      return isValidJMSPropertyName(s, null);
   }

   // Check that a string is a valid JMS property name
   public static boolean isValidJMSPropertyName(String s, List<String> metaJMSPropertyNames) {
      if ((s == null) || (s.isEmpty())) {
         return false;
      }
      if (!Character.isJavaIdentifierStart(s.charAt(0))) {
         return false;
      }
      for (int i = 1; i < s.length(); i++) {
         if (!Character.isJavaIdentifierPart(s.charAt(i))) {
            return false;
         }
      }
      if (s.startsWith("JMSX")) {
         // JMSX* properties are only OK if they are in the list of the extra properties for the Queue Manager
         if (metaJMSPropertyNames == null) {
            return true;
         }
         for (String propertyName : metaJMSPropertyNames) {
            if (propertyName.equals(s)) {
               return true;
            }
         }
         return false;
      }

      if (s.startsWith("JMS")) {
         return false;
      }
      return true;
   }

   // ---------------------------
   // Manage Images
   // ---------------------------

   // Keep images in cache
   private static final int                IMAGE_CACHE_SIZE = 64;
   private static final Map<String, Image> images           = new HashMap<String, Image>(IMAGE_CACHE_SIZE);

   // Read an image from FS
   public static Image getImage(Class<?> clazz, String imageName) {
      Image i = images.get(imageName);
      if (i == null) {
         Bundle bundle = FrameworkUtil.getBundle(clazz);
         URL url = FileLocator.find(bundle, new Path(imageName), null);
         ImageDescriptor image = ImageDescriptor.createFromURL(url);
         i = image.createImage();
         images.put(imageName, i);
      }
      return i;
   }

   public static class CopyDirVisitor extends SimpleFileVisitor<java.nio.file.Path> {
      private final java.nio.file.Path fromPath;
      private final java.nio.file.Path toPath;
      private final CopyOption         copyOption;

      public CopyDirVisitor(java.nio.file.Path fromPath, java.nio.file.Path toPath, CopyOption copyOption) {
         this.fromPath = fromPath;
         this.toPath = toPath;
         this.copyOption = copyOption;
      }

      @Override
      public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs) throws IOException {
         java.nio.file.Path targetPath = toPath.resolve(fromPath.relativize(dir));
         if (!Files.exists(targetPath)) {
            Files.createDirectory(targetPath);
         }
         return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
         Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
         return FileVisitResult.CONTINUE;
      }
   }

   // ---------------------------
   // Enable/Disable Menu safe way
   // ---------------------------
   public static boolean enableMenu(MMenuItem menuItem) {
      if (menuItem != null) {
         menuItem.setVisible(true);
      }
      return true;

   }

   public static boolean disableMenu(MMenuItem menuItem) {
      if (menuItem != null) {
         menuItem.setVisible(false);
      }
      return false;
   }

   // ---------------------------
   // Save/Read Payload
   // ---------------------------

   public static void exportPayload(Shell shell,
                                    String baseName,
                                    String correlationID,
                                    String messageID,
                                    String payloadText) throws IOException {

      String suggestedFileName = buildFileName(baseName, ".txt", correlationID, messageID);
      log.debug("fileName={}", suggestedFileName);

      FileDialog dlg = openFileDialog(shell, SWT.SAVE, suggestedFileName);
      String fn = dlg.open();
      if (fn != null) {
         StringBuffer sb2 = new StringBuffer(256);
         sb2.append(dlg.getFilterPath());
         sb2.append(File.separator);
         sb2.append(dlg.getFileName());
         String choosenFileName = sb2.toString();
         log.debug("fileName={}", suggestedFileName);

         Files.write(Paths.get(choosenFileName), payloadText.getBytes());
      }
   }

   public static void exportPayload(Shell shell,
                                    String baseName,
                                    String correlationID,
                                    String messageID,
                                    byte[] payloadBytes) throws IOException {

      String suggestedFileName = buildFileName(baseName, ".bin", correlationID, messageID);
      log.debug("fileName={}", suggestedFileName);

      FileDialog dlg = openFileDialog(shell, SWT.SAVE, suggestedFileName);
      String fn = dlg.open();
      if (fn != null) {
         StringBuffer sb2 = new StringBuffer(256);
         sb2.append(dlg.getFilterPath());
         sb2.append(File.separator);
         sb2.append(dlg.getFileName());
         String choosenFileName = sb2.toString();

         Files.write(Paths.get(choosenFileName), payloadBytes);
      }
   }

   public static void exportPayload(Shell shell, String baseName, BytesMessage bm) throws IOException, JMSException {

      String suggestedFileName = buildFileName(baseName, ".bin", bm.getJMSCorrelationID(), bm.getJMSMessageID());
      log.debug("fileName={}", suggestedFileName);

      FileDialog dlg = openFileDialog(shell, SWT.SAVE, suggestedFileName);
      String fn = dlg.open();
      if (fn != null) {
         StringBuffer sb2 = new StringBuffer(256);
         sb2.append(dlg.getFilterPath());
         sb2.append(File.separator);
         sb2.append(dlg.getFileName());
         String choosenFileName = sb2.toString();
         log.debug("fileName={}", choosenFileName);

         byte[] inByteData = new byte[(int) bm.getBodyLength()];
         bm.reset();
         bm.readBytes(inByteData);
         Files.write(Paths.get(choosenFileName), inByteData);
      }
   }

   private static String buildFileName(String baseName, String extension, String correlationID, String messageID) {
      // Build save file name..
      StringBuilder sb = new StringBuilder(256);
      sb.append(baseName);
      sb.append("_");
      if ((correlationID != null) && (correlationID.trim().length() > 0)) {
         sb.append(correlationID);
      } else {
         if (messageID == null) {
            sb.append("msg");
         } else {
            sb.append(messageID);
         }
      }
      sb.append(extension);
      return sb.toString().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
   }

   public static byte[] readFileBytes(Shell shell) throws IOException {
      FileDialog fileDialog = new FileDialog(shell);
      fileDialog.setText("Select File");
      String selected = fileDialog.open();

      // Read File into byte[]
      return Files.readAllBytes(Paths.get(selected));
   }

   private static FileDialog openFileDialog(Shell shell, int mode, String suggestedFileName) {
      FileDialog dlg = new FileDialog(shell, mode);
      dlg.setText("Save payload as...");
      dlg.setFileName(suggestedFileName);
      dlg.setOverwrite(true);
      return dlg;
   }

   // ------------------
   // Pure Utility Class
   // ------------------
   private Utils() {
      // NOP
   }

}

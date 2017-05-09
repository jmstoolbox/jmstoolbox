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
package org.titou10.jtb.template;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Utility class to manage "Templates"
 * 
 * @author Denis Forveille
 *
 */
public class TemplatesUtils {

   private static final Logger           log                = LoggerFactory.getLogger(TemplatesUtils.class);

   private static final SimpleDateFormat TEMPLATE_NAME_SDF  = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
   private static final int              BUFFER_SIZE        = 64 * 1024;
   private static final String           ENCODING           = "UTF-8";

   private static final String           TEMP_SIGNATURE     = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><jtbMessageTemplate>";
   private static final int              TEMP_SIGNATURE_LEN = TEMP_SIGNATURE.length();

   private static JAXBContext            JC;

   public static JTBMessageTemplate getTemplateFromName(IFolder parentFolder, String templateName) throws CoreException,
                                                                                                   JAXBException {
      for (IFile f : getAllTemplatesIFiles(parentFolder)) {
         if (f.getName().equals(templateName)) {
            return readTemplate(f);
         }
      }
      return null;
   }

   public static List<IFile> getAllTemplatesIFiles(IFolder parentFolder) throws CoreException {
      return listFileTree(parentFolder);
   }

   public static JTBMessageTemplate readTemplate(IFile templateFile) throws JAXBException, CoreException {
      log.debug("readTemplate: '{}'", templateFile);

      // Unmarshall the template as xml
      Unmarshaller u = getJAXBContext().createUnmarshaller();
      JTBMessageTemplate messageTemplate = (JTBMessageTemplate) u.unmarshal(templateFile.getContents());
      return messageTemplate;
   }

   // Used by D&D from OS to Template Browser
   public static JTBMessageTemplate readTemplate(String fileName) throws JAXBException, FileNotFoundException {
      log.debug("readTemplate: '{}'", fileName);

      // Read File
      File f = new File(fileName);

      // Unmarshall the template as xml
      Unmarshaller u = getJAXBContext().createUnmarshaller();
      JTBMessageTemplate messageTemplate = (JTBMessageTemplate) u.unmarshal(new FileInputStream(f));
      return messageTemplate;
   }

   public static void updateTemplate(IFile templateFile, JTBMessageTemplate template) throws JAXBException, CoreException,
                                                                                      IOException {
      log.debug("updateTemplate: '{}'", templateFile);

      // Marshall the template to xml
      Marshaller m = getJAXBContext().createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENCODING);

      // Write the result
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE)) {
         m.marshal(template, baos);
         log.debug("xml file size :  {} bytes.", baos.size());
         try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
            templateFile.setContents(is, IResource.FORCE, null);
         }
      }

   }

   // D&D from OS to Tempkate browser
   public static JTBMessageTemplate readTemplateFromOS(String fileName) throws IOException {
      log.debug("readTemplateFromOS: '{}'", fileName);

      String textPayload = new String(Files.readAllBytes(Paths.get(fileName)));
      JTBMessageTemplate messageTemplate = new JTBMessageTemplate();
      messageTemplate.setPayloadText(textPayload);
      return messageTemplate;
   }

   // Export template to OS
   public static void writeTemplateToOS(Shell shell,
                                        String destinationName,
                                        JTBMessageTemplate jtbMessageTemplate) throws JAXBException, CoreException, IOException {
      log.debug("writeTemplateToOS: '{}'", jtbMessageTemplate);

      String suggestedFileName = buildTemplateSuggestedName(destinationName, jtbMessageTemplate.getJmsTimestamp());
      suggestedFileName += Constants.JTB_TEMPLATE_FILE_EXTENSION;

      // Show the "save as" dialog
      FileDialog dlg = new FileDialog(shell, SWT.SAVE);
      dlg.setText("Export Template as...");
      dlg.setFileName(suggestedFileName);
      dlg.setFilterExtensions(new String[] { Constants.JTB_TEMPLATE_FILE_EXTENSION });
      dlg.setOverwrite(true);
      String fn = dlg.open();
      if (fn == null) {
         return;
      }

      // Build file name
      StringBuffer sb2 = new StringBuffer(256);
      sb2.append(dlg.getFilterPath());
      sb2.append(File.separator);
      sb2.append(dlg.getFileName());
      String choosenFileName = sb2.toString();
      log.debug("choosenFileName={}", choosenFileName);

      java.nio.file.Path destPath = Paths.get(choosenFileName);

      // Marshall the template to xml
      Marshaller m = getJAXBContext().createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENCODING);

      // Write the result
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE)) {
         m.marshal(jtbMessageTemplate, baos);
         log.debug("xml file size :  {} bytes.", baos.size());
         Files.write(destPath, baos.toByteArray());
      }
   }

   // D&D from Template Browser to OS
   public static String writeTemplateToOS(IFile sourceTemplate) throws CoreException, IOException {
      log.debug("writeTemplateToOS: '{}'", sourceTemplate);

      String tempFileName = sourceTemplate.getName() + Constants.JTB_TEMPLATE_FILE_EXTENSION;
      InputStream is = sourceTemplate.getContents();
      try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
         String line;
         List<String> lines = new ArrayList<>();
         while ((line = in.readLine()) != null) {
            lines.add(line);
         }
         return Utils.createAndWriteTempFile(tempFileName, null, lines);
      }
   }

   // Detect if an OS file contains a serialized template
   public static boolean isExternalTemplate(String fileName) throws IOException {

      // Read first bytes of file
      try (Reader reader = new InputStreamReader(new FileInputStream(fileName), ENCODING)) {
         char[] chars = new char[TEMP_SIGNATURE_LEN];

         int charsRead = reader.read(chars);

         if (charsRead != TEMP_SIGNATURE_LEN) {
            return false;
         }

         String firstChars = String.valueOf(chars);
         log.debug("firstChars={}", firstChars);
         return firstChars.equals(TEMP_SIGNATURE);
      }
   }

   // Export Templates Menu
   public static void exportTemplates(List<IResource> templatesToExport, String targetFolderName) throws IOException {
      log.debug("exportTemplates to {}", targetFolderName);

      // Get destination path
      java.nio.file.Path destPath = Paths.get(targetFolderName);

      for (IResource r : templatesToExport) {
         java.nio.file.Path p = r.getRawLocation().toFile().toPath(); // Absolute path to source resource
         IPath relativePath = r.getFullPath().removeFirstSegments(2); // Relative path after "JMSToolBox/Templates"

         if (r instanceof IFolder) {
            // Concatenate Template directory (relativePath) to dest folder (destPath)
            java.nio.file.Path dDir = destPath.resolve(relativePath.toString());
            Files.walkFileTree(p, new CopyDirVisitor(p, dDir, StandardCopyOption.COPY_ATTRIBUTES));
         } else {

            // Create directory under dest folder if required
            IPath relativeDir = relativePath.removeLastSegments(1); // Remove template file name
            java.nio.file.Path dDir = destPath.resolve(relativeDir.toString());
            if (!Files.exists(dDir)) {
               Files.createDirectories(dDir);
            }

            // Copy File
            java.nio.file.Path dFile = destPath.resolve(relativePath.toString());
            Files.copy(p, dFile, StandardCopyOption.COPY_ATTRIBUTES);
         }
      }
   }

   // Import Templates Menu
   public static void importTemplates(java.nio.file.Path templatesFolderPath, String sourceFolderName) throws IOException {
      log.debug("importTemplates from {}", sourceFolderName);

      java.nio.file.Path srcPath = Paths.get(sourceFolderName);
      // Files.walkFileTree(sourceFolderPath, new CopyDirVisitor(sourceFolderPath, destPath, StandardCopyOption.REPLACE_EXISTING));
      Files.walkFileTree(srcPath, new CopyDirVisitor(srcPath, templatesFolderPath, StandardCopyOption.COPY_ATTRIBUTES));

   }

   // ---------------------------
   // Helpers
   // ---------------------------

   private static String buildTemplateSuggestedName(String destinationName, Long jmsTimestamp) {

      long dateToFormat = jmsTimestamp == null ? (new Date()).getTime() : jmsTimestamp;

      StringBuilder sb = new StringBuilder(64);
      sb.append(destinationName);
      sb.append("_");
      sb.append(TEMPLATE_NAME_SDF.format(dateToFormat));
      return sb.toString();
   }

   private static JAXBContext getJAXBContext() throws JAXBException {
      if (JC == null) {
         JC = JAXBContext.newInstance(JTBMessageTemplate.class);
      }
      return JC;
   }

   private static List<IFile> listFileTree(IFolder dir) throws CoreException {
      List<IFile> fileTree = new ArrayList<>();
      for (IResource entry : dir.members()) {
         if (entry instanceof IFile) {
            fileTree.add((IFile) entry);
         } else {
            fileTree.addAll(listFileTree((IFolder) entry));
         }
      }
      return fileTree;
   }

   private static class CopyDirVisitor extends SimpleFileVisitor<java.nio.file.Path> {
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

}

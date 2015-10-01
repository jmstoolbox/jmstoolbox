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
package org.titou10.jtb.template;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.dialog.TemplateSaveDialog;
import org.titou10.jtb.jms.model.JTBMessageTemplate;

/**
 * Utility class to manage "Templates"
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class TemplatesUtils {

   private static final Logger log = LoggerFactory.getLogger(TemplatesUtils.class);

   private static final SimpleDateFormat TEMPLATE_NAME_SDF = new SimpleDateFormat("yyyyMMdd-HHmmss");
   private static final int              BUFFER_SIZE       = 64 * 1024;                              // 64 K
   private static JAXBContext            JC;

   public static List<IFile> getAllTemplatesIFiles(IFolder parentFfolder) throws CoreException {
      return listFileTree(parentFfolder);
   }

   public static JTBMessageTemplate readTemplate(IFile templateFile) throws JAXBException, CoreException {
      log.debug("readTemplate {}", templateFile);

      // Unmarshall the template as xml
      Unmarshaller u = getJAXBContext().createUnmarshaller();
      JTBMessageTemplate messageTemplate = (JTBMessageTemplate) u.unmarshal(templateFile.getContents());
      return messageTemplate;
   }

   public static void updateTemplate(IFile templateFile, JTBMessageTemplate template) throws JAXBException, CoreException,
                                                                                      IOException {
      log.debug("updateTemplate {}", templateFile);

      // Marshall the template to xml
      Marshaller m = getJAXBContext().createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

      // Write the result
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE)) {
         m.marshal(template, baos);
         log.debug("xml file size :  {} bytes.", baos.size());
         try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
            templateFile.setContents(is, IResource.FORCE, null);
         }
      }

   }

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

   public static void importTemplates(java.nio.file.Path templatesFolderPath, String sourceFolderName) throws IOException {
      log.debug("importTemplates from {}", sourceFolderName);

      java.nio.file.Path srcPath = Paths.get(sourceFolderName);
      // Files.walkFileTree(sourceFolderPath, new CopyDirVisitor(sourceFolderPath, destPath, StandardCopyOption.REPLACE_EXISTING));
      Files.walkFileTree(srcPath, new CopyDirVisitor(srcPath, templatesFolderPath, StandardCopyOption.COPY_ATTRIBUTES));

   }

   public static boolean createNewTemplate(Shell shell,
                                           JTBMessageTemplate template,
                                           IFolder templateFolder,
                                           IFolder initialFolder,
                                           String baseName) throws JMSException, IOException, CoreException, JAXBException {
      log.debug("createNewTemplate basename {}", baseName);

      // Build suggested name
      long dateToFormat = (new Date()).getTime();
      if (template.getJmsTimestamp() != null) {
         dateToFormat = template.getJmsTimestamp();
      }
      StringBuilder sb = new StringBuilder(64);
      sb.append(baseName);
      sb.append("_");
      sb.append(TEMPLATE_NAME_SDF.format(dateToFormat));
      String templateName = sb.toString();

      // Show save dialog
      TemplateSaveDialog dialog = new TemplateSaveDialog(shell, templateFolder, initialFolder, templateName);
      if (dialog.open() != Window.OK) {
         return false;
      }

      // Create IFile from name
      IPath path = dialog.getSelectedPath();
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IFile iFile = root.getFile(path);

      // marshall the template as xml
      Marshaller m = getJAXBContext().createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

      // Write the result
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE)) {
         m.marshal(template, baos);
         log.debug("xml file size :  {} bytes.", baos.size());
         try (InputStream is = new ByteArrayInputStream(baos.toByteArray())) {
            if (iFile.exists()) {
               log.debug("File already exist. Ask for confirmation");
               boolean result = MessageDialog.openConfirm(shell,
                                                          "Overwrite?",
                                                          "A template with this name already exist. Overwrite it?");
               if (result) {
                  iFile.setContents(is, true, false, null);
                  return true;
               } else {
                  return false;
               }

            } else {
               iFile.create(is, IResource.NONE, null);
               return true;
            }
         }
      }
   }

   // ---------------------------
   // Helpers
   // ---------------------------
   private static JAXBContext getJAXBContext() throws JAXBException {
      if (JC == null) {
         JC = JAXBContext.newInstance(JTBMessageTemplate.class);
      }
      return JC;
   }

   private static List<IFile> listFileTree(IFolder dir) throws CoreException {
      List<IFile> fileTree = new ArrayList<IFile>();
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

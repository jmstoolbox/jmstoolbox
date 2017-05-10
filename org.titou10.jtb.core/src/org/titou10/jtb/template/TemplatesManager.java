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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.template.dialog.TemplateSaveDialog;
import org.titou10.jtb.template.gen.TemplateDirectory;
import org.titou10.jtb.template.gen.Templates;
import org.titou10.jtb.util.Constants;

/**
 * Manage all things related to "Templates"
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class TemplatesManager {

   private static final Logger                     log                                = LoggerFactory
            .getLogger(TemplatesManager.class);

   private static final String                     EMPTY_TEMPLATE_FILE                = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><templates></templates>";
   private static final String                     ENC                                = "UTF-8";
   private static final int                        BUFFER_SIZE                        = 64 * 1024;
   private static final SimpleDateFormat           TEMPLATE_NAME_SDF                  = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");

   private static final String                     TEMP_SIGNATURE                     = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><jtbMessageTemplate>";
   private static final int                        TEMP_SIGNATURE_LEN                 = TEMP_SIGNATURE.length();

   public static final TemplateDirectoryComparator ROOT_TEMPLATE_DIRECTORY_COMPARATOR = new TemplateDirectoryComparator();
   public static final String                      TEMP_DIR                           = System.getProperty("java.io.tmpdir");

   private JAXBContext                             jcTemplates;
   private JAXBContext                             jcJTBMessageTemplate;

   private IFile                                   templatesDirectoryConfigFile;
   private Templates                               templatesDirectories;

   private IFolder                                 systemTemplateDirectoryIFolder;
   private TemplateDirectory                       systemTemplateDirectory;
   private IFileStore                              systemTemplateDirectoryFileStore;

   private List<TemplateDirectory>                 templateRootDirs;
   private Map<IFileStore, TemplateDirectory>      mapTemplateRootDirs;

   private int                                     seqNumber                          = 0;

   public int initialize(IFile templatesDirectoryConfigFile, IFolder systemTemplateDirectoryFolder) throws Exception {
      log.debug("Initializing TemplatesManager");

      this.templatesDirectoryConfigFile = templatesDirectoryConfigFile;
      this.systemTemplateDirectoryIFolder = systemTemplateDirectoryFolder;

      this.jcTemplates = JAXBContext.newInstance(Templates.class);
      this.jcJTBMessageTemplate = JAXBContext.newInstance(JTBMessageTemplate.class);

      // Load and Parse Templates config file

      // Create System Template Folder if it does not exist
      if (!(systemTemplateDirectoryIFolder.exists())) {
         systemTemplateDirectoryIFolder.create(true, true, null);
      }

      if (!(templatesDirectoryConfigFile.exists())) {
         log.warn("Template file '{}' does not exist. Creating an new empty one.", Constants.JTB_TEMPLATE_CONFIG_FILE_NAME);
         try {
            this.templatesDirectoryConfigFile.create(new ByteArrayInputStream(EMPTY_TEMPLATE_FILE.getBytes(ENC)), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }

         // Before v4.1.0, templatesIFile didn't exist and templates had no extension.
         // Rename all templates present in "System" dir to add the .jtb extension
         // FIXME DF
      }

      // Parse TemplateDirectory config file
      this.templatesDirectories = parseTemplatesFile(this.templatesDirectoryConfigFile.getContents());

      // Build System TemplateDirectory
      this.systemTemplateDirectory = new TemplateDirectory();
      this.systemTemplateDirectory.setSystem(true);
      this.systemTemplateDirectory.setName(Constants.JTB_TEMPLATE_CONFIG_FOLDER_NAME);
      this.systemTemplateDirectory.setDirectory(systemTemplateDirectoryFolder.getLocation().toPortableString());

      // Build list of templates directory
      reload();

      for (Entry<IFileStore, TemplateDirectory> e : mapTemplateRootDirs.entrySet()) {
         if (e.getValue().equals(systemTemplateDirectory)) {
            this.systemTemplateDirectoryFileStore = e.getKey();
            break;
         }
      }

      log.debug("TemplatesManager initialized");
      return mapTemplateRootDirs.size();
   }

   // --------------------
   // Templates Config File
   // --------------------

   // Parse Templates File
   private Templates parseTemplatesFile(InputStream is) throws JAXBException {
      log.debug("Parsing Templates file '{}'", Constants.JTB_TEMPLATE_CONFIG_FILE_NAME);

      Unmarshaller u = jcTemplates.createUnmarshaller();
      return (Templates) u.unmarshal(is);
   }

   public boolean saveTemplates() throws JAXBException, CoreException {
      log.debug("saveTemplates");

      templatesDirectories.getTemplateDirectory().clear();
      for (TemplateDirectory td : templateRootDirs) {
         if (td.isSystem()) {
            continue;
         }
         templatesDirectories.getTemplateDirectory().add(td);
      }
      templatesWriteFile();

      return true;
   }

   public void reload() {

      // Get System Template Directory + the ones defines in config file
      this.templateRootDirs = new ArrayList<>();
      this.templateRootDirs.add(systemTemplateDirectory);
      this.templateRootDirs.addAll(templatesDirectories.getTemplateDirectory());
      Collections.sort(this.templateRootDirs, ROOT_TEMPLATE_DIRECTORY_COMPARATOR);

      mapTemplateRootDirs = new HashMap<>(this.templateRootDirs.size());
      for (TemplateDirectory td : this.templateRootDirs) {
         IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(td.getDirectory()));
         mapTemplateRootDirs.put(fileStore, td);
      }

   }

   public boolean importTemplatesDirectoryConfig(String templatesDirectoryConfigFileName) throws JAXBException, CoreException,
                                                                                          FileNotFoundException {
      log.debug("importTemplatesDirectoryConfig : {}", templatesDirectoryConfigFileName);

      // Try to parse the given file
      File f = new File(templatesDirectoryConfigFileName);

      Templates newTemplates = parseTemplatesFile(new FileInputStream(f));
      if (newTemplates == null) {
         return false;
      }

      // Merge Templates Directories
      List<TemplateDirectory> mergedTemplatesDirectories = new ArrayList<>(templateRootDirs);
      for (TemplateDirectory td : newTemplates.getTemplateDirectory()) {
         // If a template directry with the same name exist, replace it
         for (TemplateDirectory temp : templatesDirectories.getTemplateDirectory()) {
            if (temp.getName().equals(temp.getName())) {
               mergedTemplatesDirectories.remove(temp);
            }
         }
         mergedTemplatesDirectories.add(td);
      }
      templatesDirectories.getTemplateDirectory().clear();
      templatesDirectories.getTemplateDirectory().addAll(mergedTemplatesDirectories);

      // Write the templates directory config file
      templatesWriteFile();

      // int variables
      reload();

      return true;
   }

   public void exportTemplatesDirectoryConfig(String templatesDirectoryConfigFileName) throws IOException, CoreException {
      log.debug("exportTemplatesDirectoryConfig : {}", templatesDirectoryConfigFileName);
      Files.copy(templatesDirectoryConfigFile.getContents(),
                 Paths.get(templatesDirectoryConfigFileName),
                 StandardCopyOption.REPLACE_EXISTING);
   }

   // Write Variables File
   private void templatesWriteFile() throws JAXBException, CoreException {
      log.info("Writing Templates file '{}'", Constants.JTB_TEMPLATE_CONFIG_FILE_NAME);

      // Remove System Template Directories
      Templates temp = new Templates();
      List<TemplateDirectory> x = temp.getTemplateDirectory();
      for (TemplateDirectory templateDirectory : templatesDirectories.getTemplateDirectory()) {
         if (!templateDirectory.isSystem()) {
            x.add(templateDirectory);
         }
      }

      Marshaller m = jcTemplates.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(temp, sw);

      // TODO Add the logic to temporarily save the previous file in case of crash while saving

      try (InputStream is = new ByteArrayInputStream(sw.toString().getBytes(ENC))) {
         templatesDirectoryConfigFile.setContents(is, false, false, null);
      } catch (IOException e) {
         log.error("IOException", e);
         return;
      }
   }
   // ---------------------------
   // Getters and various helpers
   // ---------------------------

   public IFileStore getSystemTemplateDirectoryFileStore() {
      return systemTemplateDirectoryFileStore;
   }

   public IFileStore[] getTemplateRootDirsFileStores() {
      return (IFileStore[]) mapTemplateRootDirs.keySet().toArray(new IFileStore[0]);
   }

   public boolean isRootTemplateDirectoryFileStore(IFileStore fileStore) {
      if (fileStore == null) {
         return false;
      }
      return mapTemplateRootDirs.containsKey(fileStore);
   }

   public TemplateDirectory getTemplateDirectoryFromFileStore(IFileStore fileStore) {
      return mapTemplateRootDirs.get(fileStore);
   }

   public IFileStore appendFilenameToFileStore(IFileStore fileStore, String filename) {
      IPath p = URIUtil.toPath(fileStore.toURI());
      return EFS.getLocalFileSystem().getStore(p.append(filename));
   }

   public TemplateDirectory getDirectoryFromTemplateName(String templateFullFileName) {
      if (templateFullFileName == null) {
         return null;
      }
      for (TemplateDirectory templateDirectory : templateRootDirs) {
         if (templateFullFileName.startsWith(templateDirectory.getDirectory())) {
            return templateDirectory;
         }
      }
      return null;
   }

   // ------------------------------
   // Deal with Templates themselves
   // ------------------------------

   // Detect if an OS file contains a serialized template
   public boolean isFileStoreATemplate(String fileName) throws IOException {

      // Read first bytes of file
      try (Reader reader = new InputStreamReader(new FileInputStream(fileName), ENC)) {
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

   // Export template to OS
   public void writeTemplateToOS(Shell shell, String destinationName, JTBMessageTemplate jtbMessageTemplate) throws JAXBException,
                                                                                                             CoreException,
                                                                                                             IOException {
      log.debug("writeTemplateToOS: '{}'", jtbMessageTemplate);

      String suggestedFileName = buildTemplateSuggestedRelativeFileName(destinationName, jtbMessageTemplate.getJmsTimestamp());
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
      Marshaller m = jcJTBMessageTemplate.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);

      // Write the result
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE)) {
         m.marshal(jtbMessageTemplate, baos);
         log.debug("xml file size :  {} bytes.", baos.size());
         Files.write(destPath, baos.toByteArray());
      }
   }

   // D&D from Template Browser to OS
   public String writeTemplateToTemp(IFileStore templateFileStore) throws CoreException, IOException {
      log.debug("writeTemplateToTemp: '{}'", templateFileStore);

      String tempFileName = templateFileStore.getName() + Constants.JTB_TEMPLATE_FILE_EXTENSION;

      File temp = new File(TEMP_DIR + File.separator + tempFileName);
      if (temp.exists()) {
         temp.delete();
      }

      temp.createNewFile();
      try {
         try (BufferedInputStream bis = new BufferedInputStream(templateFileStore
                  .openInputStream(EFS.NONE, new NullProgressMonitor()), BUFFER_SIZE)) {
            Files.copy(bis, temp.toPath());
         }
         return temp.getCanonicalPath();
      } finally {
         temp.delete();
      }
   }

   public JTBMessageTemplate readTemplate(String templateFileName) throws JAXBException, CoreException, IOException {
      log.debug("readTemplate: '{}'", templateFileName);
      if (templateFileName == null) {
         return null;
      }

      File f = new File(templateFileName);
      IFileStore templateFileStore = EFS.getLocalFileSystem().getStore(f.toURI());

      return readTemplate(templateFileStore);
   }

   public JTBMessageTemplate readTemplate(IFileStore templateFileStore) throws JAXBException, CoreException, IOException {
      log.debug("readTemplate: '{}'", templateFileStore);

      if (!templateFileStore.fetchInfo().exists()) {
         return null;
      }

      // Unmarshall the template as xml
      Unmarshaller u = jcJTBMessageTemplate.createUnmarshaller();
      try (BufferedInputStream bis = new BufferedInputStream(templateFileStore.openInputStream(EFS.NONE, new NullProgressMonitor()),
                                                             BUFFER_SIZE)) {
         return (JTBMessageTemplate) u.unmarshal(bis);
      }
   }

   public void updateTemplate(IFileStore templateFileStore, JTBMessageTemplate template) throws JAXBException, CoreException,
                                                                                         IOException {
      log.debug("updateTemplate: '{}'", templateFileStore);

      // Marshall the template to xml
      Marshaller m = jcJTBMessageTemplate.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);

      // Write the result
      try (BufferedOutputStream bos = new BufferedOutputStream(templateFileStore
               .openOutputStream(EFS.NONE, new NullProgressMonitor()), BUFFER_SIZE)) {
         m.marshal(template, bos);
      }
   }

   public boolean createNewTemplate(Shell shell,
                                    JTBMessageTemplate template,
                                    IFileStore initialFolder,
                                    String destinationName) throws JMSException, IOException, CoreException, JAXBException {
      log.debug("createNewTemplate destinationName {}", destinationName);

      // initialFolder is null for System Template Directory
      if (initialFolder == null) {
         initialFolder = systemTemplateDirectoryFileStore;
      }

      // Build suggested name
      String templateName = buildTemplateSuggestedRelativeFileName(destinationName, template.getJmsTimestamp());

      // Show save dialog
      TemplateSaveDialog dialog = new TemplateSaveDialog(shell,
                                                         this,
                                                         new ArrayList<IFileStore>(mapTemplateRootDirs.keySet()),
                                                         initialFolder,
                                                         templateName);
      if (dialog.open() != Window.OK) {
         return false;
      }

      IFileStore newFileStore = dialog.getSelectedPath();
      if (newFileStore.fetchInfo().exists()) {
         boolean result = MessageDialog.openConfirm(shell, "Overwrite?", "A template with this name already exist. Overwrite it?");
         if (!result) {
            return false;
         }
      }

      updateTemplate(newFileStore, template);
      return true;
   }

   public JTBMessageTemplate getTemplateFromName(String templateName) throws CoreException, JAXBException, IOException {
      log.debug("getTemplateFromName '{}'", templateName);
      if (templateName == null) {
         return null;
      }

      IFileStore templateFileStore = EFS.getLocalFileSystem().getStore(URI.create(templateName));
      if (!templateFileStore.fetchInfo().exists()) {
         log.debug("'{}' does not exit", templateName);
         return null;
      }

      return readTemplate(templateFileStore);
   }

   public boolean isFileStoreGrandChildOfParent(IFileStore parentFileStore, IFileStore childFileStore) {
      if ((parentFileStore == null) || (childFileStore == null)) {
         return false;
      }
      if (parentFileStore.equals(childFileStore)) {
         return true;
      }
      IFileStore x = childFileStore;
      while (x.getParent() != null) {
         if (x.equals(parentFileStore)) {
            return true;
         }
         x = x.getParent();
      }
      return false;
   }

   // -------
   // Helpers
   // -------

   public final static class TemplateDirectoryComparator implements Comparator<TemplateDirectory> {

      @Override
      public int compare(TemplateDirectory o1, TemplateDirectory o2) {
         // System rootdirectories first
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

   // ----------------------------
   // Manage Template file names
   // ----------------------------

   public TemplateNameStructure buildTemplateNameStructure(String templateFullFileName) {
      TemplateNameStructure tns = new TemplateNameStructure();
      tns.templateFullFileName = templateFullFileName;

      TemplateDirectory td = getDirectoryFromTemplateName(templateFullFileName);
      tns.templateRelativeFileName = getRelativeFilenameFromTemplateName(td, templateFullFileName);

      tns.templateDirectoryName = td.getName();

      StringBuilder sb = new StringBuilder(64);
      sb.append(tns.templateDirectoryName);
      sb.append("::");
      sb.append(tns.templateRelativeFileName);
      tns.syntheticName = sb.toString();

      return tns;
   }

   public TemplateNameStructure buildTemplateNameStructure(String templateDirectoryName, String relativeFileName) {
      if ((templateDirectoryName == null) || (relativeFileName == null)) {
         return null;
      }
      TemplateDirectory td = getDirectoryFromDirectoryName(templateDirectoryName);
      // TODO DF: test if td does not exist..
      return buildTemplateNameStructure(td.getDirectory() + relativeFileName);
   }

   private String buildTemplateSuggestedRelativeFileName(String destinationName, Long jmsTimestamp) {

      long dateToFormat = jmsTimestamp == null ? (new Date()).getTime() : jmsTimestamp;

      StringBuilder sb = new StringBuilder(64);
      sb.append(destinationName);
      sb.append("_");
      sb.append(TEMPLATE_NAME_SDF.format(dateToFormat));
      sb.append("_");
      sb.append(seqNumber++);
      return sb.toString();
   }

   private TemplateDirectory getDirectoryFromDirectoryName(String templateDirectoryName) {
      if (templateDirectoryName == null) {
         return null;
      }
      for (TemplateDirectory templateDirectory : templateRootDirs) {
         if (templateDirectoryName.equals(templateDirectory.getName())) {
            return templateDirectory;
         }
      }
      return null;
   }

   private String getRelativeFilenameFromTemplateName(TemplateDirectory templateDirectory, String templateFullFileName) {
      if ((templateDirectory == null) || (templateFullFileName == null)) {
         return null;
      }
      return templateFullFileName.replace(templateDirectory.getDirectory(), "");
   }

   public class TemplateNameStructure {
      private String templateFullFileName;
      private String templateDirectoryName;
      private String templateRelativeFileName;
      private String syntheticName;

      // Packege Constructor
      TemplateNameStructure() {
      }

      public String getTemplateFullFileName() {
         return templateFullFileName;
      }

      public String getTemplateDirectoryName() {
         return templateDirectoryName;
      }

      public String getTemplateRelativeFileName() {
         return templateRelativeFileName;
      }

      public String getSyntheticName() {
         return syntheticName;
      }

   }
}

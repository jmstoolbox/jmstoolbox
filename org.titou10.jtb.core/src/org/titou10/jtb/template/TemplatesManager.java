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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javax.jms.JMSException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.template.dialog.TemplateSaveDialog;
import org.titou10.jtb.template.gen.TemplateDirectory;
import org.titou10.jtb.template.gen.Templates;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage all things related to "Templates"
 *
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class TemplatesManager {

   private static final Logger                      log                      = LoggerFactory.getLogger(TemplatesManager.class);

   private static final String                      EMPTY_TEMPLATE_FILE      = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><templates></templates>";
   private static final String                      ENC                      = "UTF-8";
   private static final String                      UNKNOWN_DIR              = "?????";
   private static final int                         BUFFER_SIZE              = 64 * 1024;
   private static final SimpleDateFormat            TEMPLATE_NAME_SDF        = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");

   private static final String                      TEMP_SIGNATURE           = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><jtbMessageTemplate>";
   private static final int                         TEMP_SIGNATURE_LEN       = TEMP_SIGNATURE.length();

   private static final TemplateDirectoryComparator ROOT_TEMP_DIR_COMPARATOR = new TemplateDirectoryComparator();
   private static final String                      TEMP_DIR                 = System.getProperty("java.io.tmpdir");

   @Inject
   private ConfigManager                            cm;

   private JAXBContext                              jcTemplates;
   private JAXBContext                              jcJTBMessageTemplate;

   private IFile                                    templatesDirectoryConfigFile;
   private Templates                                templatesDirectories;

   private IFolder                                  systemTemplateDirectoryIFolder;
   private TemplateDirectory                        systemTemplateDirectory;
   private IFileStore                               systemTemplateDirectoryFileStore;

   private TemplateDirectory                        unknownTemplateDirectory;

   private List<TemplateDirectory>                  templateRootDirs;

   private Map<IFileStore, TemplateDirectory>       mapTemplateRootDirs;

   private int                                      seqNumber                = 0;

   @PostConstruct
   public void initialize() throws Exception {
      log.debug("Initializing TemplatesManager");

      unknownTemplateDirectory = buildTemplateDirectory(true, UNKNOWN_DIR, UNKNOWN_DIR);

      this.templatesDirectoryConfigFile = cm.getJtbProject().getFile(Constants.JTB_TEMPLATE_CONFIG_FILE_NAME);
      this.systemTemplateDirectoryIFolder = cm.getJtbProject().getFolder(Constants.JTB_TEMPLATE_CONFIG_FOLDER_NAME);

      this.jcTemplates = JAXBContext.newInstance(Templates.class);
      this.jcJTBMessageTemplate = JAXBContext.newInstance(JTBMessageTemplate.class);

      // Load and Parse Templates config file

      // Create System Template Folder if it does not exist
      if (!(systemTemplateDirectoryIFolder.exists())) {
         log.warn("System Template Directory '{}' does not exist. Creating it.", systemTemplateDirectoryIFolder);
         systemTemplateDirectoryIFolder.create(true, true, null);
      }

      if (!(templatesDirectoryConfigFile.exists())) {
         log.warn("Template directory config file '{}' does not exist. Creating an new empty one.",
                  Constants.JTB_TEMPLATE_CONFIG_FILE_NAME);
         try {
            this.templatesDirectoryConfigFile.create(new ByteArrayInputStream(EMPTY_TEMPLATE_FILE.getBytes(ENC)), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }

         // Before v4.1.0, templates.xml didn't exist and templates had no extension.
         // Rename all templates present in "System Template Directory" dir to add the .jtb extension
         renameAllSystemTemplates(systemTemplateDirectoryIFolder);
      }

      // Parse TemplateDirectory config file
      this.templatesDirectories = parseTemplatesFile(this.templatesDirectoryConfigFile.getContents());

      // Build System TemplateDirectory
      this.systemTemplateDirectory = buildTemplateDirectory(true,
                                                            Constants.JTB_TEMPLATE_CONFIG_FOLDER_NAME,
                                                            systemTemplateDirectoryIFolder.getLocation().toPortableString());

      // Build list of templates directory
      reload();

      for (Entry<IFileStore, TemplateDirectory> e : mapTemplateRootDirs.entrySet()) {
         if (e.getValue().equals(systemTemplateDirectory)) {
            this.systemTemplateDirectoryFileStore = e.getKey();
            break;
         }
      }

      log.debug("TemplatesManager initialized");
   }

   public int getNbTemplates() {
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

   public void reload() throws CoreException {

      // Get System Template Directory + the ones defines in config file
      this.templateRootDirs = new ArrayList<>();
      this.templateRootDirs.add(systemTemplateDirectory);
      this.templateRootDirs.addAll(templatesDirectories.getTemplateDirectory());
      Collections.sort(this.templateRootDirs, ROOT_TEMP_DIR_COMPARATOR);

      mapTemplateRootDirs = new HashMap<>(this.templateRootDirs.size());
      for (TemplateDirectory td : this.templateRootDirs) {
         IFileStore fileStore = EFS.getStore(URIUtil.toURI(td.getDirectory()));
         mapTemplateRootDirs.put(fileStore, td);
      }

   }

   public void importTemplatesDirectoryConfig(InputStream is) throws JAXBException, CoreException, FileNotFoundException {
      log.debug("importTemplatesDirectoryConfig");

      Templates newTemplates = parseTemplatesFile(is);
      if (newTemplates == null) {
         return;
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
   }

   // Write Template File
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

   public List<TemplateDirectory> getTemplateRootDirs() {
      return templateRootDirs;
   }

   public IFileStore getSystemTemplateDirectoryFileStore() {
      return systemTemplateDirectoryFileStore;
   }

   public IFileStore[] getTemplateRootDirsFileStores() {
      return mapTemplateRootDirs.keySet().toArray(new IFileStore[0]);
   }

   public boolean isRootTemplateDirectoryFileStore(IFileStore fileStore) {
      if (fileStore == null) {
         return false;
      }
      return mapTemplateRootDirs.containsKey(fileStore);
   }

   public boolean isUnknownTemplateDirectory(TemplateNameStructure tns) {
      if (tns == null) {
         return false;
      }
      return tns.getTemplateDirectoryName().equals(unknownTemplateDirectory.getDirectory());
   }

   public TemplateDirectory getTemplateDirectoryFromFileStore(IFileStore fileStore) {
      return mapTemplateRootDirs.get(fileStore);
   }

   public IFileStore appendFilenameToFileStore(IFileStore fileStore, String fileName) {
      IPath p = URIUtil.toPath(fileStore.toURI());
      try {
         return EFS.getStore(URIUtil.toURI(p.append(fileName)));
      } catch (CoreException e) {
         // DF Should bever occur..
         log.error("exception occurred when appending file name " + fileStore + "-" + fileName, e);
         return null;
      }
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

      File f = new File(fileName);
      if (f.isDirectory()) {
         return false;
      }

      // Read first bytes of file
      try (Reader reader = new InputStreamReader(new FileInputStream(fileName), ENC)) {
         char[] chars = new char[TEMP_SIGNATURE_LEN];

         int charsRead = reader.read(chars);

         if (charsRead != TEMP_SIGNATURE_LEN) {
            return false;
         }

         String firstChars = String.valueOf(chars);
         // log.debug("firstChars={}", firstChars);
         return firstChars.equals(TEMP_SIGNATURE);
      }
   }

   // Export template to OS
   public Integer writeTemplateInBatchToOS(Shell shell, List<JTBMessage> jtbMessages) throws JAXBException, CoreException,
                                                                                      IOException, JMSException {

      log.debug("writeTemplateInBatchToOS");

      // Ask for target directory
      DirectoryDialog dlg = new DirectoryDialog(shell, SWT.SAVE);
      dlg.setText("Save templates in...");
      dlg.setMessage("Select a directory");
      String dir = dlg.open();
      if (dir == null) {
         return null;
      }
      log.debug("Target directory: {}", dir);
      dir += File.separatorChar;

      // Write/Overwrite every message payload

      Integer nb = 0;
      String choosenFileName;
      for (JTBMessage jtbMessage : jtbMessages) {
         var jtbMessageTemplate = new JTBMessageTemplate(jtbMessage);
         log.debug("writeTemplateToOS: '{}'", jtbMessageTemplate);

         choosenFileName = dir + buildTemplateSuggestedRelativeFileName(jtbMessage.getJtbDestination().getName(),
                                                                        jtbMessageTemplate.getJmsTimestamp(),
                                                                        false)
                           + Constants.JTB_TEMPLATE_FILE_EXTENSION;
         log.debug("choosenFileName={}", choosenFileName);

         // Marshall the template to xml
         Marshaller m = jcJTBMessageTemplate.createMarshaller();
         m.setProperty(Marshaller.JAXB_ENCODING, ENC);

         // Write the result
         try (ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE)) {
            m.marshal(jtbMessageTemplate, baos);
            log.debug("write template {}. xml file size :  {} bytes.", choosenFileName, baos.size());
            Utils.writeFile(choosenFileName, baos);
            nb++;
         }
      }
      return nb;
   }

   public void writeTemplateToOS(Shell shell, JTBMessage jtbMessage) throws JAXBException, CoreException, IOException,
                                                                     JMSException {
      log.debug("writeTemplateToOS: '{}'", jtbMessage);

      var jtbMessageTemplate = new JTBMessageTemplate(jtbMessage);

      var suggestedFileName = buildTemplateSuggestedRelativeFileName(jtbMessage.getJtbDestination().getName(),
                                                                     jtbMessageTemplate.getJmsTimestamp(),
                                                                     false);
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

      // Marshall the template to xml
      Marshaller m = jcJTBMessageTemplate.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);

      // Write the result
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE)) {
         m.marshal(jtbMessageTemplate, baos);
         log.debug("xml file size :  {} bytes.", baos.size());
         Utils.writeFile(choosenFileName, baos);
      }
   }

   // D&D from Template Browser to OS
   public String writeTemplateToTemp(IFileStore templateFileStore) throws CoreException, IOException {
      log.debug("writeTemplateToTemp: '{}'", templateFileStore);

      // String tempFileName = templateFileStore.getName() + Constants.JTB_TEMPLATE_FILE_EXTENSION;
      String tempFileName = templateFileStore.getName();

      File temp = new File(TEMP_DIR + File.separator + tempFileName);
      if (temp.exists()) {
         temp.delete();
      }
      temp.deleteOnExit();

      try (BufferedInputStream bis = new BufferedInputStream(templateFileStore.openInputStream(EFS.NONE, new NullProgressMonitor()),
                                                             BUFFER_SIZE)) {
         Files.copy(bis, temp.toPath());
      }
      return temp.getCanonicalPath();
   }

   public JTBMessageTemplate readTemplate(String templateFileName) throws JAXBException, CoreException, IOException {
      log.debug("readTemplate: '{}'", templateFileName);
      if (templateFileName == null) {
         return null;
      }

      return readTemplate(EFS.getStore(URIUtil.toURI(templateFileName)));
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

   public JTBMessageTemplate readTemplateFromOS(String templateFileName) throws JAXBException, FileNotFoundException {
      log.debug("readTemplateFromOS: '{}'", templateFileName);

      // Unmarshall the template as xml
      Unmarshaller u = jcJTBMessageTemplate.createUnmarshaller();
      return (JTBMessageTemplate) u.unmarshal(new FileInputStream(templateFileName));
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
      String templateName = buildTemplateSuggestedRelativeFileName(destinationName,
                                                                   template.getJmsTimestamp(),

                                                                   false);

      // Show save dialog
      TemplateSaveDialog dialog = new TemplateSaveDialog(shell,
                                                         this,
                                                         new ArrayList<>(mapTemplateRootDirs.keySet()),
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

   public JTBMessageTemplate getTemplateFromName(String templateFullFileName) throws CoreException, JAXBException, IOException {
      log.debug("getTemplateFromName '{}'", templateFullFileName);
      if (templateFullFileName == null) {
         return null;
      }

      IFileStore templateFileStore = EFS.getStore(URIUtil.toURI(templateFullFileName));
      if (!templateFileStore.fetchInfo().exists()) {
         log.debug("'{}' does not exit", templateFullFileName);
         return null;
      }

      return readTemplate(templateFileStore);
   }

   public JTBMessageTemplate getTemplateFromNameFromSystemFolder(String templateName) throws CoreException, JAXBException,
                                                                                      IOException {
      log.debug("getTemplateFromNameFromSystemFolder '{}'", templateName);
      if (templateName == null) {
         return null;
      }

      // Build full local path to system template folder
      return getTemplateFromName(systemTemplateDirectoryIFolder.getLocation().toPortableString() +
                                 "/" +
                                 templateName +
                                 Constants.JTB_TEMPLATE_FILE_EXTENSION);
   }

   // -----------------
   // TemplateDirectory
   // -----------------

   public TemplateDirectory buildTemplateDirectory(boolean system, String name, String directory) {
      TemplateDirectory td = new TemplateDirectory();
      td.setSystem(system);
      td.setName(name);
      td.setDirectory(directory);
      return td;
   }

   private final static class TemplateDirectoryComparator implements Comparator<TemplateDirectory> {

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

      String workFileName;
      if (templateFullFileName.endsWith(Constants.JTB_TEMPLATE_FILE_EXTENSION)) {
         workFileName = Utils.getNameWithoutExt(templateFullFileName);
      } else {
         workFileName = templateFullFileName;
      }

      TemplateDirectory td = getDirectoryFromTemplateName(workFileName);
      td = (td == null) ? unknownTemplateDirectory : td;

      TemplateNameStructure tns = new TemplateNameStructure();
      tns.templateFullFileName = workFileName + Constants.JTB_TEMPLATE_FILE_EXTENSION;
      tns.templateRelativeFileName = getRelativeFilenameFromTemplateName(td, workFileName);
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

      td = (td == null) ? unknownTemplateDirectory : td;

      return buildTemplateNameStructure(td.getDirectory() + relativeFileName);
   }

   private String buildTemplateSuggestedRelativeFileName(String destinationName, Long jmsTimestamp, boolean addSeq) {

      long dateToFormat = jmsTimestamp == null ? (new Date()).getTime() : jmsTimestamp;

      StringBuilder sb = new StringBuilder(64);
      sb.append(destinationName);
      sb.append("_");
      sb.append(TEMPLATE_NAME_SDF.format(dateToFormat));
      if (addSeq) {
         sb.append("_");
         sb.append(seqNumber++);
      }
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
      private String templateFullFileName;     // = D:/dev/java/JMSToolBox/Templates/dir/relatibeFileName.jtb
      private String templateDirectoryName;    // = Template
      private String templateRelativeFileName; // = /dir/relatibeFileName
      private String syntheticName;            // = Template::/dir/relatibeFileName

      // Package Constructor
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

   // ---------------------------------
   // Dealing with old System Templates
   // ---------------------------------
   private void renameAllSystemTemplates(IFolder systemTemplateDirectoryIFolder) throws IOException {

      log.warn("Before v4.1.0, file 'templates.xml' didn't exist and templates had no extension.");
      log.warn("Rename all templates present in the 'System Template Directory' dir to add the .jtb extension'");

      SimpleFileVisitor<java.nio.file.Path> sfv = new SimpleFileVisitor<>() {

         @Override
         public FileVisitResult visitFile(java.nio.file.Path path, BasicFileAttributes attrs) throws IOException {
            if (!path.toString().toLowerCase().endsWith(Constants.JTB_TEMPLATE_FILE_EXTENSION)) {
               File f1 = path.toFile();
               File f2 = new File(f1.getCanonicalPath() + Constants.JTB_TEMPLATE_FILE_EXTENSION);
               f1.renameTo(f2);
            }
            return FileVisitResult.CONTINUE;
         }
      };

      URI uri = systemTemplateDirectoryIFolder.getLocationURI();

      Files.walkFileTree(Paths.get(uri), sfv);
      log.warn("Rename Done");
   }

}

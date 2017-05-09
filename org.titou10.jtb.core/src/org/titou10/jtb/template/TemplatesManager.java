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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.jms.JMSException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
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

   private static final Logger                log                 = LoggerFactory.getLogger(TemplatesManager.class);

   private static final String                EMPTY_TEMPLATE_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><templates></templates>";
   private static final String                ENC                 = "UTF-8";
   private static final int                   BUFFER_SIZE         = 64 * 1024;
   private static final SimpleDateFormat      TEMPLATE_NAME_SDF   = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");

   public static TemplateDirectoryComparator  ROOT_TEMPLATE_DIRECTORY_COMPARATOR;

   private JAXBContext                        jcTemplates;
   private JAXBContext                        jcJTBMessageTemplate;
   private IFolder                            templatesSystemIFolder;
   private IFile                              templatesIFile;
   private Templates                          templates;

   private TemplateDirectory                  systemTemplateDirectory;
   private List<TemplateDirectory>            templateRootDirs;
   private Map<TemplateDirectory, IFileStore> mapTemplateRootDirs;

   public int initialize(IFile tIFile, IFolder tSystemTemplateFolder) throws Exception {
      log.debug("Initializing TemplatesManager");

      templatesIFile = tIFile;
      templatesSystemIFolder = tSystemTemplateFolder;

      jcTemplates = JAXBContext.newInstance(Templates.class);
      jcJTBMessageTemplate = JAXBContext.newInstance(JTBMessageTemplate.class);

      ROOT_TEMPLATE_DIRECTORY_COMPARATOR = new TemplateDirectoryComparator();

      // Load and Parse Templates config file

      // Create System Template Folder if it does not exist
      if (!(tSystemTemplateFolder.exists())) {
         tSystemTemplateFolder.create(true, true, null);
      }

      if (!(templatesIFile.exists())) {
         log.warn("Template file '{}' does not exist. Creating an new empty one.", Constants.JTB_SCRIPT_FILE_NAME);
         try {
            this.templatesIFile.create(new ByteArrayInputStream(EMPTY_TEMPLATE_FILE.getBytes(ENC)), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }

         // Before v4.1.0, templatesIFile didn't exist and templates had no extension.
         // Rename all templates present in "System" dir to add the .jtb extension
         // FIXME DF
      }

      systemTemplateDirectory = buildSystemTemplateDirectory();

      templateRootDirs = parseTemplatesFile(this.templatesIFile.getContents());

      // Build list of templates directory
      reload();

      log.debug("TemplatesManager initialized");
      return mapTemplateRootDirs.size();
   }

   // ---------
   // Templates
   // ---------

   // Parse Templates File
   private List<TemplateDirectory> parseTemplatesFile(InputStream is) throws JAXBException {
      log.debug("Parsing Templates file '{}'", Constants.JTB_TEMPLATE_FILE_NAME);

      Unmarshaller u = jcTemplates.createUnmarshaller();
      templates = (Templates) u.unmarshal(is);
      return templates.getTemplateDirectory();
   }

   public boolean saveTemplates() throws JAXBException, CoreException {
      log.debug("saveTemplates");

      templates.getTemplateDirectory().clear();
      for (TemplateDirectory td : templateRootDirs) {
         if (td.isSystem()) {
            continue;
         }
         templates.getTemplateDirectory().add(td);
      }
      templatesWriteFile();

      return true;
   }

   public void reload() {
      templateRootDirs = new ArrayList<>();
      templateRootDirs.add(systemTemplateDirectory);
      templateRootDirs.addAll(templates.getTemplateDirectory());
      Collections.sort(templateRootDirs, ROOT_TEMPLATE_DIRECTORY_COMPARATOR);

      mapTemplateRootDirs = new HashMap<>(templateRootDirs.size());
      for (TemplateDirectory templateDirectory : templateRootDirs) {
         IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(templateDirectory.getDirectory()));
         mapTemplateRootDirs.put(templateDirectory, fileStore);
      }
   }

   public List<TemplateDirectory> getTemplateRootDirs() {
      return templateRootDirs;
   }

   public Map<TemplateDirectory, IFileStore> getMapTemplateRootDirs() {
      return mapTemplateRootDirs;
   }

   public IFileStore getSystemTemplateDirectoryFileStore() {
      return mapTemplateRootDirs.get(systemTemplateDirectory);
   }

   // --------
   // Read/Write Templates
   // --------
   public JTBMessageTemplate readTemplate(IFileStore templateFile) throws JAXBException, CoreException, IOException {
      log.debug("readTemplate: '{}'", templateFile);

      // Unmarshall the template as xml
      Unmarshaller u = jcJTBMessageTemplate.createUnmarshaller();
      try (BufferedInputStream bis = new BufferedInputStream(templateFile.openInputStream(EFS.NONE, new NullProgressMonitor()),
                                                             BUFFER_SIZE)) {
         return (JTBMessageTemplate) u.unmarshal(bis);
      }
   }

   public void updateTemplate(IFileStore templateFile, JTBMessageTemplate template) throws JAXBException, CoreException,
                                                                                    IOException {
      log.debug("updateTemplate: '{}'", templateFile);

      // Marshall the template to xml
      Marshaller m = jcJTBMessageTemplate.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);

      // Write the result
      try (BufferedOutputStream bos = new BufferedOutputStream(templateFile.openOutputStream(EFS.NONE, new NullProgressMonitor()),
                                                               BUFFER_SIZE)) {
         m.marshal(template, bos);
      }
   }

   public boolean createNewTemplate(Shell shell,
                                    JTBMessageTemplate template,
                                    IFileStore initialFolder,
                                    String destinationName) throws JMSException, IOException, CoreException, JAXBException {
      log.debug("createNewTemplate destinationName {}", destinationName);

      // Build suggested name
      String templateName = buildTemplateSuggestedName(destinationName, template.getJmsTimestamp());

      // Show save dialog
      TemplateSaveDialog dialog = new TemplateSaveDialog(shell,
                                                         new ArrayList(mapTemplateRootDirs.values()),
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

   // --------
   // Builders
   // --------
   private TemplateDirectory buildSystemTemplateDirectory() {
      TemplateDirectory td = new TemplateDirectory();
      td.setSystem(true);
      td.setName(Constants.TEMPLATE_FOLDER);
      td.setDirectory(templatesSystemIFolder.getLocation().toPortableString());
      return td;
   }

   // -------
   // Helpers
   // -------
   // Write Variables File
   private void templatesWriteFile() throws JAXBException, CoreException {
      log.info("Writing Templates file '{}'", Constants.JTB_TEMPLATE_FILE_NAME);

      Marshaller m = jcTemplates.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(templates, sw);

      // TODO Add the logic to temporarily save the previous file in case of crash while saving

      try {
         InputStream is = new ByteArrayInputStream(sw.toString().getBytes(ENC));
         templatesIFile.setContents(is, false, false, null);
      } catch (UnsupportedEncodingException e) {
         // Impossible
         log.error("UnsupportedEncodingException", e);
         return;
      }
   }

   public final class TemplateDirectoryComparator implements Comparator<TemplateDirectory> {

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

   private String buildTemplateSuggestedName(String destinationName, Long jmsTimestamp) {

      long dateToFormat = jmsTimestamp == null ? (new Date()).getTime() : jmsTimestamp;

      StringBuilder sb = new StringBuilder(64);
      sb.append(destinationName);
      sb.append("_");
      sb.append(TEMPLATE_NAME_SDF.format(dateToFormat));
      return sb.toString();
   }

}

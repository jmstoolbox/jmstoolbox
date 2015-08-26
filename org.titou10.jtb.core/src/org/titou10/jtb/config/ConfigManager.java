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
package org.titou10.jtb.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.wb.swt.SWTResourceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.FrameworkWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.Config;
import org.titou10.jtb.config.gen.QManagerDef;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.script.ScriptJAXBParentListener;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.Scripts;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.JarUtils;
import org.titou10.jtb.util.SLF4JConfigurator;
import org.titou10.jtb.variable.VariablesUtils;
import org.titou10.jtb.variable.gen.Variable;
import org.titou10.jtb.variable.gen.Variables;

/**
 * Bootstrap JMSToolbox, manage the configuration files and working areas
 * 
 * @author Denis Forveille
 * 
 */
@Creatable
@Singleton
@SuppressWarnings("restriction")
public class ConfigManager {

   private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);

   private static final String STARS = "**************************************************";

   private static final String EMPTY_CONFIG_FILE   = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><config></config>";
   private static final String EMPTY_VARIABLE_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><variables></variables>";
   private static final String EMPTY_SCRIPT_FILE   = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><scripts><directory name=\"Scripts\"/></scripts>";

   private IProject jtbProject;

   private IFile  configIFile;
   private Config config;

   private IFolder templateFolder;

   private IFile          variablesIFile;
   private Variables      variablesDef;
   private List<Variable> variables;

   private IFile   scriptsIFile;
   private Scripts scripts;

   private static PreferenceStore preferenceStore;

   // Business Data
   private Map<String, MetaQManager> metaQManagers = new HashMap<>();

   private List<MetaQManager> installedPlugins = new ArrayList<>();
   private List<QManager>     runningQManagers = new ArrayList<>();

   private List<JTBSession> jtbSessions = new ArrayList<>();

   // JAXB Contexts
   private JAXBContext jcConfig;
   private JAXBContext jcVariables;
   private JAXBContext jcScripts;

   // -----------------
   // Lifecycle Methods
   // -----------------

   @PostContextCreate
   public void initConfig(JTBStatusReporter jtbStatusReporter) throws JAXBException {
      System.out.println("Initializing JMSToolBox.");

      try {
         jtbProject = createOrOpenProject(null);
      } catch (CoreException e) {
         jtbStatusReporter.showError("An exception occurred while opening internal project", e, "");
         return;
      }

      // Init slf4j + logback
      // this is AFTER createOrOpenProject because createOrOpenProject initialise the directory where to put the log file...
      initSLF4J();

      // ---------------------------------------------------------
      // Initialize JAXBContexts
      // ---------------------------------------------------------
      jcConfig = JAXBContext.newInstance(Config.class);
      jcVariables = JAXBContext.newInstance(Variables.class);
      jcScripts = JAXBContext.newInstance(Scripts.class);

      // ---------------------------------------------------------
      // Configuration files + Variables + Templates + Preferences
      // ---------------------------------------------------------

      // Load and parse Config file
      try {
         configIFile = loadConfigFile(null);
         config = parseConfigFile(configIFile.getContents());
      } catch (CoreException | JAXBException e) {
         jtbStatusReporter.showError("An exception occurred while parsing Config file", e, "");
         return;
      }

      // Load and parse Variables file, initialise availabe variables
      try {
         variablesIFile = loadVariablesFile(null);
         variablesDef = parseVariablesFile(variablesIFile.getContents());
         initVariables();
      } catch (CoreException | JAXBException e) {
         jtbStatusReporter.showError("An exception occurred while parsing Variables file", e, "");
         return;
      }

      // Load and parse Scripts file
      try {
         scriptsIFile = loadScriptsFile(null);
         scripts = parseScriptsFile(scriptsIFile.getContents());

         // Bug correction: in version < v1.2.0 the empty script file was incorectly created (without the "Scripts" directory)
         if (scripts.getDirectory().isEmpty()) {
            log.warn("Invalid empty Scripts file encountered. Creating a new empty one");
            scriptsIFile.delete(true, null);
            scriptsIFile = loadScriptsFile(null);
            scripts = parseScriptsFile(scriptsIFile.getContents());
         }
      } catch (CoreException | JAXBException e) {
         jtbStatusReporter.showError("An exception occurred while parsing Scripts file", e, "");
         return;
      }

      // Create or locate Template folder
      try {
         templateFolder = locateTemplateFolder();
      } catch (CoreException e) {
         jtbStatusReporter.showError("An exception occurred while creating Template folders", e, "");
         return;
      }

      // Load preferences
      preferenceStore = loadPreferences();

      // ----------------------------------------
      // Build working QManagers from Config file
      // ----------------------------------------
      metaQManagers = new HashMap<>();
      for (QManagerDef qManagerDef : config.getQManagerDef()) {
         metaQManagers.put(qManagerDef.getId(), new MetaQManager(qManagerDef));
      }

      // ----------
      // Extensions
      // ----------

      try {
         // Discover Extensions/Plugins installed with the application
         discoverPlugins();

         // For each Extensions/Plugins, create a resource bundle to handle classparth with the associated jars files
         createResourceBundles(jtbStatusReporter);

         // Instantiate plugins
         instantiateQManagers();

      } catch (InvalidRegistryObjectException | BundleException | IOException e) {
         jtbStatusReporter.showError("An exception occurred while initialising plugins", e, "");
         return;
      }

      // Instantiate JTBSession corresponding to the sessions
      for (SessionDef sessionDef : config.getSessionDef()) {
         log.debug("SessionDef found: {}", sessionDef.getName());

         // Find the related Q Manager
         MetaQManager mdqm = metaQManagers.get(sessionDef.getQManagerDef());
         if (mdqm != null) {
            jtbSessions.add(new JTBSession(sessionDef, mdqm));
         } else {
            log.warn("Config file contains a SessionDef '{}' with QManager '{}' that does correspond to a loaded plugin. Ignoring it.",
                     sessionDef.getName(),
                     sessionDef.getQManagerDef());
         }
      }

      // Build QManager Lists
      for (MetaQManager mdqm : metaQManagers.values()) {
         if (mdqm.getIce() != null) {
            installedPlugins.add(mdqm);
         }
         if (mdqm.getQmanager() != null) {
            runningQManagers.add(mdqm.getQmanager());
         }
      }
      Collections.sort(installedPlugins);
      Collections.sort(runningQManagers);

      // Information Message
      Version v = FrameworkUtil.getBundle(ConfigManager.class).getVersion();
      int nbScripts = countScripts(scripts.getDirectory());

      log.info(STARS);
      log.info("{}",
               String.format("* JMSToolbox v%d.%d.%d successfully initialized with:", v.getMajor(), v.getMinor(), v.getMicro()));
      log.info("{}", String.format("* - %3d installed plugin", installedPlugins.size()));
      log.info("{}", String.format("* - %3d running plugins", runningQManagers.size()));
      log.info("{}", String.format("* - %3d QManagersDefs", config.getQManagerDef().size()));
      log.info("{}", String.format("* - %3d sessions", jtbSessions.size()));
      log.info("{}", String.format("* - %3d scripts", nbScripts));
      log.info("{}", String.format("* - %3d variables", variables.size()));
      log.info(STARS);
   }

   // Center Window
   @ProcessAdditions
   void processAdditions(MApplication app, EModelService modelService, Display display) {
      Monitor monitor = display.getPrimaryMonitor();
      Rectangle monitorRect = monitor.getBounds();
      MTrimmedWindow window = (MTrimmedWindow) modelService.find(Constants.MAIN_WINDOW, app);
      int windowx = monitorRect.width * 9 / 10;
      int windowy = monitorRect.height * 9 / 10;
      int x = monitorRect.x + (monitorRect.width - windowx) / 2;
      int y = monitorRect.y + (monitorRect.height - windowy) / 2;
      window.setWidth(windowx);
      window.setHeight(windowy);
      window.setX(x);
      window.setY(y);
   }

   @PreSave
   public void preSave(MApplication app, EModelService modelService) {
      log.trace("Shutting Down");
      // JobManager.shutdown();
      SWTResourceManager.dispose();
   }

   // -----------------
   // Session Managment
   // -----------------

   public void addSession(QManager qManager, SessionDef newSessionDef) throws JAXBException, CoreException {
      log.debug("addSession '{}' for Queue Manager '{}'", newSessionDef.getName(), qManager.getName());

      // Find the QManager def corresponding to the QManager
      MetaQManager mdqm = null;
      for (MetaQManager metaQManager : metaQManagers.values()) {
         if (metaQManager.getDisplayName().equals(qManager.getName())) {
            mdqm = metaQManager;
            break;
         }
      }

      // Appends when a new QM is used in a new session but does not yet exists in the config file
      QManagerDef qManagerDef = mdqm.getqManagerDef();
      if (qManagerDef == null) {
         qManagerDef = createNewQManagerDef(mdqm);
      }
      newSessionDef.setQManagerDef(mdqm.getId());

      // Add the session def to the configuration file
      config.getSessionDef().add(newSessionDef);
      Collections.sort(config.getSessionDef(), new Comparator<SessionDef>() {
         @Override
         public int compare(SessionDef o1, SessionDef o2) {
            return o1.getName().compareTo(o2.getName());
         }
      });
      writeConfigFile();

      // Create the new JTB Session and add it to the current config
      JTBSession newJTBSession = new JTBSession(newSessionDef, mdqm);
      jtbSessions.add(newJTBSession);
      Collections.sort(jtbSessions);
   }

   public void editSession() throws JAXBException, CoreException {
      log.debug("editSession");
      writeConfigFile();
   }

   public void removeSession(JTBSession jtbSession) throws JAXBException, CoreException {
      log.debug("removeSession {}", jtbSession);

      // Remove the session from the defintions of sessions
      SessionDef sessionDef = getSessionDefFromJTBSession(jtbSession);
      config.getSessionDef().remove(sessionDef);

      // Remove the session from the current config
      jtbSessions.remove(jtbSession);

      // Write the new Config file
      writeConfigFile();
   }

   public void duplicateSession(JTBSession sourceJTBSession, String newName) throws JAXBException, CoreException {
      log.debug("duplicateSession {} to '{}'", sourceJTBSession, newName);

      SessionDef sourceSessionDef = sourceJTBSession.getSessionDef();

      // Create the new Session Def
      SessionDef newSessionDef = new SessionDef();
      newSessionDef.setFolder(sourceSessionDef.getFolder());
      newSessionDef.setHost(sourceSessionDef.getHost());
      newSessionDef.setPassword(sourceSessionDef.getPassword());
      newSessionDef.setPort(sourceSessionDef.getPort());
      newSessionDef.setProperties(sourceSessionDef.getProperties());
      newSessionDef.setQManagerDef(sourceSessionDef.getQManagerDef());
      newSessionDef.setUserid(sourceSessionDef.getUserid());

      newSessionDef.setName(newName);

      // Add the session def to the configuration file
      config.getSessionDef().add(newSessionDef);
      Collections.sort(config.getSessionDef(), new Comparator<SessionDef>() {
         @Override
         public int compare(SessionDef o1, SessionDef o2) {
            return o1.getName().compareTo(o2.getName());
         }
      });
      writeConfigFile();

      // Create the new JTB Session and add it to the current config
      JTBSession newJTBSession = new JTBSession(newSessionDef, sourceJTBSession.getMdqm());
      jtbSessions.add(newJTBSession);
      Collections.sort(jtbSessions);
   }

   public SessionDef getSessionDefByName(String sessionDefName) {
      for (SessionDef sessionDef : config.getSessionDef()) {
         if (sessionDef.getName().equalsIgnoreCase(sessionDefName)) {
            return sessionDef;
         }
      }
      return null;
   }

   public JTBSession getJTBSessionByName(String sessionName) {
      for (JTBSession jtbSession : jtbSessions) {
         if (jtbSession.getName().equals(sessionName)) {
            return jtbSession;
         }
      }
      return null;
   }

   public List<JTBSession> getJtbSessions() {
      return jtbSessions;
   }

   // -----------------------
   // QManagerDefs Managment
   // -----------------------

   public boolean saveConfigQManager(MetaQManager metaQManager, SortedSet<String> jarNames) throws JAXBException, CoreException {

      QManagerDef qManagerDef = metaQManager.getqManagerDef();

      List<String> jars = qManagerDef.getJar();
      jars.clear();
      jars.addAll(jarNames);

      // No need to update othere cache as the application will restart..

      writeConfigFile();

      return true;
   }

   public QManagerDef createNewQManagerDef(MetaQManager mdqm) {
      log.warn("No QManager with name '{}' found in config. Creating a new one", mdqm.getDisplayName());
      QManagerDef qmd = new QManagerDef();
      qmd.setName(mdqm.getDisplayName());
      qmd.setId(mdqm.getId());

      mdqm.setqManagerDef(qmd);

      config.getQManagerDef().add(qmd);
      Collections.sort(config.getQManagerDef(), new Comparator<QManagerDef>() {
         @Override
         public int compare(QManagerDef o1, QManagerDef o2) {
            return o1.getName().compareTo(o2.getName());
         }
      });
      return qmd;
   }

   // -------
   // Helpers
   // -------

   private void initSLF4J() {
      String logFileName = jtbProject.getLocation().append(Constants.JTB_LOG_FILE_NAME).toOSString();
      System.setProperty(Constants.JTB_PROPERTY_FILE_NAME, logFileName);
      SLF4JConfigurator.configure();
   }

   private SessionDef getSessionDefFromJTBSession(JTBSession jtbSession) {
      List<SessionDef> sessionDefs = config.getSessionDef();
      for (SessionDef sessionDef : sessionDefs) {
         if (sessionDef.getName().equals(jtbSession.getName())) {
            return sessionDef;
         }
      }
      return null;
   }

   private void discoverPlugins() {

      IExtensionRegistry registry = Platform.getExtensionRegistry();
      IConfigurationElement[] plugins = registry.getConfigurationElementsFor(Constants.JTB_EXTENSION_POINT);
      for (int i = 0; i < plugins.length; i++) {
         IConfigurationElement ice = plugins[i];
         log.debug("Extension/Plugin found: '{}'", ice.getNamespaceIdentifier());

         // Add or update the WorkingQManager
         String id = ice.getNamespaceIdentifier();
         MetaQManager wqm = metaQManagers.get(id);
         if (wqm == null) {
            metaQManagers.put(ice.getNamespaceIdentifier(), new MetaQManager(ice));
         } else {
            wqm.setIce(ice);
         }
      }
   }

   // Create one resource bundle with classpath per plugin found
   private void createResourceBundles(JTBStatusReporter jtbStatusReporter) throws BundleException, InvalidRegistryObjectException,
                                                                           IOException {

      BundleContext ctx = InternalPlatform.getDefault().getBundleContext();
      Bundle thisBundle = FrameworkUtil.getBundle(this.getClass());
      String workDirectry = Platform.getStateLocation(thisBundle).toString();

      for (String pluginId : metaQManagers.keySet()) {

         // Check if there is a config for this plugins
         QManagerDef qManagerDef = metaQManagers.get(pluginId).getqManagerDef();
         if (qManagerDef != null) {

            // Dynamically create a bundle with the library in its classpath and start it
            String fileName;
            try {
               fileName = JarUtils.createBundle(workDirectry, pluginId, qManagerDef.getJar());
            } catch (Exception e) {
               jtbStatusReporter.showError("An exception occurred while initializig the application : " + e.getMessage(), null);
               return;
            }
            if (fileName != null) {
               Bundle resourceBundle = ctx.installBundle("file:" + fileName);
               resourceBundle.start();
               log.debug("State of resource bundle after start {}", resourceBundle.getState());
            }
         } else {
            log.warn("No 'QManagerDef' found in config file for pluginId '{}'. No resource Bundle will be created for it.",
                     pluginId);
         }
      }
   }

   private void instantiateQManagers() {

      BundleContext ctx = InternalPlatform.getDefault().getBundleContext();

      if (log.isDebugEnabled()) {
         for (Bundle aa : ctx.getBundles()) {
            if (aa.getLocation().contains("titou")) {
               log.debug("OSGI Bundle for JTBToolBox found : {}", aa.getLocation());
            }
         }
      }

      for (MetaQManager wqm : metaQManagers.values()) {
         IConfigurationElement ice = wqm.getIce();

         // Do not try to instantiate plugins that are not active..
         if (ice == null) {
            continue;
         }

         log.debug("About to instantiate QM. id: '{}' classname: '{}'", wqm.getId(), wqm.getPluginClassName());

         // Instanciate QManager
         Object o;
         try {
            o = ice.createExecutableExtension(Constants.JTB_EXTENSION_POINT_CLASS_ATTR);
            // Yes, we catch Error to capture compilation errors dues to invalid/missing jars..
         } catch (Error | CoreException e) {
            log.error("Probleme when instatiating '{}'. Skip it", ice.getNamespaceIdentifier(), e);
            continue;
         }
         if (o instanceof QManager) {

            // Update WorkingQManager
            QManager qm = (QManager) o;
            wqm.setQmanager(qm);
            qm.setName(wqm.getDisplayName());
            log.info("Instantiated Queue Manager '{}'", wqm.getDisplayName());

            if (log.isDebugEnabled()) {
               FrameworkWiring xx = ctx.getBundle(0).adapt(FrameworkWiring.class);
               Bundle qqq = FrameworkUtil.getBundle(o.getClass());
               log.debug("bundle closure  : {}", xx.getDependencyClosure(Collections.singletonList(qqq)));
               log.debug("bundle headers  : {}", qqq.getHeaders());
               log.debug("bundle state    : {}", qqq.getState());
               log.debug("bundle location : {}", qqq.getLocation());
            }
         }
      }
      if (log.isDebugEnabled()) {
         for (Bundle aa : ctx.getBundles()) {
            if (aa.getLocation().contains("titou")) {
               log.debug("OSGI Bundle for JTBToolBox found : {}", aa.getLocation());
            }
         }
      }
   }

   private IProject createOrOpenProject(IProgressMonitor monitor) throws CoreException {
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      IProject project = root.getProject(Constants.JTB_CONFIG_PROJECT);

      if (project.exists()) {
         log.debug("Project {} already exists. Use it.", Constants.JTB_CONFIG_PROJECT);
         project.open(monitor);
      } else {
         log.debug("Project {} does not exist.", Constants.JTB_CONFIG_PROJECT);
         project.create(monitor);
         project.open(monitor);
      }
      project.refreshLocal(IResource.DEPTH_INFINITE, null);
      return project;
   }

   private IFolder locateTemplateFolder() throws CoreException {
      IFolder templateFolder = jtbProject.getFolder(Constants.TEMPLATE_FOLDER);
      if (!(templateFolder.exists())) {
         templateFolder.create(true, true, null);
      }
      return templateFolder;
   }

   private PreferenceStore loadPreferences() {
      String preferenceFileName = jtbProject.getLocation().toOSString() + File.separatorChar + Constants.PREFERENCE_FILE_NAME;
      log.debug("preferenceFileName : {}", preferenceFileName);
      PreferenceStore ps = new PreferenceStore(preferenceFileName);
      try {
         ps.load();
      } catch (IOException e) {
         // NOP
      }

      // Set DefaultValues
      ps.setDefault(Constants.PREF_MAX_MESSAGES, Constants.PREF_MAX_MESSAGES_DEFAULT);
      ps.setDefault(Constants.PREF_AUTO_REFRESH_DELAY, Constants.PREF_AUTO_REFRESH_DELAY_DEFAULT);
      ps.setDefault(Constants.PREF_SHOW_SYSTEM_OBJECTS, Constants.PREF_SHOW_SYSTEM_OBJECTS_DEFAULT);

      return ps;
   }

   // -----------
   // Config File
   // -----------

   private IFile loadConfigFile(IProgressMonitor monitor) {

      IFile file = jtbProject.getFile(Constants.JTB_CONFIG_FILE_NAME);
      if (!(file.exists())) {
         log.warn("Config file '{}' does not exist. Creating an new empty one.", Constants.JTB_CONFIG_FILE_NAME);
         try {
            file.create(new ByteArrayInputStream(EMPTY_CONFIG_FILE.getBytes("UTF-8")), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }
      }

      return file;
   }

   // Parse Config File into Config Object
   private Config parseConfigFile(InputStream is) throws JAXBException {
      log.debug("Parsing file '{}'", Constants.JTB_CONFIG_FILE_NAME);

      Unmarshaller u = jcConfig.createUnmarshaller();
      return (Config) u.unmarshal(is);
   }

   // Write Config File
   private void writeConfigFile() throws JAXBException, CoreException {
      log.info("Writing file '{}'", Constants.JTB_CONFIG_FILE_NAME);

      Marshaller m = jcConfig.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(config, sw);

      // TODO Ajouter la logique pour sauver temporairement le fichier de config precedent en cas de crash

      InputStream is = new ByteArrayInputStream(sw.toString().getBytes());
      configIFile.setContents(is, false, false, null);

   }

   public boolean importConfiguration(String configFileName) throws JAXBException, CoreException, FileNotFoundException {

      // Try to parse the given file
      File f = new File(configFileName);
      config = parseConfigFile(new FileInputStream(f));

      if (config == null) {
         return false;
      }

      // Write the config file
      writeConfigFile();

      return true;
   }

   public void exportConfiguration(String configFileName) throws IOException, CoreException {
      Files.copy(configIFile.getContents(), Paths.get(configFileName), StandardCopyOption.REPLACE_EXISTING);
   }

   // ---------
   // Variables
   // ---------

   private IFile loadVariablesFile(IProgressMonitor monitor) {

      IFile file = jtbProject.getFile(Constants.JTB_VARIABLE_FILE_NAME);
      if (!(file.exists())) {
         log.warn("Variables file '{}' does not exist. Creating an new empty one.", Constants.JTB_VARIABLE_FILE_NAME);
         try {
            file.create(new ByteArrayInputStream(EMPTY_VARIABLE_FILE.getBytes("UTF-8")), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }
      }

      return file;
   }

   // Parse Variables File into Variables Object
   private Variables parseVariablesFile(InputStream is) throws JAXBException {
      log.debug("Parsing Variable file '{}'", Constants.JTB_VARIABLE_FILE_NAME);

      Unmarshaller u = jcVariables.createUnmarshaller();
      return (Variables) u.unmarshal(is);
   }

   public boolean saveVariables() throws JAXBException, CoreException {
      log.debug("saveVariables");
      variablesDef.getVariable().clear();
      for (Variable v : variables) {
         if (v.isSystem()) {
            continue;
         }
         variablesDef.getVariable().add(v);
      }
      writeVariablesFile();

      return true;
   }

   public void initVariables() {
      List<Variable> listVariables = new ArrayList<>();
      listVariables.addAll(variablesDef.getVariable());
      listVariables.addAll(VariablesUtils.getSystemVariables());
      Collections.sort(listVariables, new Comparator<Variable>() {
         @Override
         public int compare(Variable o1, Variable o2) {
            // System variables first
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
      });
      log.debug("{} variable(s) loaded.", listVariables.size());
      variables = listVariables;
   }

   // Write Variables File
   private void writeVariablesFile() throws JAXBException, CoreException {
      log.info("Writing Variable file '{}'", Constants.JTB_VARIABLE_FILE_NAME);

      Marshaller m = jcVariables.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(variablesDef, sw);

      // TODO Ajouter la logique pour sauver temporairement le fichier de variables precedent en cas de crash

      InputStream is = new ByteArrayInputStream(sw.toString().getBytes());
      variablesIFile.setContents(is, false, false, null);
   }

   public boolean importVariables(String variableFileName) throws JAXBException, CoreException, FileNotFoundException {

      // Try to parse the given file
      File f = new File(variableFileName);
      Variables newVars = parseVariablesFile(new FileInputStream(f));

      if (newVars == null) {
         return false;
      }

      // Merge variables
      List<Variable> mergedVariables = new ArrayList<>(variablesDef.getVariable());
      for (Variable v : newVars.getVariable()) {
         // If a variablw with the same name exist, replace it
         for (Variable temp : variablesDef.getVariable()) {
            if (temp.getName().equals(v.getName())) {
               mergedVariables.remove(temp);
            }
         }
         mergedVariables.add(v);
      }
      variablesDef.getVariable().clear();
      variablesDef.getVariable().addAll(mergedVariables);

      // Write the variable file
      writeVariablesFile();

      // int variables
      initVariables();

      return true;
   }

   public void exportVariables(String variableFileName) throws IOException, CoreException {
      Files.copy(variablesIFile.getContents(), Paths.get(variableFileName), StandardCopyOption.REPLACE_EXISTING);
   }

   public List<Variable> getVariables() {
      return variables;
   }

   // ---------
   // Scripts
   // ---------
   private IFile loadScriptsFile(IProgressMonitor monitor) {

      IFile file = jtbProject.getFile(Constants.JTB_SCRIPT_FILE_NAME);
      if (!(file.exists())) {
         log.warn("Scripts file '{}' does not exist. Creating an new empty one.", Constants.JTB_SCRIPT_FILE_NAME);
         try {
            file.create(new ByteArrayInputStream(EMPTY_SCRIPT_FILE.getBytes("UTF-8")), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }
      }

      return file;
   }

   // Parse Script File
   private Scripts parseScriptsFile(InputStream is) throws JAXBException {
      log.debug("Parsing Script file '{}'", Constants.JTB_SCRIPT_FILE_NAME);

      Unmarshaller u = jcScripts.createUnmarshaller();
      u.setListener(new ScriptJAXBParentListener());
      return (Scripts) u.unmarshal(is);
   }

   // Write Variables File
   public void writeScriptFile() throws JAXBException, CoreException {
      log.info("Writing file '{}'", Constants.JTB_SCRIPT_FILE_NAME);

      Marshaller m = jcScripts.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(scripts, sw);

      // TODO Ajouter la logique pour sauver temporairement le fichier de scripts precedent en cas de crash

      InputStream is = new ByteArrayInputStream(sw.toString().getBytes());
      scriptsIFile.setContents(is, false, false, null);
   }

   public boolean importScripts(String scriptsFileName) throws JAXBException, CoreException, FileNotFoundException {

      // Try to parse the given file
      File f = new File(scriptsFileName);
      Scripts newScripts = parseScriptsFile(new FileInputStream(f));

      if (newScripts == null) {
         return false;
      }

      // TODO Merge instead of replace
      scripts = newScripts;

      // Write the variable file
      writeScriptFile();

      return true;
   }

   public void exportScripts(String scriptsFileName) throws IOException, CoreException {
      Files.copy(scriptsIFile.getContents(), Paths.get(scriptsFileName), StandardCopyOption.REPLACE_EXISTING);
   }

   private int countScripts(List<Directory> dirs) {
      int nb = 0;
      for (Directory directory : dirs) {
         nb += directory.getScript().size();
         nb += countScripts(directory.getDirectory());
      }
      return nb;
   }

   public Scripts getScripts() {
      return scripts;
   }

   // -------
   // Getters
   // -------

   public IFolder getTemplateFolder() {
      return templateFolder;
   }

   public List<MetaQManager> getInstalledPlugins() {
      return installedPlugins;
   }

   public List<QManager> getRunningQManagers() {
      return runningQManagers;
   }

   public PreferenceStore getPreferenceStore() {
      return preferenceStore;
   }

   // TODO eurk..
   public static PreferenceStore getPreferenceStore2() {
      return preferenceStore;
   }

}

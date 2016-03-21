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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
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
import org.eclipse.jface.preference.PreferencePage;
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
import org.titou10.jtb.connector.ExternalConfigManager;
import org.titou10.jtb.connector.ExternalConnector;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.script.ScriptJAXBParentListener;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.Scripts;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.JarUtils;
import org.titou10.jtb.util.SLF4JConfigurator;
import org.titou10.jtb.util.TrustEverythingSSLTrustManager;
import org.titou10.jtb.variable.VariablesUtils;
import org.titou10.jtb.variable.gen.Variable;
import org.titou10.jtb.variable.gen.Variables;

/**
 * Bootstrap JMSToolBox, manage the configuration files and working areas
 * 
 * @author Denis Forveille
 * 
 */
@Creatable
@Singleton
@SuppressWarnings("restriction")
public class ConfigManager {

   private static final Logger       log                   = LoggerFactory.getLogger(ConfigManager.class);

   private static final String       STARS                 = "***************************************************";

   private static final String       ENC                   = "UTF-8";

   private static final String       EMPTY_CONFIG_FILE     = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><config></config>";
   private static final String       EMPTY_VARIABLE_FILE   = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><variables></variables>";
   private static final String       EMPTY_SCRIPT_FILE     = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><scripts><directory name=\"Scripts\"/></scripts>";

   @Inject
   private IExtensionRegistry        registry;

   private IProject                  jtbProject;

   private IFile                     configIFile;
   private Config                    config;

   private IFolder                   templateFolder;

   private IFile                     variablesIFile;
   private Variables                 variablesDef;
   private List<Variable>            variables;

   private IFile                     scriptsIFile;
   private Scripts                   scripts;

   private static PreferenceStore    preferenceStore;
   private List<ExternalConnector>   ecWithPreferencePages = new ArrayList<>();

   // Business Data
   private Map<String, MetaQManager> metaQManagers         = new HashMap<>();

   private List<MetaQManager>        installedPlugins      = new ArrayList<>();
   private List<QManager>            runningQManagers      = new ArrayList<>();

   private List<JTBSession>          jtbSessions           = new ArrayList<>();

   private int                       nbExternalConnectors;

   // JAXB Contexts
   private JAXBContext               jcConfig;
   private JAXBContext               jcVariables;
   private JAXBContext               jcScripts;

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
         configIFile = configurationLoadFile(null);
         config = configurationParseFile(configIFile.getContents());
      } catch (CoreException | JAXBException e) {
         jtbStatusReporter.showError("An exception occurred while parsing Config file", e, "");
         return;
      }

      // Load and parse Variables file, initialise availabe variables
      try {
         variablesIFile = variablesLoadFile(null);
         variablesDef = variablesParseFile(variablesIFile.getContents());
         variablesInit();
      } catch (CoreException | JAXBException e) {
         jtbStatusReporter.showError("An exception occurred while parsing Variables file", e, "");
         return;
      }

      // Load and parse Scripts file
      try {
         scriptsIFile = scriptsLoadFile(null);
         scripts = scriptsParseFile(scriptsIFile.getContents());

         // Bug correction: in version < v1.2.0 the empty script file was incorectly created (without the "Scripts" directory)
         if (scripts.getDirectory().isEmpty()) {
            log.warn("Invalid empty Scripts file encountered. Creating a new empty one");
            scriptsIFile.delete(true, null);
            scriptsIFile = scriptsLoadFile(null);
            scripts = scriptsParseFile(scriptsIFile.getContents());
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

      // ----------------------------------------
      // Load preferences
      // ----------------------------------------
      preferenceStore = loadPreferences();

      // ----------------------------------------
      // Apply TrustEverythingSSLTrustManager is required
      // ----------------------------------------
      boolean trustAllCertificates = preferenceStore.getBoolean(Constants.PREF_TRUST_ALL_CERTIFICATES);
      if (trustAllCertificates) {
         // Accept all Untrust Certificates
         try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] { new TrustEverythingSSLTrustManager() }, null);
            SSLContext.setDefault(ctx);
            log.warn("Using the TrustEverythingSSLTrustManager TrustManager: No server certificate will be validated");
         } catch (NoSuchAlgorithmException | KeyManagementException e) {
            jtbStatusReporter.showError("An exception occurred while using the TrustAllCertificatesManager", e, "");
            return;
         }
      }

      // ----------------------------------------
      // Build working QManagers from Config file
      // ----------------------------------------
      metaQManagers = new HashMap<>();
      for (QManagerDef qManagerDef : config.getQManagerDef()) {
         metaQManagers.put(qManagerDef.getId(), new MetaQManager(qManagerDef));
      }

      // ---------------------
      // QM Plugins Extensions
      // ---------------------
      try {
         // Discover Extensions/Plugins installed with the application
         discoverQMPlugins();

         // For each Extensions/Plugins, create a resource bundle to handle classparth with the associated jars files
         createResourceBundles(jtbStatusReporter);

         // Instantiate plugins
         instantiateQManagers();

      } catch (InvalidRegistryObjectException | BundleException | IOException e) {
         jtbStatusReporter.showError("An exception occurred while initializing plugins", e, "");
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

      // -----------------------------
      // Connectors Plugins Extensions
      // -----------------------------
      // Discover Connectors Plugins installed with the application
      try {
         discoverAndInitializeConnectorsPlugins();
      } catch (Exception e) {
         // This is not a reason to not start..
         jtbStatusReporter.showError(
                                     "An exception occurred while initializing external connector plugins. Some functions may not work",
                                     e,
                                     "");
      }

      // ---------------------
      // Information Message
      // ---------------------
      Version v = FrameworkUtil.getBundle(ConfigManager.class).getVersion();
      int nbScripts = scriptsCount(scripts.getDirectory());

      log.info(STARS);
      log.info("{}",
               String.format("* JMSToolBox v%d.%d.%d successfully initialized with:", v.getMajor(), v.getMinor(), v.getMicro()));
      log.info("{}", String.format("* - %3d installed plugins", installedPlugins.size()));
      log.info("{}", String.format("* - %3d running plugins", runningQManagers.size()));
      log.info("{}", String.format("* - %3d external connector plugins", nbExternalConnectors));
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

   // -------
   // Helpers
   // -------

   private void initSLF4J() {
      String logFileName = jtbProject.getLocation().append(Constants.JTB_LOG_FILE_NAME).toOSString();
      System.setProperty(Constants.JTB_PROPERTY_FILE_NAME, logFileName);
      SLF4JConfigurator.configure();
   }

   // -----------------
   // Connector Plugins
   // -----------------

   private SessionDef getSessionDefFromJTBSession(JTBSession jtbSession) {
      List<SessionDef> sessionDefs = config.getSessionDef();
      for (SessionDef sessionDef : sessionDefs) {
         if (sessionDef.getName().equals(jtbSession.getName())) {
            return sessionDef;
         }
      }
      return null;
   }

   private void discoverAndInitializeConnectorsPlugins() throws Exception {

      IConfigurationElement[] plugins = registry.getConfigurationElementsFor(Constants.JTB_EXTENSION_POINT_EC);
      for (IConfigurationElement ice : plugins) {
         String name = ice.getNamespaceIdentifier();
         log.debug("External Connector found: '{}'", name);

         // Instanciate External Connector
         Object o;
         try {
            o = ice.createExecutableExtension(Constants.JTB_EXTENSION_POINT_EC_CLASS_ATTR);
         } catch (Error | CoreException e) {
            log.error("Problem when initializing External Connectors '{}'. Skip it", name, e);
            continue;
         }
         if (o instanceof ExternalConnector) {
            ExternalConfigManager ecm = new ExternalConfigManager(this);

            ExternalConnector ec = (ExternalConnector) o;

            // Get PP before initializing in case init goes bad, this way user can change the port for example..
            PreferencePage pp = ec.getPreferencePage();
            if (pp != null) {
               ecWithPreferencePages.add(ec);
            }

            nbExternalConnectors++;

            ec.initialize(ecm);
            // executeExtension(ec, this);

            log.info("External connector '{}' initialized.", name);
         }
      }
   }

   // private void executeExtension(final ExternalConnector ec, final ConfigManager cm) {
   // ISafeRunnable runnable = new ISafeRunnable() {
   // @Override
   // public void handleException(Throwable e) {
   // System.out.println("Exception in client");
   // }
   //
   // @Override
   // public void run() throws Exception {
   // ExternalConfigManager ecm = new ExternalConfigManager(cm);
   // ec.initialize(ecm);
   // }
   // };
   // SafeRunner.run(runnable);
   // }

   // -----------------
   // QM Plugins
   // -----------------

   private void discoverQMPlugins() {

      IConfigurationElement[] plugins = registry.getConfigurationElementsFor(Constants.JTB_EXTENSION_POINT_QM);
      for (IConfigurationElement ice : plugins) {
         log.debug("QM plugin found: '{}'", ice.getNamespaceIdentifier());

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
   private void createResourceBundles(JTBStatusReporter jtbStatusReporter) throws BundleException,
                                                                           InvalidRegistryObjectException,
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
               log.debug("OSGI Bundle for JMSToolBox found : {}", aa.getLocation());
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
            o = ice.createExecutableExtension(Constants.JTB_EXTENSION_POINT_QM_CLASS_ATTR);
            // Yes, we catch Error to capture compilation errors dues to invalid/missing jars..
         } catch (Error | CoreException e) {
            log.error("Problem when instatiating '{}'. Skip it", ice.getNamespaceIdentifier(), e);
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

   public IFolder locateTemplateFolder() throws CoreException {
      IFolder templateFolder = jtbProject.getFolder(Constants.TEMPLATE_FOLDER);
      if (!(templateFolder.exists())) {
         templateFolder.create(true, true, null);
      }
      return templateFolder;
   }

   // -----------------------
   // QManagerDefs Managment
   // -----------------------

   public QManagerDef qManagerDefAdd(MetaQManager mdqm) {
      log.warn("No QManager with name '{}' found in config. Creating a new one", mdqm.getDisplayName());
      QManagerDef qmd = new QManagerDef();
      qmd.setName(mdqm.getDisplayName());
      qmd.setId(mdqm.getId());

      mdqm.setqManagerDef(qmd);

      config.getQManagerDef().add(qmd);
      Collections.sort(config.getQManagerDef(), new QManagerDefComparator());
      return qmd;
   }

   private static final class QManagerDefComparator implements Comparator<QManagerDef> {
      @Override
      public int compare(QManagerDef o1, QManagerDef o2) {
         return o1.getName().compareTo(o2.getName());
      }
   }

   // -----------------
   // Session Managment
   // -----------------

   public void sessionAdd(QManager qManager, SessionDef newSessionDef) throws JAXBException, CoreException {
      log.debug("sessionAdd '{}' for Queue Manager '{}'", newSessionDef.getName(), qManager.getName());

      // Find the QManager def corresponding to the QManager
      MetaQManager mdqm = null;
      for (MetaQManager metaQManager : metaQManagers.values()) {
         if (metaQManager.getDisplayName().equals(qManager.getName())) {
            mdqm = metaQManager;
            break;
         }
      }

      // Happens when a new QM is used in a new session but does not yet exists in the config file
      QManagerDef qManagerDef = mdqm.getqManagerDef();
      if (qManagerDef == null) {
         qManagerDef = qManagerDefAdd(mdqm);
      }
      newSessionDef.setQManagerDef(mdqm.getId());

      // Add the session def to the configuration file
      config.getSessionDef().add(newSessionDef);
      Collections.sort(config.getSessionDef(), new SessionDefComparator());
      configurationWriteFile();

      // Create the new JTB Session and add it to the current config
      JTBSession newJTBSession = new JTBSession(newSessionDef, mdqm);
      jtbSessions.add(newJTBSession);
      Collections.sort(jtbSessions);
   }

   public void sessionEdit() throws JAXBException, CoreException {
      log.debug("sessionEdit");
      configurationWriteFile();
   }

   public void sessionRemove(JTBSession jtbSession) throws JAXBException, CoreException {
      log.debug("sessionRemove {}", jtbSession);

      // Remove the session from the defintions of sessions
      SessionDef sessionDef = getSessionDefFromJTBSession(jtbSession);
      config.getSessionDef().remove(sessionDef);

      // Remove the session from the current config
      jtbSessions.remove(jtbSession);

      // Write the new Config file
      configurationWriteFile();
   }

   public void sessionDuplicate(JTBSession sourceJTBSession, String newName) throws JAXBException, CoreException {
      log.debug("sessionDuplicate {} to '{}'", sourceJTBSession, newName);

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
      Collections.sort(config.getSessionDef(), new SessionDefComparator());
      configurationWriteFile();

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

   private static final class SessionDefComparator implements Comparator<SessionDef> {
      @Override
      public int compare(SessionDef o1, SessionDef o2) {
         return o1.getName().compareTo(o2.getName());
      }
   }

   // -----------
   // Preferences
   // -----------

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
      ps.setDefault(Constants.PREF_TRUST_ALL_CERTIFICATES, Constants.PREF_TRUST_ALL_CERTIFICATES_DEFAULT);
      ps.setDefault(Constants.PREF_CLEAR_LOGS_EXECUTION, Constants.PREF_CLEAR_LOGS_EXECUTION_DEFAULT);

      return ps;
   }

   public List<PreferencePage> getPluginsPreferencePages() {
      List<PreferencePage> res = new ArrayList<>();

      for (ExternalConnector ec : ecWithPreferencePages) {
         res.add(ec.getPreferencePage());
      }
      return res;
   }

   public PreferenceStore getPreferenceStore() {
      return preferenceStore;
   }

   // TODO eurk..
   public static PreferenceStore getPreferenceStore2() {
      return preferenceStore;
   }
   // ------------------
   // Configuration File
   // ------------------

   public boolean configurationImport(String configFileName) throws JAXBException, CoreException, FileNotFoundException {

      // Try to parse the given file
      File f = new File(configFileName);
      config = configurationParseFile(new FileInputStream(f));

      if (config == null) {
         return false;
      }

      // Write the config file
      configurationWriteFile();

      return true;
   }

   public void configurationExport(String configFileName) throws IOException, CoreException {
      Files.copy(configIFile.getContents(), Paths.get(configFileName), StandardCopyOption.REPLACE_EXISTING);
   }

   public boolean configurationSave(MetaQManager metaQManager, SortedSet<String> jarNames) throws JAXBException, CoreException {

      QManagerDef qManagerDef = metaQManager.getqManagerDef();

      List<String> jars = qManagerDef.getJar();
      jars.clear();
      jars.addAll(jarNames);

      // No need to update other cache as the application will restart..

      configurationWriteFile();

      return true;
   }

   private IFile configurationLoadFile(IProgressMonitor monitor) {

      IFile file = jtbProject.getFile(Constants.JTB_CONFIG_FILE_NAME);
      if (!(file.exists())) {
         log.warn("Config file '{}' does not exist. Creating an new empty one.", Constants.JTB_CONFIG_FILE_NAME);
         try {
            file.create(new ByteArrayInputStream(EMPTY_CONFIG_FILE.getBytes(ENC)), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }
      }

      return file;
   }

   // Parse Config File into Config Object
   private Config configurationParseFile(InputStream is) throws JAXBException {
      log.debug("configurationParseFile file '{}'", Constants.JTB_CONFIG_FILE_NAME);

      Unmarshaller u = jcConfig.createUnmarshaller();
      return (Config) u.unmarshal(is);
   }

   // Write Config File
   private void configurationWriteFile() throws JAXBException, CoreException {
      log.info("configurationWriteFile file '{}'", Constants.JTB_CONFIG_FILE_NAME);

      Marshaller m = jcConfig.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(config, sw);

      // TODO Add the logic to temporarily save the previous file in case of crash while saving

      try {
         InputStream is = new ByteArrayInputStream(sw.toString().getBytes(ENC));
         configIFile.setContents(is, false, false, null);
      } catch (UnsupportedEncodingException e) {
         // Impossible
         log.error("UnsupportedEncodingException", e);
         return;
      }
   }

   // ---------
   // Variables
   // ---------

   public boolean variablesImport(String variableFileName) throws JAXBException, CoreException, FileNotFoundException {

      // Try to parse the given file
      File f = new File(variableFileName);
      Variables newVars = variablesParseFile(new FileInputStream(f));

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
      variablesWriteFile();

      // int variables
      variablesInit();

      return true;
   }

   public void variablesExport(String variableFileName) throws IOException, CoreException {
      Files.copy(variablesIFile.getContents(), Paths.get(variableFileName), StandardCopyOption.REPLACE_EXISTING);
   }

   public void variablesInit() {
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

   private IFile variablesLoadFile(IProgressMonitor monitor) {

      IFile file = jtbProject.getFile(Constants.JTB_VARIABLE_FILE_NAME);
      if (!(file.exists())) {
         log.warn("Variables file '{}' does not exist. Creating an new empty one.", Constants.JTB_VARIABLE_FILE_NAME);
         try {
            file.create(new ByteArrayInputStream(EMPTY_VARIABLE_FILE.getBytes(ENC)), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }
      }

      return file;
   }

   // Parse Variables File into Variables Object
   private Variables variablesParseFile(InputStream is) throws JAXBException {
      log.debug("Parsing Variable file '{}'", Constants.JTB_VARIABLE_FILE_NAME);

      Unmarshaller u = jcVariables.createUnmarshaller();
      return (Variables) u.unmarshal(is);
   }

   public boolean variablesSave() throws JAXBException, CoreException {
      log.debug("variablesSave");

      variablesDef.getVariable().clear();
      for (Variable v : variables) {
         if (v.isSystem()) {
            continue;
         }
         variablesDef.getVariable().add(v);
      }
      variablesWriteFile();

      return true;
   }

   // Write Variables File
   private void variablesWriteFile() throws JAXBException, CoreException {
      log.info("Writing Variable file '{}'", Constants.JTB_VARIABLE_FILE_NAME);

      Marshaller m = jcVariables.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(variablesDef, sw);

      // TODO Add the logic to temporarily save the previous file in case of crash while saving

      try {
         InputStream is = new ByteArrayInputStream(sw.toString().getBytes(ENC));
         variablesIFile.setContents(is, false, false, null);
      } catch (UnsupportedEncodingException e) {
         // Impossible
         log.error("UnsupportedEncodingException", e);
         return;
      }
   }

   public List<Variable> getVariables() {
      return variables;
   }

   // ---------
   // Scripts
   // ---------

   public boolean scriptsImport(String scriptsFileName) throws JAXBException, CoreException, FileNotFoundException {

      // Try to parse the given file
      File f = new File(scriptsFileName);
      Scripts newScripts = scriptsParseFile(new FileInputStream(f));

      if (newScripts == null) {
         return false;
      }

      // TODO Merge instead of replace
      scripts = newScripts;

      // Write the variable file
      scriptsWriteFile();

      return true;
   }

   public void scriptsExport(String scriptsFileName) throws IOException, CoreException {
      Files.copy(scriptsIFile.getContents(), Paths.get(scriptsFileName), StandardCopyOption.REPLACE_EXISTING);
   }

   private IFile scriptsLoadFile(IProgressMonitor monitor) {

      IFile file = jtbProject.getFile(Constants.JTB_SCRIPT_FILE_NAME);
      if (!(file.exists())) {
         log.warn("Scripts file '{}' does not exist. Creating an new empty one.", Constants.JTB_SCRIPT_FILE_NAME);
         try {
            file.create(new ByteArrayInputStream(EMPTY_SCRIPT_FILE.getBytes(ENC)), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }
      }

      return file;
   }

   // Parse Script File
   private Scripts scriptsParseFile(InputStream is) throws JAXBException {
      log.debug("Parsing Script file '{}'", Constants.JTB_SCRIPT_FILE_NAME);

      Unmarshaller u = jcScripts.createUnmarshaller();
      u.setListener(new ScriptJAXBParentListener());
      return (Scripts) u.unmarshal(is);
   }

   // Write Variables File
   public void scriptsWriteFile() throws JAXBException, CoreException {
      log.info("scriptsWriteFile file '{}'", Constants.JTB_SCRIPT_FILE_NAME);

      Marshaller m = jcScripts.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(scripts, sw);

      // TODO Add the logic to temporarily save the previous file in case of crash while saving

      try {
         InputStream is = new ByteArrayInputStream(sw.toString().getBytes(ENC));
         scriptsIFile.setContents(is, false, false, null);
      } catch (UnsupportedEncodingException e) {
         // Impossible
         log.error("UnsupportedEncodingException", e);
         return;
      }
   }

   private int scriptsCount(List<Directory> dirs) {
      int nb = 0;
      for (Directory directory : dirs) {
         nb += directory.getScript().size();
         nb += scriptsCount(directory.getDirectory());
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

}

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
package org.titou10.jtb.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.util.Constants;

/**
 * 
 * Extension to
 * http://help.eclipse.org/oxygen/topic/org.eclipse.platform.doc.isv/reference/api/org/eclipse/jface/preference/PreferenceStore.html
 * 
 * See this class for copyright
 * 
 * Same as org.eclipse.jface.preference.PreferenceStore with remove() and some utility methods
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class JTBPreferenceStore extends EventManager implements IPersistentPreferenceStore {

   // -----------------------------------------------------------
   // Extra attributes and methods
   // -----------------------------------------------------------

   private static final Logger log = LoggerFactory.getLogger(JTBPreferenceStore.class);

   @Inject
   private ConfigManager       cm;

   private String              preferenceFileName;

   @PostConstruct
   private void initialize() throws IOException {

      preferenceFileName = cm.getJtbProject().getLocation().toOSString() + File.separatorChar + Constants.PREFERENCE_FILE_NAME;
      log.debug("Loading Preference file '{}'", preferenceFileName);

      this.filename = preferenceFileName;
      load();

      setDefault(Constants.PREF_MAX_MESSAGES, Constants.PREF_MAX_MESSAGES_DEFAULT);
      setDefault(Constants.PREF_AUTO_REFRESH_DELAY, Constants.PREF_AUTO_REFRESH_DELAY_DEFAULT);
      setDefault(Constants.PREF_SHOW_SYSTEM_OBJECTS, Constants.PREF_SHOW_SYSTEM_OBJECTS_DEFAULT);
      setDefault(Constants.PREF_AUTO_RESIZE_COLS_BROWSER, Constants.PREF_AUTO_RESIZE_COLS_BROWSER_DEFAULT);
      setDefault(Constants.PREF_EDIT_MESSAGE_DND, Constants.PREF_EDIT_MESSAGE_DND_DEFAULT);
      setDefault(Constants.PREF_SHOW_NON_BROWSABLE_Q, Constants.PREF_SHOW_NON_BROWSABLE_Q_DEFAULT);
      setDefault(Constants.PREF_TRUST_ALL_CERTIFICATES, Constants.PREF_TRUST_ALL_CERTIFICATES_DEFAULT);
      setDefault(Constants.PREF_CLEAR_LOGS_EXECUTION, Constants.PREF_CLEAR_LOGS_EXECUTION_DEFAULT);
      setDefault(Constants.PREF_MAX_MESSAGES_TOPIC, Constants.PREF_MAX_MESSAGES_TOPIC_DEFAULT);
      setDefault(Constants.PREF_CONN_CLIENT_ID_PREFIX, Constants.PREF_CONN_CLIENT_ID_PREFIX_DEFAULT);
      setDefault(Constants.PREF_XML_INDENT, Constants.PREF_XML_INDENT_DEFAULT);
      setDefault(Constants.PREF_SYNCHRONIZE_SESSIONS_MESSAGES, Constants.PREF_SYNCHRONIZE_SESSIONS_MESSAGES_DEFAULT);
      setDefault(Constants.PREF_MESSAGE_TAB_DISPLAY, Constants.PREF_MESSAGE_TAB_DISPLAY_DEFAULT);
      setDefault(Constants.PREF_MESSAGE_TEXT_MONOSPACED, Constants.PREF_MESSAGE_TEXT_MONOSPACED_DEFAULT);
      setDefault(Constants.PREF_COLUMNSSET_DEFAULT_NAME, Constants.JTB_COLUMNSSETS_SYSTEM_CS_NAME);
   }

   public String getPreferenceFileName() {
      return preferenceFileName;
   }

   public void remove(String key) {
      properties.remove(key);
   }

   public void removeAllWithPrefix(String keyPrefix) {
      log.debug("removeAllWithPrefix {}", keyPrefix);

      List<String> keysToRemove = new ArrayList<>();
      for (Object k : properties.keySet()) {
         String key = (String) k;
         if (key.startsWith(keyPrefix)) {
            keysToRemove.add(key);
         }
      }
      for (String key : keysToRemove) {
         properties.remove(key);
      }
   }

   public void importColumnsSetsPreferences(InputStream is) throws IOException {
      log.debug("importColumnsSetsPreferences");

      Properties tempProperties = new Properties();
      tempProperties.load(is);

      // Keep only the right entries
      Map<Object, Object> res = tempProperties.entrySet().stream()
               .filter(e -> ((String) e.getKey()).startsWith(Constants.PREF_COLUMNSSET_DEFAULT_DEST_PREFIX))
               .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

      log.debug("Properties to add: {}", res);

      // Add the result to the preferences
      properties.putAll(res);

      save();
   }

   public Map<String, String> getAllWithPrefix(String keyPrefix) {
      log.debug("getAllWithPrefix {}", keyPrefix);

      return properties.entrySet().stream().filter(e -> ((String) e.getKey()).startsWith(keyPrefix))
               .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue() == null ? null : e.getValue().toString()));

   }

   public void importPreferencesExceptColumnsSets(InputStream is) throws IOException {
      log.debug("importPreferencesExceptColumnsSets");

      Properties tempProperties = new Properties();
      tempProperties.load(is);

      // Keep only the right entries
      Map<Object, Object> res = tempProperties.entrySet().stream()
               .filter(e -> !((String) e.getKey()).startsWith(Constants.PREF_COLUMNSSET_DEFAULT_DEST_PREFIX))
               .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

      log.debug("Properties to add: {}", res);

      // Add the result to the preferences
      properties.putAll(res);

      save();
   }

   public String buildPreferenceKeyForQDepthFilter(String jtbSessionName) {
      return Constants.PREF_Q_DEPTH_FILTER_KEY_PREFIX + jtbSessionName;
   }

   public String buildPreferenceKeyForDestinationCS(JTBDestination jtbDestination) {
      String key = buildPreferenceKeyForSessionNameCS(jtbDestination.getJtbConnection().getSessionName()) +
                   "." +
                   jtbDestination.getName();
      return key.replaceAll("=", "_");
   }

   public String buildPreferenceKeyForSessionNameCS(String jtbSessionName) {
      String key = Constants.PREF_COLUMNSSET_DEFAULT_DEST_PREFIX + jtbSessionName;
      return key.replaceAll("=", "_");
   }

   public void load() throws IOException {
      if (filename == null) {
         throw new IOException("File name not specified");//$NON-NLS-1$
      }
      FileInputStream in;
      try {
         in = new FileInputStream(filename);
      } catch (FileNotFoundException fnfe) {
         log.warn("File {} does not exists create it", filename);
         File f = new File(filename);
         f.createNewFile();
         in = new FileInputStream(filename);
      }
      load(in);
      in.close();
   }

   // -----------------------------------------------------------
   // All below from org.eclipse.jface.preference.PreferenceStore
   // -----------------------------------------------------------

   private Properties properties;
   private Properties defaultProperties;
   private boolean    dirty = false;
   private String     filename;

   public JTBPreferenceStore() {
      defaultProperties = new Properties();
      properties = new Properties(defaultProperties);
   }

   public JTBPreferenceStore(String filename) {
      this();
      Assert.isNotNull(filename);
      this.filename = filename;
   }

   @Override
   public void addPropertyChangeListener(IPropertyChangeListener listener) {
      addListenerObject(listener);
   }

   @Override
   public boolean contains(String name) {
      return (properties.containsKey(name) || defaultProperties.containsKey(name));
   }

   @Override
   public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
      final Object[] finalListeners = getListeners();
      // Do we need to fire an event.
      if (finalListeners.length > 0 && (oldValue == null || !oldValue.equals(newValue))) {
         final PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
         for (Object finalListener : finalListeners) {
            final IPropertyChangeListener l = (IPropertyChangeListener) finalListener;
            SafeRunnable.run(new SafeRunnable(JFaceResources.getString("PreferenceStore.changeError")) { //$NON-NLS-1$
               @Override
               public void run() {
                  l.propertyChange(pe);
               }
            });
         }
      }
   }

   @Override
   public boolean getBoolean(String name) {
      return getBoolean(properties, name);
   }

   private boolean getBoolean(Properties p, String name) {
      String value = p != null ? p.getProperty(name) : null;
      if (value == null) {
         return BOOLEAN_DEFAULT_DEFAULT;
      }
      if (value.equals(IPreferenceStore.TRUE)) {
         return true;
      }
      return false;
   }

   @Override
   public boolean getDefaultBoolean(String name) {
      return getBoolean(defaultProperties, name);
   }

   @Override
   public double getDefaultDouble(String name) {
      return getDouble(defaultProperties, name);
   }

   @Override
   public float getDefaultFloat(String name) {
      return getFloat(defaultProperties, name);
   }

   @Override
   public int getDefaultInt(String name) {
      return getInt(defaultProperties, name);
   }

   @Override
   public long getDefaultLong(String name) {
      return getLong(defaultProperties, name);
   }

   @Override
   public String getDefaultString(String name) {
      return getString(defaultProperties, name);
   }

   @Override
   public double getDouble(String name) {
      return getDouble(properties, name);
   }

   private double getDouble(Properties p, String name) {
      String value = p != null ? p.getProperty(name) : null;
      if (value == null) {
         return DOUBLE_DEFAULT_DEFAULT;
      }
      double ival = DOUBLE_DEFAULT_DEFAULT;
      try {
         ival = Double.valueOf(value).doubleValue();
      } catch (NumberFormatException e) {}
      return ival;
   }

   @Override
   public float getFloat(String name) {
      return getFloat(properties, name);
   }

   private float getFloat(Properties p, String name) {
      String value = p != null ? p.getProperty(name) : null;
      if (value == null) {
         return FLOAT_DEFAULT_DEFAULT;
      }
      float ival = FLOAT_DEFAULT_DEFAULT;
      try {
         ival = Float.valueOf(value).floatValue();
      } catch (NumberFormatException e) {}
      return ival;
   }

   @Override
   public int getInt(String name) {
      return getInt(properties, name);
   }

   private int getInt(Properties p, String name) {
      String value = p != null ? p.getProperty(name) : null;
      if (value == null) {
         return INT_DEFAULT_DEFAULT;
      }
      int ival = 0;
      try {
         ival = Integer.parseInt(value);
      } catch (NumberFormatException e) {}
      return ival;
   }

   @Override
   public long getLong(String name) {
      return getLong(properties, name);
   }

   private long getLong(Properties p, String name) {
      String value = p != null ? p.getProperty(name) : null;
      if (value == null) {
         return LONG_DEFAULT_DEFAULT;
      }
      long ival = LONG_DEFAULT_DEFAULT;
      try {
         ival = Long.parseLong(value);
      } catch (NumberFormatException e) {}
      return ival;
   }

   @Override
   public String getString(String name) {
      return getString(properties, name);
   }

   private String getString(Properties p, String name) {
      String value = p != null ? p.getProperty(name) : null;
      if (value == null) {
         return STRING_DEFAULT_DEFAULT;
      }
      return value;
   }

   @Override
   public boolean isDefault(String name) {
      return (!properties.containsKey(name) && defaultProperties.containsKey(name));
   }

   public void list(PrintStream out) {
      properties.list(out);
   }

   public void list(PrintWriter out) {
      properties.list(out);
   }

   public void load(InputStream in) throws IOException {
      properties.load(in);
      dirty = false;
   }

   @Override
   public boolean needsSaving() {
      return dirty;
   }

   public String[] preferenceNames() {
      Set<String> set = properties.stringPropertyNames();
      return set.toArray(new String[set.size()]);
   }

   @Override
   public void putValue(String name, String value) {
      String oldValue = getString(name);
      if (oldValue == null || !oldValue.equals(value)) {
         setValue(properties, name, value);
         dirty = true;
      }
   }

   @Override
   public void removePropertyChangeListener(IPropertyChangeListener listener) {
      removeListenerObject(listener);
   }

   @Override
   public void save() throws IOException {
      if (filename == null) {
         throw new IOException("File name not specified");//$NON-NLS-1$
      }
      FileOutputStream out = null;
      try {
         out = new FileOutputStream(filename);
         save(out, null);
      } finally {
         if (out != null) {
            out.close();
         }
      }
   }

   public void save(OutputStream out, String header) throws IOException {
      properties.store(out, header);
      dirty = false;
   }

   @Override
   public void setDefault(String name, double value) {
      setValue(defaultProperties, name, value);
   }

   @Override
   public void setDefault(String name, float value) {
      setValue(defaultProperties, name, value);
   }

   @Override
   public void setDefault(String name, int value) {
      setValue(defaultProperties, name, value);
   }

   @Override
   public void setDefault(String name, long value) {
      setValue(defaultProperties, name, value);
   }

   @Override
   public void setDefault(String name, String value) {
      setValue(defaultProperties, name, value);
   }

   @Override
   public void setDefault(String name, boolean value) {
      setValue(defaultProperties, name, value);
   }

   public void setFilename(String name) {
      filename = name;
   }

   @Override
   public void setToDefault(String name) {
      if (!properties.containsKey(name))
         return;
      Object oldValue = properties.get(name);
      properties.remove(name);
      dirty = true;
      Object newValue = null;
      if (defaultProperties != null) {
         newValue = defaultProperties.get(name);
      }
      firePropertyChangeEvent(name, oldValue, newValue);
   }

   @Override
   public void setValue(String name, double value) {
      double oldValue = getDouble(name);
      if (oldValue != value) {
         setValue(properties, name, value);
         dirty = true;
         firePropertyChangeEvent(name, Double.valueOf(oldValue), Double.valueOf(value));
      }
   }

   @Override
   public void setValue(String name, float value) {
      float oldValue = getFloat(name);
      if (oldValue != value) {
         setValue(properties, name, value);
         dirty = true;
         firePropertyChangeEvent(name, Float.valueOf(oldValue), Float.valueOf(value));
      }
   }

   @Override
   public void setValue(String name, int value) {
      int oldValue = getInt(name);
      if (oldValue != value) {
         setValue(properties, name, value);
         dirty = true;
         firePropertyChangeEvent(name, Integer.valueOf(oldValue), Integer.valueOf(value));
      }
   }

   @Override
   public void setValue(String name, long value) {
      long oldValue = getLong(name);
      if (oldValue != value) {
         setValue(properties, name, value);
         dirty = true;
         firePropertyChangeEvent(name, Long.valueOf(oldValue), Long.valueOf(value));
      }
   }

   @Override
   public void setValue(String name, String value) {
      String oldValue = getString(name);
      if (oldValue == null || !oldValue.equals(value)) {
         setValue(properties, name, value);
         dirty = true;
         firePropertyChangeEvent(name, oldValue, value);
      }
   }

   @Override
   public void setValue(String name, boolean value) {
      boolean oldValue = getBoolean(name);
      if (oldValue != value) {
         setValue(properties, name, value);
         dirty = true;
         firePropertyChangeEvent(name, oldValue ? Boolean.TRUE : Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
      }
   }

   private void setValue(Properties p, String name, double value) {
      Assert.isTrue(p != null);
      p.put(name, Double.toString(value));
   }

   private void setValue(Properties p, String name, float value) {
      Assert.isTrue(p != null);
      p.put(name, Float.toString(value));
   }

   /**
    * Helper method: sets value for a given name.
    *
    * @param p
    * @param name
    * @param value
    */
   private void setValue(Properties p, String name, int value) {
      Assert.isTrue(p != null);
      p.put(name, Integer.toString(value));
   }

   private void setValue(Properties p, String name, long value) {
      Assert.isTrue(p != null);
      p.put(name, Long.toString(value));
   }

   private void setValue(Properties p, String name, String value) {
      Assert.isTrue(p != null && value != null);
      p.put(name, value);
   }

   private void setValue(Properties p, String name, boolean value) {
      Assert.isTrue(p != null);
      p.put(name, value == true ? IPreferenceStore.TRUE : IPreferenceStore.FALSE);
   }
}

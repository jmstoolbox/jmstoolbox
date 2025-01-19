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
package org.titou10.jtb.cs;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.config.JTBPreferenceStore;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.cs.gen.Column;
import org.titou10.jtb.cs.gen.ColumnKind;
import org.titou10.jtb.cs.gen.ColumnsSet;
import org.titou10.jtb.cs.gen.ColumnsSets;
import org.titou10.jtb.cs.gen.UserProperty;
import org.titou10.jtb.cs.gen.UserPropertyOrigin;
import org.titou10.jtb.cs.gen.UserPropertyType;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.util.Constants;

/**
 * Manage all things related to "Columns Sets"
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class ColumnsSetsManager {

   private static final Logger               log                     = LoggerFactory.getLogger(ColumnsSetsManager.class);

   private static final String               ENC                     = "UTF-8";
   private static final String               EMPTY_COLUMNSSETS_FILE  = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><columnsSets></columnsSets>";

   private static final SimpleDateFormat     SDF_TS                  = new SimpleDateFormat(Constants.TS_FORMAT);
   private static final SimpleDateFormat     SDF_DATE                = new SimpleDateFormat("yyyy-MM-dd");

   private static final Integer              SYSTEM_CS_NAME_HASHCODE = Constants.JTB_COLUMNSSETS_SYSTEM_CS_NAME.hashCode();

   public static final ColumnsSetsComparator COLUMNSSETS_COMPARATOR  = new ColumnsSetsComparator();

   @Inject
   private ConfigManager                     cm;

   @Inject
   private JTBPreferenceStore                ps;

   private JAXBContext                       jcColumnsSets;
   private IFile                             columnsSetsIFile;
   private ColumnsSets                       columnsSetsDef;

   private List<ColumnsSet>                  columnsSets;

   // Map ColumnSet.getName().hashCode()-> ColumSet for performance
   private Map<Integer, ColumnsSet>          mapColumnsSets;

   @PostConstruct
   private void initialize() throws Exception {
      log.debug("Initializing ColumnsSetsManager");

      this.columnsSetsIFile = cm.getJtbProject().getFile(Constants.JTB_COLUMNSSETS_CONFIG_FILE_NAME);

      // Load and Parse Visualizers config file
      this.jcColumnsSets = JAXBContext.newInstance(ColumnsSets.class);
      if (!(columnsSetsIFile.exists())) {
         log.warn("Columns Sets file '{}' does not exist. Creating an new empty one.", Constants.JTB_COLUMNSSETS_CONFIG_FILE_NAME);
         try {
            this.columnsSetsIFile.create(new ByteArrayInputStream(EMPTY_COLUMNSSETS_FILE.getBytes(ENC)), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }
      }
      this.columnsSetsDef = parseConfigFile(this.columnsSetsIFile.getContents());

      // Build list of Columns Sets
      reloadConfig();

      log.debug("ColumnsSetsManager initialized");
   }

   // ------------
   // Columns Sets
   // ------------

   public void importConfig(InputStream is) throws JAXBException, CoreException, FileNotFoundException {
      log.debug("importConfig");

      ColumnsSets newCS = parseConfigFile(is);

      if (newCS == null) {
         return;
      }

      // Merge Columns Sets
      List<ColumnsSet> mergedColumnsSets = new ArrayList<>(columnsSetsDef.getColumnsSet());
      for (ColumnsSet cs : newCS.getColumnsSet()) {
         // If a CS with the same name exist, replace it
         for (ColumnsSet temp : columnsSetsDef.getColumnsSet()) {
            if (temp.getName().equals(cs.getName())) {
               mergedColumnsSets.remove(temp);
            }
         }
         mergedColumnsSets.add(cs);
      }
      columnsSetsDef.getColumnsSet().clear();
      columnsSetsDef.getColumnsSet().addAll(mergedColumnsSets);

      // Write the config file
      writeConfigFile();

      reloadConfig();
   }

   public void saveConfig() throws JAXBException, CoreException {
      log.debug("saveColumnsSet");

      columnsSetsDef.getColumnsSet().clear();
      for (ColumnsSet cs : columnsSets) {
         if (cs.isSystem()) {
            continue;
         }
         columnsSetsDef.getColumnsSet().add(cs);
      }
      writeConfigFile();

      // Reload internal structures
      reloadConfig();
   }

   public void reloadConfig() {
      columnsSets = new ArrayList<>();
      columnsSets.addAll(columnsSetsDef.getColumnsSet());
      columnsSets.add(buildSystemColumnsSet());

      Collections.sort(columnsSets, COLUMNSSETS_COMPARATOR);

      // Build map for performance
      mapColumnsSets = columnsSets.stream().collect(Collectors.toMap(cs -> cs.getName().hashCode(), cs -> cs));
   }

   public List<ColumnsSet> getColumnsSets() {
      return columnsSets;
   }

   public ColumnsSet getColumnsSet(String csName) {
      if (csName == null) {
         return null;
      }
      return mapColumnsSets.get(csName.hashCode());
   }

   // --------
   // Builders
   // --------
   private ColumnsSet buildSystemColumnsSet() {

      ColumnsSet systemCS = new ColumnsSet();
      systemCS.setName(Constants.JTB_COLUMNSSETS_SYSTEM_CS_NAME);
      systemCS.setSystem(true);

      List<Column> cols = systemCS.getColumn();
      cols.add(buildSystemColumn(ColumnSystemHeader.JMS_TIMESTAMP));
      cols.add(buildSystemColumn(ColumnSystemHeader.JMS_MESSAGE_ID));
      cols.add(buildSystemColumn(ColumnSystemHeader.JMS_CORRELATION_ID));
      cols.add(buildSystemColumn(ColumnSystemHeader.MESSAGE_TYPE));
      cols.add(buildSystemColumn(ColumnSystemHeader.JMS_TYPE));
      cols.add(buildSystemColumn(ColumnSystemHeader.JMS_DELIVERY_MODE));
      cols.add(buildSystemColumn(ColumnSystemHeader.JMS_PRIORITY));

      return systemCS;
   }

   public Column buildSystemColumn(ColumnSystemHeader columnSystemHeader) {
      Column c = new Column();
      c.setColumnKind(ColumnKind.SYSTEM_HEADER);
      c.setSystemHeaderName(columnSystemHeader.getHeaderName());
      return c;
   }

   public Column buildUserPropertyColumn(UserPropertyOrigin origin,
                                         String userPropertyName,
                                         String userPropertyDisplay,
                                         int width,
                                         UserPropertyType upt) {
      UserProperty up = new UserProperty();
      up.setUserPropertyName(userPropertyName);
      up.setDisplayName(userPropertyDisplay);
      up.setDisplayWidth(width);
      up.setType(upt);
      up.setOrigin(origin);

      Column c = new Column();
      c.setColumnKind(ColumnKind.USER_PROPERTY);
      c.setUserProperty(up);

      return c;
   }

   public String buildDescription(ColumnsSet cs) {
      StringBuilder sb = new StringBuilder(256);
      for (Column c : cs.getColumn()) {
         sb.append(", ");
         if (c.getColumnKind() == ColumnKind.SYSTEM_HEADER) {
            sb.append(c.getSystemHeaderName());
            sb.append("(S)");
         } else {
            sb.append(c.getUserProperty().getUserPropertyName());
            if (c.getUserProperty().getOrigin() == UserPropertyOrigin.USER_PROPERTY) {
               sb.append("(U)");
            } else {
               sb.append("(M)");
            }
         }
      }
      return sb.toString().substring(2);
   }

   // ------------------------------
   // Manage "defaults" Columns Sets
   // ------------------------------
   public ColumnsSet getSystemColumnsSet() {
      return mapColumnsSets.get(SYSTEM_CS_NAME_HASHCODE);
   }

   public void saveDefaultCSForDestination(ColumnsSet columnsSet, JTBDestination jtbDestination) throws IOException {
      String preferenceKey = ps.buildPreferenceKeyForDestinationCS(jtbDestination);
      if (columnsSet == null) {
         // Request to "unset" the current ColumnsSet
         ps.remove(preferenceKey);
      } else {
         ps.setValue(preferenceKey, columnsSet.getName());
      }
      ps.save();
   }

   public ColumnSetOrigin getDefaultColumnSet(JTBDestination jtbDestination) {

      String preferenceKey = ps.buildPreferenceKeyForDestinationCS(jtbDestination);
      String csName = ps.getString(preferenceKey);
      if (csName == "") {
         // Not set in preference, delegate to higher level
         ColumnSetOrigin cso = getDefaultColumnSet(jtbDestination.getJtbConnection().getSessionName());
         cso.inherited = true;
         return cso;
      } else {
         ColumnsSet cs = getColumnsSet(csName);
         if (cs == null) {
            // Set in preference, but does not exist anymore

            // Clean PreferenceStore
            ps.remove(preferenceKey);
            try {
               ps.save();
            } catch (IOException e) {
               log.error("Exception occurred when saving preferences", e);
            }

            // Delegate to higher level
            ColumnSetOrigin cso = getDefaultColumnSet(jtbDestination.getJtbConnection().getSessionName());
            cso.inherited = true;
            return cso;
         } else {
            // Explicitely set in preference and still exist: use it
            return new ColumnSetOrigin(cs, false);
         }
      }
   }

   public ColumnSetOrigin getDefaultColumnSet(JTBSession jtbSession) {
      return getDefaultColumnSet(jtbSession.getName());
   }

   private ColumnSetOrigin getDefaultColumnSet(String sessionName) {

      // Check at the Session level
      SessionDef sd = cm.getSessionDefByName(sessionName);
      ColumnsSet cs = getColumnsSet(sd.getColumnsSetName());
      if (cs != null) {
         // Explicitely set in session and still exit, use it
         return new ColumnSetOrigin(cs, false);
      }

      // Check in preferences
      cs = getColumnsSet(ps.getString(Constants.PREF_COLUMNSSET_DEFAULT_NAME));
      if (cs != null) {
         return new ColumnSetOrigin(cs, true);
      }

      // Return default CS
      return new ColumnSetOrigin(getSystemColumnsSet(), true);
   }

   // -------
   // Helpers
   // -------

   public String getUserPropertyDisplayName(UserProperty userProperty, boolean defaultToUserPropertyName) {
      String displayName = userProperty.getDisplayName();
      if (defaultToUserPropertyName) {
         return displayName == null ? userProperty.getUserPropertyName() : displayName;
      } else {
         return displayName == null ? "" : displayName;
      }
   }

   public String getColumnUserPropertyValueAsString(Message m, UserProperty u) {

      String val = null;
      try {
         if (u.getOrigin() == UserPropertyOrigin.USER_PROPERTY) {
            val = m.getStringProperty(u.getUserPropertyName());
         } else {
            if (m instanceof MapMessage mm) {
               val = mm.getString(u.getUserPropertyName());
            }
         }
         if (val == null) {
            return "";
         }
         switch (u.getType()) {
            case LONG_TO_DATE:
               return SDF_DATE.format(new Date(Long.parseLong(val)));
            case LONG_TO_TS:
               return SDF_TS.format(new Date(Long.parseLong(val)));
            default:
               return val;
         }
      } catch (JMSException e) {
         log.error("Exception while reading/formatting UserProperty '{}'.  {} {}",
                   u.getUserPropertyName(),
                   e.getClass(),
                   e.getMessage());
         return "?? " + val + " ??";
      }
   }

   public Object getColumnUserPropertyValue(Message m, UserProperty u) {
      try {
         if (u.getOrigin() == UserPropertyOrigin.USER_PROPERTY) {
            return m.getObjectProperty(u.getUserPropertyName());
         } else {
            if (m instanceof MapMessage mm) {
               return mm.getObject(u.getUserPropertyName());
            } else {
               return null;
            }
         }
      } catch (JMSException e) {
         log.error("Exception while reading/formatting UserProperty '{}'.  {} {}",
                   u.getUserPropertyName(),
                   e.getClass(),
                   e.getMessage());
         return null;
      }
   }

   // Parse ColumnsSets File into ColumnsSets Object
   private ColumnsSets parseConfigFile(InputStream is) throws JAXBException {
      log.debug("Parsing ColumnsSets file '{}'", Constants.JTB_COLUMNSSETS_CONFIG_FILE_NAME);

      Unmarshaller u = jcColumnsSets.createUnmarshaller();
      return (ColumnsSets) u.unmarshal(is);
   }

   // Write config File
   private void writeConfigFile() throws JAXBException, CoreException {
      log.info("Writing ColumnsSets file '{}'", Constants.JTB_COLUMNSSETS_CONFIG_FILE_NAME);

      Marshaller m = jcColumnsSets.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(columnsSetsDef, sw);

      // TODO Add the logic to temporarily save the previous file in case of crash while saving

      try (InputStream is = new ByteArrayInputStream(sw.toString().getBytes(ENC))) {
         columnsSetsIFile.setContents(is, false, false, null);
      } catch (IOException e) {
         log.error("IOException", e);
         return;
      }
   }

   public final static class ColumnsSetsComparator implements Comparator<ColumnsSet> {

      @Override
      public int compare(ColumnsSet o1, ColumnsSet o2) {
         // System columns sets first
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

   public final class ColumnSetOrigin {
      public ColumnsSet columnsSet;
      public boolean    inherited;

      public ColumnSetOrigin(ColumnsSet columnsSet, boolean inherited) {
         this.columnsSet = columnsSet;
         this.inherited = inherited;
      }
   }
}

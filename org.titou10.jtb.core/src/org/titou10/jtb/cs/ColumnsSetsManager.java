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
package org.titou10.jtb.cs;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Singleton;
import javax.jms.Message;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.cs.gen.Column;
import org.titou10.jtb.cs.gen.ColumnKind;
import org.titou10.jtb.cs.gen.ColumnsSet;
import org.titou10.jtb.cs.gen.ColumnsSets;
import org.titou10.jtb.cs.gen.UserProperty;
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

   private static final SimpleDateFormat     SDF_TS                  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
   private static final SimpleDateFormat     SDF_DATE                = new SimpleDateFormat("yyyy-MM-dd");

   private static final String               SYSTEM_CS_NAME          = "System";
   private static final Integer              SYSTEM_CS_NAME_HASHCODE = SYSTEM_CS_NAME.hashCode();

   public static final ColumnsSetsComparator COLUMNSSETS_COMPARATOR  = new ColumnsSetsComparator();

   private JAXBContext                       jcColumnsSets;
   private IFile                             columnsSetsIFile;
   private ColumnsSets                       columnsSetsDef;

   private List<ColumnsSet>                  columnsSets;

   // Map ColumnSet.getName().hashCode()-> ColumSet for performance
   private Map<Integer, ColumnsSet>          mapColumnsSets;

   public int initialize(IFile vIFile) throws Exception {
      log.debug("Initializing VariablesManager");

      columnsSetsIFile = vIFile;

      // Load and Parse Visualizers config file
      jcColumnsSets = JAXBContext.newInstance(ColumnsSets.class);
      if (!(columnsSetsIFile.exists())) {
         log.warn("Columns Sets file '{}' does not exist. Creating an new empty one.", Constants.JTB_VARIABLE_CONFIG_FILE_NAME);
         try {
            this.columnsSetsIFile.create(new ByteArrayInputStream(EMPTY_COLUMNSSETS_FILE.getBytes(ENC)), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }
      }
      columnsSetsDef = parseColumnsSetsFile(this.columnsSetsIFile.getContents());

      // Build list of visualizers
      reload();

      log.debug("VariablesManager initialized");
      return columnsSets.size();
   }

   // ------------
   // Columns Sets
   // ------------

   public boolean importColumnsSet(String columnsSetsFileName) throws JAXBException, CoreException, FileNotFoundException {
      log.debug("importColumnsSet : {}", columnsSetsFileName);

      // Try to parse the given file
      File f = new File(columnsSetsFileName);
      ColumnsSets newCS = parseColumnsSetsFile(new FileInputStream(f));

      if (newCS == null) {
         return false;
      }

      // Merge variables
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

      // Write the variable file
      columnsSetsWriteFile();

      // int variables
      reload();

      return true;
   }

   public void exportColumnsSet(String columnsSetsFileName) throws IOException, CoreException {
      log.debug("exportColumnsSet : {}", columnsSetsFileName);
      Files.copy(columnsSetsIFile.getContents(), Paths.get(columnsSetsFileName), StandardCopyOption.REPLACE_EXISTING);
   }

   public boolean saveColumnsSet() throws JAXBException, CoreException {
      log.debug("saveColumnsSet");

      columnsSetsDef.getColumnsSet().clear();
      for (ColumnsSet cs : columnsSets) {
         if (cs.isSystem()) {
            continue;
         }
         columnsSetsDef.getColumnsSet().add(cs);
      }
      columnsSetsWriteFile();

      return true;
   }

   public void reload() {
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

   public ColumnsSet getSystemColumnsSet() {
      return mapColumnsSets.get(SYSTEM_CS_NAME_HASHCODE);
   }

   public ColumnsSet getColumnsSet(String csName) {
      return mapColumnsSets.get(csName.hashCode());
   }

   // --------
   // Builders
   // --------
   private ColumnsSet buildSystemColumnsSet() {

      ColumnsSet systemCS = new ColumnsSet();
      systemCS.setName(SYSTEM_CS_NAME);
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

   private Column buildSystemColumn(ColumnSystemHeader columnSystemHeader) {
      Column c = new Column();
      c.setColumnKind(ColumnKind.SYSTEM_HEADER);
      c.setSystemHeaderName(columnSystemHeader.getHeaderName());
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
            sb.append("(U)");
         }
      }
      return sb.toString().substring(2);
   }

   // -------
   // Helpers
   // -------

   public String getColumnUserPropertyValue(Message m, Column c) {

      String val = null;
      UserProperty u = c.getUserProperty();
      try {
         val = m.getStringProperty(u.getUserPropertyName());
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
      } catch (Exception e) {
         log.error("Exception while reading/formatting UserProperty '{}'.  {} {}",
                   u.getUserPropertyName(),
                   e.getClass(),
                   e.getMessage());
         return "?? " + val + " ??";
      }
   }

   // Parse ColumnsSets File into ColumnsSets Object
   private ColumnsSets parseColumnsSetsFile(InputStream is) throws JAXBException {
      log.debug("Parsing ColumnsSets file '{}'", Constants.JTB_COLUMNSSETS_CONFIG_FILE_NAME);

      Unmarshaller u = jcColumnsSets.createUnmarshaller();
      return (ColumnsSets) u.unmarshal(is);
   }

   // Write Variables File
   private void columnsSetsWriteFile() throws JAXBException, CoreException {
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
   }

}

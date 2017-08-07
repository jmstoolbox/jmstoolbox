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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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

   private static final Logger               log                    = LoggerFactory.getLogger(ColumnsSetsManager.class);

   private static final String               ENC                    = "UTF-8";
   private static final String               EMPTY_COLUMNSSETS_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><columnsSets></columnsSets>";

   public static final ColumnsSetsComparator COLUMNSSETS_COMPARATOR = new ColumnsSetsComparator();

   private JAXBContext                       jcColumnsSets;
   private IFile                             columnsSetsIFile;
   private ColumnsSets                       columnsSetsDef;

   private List<ColumnsSet>                  columnsSets;

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

   // public boolean importColumnsSet(String columnsSetsFileName) throws JAXBException, CoreException, FileNotFoundException {
   // log.debug("importColumnsSet : {}", columnsSetsFileName);
   //
   // // Try to parse the given file
   // File f = new File(columnsSetsFileName);
   // ColumnsSet newCS = parseColumnsSetsFile(new FileInputStream(f));
   //
   // if (newCS == null) {
   // return false;
   // }
   //
   // // Merge variables
   // List<ColumnsSet> mergedColumnsSets = new ArrayList<>(columnsSetsDef.getColumnsSet());
   // for (Column c : newCS.getColumns()) {
   // // If a CS with the same name exist, replace it
   // for (ColumnsSet temp : columnsSetsDef.getColumnsSet()) {
   // if (temp.getName().equals(c.getUserProperty())) {
   // mergedColumnsSets.remove(temp);
   // }
   // }
   // mergedColumnsSets.add(c);
   // }
   // columnsSetsDef.getColumnsSet().clear();
   // columnsSetsDef.getColumnsSet().addAll(mergedColumnsSets);
   //
   // // Write the variable file
   // columnsSetsWriteFile();
   //
   // // int variables
   // reload();
   //
   // return true;
   // }

   public void exportColumnsSet(String columnsSetsFileName) throws IOException, CoreException {
      log.debug("exportColumnsSet : {}", columnsSetsFileName);
      Files.copy(columnsSetsIFile.getContents(), Paths.get(columnsSetsFileName), StandardCopyOption.REPLACE_EXISTING);
   }

   // public boolean saveColumnsSet() throws JAXBException, CoreException {
   // log.debug("saveColumnsSet");
   //
   // columnsSetsDef.getColumnsSet().clear();
   // for (ColumnsSet cs : columnsSets) {
   // if (cs.isSystem()) {
   // continue;
   // }
   // columnsSetsDef.getColumnsSet().add(cs);
   // }
   // columnsSetsWriteFile();
   //
   // return true;
   // }

   public void reload() {
      columnsSets = new ArrayList<>();
      columnsSets.addAll(columnsSetsDef.getColumnsSet());
      columnsSets.addAll(buildSystemColumnsSets());

      Collections.sort(columnsSets, COLUMNSSETS_COMPARATOR);
   }

   public List<ColumnsSet> getColumnsSets() {
      return columnsSets;
   }

   // --------
   // Builders
   // --------
   private List<ColumnsSet> buildSystemColumnsSets() {

      ColumnsSet defaultCS = new ColumnsSet();
      defaultCS.setName("default");
      defaultCS.setSystem(true);

      List<Column> cols = defaultCS.getColumns();
      Column c = new Column();
      c.setColumnKind(ColumnKind.SYSTEM_HEADER);
      c.setSystemHeaderName(JTBSystemHeader.JMS_TIMESTAMP.getHeaderName());
      cols.add(c);
      c = new Column();
      c.setColumnKind(ColumnKind.SYSTEM_HEADER);
      c.setSystemHeaderName(JTBSystemHeader.JMS_MESSAGE_ID.getHeaderName());
      cols.add(c);
      c = new Column();
      c.setColumnKind(ColumnKind.SYSTEM_HEADER);
      c.setSystemHeaderName(JTBSystemHeader.MESSAGE_TYPE.getHeaderName());
      cols.add(c);
      c = new Column();
      c.setColumnKind(ColumnKind.SYSTEM_HEADER);
      c.setSystemHeaderName(JTBSystemHeader.JMS_TYPE.getHeaderName());
      cols.add(c);
      c = new Column();
      c.setColumnKind(ColumnKind.SYSTEM_HEADER);
      c.setSystemHeaderName(JTBSystemHeader.JMS_DELIVERY_MODE.getHeaderName());
      cols.add(c);
      c = new Column();
      c.setColumnKind(ColumnKind.SYSTEM_HEADER);
      c.setSystemHeaderName(JTBSystemHeader.JMS_PRIORITY.getHeaderName());
      cols.add(c);

      List<ColumnsSet> list = new ArrayList<>(1);
      list.add(defaultCS);
      return list;
   }

   // -------
   // Helpers
   // -------

   // Parse ColumnsSets File into ColumnsSets Object
   private ColumnsSets parseColumnsSetsFile(InputStream is) throws JAXBException {
      log.debug("Parsing ColumnsSets file '{}'", Constants.JTB_COLUMNSSETS_CONFIG_FILE_NAME);

      Unmarshaller u = jcColumnsSets.createUnmarshaller();
      return (ColumnsSets) u.unmarshal(is);
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

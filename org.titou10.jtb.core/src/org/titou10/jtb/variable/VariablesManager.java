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
package org.titou10.jtb.variable;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;
import org.titou10.jtb.variable.gen.Variable;
import org.titou10.jtb.variable.gen.VariableDateTimeKind;
import org.titou10.jtb.variable.gen.VariableDateTimeOffsetTU;
import org.titou10.jtb.variable.gen.VariableKind;
import org.titou10.jtb.variable.gen.VariableStringKind;
import org.titou10.jtb.variable.gen.Variables;

/**
 * Manage all things related to "Variables"
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class VariablesManager {

   private static final Logger            log                    = LoggerFactory.getLogger(VariablesManager.class);

   private static final String            ENC                    = "UTF-8";
   private static final String            EMPTY_VARIABLE_FILE    = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><variables></variables>";

   private static final String            CHARS_1                = "abcdefghijklmnopqrstuvwxyz";
   private static final String            CHARS_2                = CHARS_1.toUpperCase();
   private static final String            CHARS_3                = "0123456789";

   private static final String            CHARS_ALPHABETIC       = CHARS_1 + CHARS_2;
   private static final String            CHARS_ALPHANUMERIC     = CHARS_ALPHABETIC + CHARS_3;
   private static final String            CHARS_NUMERIC          = CHARS_3;

   private static final int               CHARS_ALPHABETIC_LEN   = CHARS_ALPHABETIC.length();
   private static final int               CHARS_ALPHANUMERIC_LEN = CHARS_ALPHANUMERIC.length();
   private static final int               CHARS_NUMERIC_LEN      = CHARS_NUMERIC.length();

   private static final int               INT_MIN                = 0;
   private static final int               INT_MAX                = 9999;

   public static final VariableComparator VARIABLE_COMPARATOR    = new VariableComparator();

   @Inject
   private ConfigManager                  cm;

   private JAXBContext                    jcVariables;
   private IFile                          variablesIFile;
   private Variables                      variablesDef;

   private List<Variable>                 variables;

   @PostConstruct
   private void initialize() throws Exception {
      log.debug("Initializing VariablesManager");

      variablesIFile = cm.getJtbProject().getFile(Constants.JTB_VARIABLE_CONFIG_FILE_NAME);

      // Load and Parse Visualizers config file
      jcVariables = JAXBContext.newInstance(Variables.class);
      if (!(variablesIFile.exists())) {
         log.warn("Variables file '{}' does not exist. Creating an new empty one.", Constants.JTB_VARIABLE_CONFIG_FILE_NAME);
         try {
            this.variablesIFile.create(new ByteArrayInputStream(EMPTY_VARIABLE_FILE.getBytes(ENC)), false, null);
         } catch (UnsupportedEncodingException | CoreException e) {
            // Impossible
         }
      }
      variablesDef = parseVariablesFile(this.variablesIFile.getContents());

      // Build list of variables
      reloadConfig();

      log.debug("VariablesManager initialized");
   }

   // ---------
   // Variables
   // ---------
   public void importConfig(InputStream is) throws JAXBException, CoreException, FileNotFoundException {
      log.debug("importConfig");

      Variables newVars = parseVariablesFile(is);

      if (newVars == null) {
         return;
      }

      // Merge variables
      List<Variable> mergedVariables = new ArrayList<>(variablesDef.getVariable());
      for (Variable v : newVars.getVariable()) {
         // If a variable with the same name exist, replace it
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
      reloadConfig();
   }

   public void saveConfig() throws JAXBException, CoreException {
      log.debug("saveConfig");

      variablesDef.getVariable().clear();
      for (Variable v : variables) {
         if (v.isSystem()) {
            continue;
         }
         variablesDef.getVariable().add(v);
      }
      variablesWriteFile();
   }

   public void reloadConfig() {
      variables = new ArrayList<>();
      variables.addAll(variablesDef.getVariable());
      variables.addAll(buildSystemVariables());

      Collections.sort(variables, VARIABLE_COMPARATOR);
   }

   public List<Variable> getVariables() {
      return variables;
   }

   // ---------------------------
   // Variables Helper
   // ---------------------------

   public String replaceDataFileVariables(Map<String, String> dataFileVariables, String originalText) {
      if (Utils.isEmpty(originalText)) {
         return originalText;
      }

      String res = originalText;

      for (Entry<String, String> e : dataFileVariables.entrySet()) {
         String v = buildVariableReplaceName(e.getKey());
         res = res.replaceAll(v, e.getValue());
      }

      return res;
   }

   public String replaceTemplateVariables(String originalText) {
      if (Utils.isEmpty(originalText)) {
         return originalText;
      }

      StringBuilder tag1 = new StringBuilder(64);
      StringBuilder tag2 = new StringBuilder(64);
      Random r = new Random(System.nanoTime());

      String res = originalText;

      // For each possible variable
      for (Variable v : variables) {
         tag2.setLength(0);
         tag2.append(buildVariableDisplayName(v));

         if (res.contains(tag2)) {
            tag1.setLength(0);
            tag1.append(buildVariableReplaceName(v.getName()));
            String val = resolveVariable(r, v);
            res = res.replaceAll(tag1.toString(), val);
         }
      }
      return res;
   }

   public String resolveVariable(Random r, Variable variable) {

      switch (variable.getKind()) {
         case DATE:
            SimpleDateFormat sdf = new SimpleDateFormat(variable.getDateTimePattern());
            switch (variable.getDateTimeKind()) {
               case STANDARD:
                  return sdf.format(new Date());

               case RANGE:
                  long minDate = variable.getDateTimeMin().toGregorianCalendar().getTime().getTime();
                  long maxDate = variable.getDateTimeMax().toGregorianCalendar().getTime().getTime();
                  long diff = maxDate - minDate;
                  long date = nextLong(r, diff) + minDate;

                  return sdf.format(new Date(date));

               case OFFSET:
                  Calendar c = new GregorianCalendar();
                  switch (variable.getDateTimeOffsetTU()) {
                     case DAYS:
                        c.add(Calendar.DAY_OF_MONTH, variable.getDateTimeOffset());
                        break;
                     case HOURS:
                        c.add(Calendar.HOUR_OF_DAY, variable.getDateTimeOffset());
                        break;
                     case MILLISECONDS:
                        c.add(Calendar.MILLISECOND, variable.getDateTimeOffset());
                        break;
                     case MINUTES:
                        c.add(Calendar.MINUTE, variable.getDateTimeOffset());
                        break;
                     case MONTHS:
                        c.add(Calendar.MONTH, variable.getDateTimeOffset());
                        break;
                     case SECONDS:
                        c.add(Calendar.SECOND, variable.getDateTimeOffset());
                        break;
                     case YEARS:
                        c.add(Calendar.YEAR, variable.getDateTimeOffset());
                        break;
                  }

                  return sdf.format(c.getTime());

            }

         case INT:
            int val = r.nextInt(variable.getMax() - variable.getMin()) + variable.getMin();
            return String.valueOf(val);

         case LIST:
            int index = r.nextInt(variable.getListValue().size());
            return variable.getListValue().get(index);

         case STRING:
            char[] text = new char[variable.getStringLength()];
            switch (variable.getStringKind()) {
               case ALPHABETIC:
                  for (int i = 0; i < variable.getStringLength(); i++) {
                     text[i] = CHARS_ALPHABETIC.charAt(r.nextInt(CHARS_ALPHABETIC_LEN));
                  }
               case ALPHANUMERIC:
                  for (int i = 0; i < variable.getStringLength(); i++) {
                     text[i] = CHARS_ALPHANUMERIC.charAt(r.nextInt(CHARS_ALPHANUMERIC_LEN));
                  }
                  break;
               case NUMERIC:
                  for (int i = 0; i < variable.getStringLength(); i++) {
                     text[i] = CHARS_NUMERIC.charAt(r.nextInt(CHARS_NUMERIC_LEN));
                  }
                  break;
               case CUSTOM:
                  for (int i = 0; i < variable.getStringLength(); i++) {
                     text[i] = variable.getStringChars().charAt(r.nextInt(variable.getStringChars().length()));
                  }
                  break;
            }
            return new String(text);
      }

      // Impossible
      return null;
   }

   // ----------------------
   // Variable Name Builders
   // ----------------------

   public String buildVariableDisplayName(Variable v) {
      StringBuilder sb = new StringBuilder(64);
      sb.append("${");
      sb.append(v.getName());
      sb.append("}");
      return sb.toString();
   }

   public String buildVariableReplaceName(String name) {
      StringBuilder sb = new StringBuilder(64);
      sb.append("\\$\\{");
      sb.append(name);
      sb.append("\\}");
      return sb.toString();
   }

   public String buildDescription(Variable variable) {
      StringBuilder sb = new StringBuilder(128);

      switch (variable.getKind()) {
         case DATE:
            switch (variable.getDateTimeKind()) {
               case STANDARD:
                  sb.append("Current date with format [");
                  sb.append(variable.getDateTimePattern());
                  sb.append("]");
                  break;

               case RANGE:
                  SimpleDateFormat sdf = new SimpleDateFormat(variable.getDateTimePattern());
                  sb.append("Random Date/Time with format [");
                  sb.append(variable.getDateTimePattern());
                  sb.append("] between '");
                  sb.append(sdf.format(variable.getDateTimeMin().toGregorianCalendar().getTime()));
                  sb.append("' and '");
                  sb.append(sdf.format(variable.getDateTimeMax().toGregorianCalendar().getTime()));
                  sb.append("'");

                  break;

               case OFFSET:
                  sb.append("Current date with format [");
                  sb.append(variable.getDateTimePattern());
                  sb.append("] with an offset of ");
                  sb.append(variable.getDateTimeOffset());
                  sb.append(" ");
                  sb.append(variable.getDateTimeOffsetTU().name());
                  break;

            }
            break;

         case INT:
            sb.append("Random Integer between ");
            sb.append(variable.getMin());
            sb.append(" and ");
            sb.append(variable.getMax());
            break;

         case LIST:
            sb.append("A random String from [ ");
            boolean pasPremier = false;
            for (String value : variable.getListValue()) {
               if (pasPremier) {
                  sb.append(";");
               }
               pasPremier = true;
               sb.append(value);
            }
            sb.append(" ]");
            break;

         case STRING:
            switch (variable.getStringKind()) {
               case ALPHABETIC:
               case ALPHANUMERIC:
                  sb.append(variable.getStringKind().name());
                  sb.append(" string of ");
                  sb.append(variable.getStringLength());
                  sb.append(" characters length");
                  break;
               case NUMERIC:
                  sb.append(variable.getStringKind().name());
                  sb.append(" string of ");
                  sb.append(variable.getStringLength());
                  sb.append(" digits");
                  break;
               case CUSTOM:
                  sb.append("String of ");
                  sb.append(variable.getStringLength());
                  sb.append(" characters from [");
                  sb.append(variable.getStringChars());
                  sb.append("]");
                  break;
            }
      }

      return sb.toString();
   }

   // --------
   // Builders
   // --------
   private List<Variable> buildSystemVariables() {
      List<Variable> list = new ArrayList<>(6);

      list.add(buildDateVariable(true, "currentDate", VariableDateTimeKind.STANDARD, "yyyy-MM-dd", null, null, null, null));
      list.add(buildDateVariable(true, "currentTime", VariableDateTimeKind.STANDARD, "HH:mm:ss", null, null, null, null));
      list.add(buildDateVariable(true,
                                 "currentTimestamp",
                                 VariableDateTimeKind.STANDARD,
                                 Constants.TS_FORMAT,
                                 null,
                                 null,
                                 null,
                                 null));
      list.add(buildDateVariable(true,
                                 "xmlCurrentDateTime",
                                 VariableDateTimeKind.STANDARD,
                                 "yyyy-MM-dd'T'HH:mm:ss.SSS",
                                 null,
                                 null,
                                 null,
                                 null));
      list.add(buildIntVariable(true, "int", INT_MIN, INT_MAX));
      list.add(buildStringVariable(true, "string", VariableStringKind.ALPHANUMERIC, 16, null));

      return list;
   }

   public Variable buildDateVariable(boolean system,
                                     String name,
                                     VariableDateTimeKind kind,
                                     String pattern,
                                     Calendar min,
                                     Calendar max,
                                     Integer offset,
                                     VariableDateTimeOffsetTU offsetTU) {
      Variable v = new Variable();
      v.setSystem(system);
      v.setKind(VariableKind.DATE);
      v.setName(name);

      v.setDateTimeKind(kind);
      v.setDateTimePattern(pattern);
      v.setDateTimeMin(toXMLGregorianCalendar(min));
      v.setDateTimeMax(toXMLGregorianCalendar(max));
      v.setDateTimeOffset(offset);
      v.setDateTimeOffsetTU(offsetTU);

      return v;
   }

   public Variable buildIntVariable(boolean system, String name, Integer min, Integer max) {
      Variable v = new Variable();
      v.setSystem(system);
      v.setKind(VariableKind.INT);
      v.setName(name);

      v.setMin(min);
      v.setMax(max);

      return v;
   }

   public Variable buildStringVariable(boolean system, String name, VariableStringKind kind, Integer length, String characters) {
      Variable v = new Variable();
      v.setSystem(system);
      v.setKind(VariableKind.STRING);
      v.setName(name);

      v.setStringLength(length);
      v.setStringKind(kind);
      v.setStringChars(characters);

      return v;
   }

   public Variable buildListVariable(boolean system, String name, List<String> values) {

      Variable v = new Variable();
      v.setSystem(system);
      v.setKind(VariableKind.LIST);
      v.setName(name);

      v.getListValue().addAll(values);

      return v;
   }

   // -------
   // Helpers
   // -------

   private XMLGregorianCalendar toXMLGregorianCalendar(Calendar c) {
      if (c == null) {
         return null;
      }
      GregorianCalendar gc = new GregorianCalendar();
      gc.setTimeInMillis(c.getTimeInMillis());
      try {
         return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
      } catch (DatatypeConfigurationException e) {
         log.error("toXMLGregorianCalendar: DatatypeConfigurationException for {}", c.getTime());
         return null;
      }
   }

   private long nextLong(Random rng, long n) {
      // error checking and 2^x checking removed for simplicity.
      long bits, val;
      do {
         bits = (rng.nextLong() << 1) >>> 1;
         val = bits % n;
      } while (bits - val + (n - 1) < 0L);
      return val;
   }

   // Parse Variables File into Variables Object
   private Variables parseVariablesFile(InputStream is) throws JAXBException {
      log.debug("Parsing Variable file '{}'", Constants.JTB_VARIABLE_CONFIG_FILE_NAME);

      Unmarshaller u = jcVariables.createUnmarshaller();
      return (Variables) u.unmarshal(is);
   }

   // Write Variables File
   private void variablesWriteFile() throws JAXBException, CoreException {
      log.info("Writing Variable file '{}'", Constants.JTB_VARIABLE_CONFIG_FILE_NAME);

      Marshaller m = jcVariables.createMarshaller();
      m.setProperty(Marshaller.JAXB_ENCODING, ENC);
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

      StringWriter sw = new StringWriter(2048);
      m.marshal(variablesDef, sw);

      // TODO Add the logic to temporarily save the previous file in case of crash while saving

      try (InputStream is = new ByteArrayInputStream(sw.toString().getBytes(ENC))) {
         variablesIFile.setContents(is, false, false, null);
      } catch (IOException e) {
         log.error("IOException", e);
         return;
      }
   }

   public final static class VariableComparator implements Comparator<Variable> {

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
   }

}

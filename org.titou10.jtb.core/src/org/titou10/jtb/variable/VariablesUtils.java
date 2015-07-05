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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import javax.inject.Singleton;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.ui.part.QManagersViewPart;
import org.titou10.jtb.variable.gen.Variable;
import org.titou10.jtb.variable.gen.VariableDateTimeKind;
import org.titou10.jtb.variable.gen.VariableKind;
import org.titou10.jtb.variable.gen.VariableStringKind;

/**
 * Utility class to manage "Variables"
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class VariablesUtils {

   private static final Logger log                    = LoggerFactory.getLogger(QManagersViewPart.class);

   private static final String CHARS_1                = "abcdefghijklmnopqrstuvwxyz";
   private static final String CHARS_2                = CHARS_1.toUpperCase();
   private static final String CHARS_3                = "0123456789";

   private static final String CHARS_ALPHABETIC       = CHARS_1 + CHARS_2;
   private static final String CHARS_ALPHANUMERIC     = CHARS_ALPHABETIC + CHARS_3;
   private static final String CHARS_NUMERIC          = CHARS_3;

   private static final int    CHARS_ALPHABETIC_LEN   = CHARS_ALPHABETIC.length();
   private static final int    CHARS_ALPHANUMERIC_LEN = CHARS_ALPHANUMERIC.length();
   private static final int    CHARS_NUMERIC_LEN      = CHARS_NUMERIC.length();

   private static final int    INT_MIN                = 0;
   private static final int    INT_MAX                = 9999;

   private static final String DATE_FORMAT            = "yyyy-MM-dd";

   // ---------------------------
   // Templates Helper
   // ---------------------------

   public static String replaceTemplateVariables(List<Variable> variables, String originalText) {
      if ((originalText == null) || (originalText.trim().isEmpty())) {
         return originalText;
      }

      // TODO Manage instance variables, ie ${var:n}

      StringBuilder tag1 = new StringBuilder(32);
      StringBuilder tag2 = new StringBuilder(32);
      Random r = new Random(System.nanoTime());

      String res = originalText;

      // For each possible variable
      for (Variable v : variables) {
         tag1.setLength(0);
         tag2.setLength(0);
         tag1.append("\\$\\{").append(v.getName()).append("\\}");
         tag2.append(buildVariableDisplayName(v));

         if (res.contains(tag2)) {
            String val = resolveVariable(r, v);
            res = res.replaceAll(tag1.toString(), val);
         }
      }
      return res;
   }

   public static String buildVariableDisplayName(Variable v) {
      StringBuilder sb = new StringBuilder(64);
      sb.append("${");
      sb.append(v.getName());
      sb.append("}");
      return sb.toString();
   }

   public static String resolveVariable(Random r, Variable variable) {

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

   public static List<Variable> getSystemVariables() {
      log.debug("getSystemVariables");

      List<Variable> list = new ArrayList<Variable>(5);

      list.add(buildDateVariable(true, "currentDate", VariableDateTimeKind.STANDARD, "yyyy-MM-dd", null, null));
      list.add(buildDateVariable(true, "currentTime", VariableDateTimeKind.STANDARD, "HH:mm:ss", null, null));
      list.add(buildDateVariable(true, "currentTimestamp", VariableDateTimeKind.STANDARD, "yyyy-MM-dd-HH:mm:ss.SSS", null, null));
      list.add(buildDateVariable(true, "xmlCurrentDateTime", VariableDateTimeKind.STANDARD, "yyyy-MM-dd'T'HH:mm:ss.SSS", null, null));
      list.add(buildIntVariable(true, "int", INT_MIN, INT_MAX));
      list.add(buildStringVariable(true, "string", VariableStringKind.ALPHANUMERIC, 16, null));

      return list;

   }

   public static String buildDescription(Variable variable) {
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
                  SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                  sb.append("Random Date/Time with format [");
                  sb.append(variable.getDateTimePattern());
                  sb.append("] between '");
                  sb.append(sdf.format(variable.getDateTimeMin().toGregorianCalendar().getTime()));
                  sb.append("' and '");
                  sb.append(sdf.format(variable.getDateTimeMax().toGregorianCalendar().getTime()));
                  sb.append("'");

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

   public static Variable buildDateVariable(boolean system,
                                            String name,
                                            VariableDateTimeKind kind,
                                            String pattern,
                                            Calendar min,
                                            Calendar max) {
      Variable v = new Variable();
      v.setSystem(system);
      v.setKind(VariableKind.DATE);
      v.setName(name);

      v.setDateTimeKind(kind);
      v.setDateTimePattern(pattern);
      v.setDateTimeMin(toXMLGregorianCalendar(min));
      v.setDateTimeMax(toXMLGregorianCalendar(max));

      return v;
   }

   public static Variable buildIntVariable(boolean system, String name, Integer min, Integer max) {
      Variable v = new Variable();
      v.setSystem(system);
      v.setKind(VariableKind.INT);
      v.setName(name);

      v.setMin(min);
      v.setMax(max);

      return v;
   }

   public static Variable buildStringVariable(boolean system,
                                              String name,
                                              VariableStringKind kind,
                                              Integer length,
                                              String characters) {
      Variable v = new Variable();
      v.setSystem(system);
      v.setKind(VariableKind.STRING);
      v.setName(name);

      v.setStringLength(length);
      v.setStringKind(kind);
      v.setStringChars(characters);

      return v;
   }

   public static Variable buildListVariable(boolean system, String name, List<String> values) {

      Variable v = new Variable();
      v.setSystem(system);
      v.setKind(VariableKind.LIST);
      v.setName(name);

      v.getListValue().addAll(values);

      return v;
   }

   public static XMLGregorianCalendar toXMLGregorianCalendar(Calendar c) {
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

   // -------
   // Helpers
   // -------

   private static long nextLong(Random rng, long n) {
      // error checking and 2^x checking removed for simplicity.
      long bits, val;
      do {
         bits = (rng.nextLong() << 1) >>> 1;
         val = bits % n;
      } while (bits - val + (n - 1) < 0L);
      return val;
   }

}

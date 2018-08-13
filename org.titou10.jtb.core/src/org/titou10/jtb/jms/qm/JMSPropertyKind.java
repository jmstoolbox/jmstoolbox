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
package org.titou10.jtb.jms.qm;

/**
 * "Kind" of parameter or properties for JMS Connection or Messages
 * 
 * @author Denis Forveille
 *
 */
public enum JMSPropertyKind {
                             STRING("String", String.class),

                             INT("Integer", Integer.class),

                             LONG("Long", Long.class),

                             DOUBLE("Double", Double.class),

                             BOOLEAN("Boolean", Boolean.class),

                             SHORT("Short", Short.class),

                             FLOAT("Float", Float.class);

   private static final String[] DISPLAY_NAMES;
   private String                displayName;
   private String                className;

   // -----------
   // Constructor
   // -----------
   private JMSPropertyKind(String displayName, Class<?> clazz) {
      this.displayName = displayName;
      this.className = clazz.getName();
   }

   static {
      DISPLAY_NAMES = new String[values().length];
      int i = 0;
      for (JMSPropertyKind kind : values()) {
         DISPLAY_NAMES[i++] = kind.displayName;
      }
   }

   // ----------------
   // Getters /setters
   // ----------------

   public String getDisplayName() {
      return displayName;
   }

   public static String[] getDisplayNames() {
      return DISPLAY_NAMES;
   }

   // -----------
   // Helpers
   // -----------
   public static JMSPropertyKind fromObjectClassname(Object o) {
      if (o == null) {
         return STRING;
      }

      String className = o.getClass().getName();
      for (JMSPropertyKind jmsPropertyKind : JMSPropertyKind.values()) {
         if (jmsPropertyKind.className.equals(className)) {
            return jmsPropertyKind;
         }
      }
      return STRING;
   }

   public static JMSPropertyKind fromDisplayName(String displayName) {
      if (displayName == null) {
         return null;
      }
      for (JMSPropertyKind jmsPropertyKind : JMSPropertyKind.values()) {
         if (jmsPropertyKind.getDisplayName().equals(displayName)) {
            return jmsPropertyKind;
         }
      }
      return null;
   }

   public static boolean validateValue(JMSPropertyKind kind, String value) {

      if (value == null) {
         return true;
      }

      switch (kind) {
         case STRING:
            return true;
         case BOOLEAN:
            try {
               Boolean.parseBoolean(value);
               return true;
            } catch (NumberFormatException nfe) {
               return false;
            }
         case LONG:
            try {
               Long.parseLong(value);
               return true;
            } catch (NumberFormatException nfe) {
               return false;
            }
         case INT:
            try {
               Integer.parseInt(value);
               return true;
            } catch (NumberFormatException nfe) {
               return false;
            }
         case SHORT:
            try {
               Short.parseShort(value);
               return true;
            } catch (NumberFormatException nfe) {
               return false;
            }
         case FLOAT:
            try {
               Float.parseFloat(value);
               return true;
            } catch (NumberFormatException nfe) {
               return false;
            }
         case DOUBLE:
            try {
               Double.parseDouble(value);
               return true;
            } catch (NumberFormatException nfe) {
               return false;
            }
      }
      return true;
   }

}

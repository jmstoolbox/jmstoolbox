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

import org.titou10.jtb.util.Utils;

/**
 * "Kind" of parameter or properties for JMS Connection or Messages
 * 
 * @author Denis Forveille
 *
 */
public enum JMSPropertyKind {
                             STRING(
                                    "String",
                                    String.class,
                                    JMSSelectorOperator.EQUAL,
                                    JMSSelectorOperator.DIFFERENT,
                                    JMSSelectorOperator.LIKE,
                                    JMSSelectorOperator.NOT_LIKE,
                                    JMSSelectorOperator.IN,
                                    JMSSelectorOperator.NOT_IN,
                                    JMSSelectorOperator.IS_NULL,
                                    JMSSelectorOperator.IS_NOT_NULL),

                             INT(
                                 "Integer",
                                 Integer.class,
                                 JMSSelectorOperator.EQUAL,
                                 JMSSelectorOperator.DIFFERENT,
                                 JMSSelectorOperator.GREATER,
                                 JMSSelectorOperator.GREATER_EQUAL,
                                 JMSSelectorOperator.LOWER,
                                 JMSSelectorOperator.LOWER_EQUAL,
                                 JMSSelectorOperator.IS_NULL,
                                 JMSSelectorOperator.IS_NOT_NULL),

                             LONG(
                                  "Long",
                                  Long.class,
                                  JMSSelectorOperator.EQUAL,
                                  JMSSelectorOperator.DIFFERENT,
                                  JMSSelectorOperator.GREATER,
                                  JMSSelectorOperator.GREATER_EQUAL,
                                  JMSSelectorOperator.LOWER,
                                  JMSSelectorOperator.LOWER_EQUAL,
                                  JMSSelectorOperator.IS_NULL,
                                  JMSSelectorOperator.IS_NOT_NULL),

                             DOUBLE(
                                    "Double",
                                    Double.class,
                                    JMSSelectorOperator.EQUAL,
                                    JMSSelectorOperator.DIFFERENT,
                                    JMSSelectorOperator.GREATER,
                                    JMSSelectorOperator.GREATER_EQUAL,
                                    JMSSelectorOperator.LOWER,
                                    JMSSelectorOperator.LOWER_EQUAL,
                                    JMSSelectorOperator.IS_NULL,
                                    JMSSelectorOperator.IS_NOT_NULL),

                             BOOLEAN(
                                     "Boolean",
                                     Boolean.class,
                                     JMSSelectorOperator.EQUAL,
                                     JMSSelectorOperator.DIFFERENT,
                                     JMSSelectorOperator.IS_NULL,
                                     JMSSelectorOperator.IS_NOT_NULL),

                             SHORT(
                                   "Short",
                                   Short.class,
                                   JMSSelectorOperator.EQUAL,
                                   JMSSelectorOperator.DIFFERENT,
                                   JMSSelectorOperator.GREATER,
                                   JMSSelectorOperator.GREATER_EQUAL,
                                   JMSSelectorOperator.LOWER,
                                   JMSSelectorOperator.LOWER_EQUAL,
                                   JMSSelectorOperator.IS_NULL,
                                   JMSSelectorOperator.IS_NOT_NULL),

                             FLOAT(
                                   "Float",
                                   Float.class,
                                   JMSSelectorOperator.EQUAL,
                                   JMSSelectorOperator.DIFFERENT,
                                   JMSSelectorOperator.GREATER,
                                   JMSSelectorOperator.GREATER_EQUAL,
                                   JMSSelectorOperator.LOWER,
                                   JMSSelectorOperator.LOWER_EQUAL,
                                   JMSSelectorOperator.IS_NULL,
                                   JMSSelectorOperator.IS_NOT_NULL);

   private static final String[] DISPLAY_NAMES;
   private String                displayName;
   private String                className;
   private JMSSelectorOperator[] operators;

   // -----------
   // Constructor
   // -----------
   private JMSPropertyKind(String displayName, Class<?> clazz, JMSSelectorOperator... operators) {
      this.displayName = displayName;
      this.className = clazz.getName();
      this.operators = operators;
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

   public JMSSelectorOperator[] getOperators() {
      return operators;
   }

   // -----------
   // Helpers
   // -----------
   public static JMSSelectorOperator[] operatorsFromObjectClassname(Object o) {
      if (o == null) {
         return JMSPropertyKind.STRING.getOperators();
      }

      String className = o.getClass().getName();
      for (JMSPropertyKind jmsPropertyKind : JMSPropertyKind.values()) {
         if (jmsPropertyKind.className.equals(className)) {
            return jmsPropertyKind.getOperators();
         }
      }
      return JMSPropertyKind.STRING.getOperators();
   }

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

      if (Utils.isEmpty(value)) {
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

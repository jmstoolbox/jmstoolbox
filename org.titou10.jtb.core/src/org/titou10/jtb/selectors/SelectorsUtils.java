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
package org.titou10.jtb.selectors;

import java.util.Locale;

import org.titou10.jtb.jms.qm.JMSSelectorOperator;
import org.titou10.jtb.util.Utils;

/**
 * Group methods to deal with Selectors
 * 
 * @author Denis Forveille
 *
 */
public final class SelectorsUtils {

   private static final String KEY_SEARCH_STRING = "%s ";
   private static final String SEARCH_NUMBER     = "%d";
   private static final String SEARCH_DOUBLE     = "%s %s %f";
   private static final String SEARCH_BOOLEAN    = "%b";
   private static final String SEARCH_NULL       = "%s IS null";
   private static final String QUOTE             = "'";
   private static final String DOUBLE_QUOTE      = "''";

   public static String formatSelector(String key, Object value, JMSSelectorOperator operator, boolean shorten) {

      if (Utils.isEmpty(value)) {
         return String.format(SEARCH_NULL, key);
      }

      // // Special treatment for Timestamps
      // if (ColumnSystemHeader.isTimestamp(key)) {
      // value = Utils.extractLongFromTimestamp(value);
      // }
      //
      // // Special treatment for JMS_DELIVERY_MODE
      // if (ColumnSystemHeader.fromHeaderName(key) == ColumnSystemHeader.JMS_DELIVERY_MODE) {
      // value = Utils.extractJTBDeliveryMode(value);
      // }

      String v;
      Class<?> clazz = value.getClass();
      switch (clazz.getSimpleName()) {

         case "Boolean":
            v = String.format(SEARCH_BOOLEAN, value);
            return String.format(KEY_SEARCH_STRING + operator.getFormat(), key, v);

         case "Short":
         case "Integer":
         case "Long":
            v = String.format(SEARCH_NUMBER, value);
            return String.format(KEY_SEARCH_STRING + operator.getFormat(), key, v);

         case "Float":
         case "Double":
            v = String.format(Locale.US, SEARCH_DOUBLE, value);
            return String.format(KEY_SEARCH_STRING + operator.getFormat(), key, v);

         default:
            // Byte
            // String
            // Escape quotes
            String stringValue = value.toString().replaceAll(QUOTE, DOUBLE_QUOTE);
            if (shorten) {
               v = Utils.stringShortener(stringValue, 16);
            } else {
               v = stringValue;
            }
            ;
            v = "'" + v + "'";
            return String.format(KEY_SEARCH_STRING + operator.getFormat(), key, v);
      }
   }

   // ------------------
   // Pure Utility Class
   // ------------------
   private SelectorsUtils() {
      // NOP
   }

}

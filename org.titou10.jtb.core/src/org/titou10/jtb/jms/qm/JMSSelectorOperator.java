/*
 * Copyright (C) 2018 Denis Forveille titou10.titou10@gmail.com
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
 * Operator that can be used in selectors
 * 
 * @author Denis Forveille
 *
 */
public enum JMSSelectorOperator {
                                 EQUAL("= %s"),

                                 DIFFERENT("<> %s"),

                                 GREATER("> %s"),

                                 GREATER_EQUAL(">= %s"),

                                 LOWER("< %s"),

                                 LOWER_EQUAL("<= %s"),

                                 LIKE("LIKE %s"),

                                 NOT_LIKE("NOT LIKE %s"),

                                 IN("IN (%s)"),

                                 NOT_IN("NOT IN (%s)"),

                                 IS_NULL("IS NULL"),

                                 IS_NOT_NULL("IS NOT NULL");

   private String format;

   // -----------
   // Constructor
   // -----------
   private JMSSelectorOperator(String format) {
      this.format = format;
   }

   // ----------------
   // Getters /setters
   // ----------------

   public String getFormat() {
      return format;
   }

   // -----------
   // Helpers
   // -----------

}

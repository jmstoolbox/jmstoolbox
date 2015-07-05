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
package org.titou10.jtb.ui;

import org.titou10.jtb.config.gen.Properties.Property;
import org.titou10.jtb.jms.qm.QManagerProperty;

/**
 * Hold a Property (name-value pair)
 * 
 * @author Denis Forveille
 *
 */
public class UINameValue {
   private String name;
   private String value;

   // -----------
   // Constructor
   // -----------
   public UINameValue(String name, String value) {
      this.name = name;
      this.value = value;
   }

   public UINameValue(QManagerProperty property) {
      this.name = property.getName();
   }

   public UINameValue(Property property) {
      this.name = property.getName();
      this.value = property.getValue();
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("UIProperty [");
      builder.append(name);
      builder.append("=");
      builder.append(value);
      builder.append("]");
      return builder.toString();
   }

   // ----------------------------
   // Standard Getters and Setters
   // ----------------------------

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getValue() {
      return value;
   }

   public void setValue(String value) {
      this.value = value;
   }

}

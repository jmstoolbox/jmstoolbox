/* Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>. */
package org.titou10.jtb.ui;

import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManagerProperty;

/**
 * "Queue Manager" Property with its value for UI
 * 
 * @author Denis Forveille
 *
 */
public class UIProperty implements Comparable<UIProperty> {
   private String          name;
   private String          value;
   private String          toolTip;
   private boolean         required;
   private JMSPropertyKind kind;
   private boolean         requiresEncoding;

   // -----------
   // Constructor
   // -----------
   public UIProperty(QManagerProperty property) {
      this.name = property.getName();
      this.value = property.getDefaultValue();
      this.toolTip = property.getToolTip();
      this.required = property.getRequired();
      this.kind = JMSPropertyKind.valueOf(property.getKind().name());
      this.requiresEncoding = property.isRequiresEncoding();
   }

   // ----------------------------
   // toString
   // ----------------------------
   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(256);
      builder.append("UIProperty [name=");
      builder.append(name);
      builder.append(", value=");
      builder.append(value);
      builder.append(", toolTip=");
      builder.append(toolTip);
      builder.append(", required=");
      builder.append(required);
      builder.append(", kind=");
      builder.append(kind);
      builder.append(", requiresEncoding=");
      builder.append(requiresEncoding);
      builder.append("]");
      return builder.toString();
   }

   // ----------------------------
   // Comparable
   // ----------------------------
   @Override
   public int compareTo(UIProperty o) {
      boolean bothRequired = this.required == o.isRequired();
      if (bothRequired) {
         // Both are required
         boolean bothValue = (this.value == null || this.value.isEmpty()) == (o.value == null || o.value.isEmpty());
         if (bothValue) {
            // both required and both with a value,sort on name
            return this.name.compareTo(o.getName());
         } else {
            // both required, only one with a value, put the one with a value first
            if (this.value != null && !this.value.isEmpty()) {
               return -1;
            } else {
               return 1;
            }
         }
      } else {
         // Only one is required, put required first
         if (this.required) {
            return -1;
         } else {
            return 1;
         }
      }
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

   public boolean isRequired() {
      return required;
   }

   public void setRequired(boolean required) {
      this.required = required;
   }

   public JMSPropertyKind getKind() {
      return kind;
   }

   public void setKind(JMSPropertyKind kind) {
      this.kind = kind;
   }

   public boolean isRequiresEncoding() {
      return requiresEncoding;
   }

   public void setRequiresEncoding(boolean requiresEncoding) {
      this.requiresEncoding = requiresEncoding;
   }

   public String getToolTip() {
      return toolTip;
   }

   public void setToolTip(String toolTip) {
      this.toolTip = toolTip;
   }

}

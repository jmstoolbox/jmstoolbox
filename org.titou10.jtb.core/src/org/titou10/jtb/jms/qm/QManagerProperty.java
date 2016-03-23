/* Copyright (C) 2015 Denis Forveille titou10.titou10@gmail.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>. */
package org.titou10.jtb.jms.qm;

/**
 * 
 * Property exposed by a Queue Manager
 * 
 * @author Denis Forveille
 *
 */
public class QManagerProperty {

   private String          name;
   private boolean         required;
   private JMSPropertyKind kind;
   private String          toolTip;
   private boolean         requiresEncoding;

   // ------------------------
   // Constructors
   // ------------------------
   public QManagerProperty() {
   }

   public QManagerProperty(String name, boolean required, JMSPropertyKind kind) {
      this(name, required, kind, false, null);
   }

   public QManagerProperty(String name, boolean required, JMSPropertyKind kind, boolean requiresEncoding) {
      this(name, required, kind, requiresEncoding, null);
   }

   public QManagerProperty(String name, boolean required, JMSPropertyKind kind, boolean requiresEncoding, String toolTip) {
      this.name = name;
      this.required = required;
      this.kind = kind;
      this.requiresEncoding = requiresEncoding;
      this.toolTip = toolTip;
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public boolean getRequired() {
      return required;
   }

   public JMSPropertyKind getKind() {
      return kind;
   }

   public void setKind(JMSPropertyKind kind) {
      this.kind = kind;
   }

   public void setRequired(boolean required) {
      this.required = required;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getToolTip() {
      return toolTip;
   }

   public void setToolTip(String toolTip) {
      this.toolTip = toolTip;
   }

   public boolean isRequiresEncoding() {
      return requiresEncoding;
   }

   public void setRequiresEncoding(boolean requiresEncoding) {
      this.requiresEncoding = requiresEncoding;
   }

}

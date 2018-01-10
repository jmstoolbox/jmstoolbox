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
package org.titou10.jtb.sessiontype;

import org.eclipse.swt.graphics.Color;

/**
 * Session Type
 * 
 * @author Denis Forveille
 *
 */
public class SessionType {

   private boolean system;
   private String  name;
   private String  description;
   private Color   color;

   // ------------
   // Constructors
   // ------------

   public SessionType(boolean system, String name, String description, Color color) {
      this.system = system;
      this.name = name;
      this.description = description;
      this.color = color;
   }

   // ------------------------
   // toString()
   // ------------------------

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(256);
      builder.append("SessionType [name=");
      builder.append(name);
      builder.append(", description=");
      builder.append(description);
      builder.append(", color=");
      builder.append(color);
      builder.append("]");
      return builder.toString();
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public Color getColor() {
      return color;
   }

   public void setColor(Color color) {
      this.color = color;
   }

   public boolean isSystem() {
      return system;
   }

   public void setSystem(boolean system) {
      this.system = system;
   }

}

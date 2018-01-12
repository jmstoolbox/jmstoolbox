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

import java.util.UUID;

import org.eclipse.swt.graphics.Color;

/**
 * Session Type
 * 
 * @author Denis Forveille
 *
 */
public class SessionType {

   private String id;
   private String name;
   private Color  color;

   // ------------
   // Constructors
   // ------------

   public SessionType(String name, Color color) {
      this(UUID.randomUUID().toString(), name, color);
   }

   public SessionType(String id, String name, Color color) {
      this.id = id;
      this.name = name;
      this.color = color;
   }

   // ------------------------
   // toString()
   // ------------------------

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(256);
      builder.append("SessionType [id=");
      builder.append(id);
      builder.append(", name=");
      builder.append(name);
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

   public Color getColor() {
      return color;
   }

   public void setColor(Color color) {
      this.color = color;
   }

   public String getId() {
      return id;
   }

   public void setId(String id) {
      this.id = id;
   }

}

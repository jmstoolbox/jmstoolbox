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
public class SessionType implements Comparable<SessionType> {

   private String name;
   private Color  color;

   // ------------
   // Constructors
   // ------------

   public SessionType(String name, Color color) {
      this.name = name;
      this.color = color;
   }

   // ------------------------
   // Comparable
   // ------------------------
   @Override
   public int compareTo(SessionType o) {
      return this.name.compareTo(o.getName());
   }

   // ------------------------
   // toString()
   // ------------------------

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(128);
      builder.append("SessionType [name=");
      builder.append(name);
      builder.append(", color=");
      builder.append(color);
      builder.append("]");
      return builder.toString();
   }

   // ------------------------
   // hashCode()/equals()
   // ------------------------
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      SessionType other = (SessionType) obj;
      if (name == null) {
         if (other.name != null)
            return false;
      } else
         if (!name.equals(other.name))
            return false;
      return true;
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

}

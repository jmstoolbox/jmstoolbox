/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.connector.transport;

import org.titou10.jtb.jms.model.JTBDestination;

/**
 * Transport Object for {@link JTBDestination}
 * 
 * @author Denis Forveille
 * 
 */
public class Destination {

   public enum Type {
                     QUEUE,
                     TOPIC
   }

   private String name;
   private Type   type;

   public Destination(String name, Type type) {
      this.name = name;
      this.type = type;
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

   public Type getType() {
      return type;
   }

   public void setType(Type type) {
      this.type = type;
   }

}

/*
 * Copyright (C) 2015-2017 Denis Forveille titou10.titou10@gmail.com
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

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Information for a script execution used by an External Connector
 * 
 * @author Denis Forveille
 *
 */
@XmlRootElement
public class ScriptOutput {

   private Integer nbMessages;

   // ------------------------
   // toString()
   // ------------------------

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(128);
      builder.append("ScriptInput [nbMessages=");
      builder.append(nbMessages);
      builder.append("]");
      return builder.toString();
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public Integer getNbMessages() {
      return nbMessages;
   }

   public void setNbMessages(Integer nbMessages) {
      this.nbMessages = nbMessages;
   }

}

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
package org.titou10.jtb.rest.transport;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MessageTransport {

   private String sessionName;
   private String destinationName;
   private String payload;

   // ------------------------
   // toString()
   // ------------------------

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(128);
      builder.append("MessageTransport [sessionName=");
      builder.append(sessionName);
      builder.append(", destinationName=");
      builder.append(destinationName);
      builder.append(", payload=");
      builder.append(payload);
      builder.append("]");
      return builder.toString();
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public String getSessionName() {
      return sessionName;
   }

   public void setSessionName(String sessionName) {
      this.sessionName = sessionName;
   }

   public String getDestinationName() {
      return destinationName;
   }

   public void setDestinationName(String destinationName) {
      this.destinationName = destinationName;
   }

   public String getPayload() {
      return payload;
   }

   public void setPayload(String payload) {
      this.payload = payload;
   }

}

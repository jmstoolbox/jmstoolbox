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
package org.titou10.jtb.jms.model;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

/**
 * JMS Message Types
 * 
 * @author Denis Forveille
 *
 */
public enum JTBMessageType {
                            TEXT("Text"),
                            BYTES("Bytes"),
                            MESSAGE("Message"),
                            MAP("Map"),
                            OBJECT("Object"),
                            STREAM("Stream");

   private String description;

   // --------------------------
   // Constructor / Initialisers
   // --------------------------
   private JTBMessageType(String description) {
      this.description = description;
   }

   public static String[] getTypes() {

      // Fix order
      String[] res = new String[6];
      res[0] = TEXT.getDescription();
      res[1] = BYTES.getDescription();
      res[2] = MESSAGE.getDescription();
      res[3] = MAP.getDescription();
      res[4] = OBJECT.getDescription();
      res[5] = STREAM.getDescription();
      return res;
   }

   // --------------
   // Static Helpers
   // --------------
   public static JTBMessageType fromDescription(String description) {
      for (JTBMessageType i : JTBMessageType.values()) {
         if (i.getDescription().equals(description)) {
            return i;
         }
      }
      return null;
   }

   public static JTBMessageType fromJMSMessage(Message message) {
      if (message instanceof TextMessage) {
         return JTBMessageType.TEXT;
      }
      if (message instanceof BytesMessage) {
         return JTBMessageType.BYTES;
      }
      if (message instanceof MapMessage) {
         return JTBMessageType.MAP;
      }
      if (message instanceof StreamMessage) {
         return JTBMessageType.STREAM;
      }
      if (message instanceof ObjectMessage) {
         return JTBMessageType.OBJECT;
      }
      if (message instanceof Message) {
         return JTBMessageType.MESSAGE;
      }
      return null;
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public String getDescription() {
      return description;
   }

}

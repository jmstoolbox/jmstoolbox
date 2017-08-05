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
package org.titou10.jtb.jms.model;

import javax.jms.Destination;

/**
 * 
 * Encapsulates a JMS Destination
 * 
 * @author Denis Forveille
 * 
 */
public abstract class JTBDestination implements JTBObject {

   private JTBConnection jtbConnection;
   private String        name;
   private Destination   jmsDestination;

   // ------------------------
   // Constructor
   // ------------------------

   public JTBDestination(JTBConnection jtbConnection, String name, Destination jmsDestination) {
      this.jtbConnection = jtbConnection;
      this.name = name;
      this.jmsDestination = jmsDestination;
   }

   // ------------------------
   // Helpers
   // ------------------------

   public final boolean isJTBQueue() {
      return this instanceof JTBQueue;
   }

   public final boolean isJTBTopic() {
      return this instanceof JTBTopic;
   }

   public final JTBQueue getAsJTBQueue() {
      if (isJTBQueue()) {
         return (JTBQueue) this;
      }
      throw new UnsupportedOperationException("This method can only be called for instance of JTBQueue");
   }

   public final JTBTopic getAsJTBTopic() {
      if (isJTBTopic()) {
         return (JTBTopic) this;
      }
      throw new UnsupportedOperationException("This method can only be called for instance of JTBTopic");
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   @Override
   public String getName() {
      return name;
   }

   public Destination getJmsDestination() {
      return jmsDestination;
   }

   public JTBConnection getJtbConnection() {
      return jtbConnection;
   }

}

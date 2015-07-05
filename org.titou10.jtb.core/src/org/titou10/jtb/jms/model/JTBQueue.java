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

import java.util.Comparator;

import javax.jms.Queue;

/**
 * 
 * Encapsulates a JMS Queue
 * 
 * @author Denis Forveille
 * 
 */
public class JTBQueue extends JTBDestination {

   private Queue jmsQueue;

   // ------------------------
   // Constructor
   // ------------------------

   public JTBQueue(JTBSession jtbSession, String name, Queue jmsQueue) {
      super(jtbSession, name, jmsQueue);
      this.jmsQueue = jmsQueue;
   }

   // -------------
   // Class Helpers
   // -------------

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("JTBQueue [");
      builder.append(getName());
      builder.append("]");
      return builder.toString();
   }

   public static class JTBQueueComparator implements Comparator<JTBQueue> {

      @Override
      public int compare(JTBQueue o1, JTBQueue o2) {
         return (o1.getName().compareTo(o2.getName()));
      }
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public Queue getJmsQueue() {
      return jmsQueue;
   }

}

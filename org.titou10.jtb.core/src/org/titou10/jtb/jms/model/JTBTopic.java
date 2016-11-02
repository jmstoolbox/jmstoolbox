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

import javax.jms.Topic;

/**
 * 
 * Encapsulates a JMS Topic
 * 
 * @author Denis Forveille
 * 
 */
public class JTBTopic extends JTBDestination implements Comparable<JTBTopic> {

   private Topic topic;

   // ------------------------
   // Constructor
   // ------------------------

   public JTBTopic(JTBConnection jtbConnection, String name, Topic jmsTopic) {
      super(jtbConnection, name, jmsTopic);
      this.topic = jmsTopic;
   }

   // -------------
   // Class Helpers
   // -------------

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(128);
      builder.append("JTBTopic [");
      builder.append(getName());
      builder.append("]");
      return builder.toString();
   }

   @Override
   public int compareTo(JTBTopic o2) {
      return (this.getName().compareTo(o2.getName()));
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public Topic getTopic() {
      return topic;
   }

}

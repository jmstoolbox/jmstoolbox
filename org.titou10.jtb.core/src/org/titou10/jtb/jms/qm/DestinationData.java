/*
 * Copyright (C) 2017 Denis Forveille titou10.titou10@gmail.com
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

package org.titou10.jtb.jms.qm;

import java.util.SortedSet;

/**
 * Transport object with the list of destinations as manages by Queue Manager
 * 
 * @author Denis Forveille
 * 
 */
public class DestinationData {

   private SortedSet<QueueData> listQueueData;
   private SortedSet<TopicData> listTopicData;

   // ------------------------
   // Constructor
   // ------------------------
   public DestinationData(SortedSet<QueueData> listQueueData, SortedSet<TopicData> listTopicData) {
      this.listQueueData = listQueueData;
      this.listTopicData = listTopicData;
   }

   // ------------------------
   // Standard Getters
   // ------------------------
   public SortedSet<QueueData> getListQueueData() {
      return listQueueData;
   }

   public SortedSet<TopicData> getListTopicData() {
      return listTopicData;
   }

}

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
package org.titou10.jtb.ui.navigator;

import java.util.ArrayList;
import java.util.List;

import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBTopic;

/**
 * JTB Session tree node
 * 
 * Manages a JTBSession
 * 
 * Has 2 "folders": - Queues - Topics
 * 
 * @author Denis Forveille
 * 
 */
public class NodeJTBSession extends NodeAbstract {

   private List<NodeFolder<?>> folders;

   // -----------
   // Constructor
   // -----------

   public NodeJTBSession(JTBSession jtbSession) {
      super(jtbSession, null);
   }

   // -----------
   // NodeAbstract
   // -----------

   @Override
   public List<NodeFolder<?>> getChildren() {
      return folders;
   }

   @Override
   public Boolean hasChildren() {
      JTBSession jtbSession = (JTBSession) getBusinessObject();

      folders = new ArrayList<>();

      // No children if the session is not connected
      if (!(jtbSession.isConnected())) {
         return false;
      }

      List<NodeJTBQueue> nodeQueues = new ArrayList<>();
      for (JTBQueue jtbQueue : jtbSession.getJtbQueues()) {
         nodeQueues.add(new NodeJTBQueue(jtbQueue, this));
      }
      folders.add(new NodeFolder<NodeJTBQueue>("Queues", this, nodeQueues));

      List<NodeJTBTopic> nodeTopics = new ArrayList<>();
      for (JTBTopic jtbTopic : jtbSession.getJtbTopics()) {
         nodeTopics.add(new NodeJTBTopic(jtbTopic, this));
      }
      folders.add(new NodeFolder<NodeJTBTopic>("Topics", this, nodeTopics));

      return true;
   }

}

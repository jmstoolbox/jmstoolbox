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
package org.titou10.jtb.ui.navigator;

import java.util.Objects;
import java.util.SortedSet;

import org.titou10.jtb.jms.model.JTBObject;

/**
 * Parent class for all Nodes
 *
 * @author Denis Forveille
 *
 */
public abstract class NodeAbstract implements JTBObject, Comparable<NodeAbstract> {

   private JTBObject    businessObject;
   private NodeAbstract parentNode;

   public NodeAbstract(JTBObject businessObject, NodeAbstract parentNode) {
      this.businessObject = businessObject;
      this.parentNode = parentNode;
   }

   public abstract SortedSet<? extends NodeAbstract> getChildren();

   public abstract Boolean hasChildren();

   // -------------------------
   // Helpers
   // -------------------------
   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder(64);
      sb.append(this.getClass().getSimpleName());
      sb.append(" [");
      sb.append(getName());
      sb.append("]");
      return sb.toString();
   }

   // ----------------------------------------------------------
   // hashCode/equals (Required to remmeber tree expanded state)
   // ----------------------------------------------------------

   @Override
   public int hashCode() {
      return Objects.hash(businessObject, parentNode);
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (!(obj instanceof NodeAbstract)) {
         return false;
      }
      NodeAbstract other = (NodeAbstract) obj;
      return Objects.equals(businessObject, other.businessObject) && Objects.equals(parentNode, other.parentNode);
   }

   // -----------
   // Comparable
   // -----------

   @Override
   public int compareTo(NodeAbstract o) {
      // Sort Folders first, then sessions
      if (this instanceof NodeFolder) {
         if (o instanceof NodeFolder) {
            return this.getName().compareTo(o.getName());
         } else {
            return -1;
         }
      }

      if (o instanceof NodeFolder) {
         return 1;
      }

      return this.getName().compareTo(o.getName());
   }

   // -------------------------
   // Getters/Setters Standards
   // -------------------------
   @Override
   public String getName() {
      return businessObject.getName();
   }

   public NodeAbstract getParentNode() {
      return parentNode;
   }

   public JTBObject getBusinessObject() {
      return businessObject;
   }

}

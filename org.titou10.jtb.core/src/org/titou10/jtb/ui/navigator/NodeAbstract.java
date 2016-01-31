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
   public String toString() {
      StringBuilder sb = new StringBuilder(64);
      sb.append(this.getClass().getSimpleName());
      sb.append(" : ");
      sb.append(getName());

      return sb.toString();
   }

   // ----------------------------------------------------------
   // hashCode/equals (Required to remmeber tree expanded state)
   // ----------------------------------------------------------

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((businessObject == null) ? 0 : businessObject.getName().hashCode());
      result = prime * result + ((parentNode == null) ? 0 : parentNode.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      NodeAbstract other = (NodeAbstract) obj;
      if (businessObject == null) {
         if (other.businessObject != null)
            return false;
      } else
         if (!businessObject.getName().equals(other.businessObject.getName()))
            return false;
      if (parentNode == null) {
         if (other.parentNode != null)
            return false;
      } else
         if (!parentNode.equals(other.parentNode))
            return false;
      return true;
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

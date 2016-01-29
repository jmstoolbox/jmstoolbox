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
package org.titou10.jtb.ui.navigator;

import java.util.SortedSet;

/**
 * 
 * A folder of NodeAbstract objects in the tree
 * 
 * @author Denis Forveille
 * 
 * @param <T>
 */
public class NodeFolder<T extends NodeAbstract> extends NodeAbstract {

   private String       folderName;
   private SortedSet<T> childrenNodes;

   public NodeFolder(String folderName, NodeAbstract parentNode, SortedSet<T> childrenNodes) {
      super(null, parentNode);
      this.folderName = folderName;
      this.childrenNodes = childrenNodes;
   }

   // ----------------------------------------------------------
   // hashCode/equals (Required to remmeber tree expanded state)
   // ----------------------------------------------------------

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = super.hashCode();
      result = prime * result + ((folderName == null) ? 0 : folderName.hashCode());
      return result;
   }

   @Override
   @SuppressWarnings("rawtypes")
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (!super.equals(obj))
         return false;
      if (getClass() != obj.getClass())
         return false;
      NodeFolder other = (NodeFolder) obj;
      if (folderName == null) {
         if (other.folderName != null)
            return false;
      } else
         if (!folderName.equals(other.folderName))
            return false;
      return true;
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public void addChild(T child) {
      childrenNodes.add(child);
   }

   @Override
   public String getName() {
      return folderName;
   }

   @Override
   public SortedSet<T> getChildren() {
      return childrenNodes;
   }

   @Override
   public Boolean hasChildren() {
      return (childrenNodes.size() > 0);
   }

}

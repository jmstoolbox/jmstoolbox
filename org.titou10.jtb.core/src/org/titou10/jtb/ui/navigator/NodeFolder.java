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

import java.util.List;

/**
 * 
 * A folder of NodeAbstract objects in the tree
 * 
 * @author Denis Forveille
 * 
 * @param <T>
 */
public class NodeFolder<T extends NodeAbstract> extends NodeAbstract {

   private String  folderName;
   private List<T> childrenNodes;

   public NodeFolder(String folderName, NodeAbstract parentNode, List<T> childrenNodes) {
      super(null, parentNode);
      this.folderName = folderName;
      this.childrenNodes = childrenNodes;
   }

   public void addChild(T child) {
      childrenNodes.add(child);
   }

   @Override
   public String getName() {
      return folderName;
   }

   @Override
   public List<T> getChildren() {
      return childrenNodes;
   }

   @Override
   public Boolean hasChildren() {
      return (childrenNodes.size() > 0);
   }

}

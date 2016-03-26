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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Manage the list of JTBSession
 * 
 * @author Denis Forveille
 *
 */
public class NodeJTBSessionProvider implements ITreeContentProvider {

   @Override
   @SuppressWarnings("unchecked")
   public Object[] getElements(Object inputElement) {
      SortedSet<NodeAbstract> nodes = (SortedSet<NodeAbstract>) inputElement;
      return nodes.toArray();
   }

   @Override
   public boolean hasChildren(Object element) {
      if (element instanceof NodeAbstract) {
         NodeAbstract node = (NodeAbstract) element;
         return node.hasChildren();
      }
      return false;
   }

   @Override
   public Object[] getChildren(Object parentElement) {
      NodeAbstract parentNode = (NodeAbstract) parentElement;
      return parentNode.getChildren().toArray();
   }

   @Override
   public Object getParent(Object childElement) {
      NodeAbstract childNode = (NodeAbstract) childElement;
      return childNode.getParentNode();
   }

   @Override
   public void dispose() {
      // NOP
   }

   @Override
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      // NOP
   }
}

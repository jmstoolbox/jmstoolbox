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
package org.titou10.jtb.script;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.Script;

/**
 * 
 * ITreeContentProvider for trees that show Scripts
 * 
 * @author Denis Forveille
 *
 */
public final class ScriptsTreeContentProvider implements ITreeContentProvider {

   // private static final Logger log = LoggerFactory.getLogger(ScriptsTreeContentProvider.class);

   @SuppressWarnings("unchecked")
   public Object[] getElements(Object inputElement) {
      List<Directory> x = (List<Directory>) inputElement;
      return x.toArray();
   }

   public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof Directory) {

         Directory dir = (Directory) parentElement;

         Object[] res = new Object[dir.getDirectory().size() + dir.getScript().size()];
         int i = 0;
         for (Directory d : dir.getDirectory()) {
            res[i++] = d;
         }
         for (Script s : dir.getScript()) {
            res[i++] = s;
         }

         Arrays.sort(res, new ScriptComparator());
         return res;
      }

      return null;
   }

   public Object getParent(Object element) {
      if (element instanceof Directory) {
         Directory dir = (Directory) element;
         return dir.getParent();
      }
      if (element instanceof Script) {
         Script script = (Script) element;
         return script.getParent();
      }
      return null;
   }

   public boolean hasChildren(Object element) {
      if (element instanceof Directory) {
         Directory dir = (Directory) element;
         if ((dir.getDirectory().isEmpty()) && (dir.getScript().isEmpty())) {
            return false;
         } else {
            return true;
         }
      }
      return false;
   }

   @Override
   public void dispose() {
   }

   @Override
   public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
   }

   /**
    * Sort Scripts first on folders, then on scripts
    * 
    * @author Denis Forveille
    *
    */
   private final class ScriptComparator implements Comparator<Object> {

      @Override
      public int compare(Object o1, Object o2) {
         if (o1 instanceof Directory) {
            if (o2 instanceof Directory) {
               return ((Directory) o1).getName().compareTo(((Directory) o2).getName());
            } else {
               return -1;
            }
         } else {
            if (o2 instanceof Directory) {
               return 1;
            } else {
               return ((Script) o1).getName().compareTo(((Script) o2).getName());
            }
         }
      }
   }
}

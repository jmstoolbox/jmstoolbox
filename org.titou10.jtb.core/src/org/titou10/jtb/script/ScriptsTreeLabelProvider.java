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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.Script;
import org.titou10.jtb.util.Utils;

/**
 * 
 * LabelProvider for trees that show Templates
 * 
 * @author Denis Forveille
 *
 */
public final class ScriptsTreeLabelProvider extends LabelProvider {

   @Override
   public Image getImage(Object element) {
      if (element instanceof Directory) {
         return Utils.getImage(this.getClass(), "icons/scripts/folder.png");
      } else {
         return Utils.getImage(this.getClass(), "icons/scripts/script.png");
      }
   }

   @Override
   public String getText(Object element) {
      if (element instanceof Directory) {
         return ((Directory) element).getName();
      }
      if (element instanceof Script) {
         return ((Script) element).getName();
      }
      return "???" + element.getClass();

   }
}

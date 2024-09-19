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

import jakarta.xml.bind.Unmarshaller.Listener;

import org.titou10.jtb.script.gen.Directory;
import org.titou10.jtb.script.gen.Script;

/**
 * Assign the "parent" directory to Script or Directories when unmarshalling Scripts config file
 * 
 * @author Denis Forveille
 *
 */
public class ScriptJAXBParentListener extends Listener {

   @Override
   public void afterUnmarshal(Object target, Object parent) {

      if (parent instanceof Directory) {
         Directory p = (Directory) parent;
         if (target instanceof Directory) {
            Directory d = (Directory) target;
            d.setParent(p);
         }
         if (target instanceof Script) {
            Script s = (Script) target;
            s.setParent(p);
         }
      }
   }

}

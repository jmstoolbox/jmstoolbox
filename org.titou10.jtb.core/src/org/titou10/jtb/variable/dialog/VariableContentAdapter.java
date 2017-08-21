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
package org.titou10.jtb.variable.dialog;

import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Insert the selected variable, eventually replace the '$' character used to activate content assist
 * 
 * @author Denis Forveille
 *
 */
public class VariableContentAdapter extends TextContentAdapter {

   private static final Logger log = LoggerFactory.getLogger(VariableContentAdapter.class);

   @Override
   public void insertControlContents(Control control, String replacementText, int cursorPosition) {

      if (cursorPosition == 0) {
         return;
      }

      Text t = (Text) control;
      Point selection = t.getSelection();
      String originalText = t.getText();

      log.debug("insertControlContents selection={} text={} cursorPosition={}", selection, replacementText, cursorPosition);

      int posMarker = selection.y - 1;
      if (posMarker >= 0) {
         if (originalText.charAt(posMarker) == '$') {
            posMarker--;
         }
      }
      selection.y = posMarker + 1;

      t.setSelection(selection);
      t.insert(replacementText);
   }
}

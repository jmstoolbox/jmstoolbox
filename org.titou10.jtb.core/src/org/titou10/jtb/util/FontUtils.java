/*
 * Copyright (C) 2024 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.util;

import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;

/**
 *
 * Utility class for fonts
 *
 * @author Thomas Raddatz (tools400)
 *
 */
public final class FontUtils {

   /*
    * http://www.java2s.com/Tutorial/Java/0300__SWT-2D-Graphics/
    * UsingFontMetricstogetcharwidth.htm (FontMetricsCharWidth)
    */
   public static double getFontCharWidth(Drawable aDrawable) {
      GC gc = null;
      try {
         gc = new GC(aDrawable);
         return gc.getFontMetrics().getAverageCharacterWidth();
      } finally {
         if (gc != null) {
            gc.dispose();
         }
      }
   }

   /*
    * http://www.java2s.com/Tutorial/Java/0300__SWT-2D-Graphics/
    * UsingFontMetricstogetcharwidth.htm (FontMetricsCharWidth)
    */
   public static int getFontCharHeight(Drawable aDrawable) {
      GC gc = null;
      try {
         gc = new GC(aDrawable);
         return gc.getFontMetrics().getHeight();
      } finally {
         if (gc != null) {
            gc.dispose();
         }
      }
   }

}

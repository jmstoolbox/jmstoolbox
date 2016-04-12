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
package org.titou10.jtb.rest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that holds various utility methods
 * 
 * @author Denis Forveille
 *
 */
public final class Utils {

   private static final Logger log = LoggerFactory.getLogger(Utils.class);

   public static Throwable getCause(Throwable e) {
      Throwable cause = null;
      Throwable result = e;

      while (null != (cause = result.getCause()) && (result != cause)) {
         result = cause;
      }
      return result;
   }

   // ------------------
   // Pure Utility Class
   // ------------------
   private Utils() {
      // NOP
   }

}

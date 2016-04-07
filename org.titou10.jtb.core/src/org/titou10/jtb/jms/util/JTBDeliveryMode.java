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
package org.titou10.jtb.jms.util;

import javax.jms.DeliveryMode;

/**
 * 
 * Enum representation of a {@link DeliveryMode}
 * 
 * 
 * @author Denis Forveille
 *
 */
public enum JTBDeliveryMode {
                             NON_PERSISTENT(DeliveryMode.NON_PERSISTENT),
                             PERSISTENT(DeliveryMode.PERSISTENT);

   private int intValue;

   // --------------------------
   // Constructor / Initialisers
   // --------------------------
   private JTBDeliveryMode(int intValue) {
      this.intValue = intValue;
   }

   // --------------
   // Static Helpers
   // --------------

   public static JTBDeliveryMode fromValue(Integer intValue) {
      if (intValue == null) {
         return null;
      }
      for (JTBDeliveryMode dm : values()) {
         if (dm.intValue == intValue) {
            return dm;
         }
      }
      throw new IllegalArgumentException("'" + intValue + "' is not a valid DeliveryMode");
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public int intValue() {
      return intValue;
   }
}

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
package org.titou10.jtb.ui.dnd;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * 
 * Drag & Drop Transfer for Templates and Template Folders
 * 
 * @author Denis Forveille
 *
 */
public class TransferTemplate extends ByteArrayTransfer {

   private static final String     MYTYPENAME = "jtb_Template";
   private static final int        MYTYPEID   = registerType(MYTYPENAME);
   private static TransferTemplate instance   = new TransferTemplate();

   public static TransferTemplate getInstance() {
      return instance;
   }

   @Override
   protected int[] getTypeIds() {
      return new int[] { MYTYPEID };
   }

   @Override
   protected String[] getTypeNames() {
      return new String[] { MYTYPENAME };
   }

   @Override
   protected void javaToNative(Object object, TransferData transferData) {
      // NOP
   }

   @Override
   protected Object nativeToJava(TransferData transferData) {
      return null;
   }

}

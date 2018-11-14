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
package org.titou10.jtb.ui.part;

import java.util.Map;

import javax.jms.Message;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.titou10.jtb.cs.ColumnSystemHeader;

/**
 * TableLabelProvider for ColumnSystemHeader
 * 
 * @author Denis Forveille
 * 
 */
public final class ColumnSystemHeaderTableLabelProvider implements ITableLabelProvider {

   private Message jmsMessage;

   public void setJmsMessage(Message jmsMessage) {
      this.jmsMessage = jmsMessage;
   }

   @Override
   @SuppressWarnings("unchecked")
   public String getColumnText(Object element, int columnIndex) {
      Map.Entry<String, ColumnSystemHeader> e = (Map.Entry<String, ColumnSystemHeader>) element;
      if (columnIndex == 0) {
         return e.getKey();
      }
      if (e.getValue() == null) {
         return "";
      } else {
         Object o = e.getValue().getColumnSystemValue(jmsMessage);
         return o == null ? "" : o.toString();
      }
   }

   @Override
   public Image getColumnImage(Object arg0, int arg1) {
      return null;
   }

   @Override
   public void addListener(ILabelProviderListener arg0) {
      // NOP
   }

   @Override
   public void dispose() {
      // NOP
   }

   @Override
   public boolean isLabelProperty(Object arg0, String arg1) {
      return false;
   }

   @Override
   public void removeListener(ILabelProviderListener arg0) {
      // NOP
   }
}

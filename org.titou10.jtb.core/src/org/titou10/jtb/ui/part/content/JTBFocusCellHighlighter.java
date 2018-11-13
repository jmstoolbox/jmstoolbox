/*
 * Copyright (C) 2018 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.ui.part.content;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.titou10.jtb.util.Constants;

/**
 * 
 * Class used to capture the content of the cell that has focus and store it in the EclipseContext
 * 
 * @author Denis Forveille
 * 
 */
public class JTBFocusCellHighlighter extends FocusCellHighlighter {

   private IEclipseContext windowContext;
   private Table           table;

   public JTBFocusCellHighlighter(ColumnViewer viewer, IEclipseContext windowContext) {
      super(viewer);
      this.windowContext = windowContext;
      this.table = ((TableViewer) viewer).getTable();
   }

   @Override
   protected void focusCellChanged(ViewerCell newCell, ViewerCell oldCell) {
      super.focusCellChanged(newCell, oldCell);

      if (newCell == null) {
         windowContext.remove(Constants.COLUMN_TYPE_COLUMN_SYSTEM_HEADER);
         windowContext.remove(Constants.COLUMN_TYPE_USER_PROPERTY);
         return;
      }

      TableColumn tableColumn = table.getColumn(newCell.getColumnIndex());

      Object csh = tableColumn.getData(Constants.COLUMN_TYPE_COLUMN_SYSTEM_HEADER);
      if (csh == null) {
         windowContext.remove(Constants.COLUMN_TYPE_COLUMN_SYSTEM_HEADER);
      } else {
         windowContext.set(Constants.COLUMN_TYPE_COLUMN_SYSTEM_HEADER, csh);
      }

      Object userProperty = tableColumn.getData(Constants.COLUMN_TYPE_USER_PROPERTY);
      if (userProperty == null) {
         windowContext.remove(Constants.COLUMN_TYPE_USER_PROPERTY);
      } else {
         windowContext.set(Constants.COLUMN_TYPE_USER_PROPERTY, userProperty);
      }

   }
}

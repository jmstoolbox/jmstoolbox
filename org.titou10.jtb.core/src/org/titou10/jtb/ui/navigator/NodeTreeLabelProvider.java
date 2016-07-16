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
package org.titou10.jtb.ui.navigator;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.SWTResourceManager;
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.util.Utils;

/**
 * TreeLabelProvider for the Session Browser
 * 
 * @author Denis Forveille
 *
 */
public class NodeTreeLabelProvider extends LabelProvider implements IColorProvider {

   private JTBSessionClientType jtbSessionClientType;

   public NodeTreeLabelProvider(JTBSessionClientType jtbSessionClientType) {
      this.jtbSessionClientType = jtbSessionClientType;
   }

   @Override
   public String getText(Object element) {
      if (element instanceof NodeAbstract) {
         NodeAbstract node = (NodeAbstract) element;

         if (element instanceof NodeJTBSession) {
            NodeJTBSession nodeJTBSession = (NodeJTBSession) element;
            JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();
            JTBConnection jtbConnection = jtbSession.getJTBConnection(jtbSessionClientType);

            // Add filterrPattern to Name
            if (jtbConnection.getFilterPattern() != null) {
               StringBuilder sb = new StringBuilder(128);
               sb.append(node.getName());
               sb.append(" [");
               sb.append(jtbConnection.getFilterPattern());
               sb.append("]");
               return sb.toString();
            }
         }

         return node.getName();
      }
      return element.toString();

   }

   @Override
   public Image getImage(Object element) {

      if (element instanceof NodeJTBQueue) {
         return Utils.getImage(this.getClass(), "icons/queue/page_white_stack.png");
      }

      if (element instanceof NodeJTBTopic) {
         return Utils.getImage(this.getClass(), "icons/topics/newspaper.png");
      }

      if (element instanceof NodeJTBSession) {
         return Utils.getImage(this.getClass(), "icons/folder_table.png");
      }

      return Utils.getImage(this.getClass(), "icons/folder.png");

   }

   @Override
   public Color getForeground(Object element) {

      if (element instanceof NodeJTBSession) {
         NodeJTBSession nodeJTBSession = (NodeJTBSession) element;
         JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();
         JTBConnection jtbConnection = jtbSession.getJTBConnection(jtbSessionClientType);

         // Display sessions with active filter in blue
         if (jtbConnection.isConnected()) {
            if (jtbConnection.isFilterApplied()) {
               return SWTResourceManager.getColor(SWT.COLOR_BLUE);
            }
         }

         // Display sessions without a valid QM in red
         if (!(jtbSession.isConnectable())) {
            return SWTResourceManager.getColor(SWT.COLOR_RED);
         }
      }
      return null;
   }

   @Override
   public Color getBackground(Object element) {
      return null;
   }

}

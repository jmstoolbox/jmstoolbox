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
package org.titou10.jtb.ui.navigator;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.wb.swt.SWTResourceManager;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.jms.model.JTBTopic;
import org.titou10.jtb.sessiontype.SessionTypeManager;

/**
 * TreeLabelProvider for the Session Browser
 * 
 * @author Denis Forveille
 *
 */
public class NodeTreeLabelProvider extends LabelProvider implements IColorProvider, IStyledLabelProvider {

   private static final int                               SQUARE_SIZE_PX = 8;

   private org.titou10.jtb.sessiontype.SessionTypeManager sessionTypeManager;
   private JTBSessionClientType                           jtbSessionClientType;

   public NodeTreeLabelProvider(SessionTypeManager sessionTypeManager, JTBSessionClientType jtbSessionClientType) {
      this.sessionTypeManager = sessionTypeManager;
      this.jtbSessionClientType = jtbSessionClientType;
   }

   @Override
   public String getText(Object element) {
      if (!(element instanceof NodeAbstract)) {
         return element.toString();
      }
      return null;
   }

   @Override
   public Image getImage(Object element) {

      if (element instanceof NodeJTBQueue) {
         NodeJTBQueue nodeJTBQueue = (NodeJTBQueue) element;
         JTBQueue jtbQueue = (JTBQueue) nodeJTBQueue.getBusinessObject();
         SessionDef sessionDef = jtbQueue.getJtbConnection().getSessionDef();
         Color sessionTypeColor = sessionTypeManager.getBackgroundColorForSessionTypeName(sessionDef.getSessionType());

         if (jtbQueue.isBrowsable()) {
            Image i = SWTResourceManager.getImage(this.getClass(), "icons/queue/page_white_stack.png");
            if (sessionTypeColor == null) {
               return i;
            }
            Image x = SWTResourceManager.createImageSolidColor(sessionTypeColor, SQUARE_SIZE_PX, SQUARE_SIZE_PX);
            return SWTResourceManager.decorateImage(i, x, SWTResourceManager.TOP_RIGHT);
         } else {
            Image i = SWTResourceManager.getImage(this.getClass(), "icons/queue/page_white_link.png");
            if (sessionTypeColor == null) {
               return i;
            }
            Image x = SWTResourceManager.createImageSolidColor(sessionTypeColor, SQUARE_SIZE_PX, SQUARE_SIZE_PX);
            return SWTResourceManager.decorateImage(i, x, SWTResourceManager.TOP_RIGHT);
         }
      }

      if (element instanceof NodeJTBTopic) {
         // return SWTResourceManager.getImage(this.getClass(), "icons/topics/newspaper.png");
         NodeJTBTopic nodeJTBTopic = (NodeJTBTopic) element;
         JTBTopic jtbQueue = (JTBTopic) nodeJTBTopic.getBusinessObject();
         SessionDef sessionDef = jtbQueue.getJtbConnection().getSessionDef();
         Color sessionTypeColor = sessionTypeManager.getBackgroundColorForSessionTypeName(sessionDef.getSessionType());

         Image i = SWTResourceManager.getImage(this.getClass(), "icons/topics/newspaper.png");
         if (sessionTypeColor == null) {
            return i;
         }
         Image x = SWTResourceManager.createImageSolidColor(sessionTypeColor, SQUARE_SIZE_PX, SQUARE_SIZE_PX);
         return SWTResourceManager.decorateImage(i, x, SWTResourceManager.TOP_RIGHT);
      }

      if (element instanceof NodeJTBSession) {
         return SWTResourceManager.getImage(this.getClass(), "icons/folder_table.png");
      }

      return SWTResourceManager.getImage(this.getClass(), "icons/folder.png");

   }

   @Override
   public Color getForeground(Object element) {
      if (element instanceof NodeJTBSession) {
         NodeJTBSession nodeJTBSession = (NodeJTBSession) element;
         JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();

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

   @Override
   public StyledString getStyledText(Object element) {

      if (!(element instanceof NodeAbstract)) {
         return new StyledString(element.toString());
      }
      NodeAbstract node = (NodeAbstract) element;

      if ((!(node instanceof NodeJTBSession)) && (!(node instanceof NodeJTBQueue)) && (!(node instanceof NodeJTBTopic))) {
         return new StyledString(node.getName());
      }

      // Special case for NodeJTBSession
      if (node instanceof NodeJTBSession) {

         NodeJTBSession nodeJTBSession = (NodeJTBSession) element;
         JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();
         JTBConnection jtbConnection = jtbSession.getJTBConnection(jtbSessionClientType);
         Color sessionTypeColor = sessionTypeManager
                  .getBackgroundColorForSessionTypeName(jtbSession.getSessionDef().getSessionType());

         String sessionName = node.getName();
         int sessionNameLength = sessionName.length();
         String textToDisplay = sessionName;

         boolean filterExists = jtbConnection.getFilterPattern() != null;
         if (jtbConnection.getFilterPattern() != null) {
            StringBuilder sb = new StringBuilder(128);
            sb.append(" [");
            sb.append(jtbConnection.getFilterPattern());
            sb.append("]");
            textToDisplay = textToDisplay + sb.toString();
         }

         StyledString sessionStyleString = new StyledString(textToDisplay);
         // Apply background color on session name
         sessionStyleString.setStyle(0, sessionNameLength, new Styler() {
            @Override
            public void applyStyles(TextStyle textStyle) {
               textStyle.background = sessionTypeColor;
            }
         });
         // Strikeout filter if not connected or filterd not applied
         if (filterExists) {
            if (jtbConnection.isConnected()) {
               int start = sessionNameLength + 2;
               int len = textToDisplay.length() - sessionNameLength - 3;
               boolean strike = jtbConnection.isFilterApplied() ? false : true;
               sessionStyleString.setStyle(start, len, new Styler() {
                  @Override
                  public void applyStyles(TextStyle textStyle) {
                     textStyle.strikeout = strike;
                  }
               });
            }
         }

         return sessionStyleString;
      }

      return new StyledString(node.getName());

      // Color c = null;
      // if (node instanceof NodeJTBQueue) {
      // NodeJTBQueue nodeJTBQueue = (NodeJTBQueue) element;
      // JTBQueue jtbQueue = (JTBQueue) nodeJTBQueue.getBusinessObject();
      // SessionDef sessionDef = jtbQueue.getJtbConnection().getSessionDef();
      // c = sessionTypeManager.getBackgroundColorForSessionTypeName(sessionDef.getSessionType());
      // }
      //
      // if (node instanceof NodeJTBTopic) {
      // NodeJTBTopic nodeJTBTopic = (NodeJTBTopic) element;
      // JTBTopic jtbTopic = (JTBTopic) nodeJTBTopic.getBusinessObject();
      // SessionDef sessionDef = jtbTopic.getJtbConnection().getSessionDef();
      // c = sessionTypeManager.getBackgroundColorForSessionTypeName(sessionDef.getSessionType());
      // }
      // if (c == null) {
      // // This session is not associated with a SessionType
      // return new StyledString(node.getName());
      // }
      //
      // // Add 2 spaces with the color associated to Session Type
      // final Color cc = c;
      // StyledString ss = new StyledString(" " + node.getName());
      // ss.setStyle(0, 2, new Styler() {
      //
      // @Override
      // public void applyStyles(TextStyle textStyle) {
      // textStyle.background = cc;
      // // textStyle.borderStyle = SWT.BORDER_DOT;
      // }
      // });
      // return ss;
   }
}

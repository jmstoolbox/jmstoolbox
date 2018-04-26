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
package org.titou10.jtb.ui.part.content;

import java.util.Deque;
import java.util.List;

import javax.jms.MessageConsumer;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.titou10.jtb.cs.gen.ColumnsSet;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBSession;

/**
 * Hold all information to the destination shown in a tab in the JTBSEssionContentViewPart
 * 
 * @author Denis Forveille
 *
 */
final class TabData {

   enum TabDataType {
                     JTBDESTINATION,
                     JTBSESSION
   }

   TabDataType             type;
   JTBDestination          jtbDestination;
   JTBSession              jtbSession;

   CTabItem                tabItem;
   TableViewer             tableViewer;
   List<TableViewerColumn> tableViewerColumns;
   ColumnsSet              columnsSet;
   Combo                   payloadSearchText;
   List<String>            payloadSearchItemsHistory;
   Combo                   selectorsSearchText;
   List<String>            selectorsSearchItemsHistory;

   // Queues specifics
   AutoRefreshJob          autoRefreshJob;
   boolean                 autoRefreshActive;

   CollectQueueDepthJob    collectQueueDepthJob;

   // Topic specifics
   Deque<JTBMessage>       topicMessages;
   int                     maxMessages;
   MessageConsumer         topicMessageConsumer;

   // Synthetic View Specific
   Text                    filterText;

   // Message selected
   JTBMessage              selectedJTBMessage;

   // ------------
   // Constructors
   // ------------
   TabData(JTBDestination jtbDestination) {
      this.type = TabDataType.JTBDESTINATION;
      this.jtbDestination = jtbDestination;
   }

   TabData(JTBSession jtbSession) {
      this.type = TabDataType.JTBSESSION;
      this.jtbSession = jtbSession;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(1024);
      builder.append("TabData [type=");
      builder.append(type);
      builder.append(", jtbDestination=");
      builder.append(jtbDestination);
      builder.append(", jtbSession=");
      builder.append(jtbSession);
      builder.append(", tabItem=");
      builder.append(tabItem);
      builder.append(", tableViewer=");
      builder.append(tableViewer);
      builder.append(", autoRefreshJob=");
      builder.append(autoRefreshJob);
      builder.append(", autoRefreshActive=");
      builder.append(autoRefreshActive);
      builder.append(", topicMessages=");
      builder.append(topicMessages);
      builder.append(", maxMessages=");
      builder.append(maxMessages);
      builder.append(", topicMessageConsumer=");
      builder.append(topicMessageConsumer);
      builder.append("]");
      return builder.toString();
   }

}

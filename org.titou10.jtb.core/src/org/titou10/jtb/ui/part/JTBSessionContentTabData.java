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
package org.titou10.jtb.ui.part;

import java.util.Deque;
import java.util.List;

import javax.jms.MessageConsumer;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Combo;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.ui.part.JTBSessionContentViewPart.AutoRefreshJob;

/**
 * Hold all information to the destination shown in a tab in the JTBSEssionContentViewPart
 * 
 * @author Denis Forveille
 *
 */
public class JTBSessionContentTabData {
   JTBDestination    jtbDestination;
   CTabItem          tabItem;
   TableViewer       tableViewer;
   AutoRefreshJob    autoRefreshJob;
   boolean           autoRefreshActive;
   Combo             searchText;
   Combo             searchType;
   List<String>      searchItemsHistory;
   Deque<JTBMessage> topicMessages;
   int               maxMessages;
   MessageConsumer   topicMessageConsumer;

   JTBSessionContentTabData(JTBDestination jtbDestination) {
      this.jtbDestination = jtbDestination;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(512);
      builder.append("TabData [jtbDestination=");
      builder.append(jtbDestination);
      builder.append(", tabItem=");
      builder.append(tabItem);
      builder.append(", tableViewer=");
      builder.append(tableViewer);
      builder.append(", autoRefreshJob=");
      builder.append(autoRefreshJob);
      builder.append(", autoRefreshActive=");
      builder.append(autoRefreshActive);
      builder.append(", searchText=");
      builder.append(searchText);
      builder.append(", searchType=");
      builder.append(searchType);
      builder.append(", searchItemsHistory=");
      builder.append(searchItemsHistory);
      builder.append(", maxMessages=");
      builder.append(maxMessages);
      builder.append(", topicMessageConsumer=");
      builder.append(topicMessageConsumer);
      builder.append("]");
      return builder.toString();
   }

}

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
package org.titou10.jtb.ui.part.content;

import java.util.Deque;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBTopic;

/**
 * MessageListener to capture messages published to a topic
 * 
 * @author Denis Forveille
 *
 */
final class TopicListener implements MessageListener {

   private static final Logger     log = LoggerFactory.getLogger(TopicListener.class);

   private UISynchronize           sync;

   private final JTBTopic          jtbTopic;
   private final Deque<JTBMessage> messages;
   private final TableViewer       tableViewer;
   private final CTabItem          tabItemTopic;
   private final int               maxSize;

   public TopicListener(UISynchronize sync,
                        JTBTopic jtbTopic,
                        Deque<JTBMessage> messages,
                        TableViewer tableViewer,
                        CTabItem tabItemTopic,
                        int maxSize) {
      this.sync = sync;

      this.messages = messages;
      this.jtbTopic = jtbTopic;
      this.tableViewer = tableViewer;
      this.tabItemTopic = tabItemTopic;
      this.maxSize = maxSize;
   };

   @Override
   public void onMessage(final Message jmsMessage) {
      sync.asyncExec(new Runnable() {
         @Override
         public void run() {
            try {
               log.debug("{} : Received message with id '{}'", jtbTopic, jmsMessage.getJMSMessageID());
               messages.addFirst(new JTBMessage(jtbTopic, jmsMessage));
               if (messages.size() > maxSize) {
                  messages.pollLast();
                  tabItemTopic.setImage(SWTResourceManager.getImage(this.getClass(), "icons/topics/warning-16.png"));
               }
            } catch (JMSException e) {
               // TODO : Notify end user?
               log.error("Exception occurred when receiving a message", e);
            }

            // Send event to refresh list of messages
            tableViewer.refresh();
         }
      });
   }
};

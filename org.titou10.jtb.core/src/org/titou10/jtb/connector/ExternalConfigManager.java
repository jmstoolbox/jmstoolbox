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
package org.titou10.jtb.connector;

import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.model.JTBSession;

/**
 * Exposes ConfigManager services to connector plugins
 * 
 * @author Denis Forveille
 *
 */
public class ExternalConfigManager {

   private static final Logger log = LoggerFactory.getLogger(ExternalConfigManager.class);

   private ConfigManager       cm;

   public ExternalConfigManager(ConfigManager cm) {
      this.cm = cm;
   }

   // Services related to Messages

   public void getMessage(String sessionName) {

   }

   public void postMessage(String sessionName, String destinationName, String payload) {
      log.warn("sendMessage : {} {} {}", sessionName, destinationName, payload);

      JTBSession jtbSession = cm.getJTBSessionByName(sessionName);
      try {
         if (!(jtbSession.isConnected())) {
            jtbSession.connectOrDisconnect();
         }

         JTBDestination jtbDestination = jtbSession.getJTBDestinationByName(destinationName);

         // Reuse connection or connect

         // Create Message
         TextMessage jmsMessage = (TextMessage) jtbSession.createJMSMessage(JTBMessageType.TEXT);
         jmsMessage.setText(payload);
         JTBMessage jtbMessage = new JTBMessage(jtbDestination, jmsMessage);

         // Post Message

         jtbSession.sendMessage(jtbMessage);
      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

   }

   public void emptyQueue(String sessionName) {

   }

   // Services related to Scripts

}

/*
 * Copyright (C) 2015-2017 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.visualizer.ui;

import javax.jms.JMSException;

import org.eclipse.core.expressions.PropertyTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;

/**
 * 
 * Enable the "Show Payload as.." menu depending on the message type
 * 
 * @author Denis Forveille
 *
 */
public class VisualizerShowPayloadAsPropertyTester extends PropertyTester {

   private static final Logger log = LoggerFactory.getLogger(VisualizerShowPayloadAsPropertyTester.class);

   @Override
   public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
      log.debug("test {} {}", receiver, property);

      JTBMessage jtbMessage = (JTBMessage) receiver;

      JTBMessageTemplate template;
      try {
         template = new JTBMessageTemplate(jtbMessage);
      } catch (JMSException e) {
         log.error("Error while creating a JTBMessageTemplate from JTBMessage", e);
         return false;
      }

      switch (jtbMessage.getJtbMessageType()) {
         case TEXT:
            if ((template.getPayloadText() == null) || (template.getPayloadText().isEmpty())) {
               return false;
            }
            return true;
         case BYTES:
            if ((template.getPayloadBytes() == null) || (template.getPayloadBytes().length == 0)) {
               return false;
            }
            return true;

         case MAP:
            if ((template.getPayloadMap() == null) || (template.getPayloadMap().isEmpty())) {
               return false;
            }
            return true;
         default:
            return false;
      }
   }

}

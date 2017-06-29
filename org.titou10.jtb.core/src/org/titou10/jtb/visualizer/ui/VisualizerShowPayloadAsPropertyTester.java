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

import java.util.List;

import javax.inject.Named;
import javax.jms.JMSException;

import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.util.Utils;

/**
 * 
 * Enable the "Open Payload as.." menu depending on the message type
 * 
 * @author Denis Forveille
 *
 */
public class VisualizerShowPayloadAsPropertyTester {

   private static final Logger log = LoggerFactory.getLogger(VisualizerShowPayloadAsPropertyTester.class);

   @Evaluate
   public boolean showOpenPayloadAs(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<JTBMessage> selection) {
      log.debug("showOpenPayloadAs {}", selection);

      if (Utils.isNullorEmpty(selection)) {
         return false;
      }

      JTBMessage jtbMessage = selection.get(0);

      try {
         JTBMessageTemplate jtbMessageTemplate = new JTBMessageTemplate(jtbMessage);
         return jtbMessageTemplate.hasPayload();
      } catch (JMSException e) {
         log.error("Error while creating a JTBMessageTemplate from JTBMessage", e);
         return false;
      }
   }

}

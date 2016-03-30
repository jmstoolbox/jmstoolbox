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
package org.titou10.jtb.handler;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.util.Constants;

/**
 * Manage the "Copy as Selector" command
 * 
 * @author Denis Forveille
 * 
 */
public class MessageCopyAsSelectorHandler {

   private static final Logger log = LoggerFactory.getLogger(MessageCopyAsSelectorHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Execute
   public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<Map.Entry<String, Object>> selection) {
      log.debug("execute. Selection : {}", selection);

      if (selection == null) {
         return;
      }

      // Refresh List of Message
      eventBroker.send(Constants.EVENT_ADD_SELECTOR_CLAUSE, selection);
   }
}

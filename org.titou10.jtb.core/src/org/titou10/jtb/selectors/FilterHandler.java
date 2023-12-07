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
package org.titou10.jtb.selectors;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.util.Constants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Excecute the action linked to the dynamic menu displayed by the "Filter..." menu
 * 
 * @author Denis Forveille
 *
 */
public class FilterHandler {

   private static final Logger log = LoggerFactory.getLogger(FilterHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Execute
   public void execute(Shell shell,
                       MMenuItem menuItem,
                       @Named(Constants.CURRENT_TAB_JTBDESTINATION) JTBDestination jtbDestination) {
      log.debug("execute");

      String selector = (String) menuItem.getTransientData().get(Constants.FILTER_PARAM_SELECTOR);

      // Add selector
      eventBroker.send(Constants.EVENT_ADD_SELECTOR_CLAUSE, selector);

      // Refresh List of Message
      if (jtbDestination.isJTBQueue()) {
         eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, jtbDestination);
      } else {
         eventBroker.send(Constants.EVENT_REFRESH_TOPIC_SHOW_MESSAGES, jtbDestination);
      }
   }
}

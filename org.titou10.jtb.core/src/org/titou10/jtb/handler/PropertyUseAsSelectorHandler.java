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
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.cs.ColumnSystemHeader;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Use as Selector" command
 * 
 * @author Denis Forveille
 * 
 */
public class PropertyUseAsSelectorHandler {

   private static final Logger log            = LoggerFactory.getLogger(PropertyUseAsSelectorHandler.class);

   private static final String SEARCH_STRING  = "%s = '%s'";
   private static final String SEARCH_NUMBER  = "%s = %d";
   private static final String SEARCH_DOUBLE  = "%s = %f";
   private static final String SEARCH_BOOLEAN = "%s = %b";
   private static final String SEARCH_NULL    = "%s IS null";
   private static final String QUOTE          = "'";
   private static final String DOUBLE_QUOTE   = "''";

   @Inject
   private IEventBroker        eventBroker;

   @Execute
   public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<Map.Entry<String, Object>> selection) {
      log.debug("execute. Selection : {}", selection);

      if (selection == null) {
         return;
      }

      StringBuilder sb = new StringBuilder(128);

      for (Map.Entry<String, Object> e : selection) {

         if (sb.length() > 0) {
            sb.append(" AND ");
         }

         String key = e.getKey();
         Object value = e.getValue();

         // Special treatment for Timestamps
         if (ColumnSystemHeader.isTimestamp(key)) {
            value = Utils.extractLongFromTimestamp(value);
         } else {
            if (Utils.isEmpty(value)) {
               sb.append(String.format(SEARCH_NULL, key));
               continue;
            }
         }

         // Special treatment for JMS_DELIVERY_MODE
         if (ColumnSystemHeader.fromHeaderName(key) == ColumnSystemHeader.JMS_DELIVERY_MODE) {
            value = Utils.extractJTBDeliveryMode(value);
         }

         Class<?> clazz = value.getClass();
         switch (clazz.getSimpleName()) {

            case "Boolean":
               sb.append(String.format(SEARCH_BOOLEAN, key, value));
               continue;

            case "Short":
            case "Integer":
            case "Long":
               sb.append(String.format(SEARCH_NUMBER, key, value));
               continue;

            case "Float":
            case "Double":
               // Locale.US = force "." as decimal point
               sb.append(String.format(Locale.US, SEARCH_DOUBLE, key, value));
               continue;

            default:
               // Byte
               // String
               // Escape quotes
               String stringValue = value.toString().replaceAll(QUOTE, DOUBLE_QUOTE);
               sb.append(String.format(SEARCH_STRING, key, stringValue));
               continue;
         }
      }

      // Refresh List of Message
      eventBroker.send(Constants.EVENT_ADD_SELECTOR_CLAUSE, sb.toString());
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<Map.Entry<String, Object>> selection,
                             @Optional MMenuItem menuItem) {

      if (Utils.isEmpty(selection)) {
         return Utils.disableMenu(menuItem);
      }

      for (Map.Entry<String, Object> e : selection) {
         // For JMS System Properties, restrict the function to allowed headers following JMS specs
         if (!ColumnSystemHeader.isSelector(e.getKey())) {
            // Show as disabled instead of removing the menu
            // return Utils.disableMenu(menuItem);
            return false;
         }
      }
      return Utils.enableMenu(menuItem);
   }
}

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

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.cs.ColumnSystemHeader;
import org.titou10.jtb.cs.gen.UserProperty;
import org.titou10.jtb.cs.gen.UserPropertyOrigin;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;
import org.titou10.jtb.visualizer.ui.VisualizerShowPayloadAsPropertyTester;

/**
 * 
 * Enable the "Filter.." menu depending on the message type
 * 
 * @author Denis Forveille
 *
 */
public class FilterPropertyTester {

   private static final Logger log = LoggerFactory.getLogger(VisualizerShowPayloadAsPropertyTester.class);

   @Evaluate
   public boolean evaluate(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<?> selection,
                           @Optional @Named(Constants.COLUMN_TYPE_COLUMN_SYSTEM_HEADER) ColumnSystemHeader csh,
                           @Optional @Named(Constants.COLUMN_TYPE_USER_PROPERTY) UserProperty userProperty) {
      log.debug("evaluate {}", selection);

      // The menu can be activates either
      // - from a message from the message browser -> List<JTBMessage>
      // - by selecting a header or property in the message viewer -> List<Map.Entry<String,Object>>
      // - by selecting a property in the message viewer -> List<Map.Entry<ColumnSystemHeader,Object>>

      // Works only if only one message is selected
      if (Utils.nullOrMoreThanOne(selection)) {
         return false;
      }

      Object o = selection.get(0);

      if (!(o instanceof JTBMessage) && !(o instanceof Map.Entry<?, ?>)) {
         // log.warn("'selection' is of class '{}' and so not of class 'JTBMessage' to show the 'Filter...' menu. Skip it",
         // selection.get(0).getClass().getName());
         return false;
      }

      if (o instanceof JTBMessage) {
         // Either csh or userProperty must be present
         if ((csh == null) && (userProperty == null)) {
            return false;
         }

         // Some system header can not be used as selectors
         if ((csh != null) && (!csh.isSelector())) {
            return false;
         }

         // UserProperties from Map entries can not be used as selectors
         if ((userProperty != null) && (userProperty.getOrigin() == UserPropertyOrigin.MAP_KEY)) {
            return false;
         }
      }

      if (o instanceof Map.Entry<?, ?>) {
         Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
         if (e.getKey() instanceof ColumnSystemHeader) {
            ColumnSystemHeader key = (ColumnSystemHeader) e.getKey();
            if (!key.isSelector()) {
               return false;
            }
         }
      }

      return true;
   }
}

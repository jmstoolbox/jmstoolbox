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
package org.titou10.jtb.handler;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.cs.ColumnSystemHeader;
import org.titou10.jtb.dialog.PropertyBuildSelectorDialog;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Build Selector" command
 * 
 * @author Denis Forveille
 * 
 */
public class PropertyBuildSelectorHandler {

   private static final Logger log = LoggerFactory.getLogger(PropertyBuildSelectorHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Execute
   public void execute(Shell shell,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<Map.Entry<String, Object>> selection) {
      log.debug("execute. Selection : {}", selection);

      String propertyName = selection.get(0).getKey();
      Long value = (Long) Utils.extractLongFromTimestamp(selection.get(0).getValue());
      long propertyValue = value == null ? 0 : value.longValue();

      // Display Build Dialog
      PropertyBuildSelectorDialog dialog = new PropertyBuildSelectorDialog(shell, propertyName, propertyValue);
      if (dialog.open() != Window.OK) {
         return;
      }
      String selector = dialog.getSelector();
      log.debug("selector={}", selector);

      // Refresh List of Message
      eventBroker.send(Constants.EVENT_ADD_SELECTOR_CLAUSE, selector);
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<Map.Entry<String, Object>> selection,
                             @Optional MMenuItem menuItem) {

      if (Utils.isEmpty(selection)) {
         return Utils.disableMenu(menuItem);
      }

      if (selection.size() > 1) {
         return Utils.disableMenu(menuItem);
      }

      // Enable menu only for Timestamp that are allowed to be selectors
      String propertyName = selection.get(0).getKey();
      if ((ColumnSystemHeader.isTimestamp(propertyName)) && (ColumnSystemHeader.isSelector(propertyName))) {
         return Utils.enableMenu(menuItem);
      } else {
         return Utils.disableMenu(menuItem);
      }
   }
}

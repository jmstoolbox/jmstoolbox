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
package org.titou10.jtb.cs.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.xml.bind.JAXBException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.cs.ColumnSystemHeader;
import org.titou10.jtb.cs.ColumnsSetsManager;
import org.titou10.jtb.cs.gen.Column;
import org.titou10.jtb.cs.gen.ColumnKind;
import org.titou10.jtb.cs.gen.ColumnsSet;
import org.titou10.jtb.cs.gen.UserPropertyOrigin;
import org.titou10.jtb.cs.gen.UserPropertyType;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Add to current Columns Set" command
 * 
 * @author Denis Forveille
 * 
 */
public class ColumnsSetsAddPropertyHandler {

   private static final Logger log = LoggerFactory.getLogger(ColumnsSetsAddPropertyHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private ColumnsSetsManager  csManager;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<Map.Entry<?, Object>> selection,
                       @Named(Constants.CURRENT_COLUMNSSET) ColumnsSet columnsSet) {
      log.debug("execute. Selection : {}", selection);

      if (selection == null) {
         return;
      }

      List<String> newProperties = new ArrayList<>();
      for (Entry<?, Object> e : selection) {
         String propertyName;
         if (e.getKey() instanceof String s) {
            propertyName = s;
         } else {
            propertyName = ((ColumnSystemHeader) e.getKey()).getHeaderName();
         }
         for (Column c : columnsSet.getColumn()) {
            if (c.getColumnKind() == ColumnKind.SYSTEM_HEADER) {
               if (c.getSystemHeaderName().equals(propertyName)) {
                  break;
               }
            } else {
               if (c.getUserProperty().getUserPropertyName().equals(propertyName)) {
                  break;
               }
            }
            newProperties.add(propertyName);
            break;
         }
      }

      for (String s : newProperties) {

         // DF: Is there a better way?
         ColumnSystemHeader csh = ColumnSystemHeader.fromHeaderName(s);
         if (csh == null) {
            Column col = csManager.buildUserPropertyColumn(UserPropertyOrigin.USER_PROPERTY, s, null, 100, UserPropertyType.STRING);
            columnsSet.getColumn().add(col);
         } else {
            columnsSet.getColumn().add(csManager.buildSystemColumn(csh));
         }
      }

      // Save CS config
      try {
         csManager.saveConfig();
      } catch (JAXBException | CoreException e1) {
         jtbStatusReporter.showError("A problem occurred while saving the columns set configuration file", e1, "");
         return;
      }

      // Tell the Message Browser to rebuild the view
      eventBroker.send(Constants.EVENT_REBUILD_VIEW_NEW_CS, "");
   }

   @CanExecute
   public boolean canExecute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<Map.Entry<?, Object>> selection,
                             @Optional @Named(Constants.CURRENT_COLUMNSSET) ColumnsSet columnsSet,
                             @Optional MMenuItem menuItem) {

      if (Utils.isEmpty(selection)) {
         return Utils.disableMenu(menuItem);
      }

      // Show menu only
      // - if the current ColumnsSet is not the System one
      // - if the property is not already in the Columns Set
      if (columnsSet.equals(csManager.getSystemColumnsSet())) {
         return Utils.disableMenu(menuItem);
      }

      for (Entry<?, Object> e : selection) {
         String propertyName;
         if (e.getKey() instanceof String s) {
            propertyName = s;
         } else {
            propertyName = ((ColumnSystemHeader) e.getKey()).getHeaderName();
         }
         for (Column c : columnsSet.getColumn()) {
            if (c.getColumnKind() == ColumnKind.SYSTEM_HEADER) {
               if (c.getSystemHeaderName().equals(propertyName)) {
                  return Utils.disableMenu(menuItem);
               }
            } else {
               if (c.getUserProperty().getUserPropertyName().equals(propertyName)) {
                  return Utils.disableMenu(menuItem);
               }
            }
         }
      }

      return Utils.enableMenu(menuItem);
   }
}

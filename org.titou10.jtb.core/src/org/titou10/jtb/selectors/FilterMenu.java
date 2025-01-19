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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.cs.ColumnSystemHeader;
import org.titou10.jtb.cs.ColumnsSetsManager;
import org.titou10.jtb.cs.gen.UserProperty;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.JMSSelectorOperator;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Dynamically show the "Filters..." menu
 * 
 * @author Denis Forveille
 *
 */
public class FilterMenu {

   private static final Logger log = LoggerFactory.getLogger(FilterMenu.class);

   @Inject
   private EModelService       modelService;

   @Inject
   private ColumnsSetsManager  csManager;

   @AboutToShow
   public void aboutToShow(List<MMenuElement> items,
                           @Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<?> selection,
                           @Named(Constants.CURRENT_TAB_JTBDESTINATION) JTBDestination jtbDestination,
                           @Optional @Named(Constants.COLUMN_TYPE_COLUMN_SYSTEM_HEADER) ColumnSystemHeader csh,
                           @Optional @Named(Constants.COLUMN_TYPE_USER_PROPERTY) UserProperty userProperty) {
      log.debug("aboutToShow {}", csh);

      // The menu can be activates either
      // - from a message from the message browser -> List<JTBMessage>
      // - by selecting a header or property in the message viewer -> List<Map.Entry<String,Object>>
      // - by selecting a property in the message viewer -> List<Map.Entry<ColumnSystemHeader,Object>>

      // Works only if only one message is selected
      if (Utils.nullOrMoreThanOne(selection)) {
         return;
      }

      Object o = selection.get(0);

      MDirectMenuItem buildSelectorMenuItem = null;
      String propertyName;
      Object value;
      JMSSelectorOperator[] operators;
      if (o instanceof JTBMessage jtbMessage) {

         // Enable menu only if the selected message is from the active tab
         if (!jtbMessage.getJtbDestination().getName().equals(jtbDestination.getName())) {
            return;
         }

         if (csh != null) {
            propertyName = csh.getHeaderName();
            value = csh.getColumnSystemValue(jtbMessage.getJmsMessage(), true, true);
            operators = csh.getJmsPropertyKind().getOperators();
            if (csh.isTimestamp()) {
               buildSelectorMenuItem = createBuildSelectorItem(csh, (Long) value);
            }
         } else {
            propertyName = userProperty.getUserPropertyName();
            value = csManager.getColumnUserPropertyValue(jtbMessage.getJmsMessage(), userProperty);
            operators = JMSPropertyKind.operatorsFromObjectClassname(value);
         }
      } else {
         Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
         if (e.getKey() instanceof ColumnSystemHeader) {
            @SuppressWarnings("unchecked")
            Map.Entry<ColumnSystemHeader, Object> v = (Map.Entry<ColumnSystemHeader, Object>) e;
            ColumnSystemHeader c = v.getKey();
            propertyName = c.getHeaderName();
            value = v.getValue();
            operators = c.getJmsPropertyKind().getOperators();
            if (c.isTimestamp()) {
               buildSelectorMenuItem = createBuildSelectorItem(c, Utils.extractLongFromTimestamp(value));
            }
         } else {
            @SuppressWarnings("unchecked")
            Map.Entry<String, Object> v = (Map.Entry<String, Object>) e;
            propertyName = v.getKey();
            value = v.getValue();
            operators = JMSPropertyKind.operatorsFromObjectClassname(value);
         }
      }

      for (JMSSelectorOperator operator : operators) {
         String selector = SelectorsUtils.formatSelector(propertyName, value, operator, false);
         String label = SelectorsUtils.formatSelector(propertyName, value, operator, true);

         MDirectMenuItem dynamicItem = modelService.createModelElement(MDirectMenuItem.class);
         dynamicItem.setLabel(label);
         dynamicItem.setIconURI(Constants.FILTER_MENU_ICON);
         dynamicItem.setContributorURI(Constants.BASE_CORE_PLUGIN);
         dynamicItem.setContributionURI(Constants.FILTER_MENU_URI);
         dynamicItem.getTransientData().put(Constants.FILTER_PARAM_SELECTOR, selector);

         items.add(dynamicItem);

      }

      if (buildSelectorMenuItem != null) {
         items.add(modelService.createModelElement(MMenuSeparator.class));
         items.add(buildSelectorMenuItem);
      }
   }

   private MDirectMenuItem createBuildSelectorItem(ColumnSystemHeader csh, Long value) {

      MDirectMenuItem buildSelectorItem = modelService.createModelElement(MDirectMenuItem.class);
      buildSelectorItem.setLabel("Build Selector...");
      buildSelectorItem.setIconURI(Constants.FILTER_MENU_ICON);
      buildSelectorItem.setContributorURI(Constants.BASE_CORE_PLUGIN);// "platform:/plugin/org.titou10.jtb.core");
      buildSelectorItem.setContributionURI(Constants.FILTER_BUILD_SELECTOR_MENU_URI);
      buildSelectorItem.getTransientData().put(Constants.FILTER_PARAM_BUILD_SELECTOR_CSH, csh);
      buildSelectorItem.getTransientData().put(Constants.FILTER_PARAM_BUILD_SELECTOR_VALUE, value);
      return buildSelectorItem;
   }
}

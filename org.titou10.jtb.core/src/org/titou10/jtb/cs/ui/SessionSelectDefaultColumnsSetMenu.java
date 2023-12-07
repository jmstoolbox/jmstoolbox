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
package org.titou10.jtb.cs.ui;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuSeparator;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.titou10.jtb.cs.ColumnsSetsManager;
import org.titou10.jtb.cs.ColumnsSetsManager.ColumnSetOrigin;
import org.titou10.jtb.cs.gen.ColumnsSet;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.ui.navigator.NodeAbstract;
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.ui.navigator.NodeJTBSession;
import org.titou10.jtb.ui.navigator.NodeJTBTopic;
import org.titou10.jtb.util.Constants;

/**
 * 
 * Display the menu on Sessions to select the default Columns Set
 * 
 * @author Denis Forveille
 *
 */
public class SessionSelectDefaultColumnsSetMenu {

   @Inject
   private EModelService      modelService;

   @Inject
   private ColumnsSetsManager csManager;

   @AboutToShow
   public void aboutToShow(List<MMenuElement> items,
                           @Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeAbstract nodeAbstract) {

      // Can be used on JTBSession, JTBQueues or JTBTopics

      JTBSession jtbSession = null;
      JTBDestination jtbDestination = null;
      ColumnSetOrigin cso = null;

      if (nodeAbstract instanceof NodeJTBSession) {
         jtbSession = (JTBSession) nodeAbstract.getBusinessObject();
         cso = csManager.getDefaultColumnSet(jtbSession);
      }
      if ((nodeAbstract instanceof NodeJTBQueue) || (nodeAbstract instanceof NodeJTBTopic)) {
         jtbDestination = (JTBDestination) nodeAbstract.getBusinessObject();
         cso = csManager.getDefaultColumnSet(jtbDestination);
      }

      List<ColumnsSet> columnsSets = csManager.getColumnsSets();
      boolean selected;
      for (ColumnsSet cs : columnsSets) {
         String csName = cs.getName();
         selected = cs.equals(cso.columnsSet);
         if (selected) {
            if (cso.inherited) {
               csName += " (inherited)";
            }
         }
         MDirectMenuItem dynamicItem = modelService.createModelElement(MDirectMenuItem.class);
         dynamicItem.setType(ItemType.RADIO);
         dynamicItem.setSelected(selected);
         dynamicItem.setLabel(csName);
         // dynamicItem.setIconURI(Constants.COLUMNSSET_MENU_ICON);
         dynamicItem.setContributorURI(Constants.BASE_CORE_PLUGIN);
         dynamicItem.setContributionURI(Constants.COLUMNSSET_MENU_URI);
         dynamicItem.getTransientData().put(Constants.COLUMNSSET_PARAM, cs);
         dynamicItem.getTransientData().put(Constants.COLUMNSSET_PARAM_JTBSESSION, jtbSession);
         dynamicItem.getTransientData().put(Constants.COLUMNSSET_PARAM_JTBDESTINATION, jtbDestination);

         items.add(dynamicItem);
      }

      // If the value is explicitely set, add an extra entry to unset it
      if (!cso.inherited) {
         MMenuSeparator separator = modelService.createModelElement(MMenuSeparator.class);
         items.add(separator);

         MDirectMenuItem unset = modelService.createModelElement(MDirectMenuItem.class);
         unset.setType(ItemType.RADIO);
         unset.setLabel("[Unset assigned Columns Set]");
         unset.setContributorURI(Constants.BASE_CORE_PLUGIN);
         unset.setContributionURI(Constants.COLUMNSSET_MENU_URI);
         unset.getTransientData().put(Constants.COLUMNSSET_PARAM, null);
         unset.getTransientData().put(Constants.COLUMNSSET_PARAM_JTBSESSION, jtbSession);
         unset.getTransientData().put(Constants.COLUMNSSET_PARAM_JTBDESTINATION, jtbDestination);

         items.add(unset);
      }

   }
}

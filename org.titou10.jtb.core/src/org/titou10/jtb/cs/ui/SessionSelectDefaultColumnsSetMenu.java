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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.ItemType;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.titou10.jtb.cs.ColumnsSetsManager;
import org.titou10.jtb.cs.gen.ColumnsSet;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.ui.navigator.NodeJTBSession;
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
                           @Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeJTBSession nodeJTBSession) {

      JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();

      ColumnsSet currentColumnsSet = csManager.getDefaultColumnSet(jtbSession);

      List<ColumnsSet> columnsSets = csManager.getColumnsSets();
      for (ColumnsSet cs : columnsSets) {
         String csName = cs.getName();

         MDirectMenuItem dynamicItem = modelService.createModelElement(MDirectMenuItem.class);
         dynamicItem.setType(ItemType.RADIO);
         dynamicItem.setSelected(cs.equals(currentColumnsSet));
         dynamicItem.setLabel(csName);
         dynamicItem.setIconURI(Constants.COLUMNSSET_MENU_ICON);
         dynamicItem.setContributorURI(Constants.BASE_CORE_PLUGIN);
         dynamicItem.setContributionURI(Constants.COLUMNSSET_MENU_URI);
         dynamicItem.getTransientData().put(Constants.COLUMNSSET_PARAM, cs);
         dynamicItem.getTransientData().put(Constants.COLUMNSSET_PARAM_JTBSESSION, jtbSession);

         items.add(dynamicItem);
      }
   }
}

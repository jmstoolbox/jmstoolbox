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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.AboutToShow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.visualizer.VisualizersManager;

/**
 * Dynamically show the "Show Payload as..." menu
 * 
 * @author Denis Forveille
 *
 */
public class VisualizerShowPayloadAsMenu {

   private static final Logger log = LoggerFactory.getLogger(VisualizerShowPayloadAsMenu.class);

   @Inject
   private EModelService       modelService;

   @Inject
   private VisualizersManager  visualizersManager;

   @AboutToShow
   public void aboutToShow(List<MMenuElement> items,
                           @Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<JTBMessage> selection,
                           @Named(Constants.CURRENT_TAB_JTBDESTINATION) JTBDestination jtbDestination) {
      log.debug("aboutToShow");

      // Works only if One message is selected
      if ((selection == null) || (selection.size() != 1)) {
         return;
      }

      JTBMessage jtbMessage = selection.get(0);

      // Enable menu only if the selected message is from the active tab
      if (!jtbMessage.getJtbDestination().getName().equals(jtbDestination.getName())) {
         return;
      }

      // Are there any visualizers linked to the kind of JMS Message?
      String[] visualizers = visualizersManager.getVizualisersNamesForMessageType(jtbMessage.getJtbMessageType());
      if (visualizers == null) {
         return;
      }

      // // Build a Menu and its sub menus
      // MMenu menu = modelService.createModelElement(MMenu.class);
      // menu.setLabel("Show Payload as...");
      // items.add(menu);

      for (String visualizerName : visualizers) {

         MDirectMenuItem dynamicItem = modelService.createModelElement(MDirectMenuItem.class);
         dynamicItem.setLabel(visualizerName);
         dynamicItem.setContributorURI("platform:/plugin/org.titou10.jtb.core");
         dynamicItem
                  .setContributionURI("bundleclass://org.titou10.jtb.core/org.titou10.jtb.visualizer.ui.VisualizerShowPayloadAsHandler");
         dynamicItem.getTransientData().put(Constants.VISUALIZER_PARAM_NAME, visualizerName);
         dynamicItem.getTransientData().put(Constants.VISUALIZER_PARAM_JTBMESSAGE, jtbMessage);

         // menu.getChildren().add(dynamicItem);
         items.add(dynamicItem);
      }
   }
}

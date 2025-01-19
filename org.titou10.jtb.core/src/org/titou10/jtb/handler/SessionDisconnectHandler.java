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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.navigator.NodeJTBSession;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Disconnection Session" command
 * 
 * @author Denis Forveille
 * 
 */
public class SessionDisconnectHandler {

   private static final Logger log = LoggerFactory.getLogger(SessionDisconnectHandler.class);

   @Inject
   private EPartService        partService;

   @Inject
   private EModelService       modelService;

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(MApplication app, @Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeJTBSession nodeJTBSession) {
      log.debug("execute. Selection : {}", nodeJTBSession);

      JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();
      try {
         jtbSession.getJTBConnection(JTBSessionClientType.GUI).disconnect();

         // Close the corresponding tab with messages in the right TabFolder
         String partName = Constants.PART_SESSION_CONTENT_PREFIX + jtbSession.getName();
         MPart part = (MPart) modelService.find(partName, app);
         partService.hidePart(part, true);

         // Refresh Session Browser
         eventBroker.send(Constants.EVENT_REFRESH_SESSION_BROWSER, nodeJTBSession);
      } catch (Exception e) {
         jtbStatusReporter.showError("Disconnect unsuccessful", e, jtbSession.getName());
         return;
      }
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional Object selection, @Optional MMenuItem menuItem) {

      // Show menu on Sessions only
      if (selection instanceof NodeJTBSession nodeJTBSession) {

         JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();

         // Show menu on connected Sessions only
         if (jtbSession.getJTBConnection(JTBSessionClientType.GUI).isConnected()) {
            return Utils.enableMenu(menuItem);
         } else {
            return Utils.disableMenu(menuItem);
         }
      }

      return Utils.disableMenu(menuItem);
   }
}

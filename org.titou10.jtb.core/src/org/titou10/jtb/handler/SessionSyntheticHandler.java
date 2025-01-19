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
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.ui.navigator.NodeAbstract;
import org.titou10.jtb.ui.navigator.NodeFolder;
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.ui.navigator.NodeJTBSession;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Session Synthetic View" command
 * 
 * @author Denis Forveille
 * 
 */
public class SessionSyntheticHandler {

   private static final Logger log = LoggerFactory.getLogger(SessionSyntheticHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private EModelService       modelService;

   @Inject
   private EPartService        partService;

   @Execute
   @SuppressWarnings("unchecked")
   public void execute(MApplication app, @Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeAbstract selection) {
      log.debug("execute. Selection : {}", selection);

      // Either right click on session, or double clic on Queue FOlder
      NodeJTBSession nodeJTBSession;
      if (selection instanceof NodeJTBSession s) {
         nodeJTBSession = s;
      } else {
         NodeFolder<NodeJTBQueue> nf = (NodeFolder<NodeJTBQueue>) selection;
         nodeJTBSession = (NodeJTBSession) nf.getParentNode();
      }

      JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();

      // Reuse or create a tab-part per Q Manager
      String partName = Constants.PART_SESSION_CONTENT_PREFIX + jtbSession.getName();
      MPart part = (MPart) modelService.find(partName, app);
      if (part == null) {

         // Create part from Part Descriptor
         part = partService.createPart(Constants.PARTDESCRITOR_SESSION_CONTENT);
         part.setLabel(jtbSession.getName());
         part.setElementId(partName);

         part.getTransientData().put(Constants.SESSION_TYPE_SESSION_DEF, jtbSession.getSessionDef());

         MPartStack stack = (MPartStack) modelService.find(Constants.PARTSTACK_QCONTENT, app);
         stack.getChildren().add(part);
      }

      // Show Part and refresh content
      partService.showPart(part, PartState.CREATE);
      eventBroker.send(Constants.EVENT_REFRESH_SESSION_SYNTHETIC_VIEW, jtbSession);
      eventBroker.send(Constants.EVENT_FOCUS_SYNTHETIC, jtbSession);
      partService.activate(part, true);
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeAbstract selection,
                             @Optional MMenuItem menuItem) {

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

      // ALlow command for Queue Folder also
      if (Utils.isQueueFolder(selection)) {
         return Utils.enableMenu(menuItem);
      }

      return Utils.disableMenu(menuItem);
   }
}

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

import javax.inject.Inject;
import javax.inject.Named;

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
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBTopic;
import org.titou10.jtb.ui.navigator.NodeJTBTopic;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Subscribe to Topic" command
 * 
 * @author Denis Forveille
 * 
 */
public class TopicSubscribeHandler {

   private static final Logger log = LoggerFactory.getLogger(TopicSubscribeHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private EModelService       modelService;

   @Inject
   private EPartService        partService;

   @Execute
   public void execute(MApplication app,
                       @Named(Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM) String context,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeJTBTopic nodeJTBTopic,
                       @Named(Constants.CURRENT_TAB_JTBDESTINATION) @Optional JTBDestination jtbDestination) {
      log.debug("execute. Selection : {}", nodeJTBTopic);

      JTBTopic jtbTopic = null;

      switch (context) {
         case Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM_TOPIC:
            jtbTopic = (JTBTopic) nodeJTBTopic.getBusinessObject();
            break;

         case Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM_MSG:
            if (jtbDestination == null) {
               return; // DF: ?? This happens sometimes
            }
            jtbTopic = jtbDestination.getAsJTBTopic();
            break;

         default:
            log.error("Invalid value : {}", context);
            return;
      }
      JTBConnection jtbConnection = jtbTopic.getJtbConnection();

      // Reuse or create a tab-part per Q Manager
      String partName = Constants.PART_SESSION_CONTENT_PREFIX + jtbConnection.getSessionName();
      MPart part = (MPart) modelService.find(partName, app);
      if (part == null) {

         // Create part from Part Descriptor
         part = partService.createPart(Constants.PARTDESCRITOR_SESSION_CONTENT);
         part.setLabel(jtbConnection.getSessionName());
         part.setElementId(partName);

         part.getTransientData().put(Constants.SESSION_TYPE_SESSION_DEF, jtbConnection.getSessionDef());

         MPartStack stack = (MPartStack) modelService.find(Constants.PARTSTACK_QCONTENT, app);
         stack.getChildren().add(part);
      }

      // Show Part and refresh content
      partService.showPart(part, PartState.CREATE);
      eventBroker.send(Constants.EVENT_REFRESH_TOPIC_SHOW_MESSAGES, jtbTopic);
      eventBroker.send(Constants.EVENT_FOCUS_CTABITEM, jtbTopic);
      partService.activate(part, true);
   }

   @CanExecute
   public boolean canExecute(@Named(Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM) String context,
                             @Named(IServiceConstants.ACTIVE_SELECTION) @Optional Object selection,
                             @Optional MMenuItem menuItem) {

      switch (context) {
         case Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM_TOPIC:
            // Show menu on Topics only
            if (selection instanceof NodeJTBTopic) {
               return Utils.enableMenu(menuItem);
            } else {
               return Utils.disableMenu(menuItem);
            }

         case Constants.COMMAND_TOPIC_SUBSCRIBE_PARAM_MSG:
            return Utils.enableMenu(menuItem);

         default:
            log.error("Invalid value : {}", context);
            return Utils.disableMenu(menuItem);
      }
   }
}

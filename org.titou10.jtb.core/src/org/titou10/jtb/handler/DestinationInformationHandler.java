/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
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

import java.util.Map;

import javax.inject.Named;
import javax.jms.Connection;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.dialog.DestinationInformationDialog;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.ui.navigator.NodeAbstract;
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.ui.navigator.NodeJTBTopic;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Destination Information" command
 * 
 * @author Denis Forveille
 * 
 */
public class DestinationInformationHandler {

   private static final Logger log = LoggerFactory.getLogger(DestinationInformationHandler.class);

   @Execute
   public void execute(Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeAbstract nodeAbstract) {
      log.debug("execute. Selection : {}", nodeAbstract);

      JTBDestination jtbDestination = (JTBDestination) nodeAbstract.getBusinessObject();
      QManager qm = jtbDestination.getJtbConnection().getQm();
      Connection jmsConnection = jtbDestination.getJtbConnection().getJmsConnection();
      String destinationName = jtbDestination.getName();

      Map<String, Object> destinationInformation;
      if (jtbDestination.isJTBQueue()) {
         destinationInformation = qm.getQueueInformation(jmsConnection, destinationName);
      } else {
         destinationInformation = qm.getTopicInformation(jmsConnection, destinationName);
      }

      DestinationInformationDialog dialog = new DestinationInformationDialog(shell, destinationName, destinationInformation);
      dialog.open();
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional Object selection, @Optional MMenuItem menuItem) {

      // Show menu on Queues and Topics only
      if ((selection instanceof NodeJTBQueue) || (selection instanceof NodeJTBTopic)) {
         return Utils.enableMenu(menuItem);
      }

      return Utils.disableMenu(menuItem);
   }
}

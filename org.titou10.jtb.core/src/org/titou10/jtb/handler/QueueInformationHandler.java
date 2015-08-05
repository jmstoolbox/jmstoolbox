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

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.dialog.QueueInformationDialog;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Queue Information" command
 * 
 * @author Denis Forveille
 * 
 */
public class QueueInformationHandler {

   private static final Logger log = LoggerFactory.getLogger(QueueInformationHandler.class);

   @Execute
   public void execute(Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeJTBQueue nodeJTBQueue) {
      log.debug("execute. Selection : {}", nodeJTBQueue);

      JTBQueue jtbQueue = (JTBQueue) nodeJTBQueue.getBusinessObject();
      Map<String, Object> queueInformation = jtbQueue.getJtbSession().getQm().getQueueInformation(jtbQueue.getName());

      QueueInformationDialog dialog = new QueueInformationDialog(shell, jtbQueue.getName(), queueInformation);
      dialog.open();
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional Object selection, @Optional MMenuItem menuItem) {
      log.debug("canExecute={}", selection);

      // Show menu on Queues only
      if (selection instanceof NodeJTBQueue) {
         return Utils.enableMenu(menuItem);
      }
      if (selection instanceof List) {
         return Utils.enableMenu(menuItem);
      }

      return Utils.disableMenu(menuItem);
   }
}

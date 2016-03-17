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
package org.titou10.jtb.rest.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.rest.RuntimeRESTConnector;
import org.titou10.jtb.rest.util.Constants;
import org.titou10.jtb.rest.util.JTBStatusReporter;

/**
 * Manage the REST Listener lifecycle
 * 
 * @author Denis Forveille
 * 
 */
public class RESTStartStopHandler {

   private static final Logger  log = LoggerFactory.getLogger(RESTStartStopHandler.class);

   @Inject
   private RuntimeRESTConnector rrc;

   @Inject
   private JTBStatusReporter    sr;

   @Execute
   public void execute(Shell shell, @Named(Constants.COMMAND_REST_STARTSTOP_PARAM) String mode) {
      log.debug("execute. Mode : {}", mode);

      switch (mode) {
         case Constants.COMMAND_REST_STARTSTOP_START:
            try {
               rrc.start();
               int port = rrc.getPort();

               MessageDialog.openInformation(shell, "Success", "REST Connector started with success, listening on port " + port);

            } catch (Exception e) {
               sr.showError("An error occurred while starting the REST connector", e);
               return;
            }

            break;

         case Constants.COMMAND_REST_STARTSTOP_STOP:
            try {
               rrc.stop();

               MessageDialog.openInformation(shell, "Success", "REST Connector stopped with success");

            } catch (Exception e) {
               sr.showError("An error occurred while starting the REST connector", e);
               return;
            }

            break;

         case Constants.COMMAND_REST_STARTSTOP_STATUS:
            String status = rrc.getStatus();
            MessageDialog.openInformation(shell, "REST Connector Status", status);

            break;

      }
   }

   @CanExecute
   public boolean canExecute(@Named(Constants.COMMAND_REST_STARTSTOP_PARAM) String mode, @Optional MMenuItem menuItem) {

      switch (mode) {
         case Constants.COMMAND_REST_STARTSTOP_START:
            // Show start menu if REST Listener is not running
            if (rrc.isRunning()) {
               return false;
            } else {
               return true;
            }

         case Constants.COMMAND_REST_STARTSTOP_STOP:
            // Show stop menu if REST Listener is running
            if (rrc.isRunning()) {
               return true;
            } else {
               return false;
            }

         case Constants.COMMAND_REST_STARTSTOP_STATUS:
            return true;
      }

      return false;
   }
}

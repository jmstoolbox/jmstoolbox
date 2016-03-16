/*
 * Copyright (C) 2016 Denis Forveille titou10.titou10@gmail.com
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.rest.RESTConnector;
import org.titou10.jtb.rest.util.Constants;
import org.titou10.jtb.rest.util.JTBStatusReporter;

/**
 * Manage the REST Listener lifecycle
 * 
 * @author Denis Forveille
 * 
 */
public class RESTStartStopHandler {

   private static final Logger log = LoggerFactory.getLogger(RESTStartStopHandler.class);

   @Inject
   private RESTConnector       rc;

   @Inject
   private JTBStatusReporter   sr;

   @Execute
   public void execute(@Named(Constants.COMMAND_REST_STARTSTOP_PARAM) String mode) {
      log.debug("execute. Mode : {}", mode);

      switch (mode) {
         case Constants.COMMAND_REST_STARTSTOP_START:
            try {
               rc.start();
            } catch (Exception e) {
               sr.showError("Error", e, "Error");
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
            break;

         case Constants.COMMAND_REST_STARTSTOP_STOP:
            try {
               rc.stop();
            } catch (Exception e) {
               sr.showError("Error", e, "Error");
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
      }

   }

   @CanExecute
   public boolean canExecute(@Named(Constants.COMMAND_REST_STARTSTOP_PARAM) String mode, @Optional MMenuItem menuItem) {

      // Show start menu if REST Listener is not running
      // Show stop menu if REST Listener is running
      switch (mode) {
         case Constants.COMMAND_REST_STARTSTOP_START:
            if (rc.isRunning()) {
               return false;
            } else {
               return true;
            }

         case Constants.COMMAND_REST_STARTSTOP_STOP:
            if (rc.isRunning()) {
               return true;
            } else {
               return false;
            }
      }

      return false;

   }
}

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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.navigator.NodeJTBSession;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Duplicate Session" command
 * 
 * @author Denis Forveille
 * 
 */
public class SessionDuplicateHandler {

   private static final Logger log = LoggerFactory.getLogger(SessionDuplicateHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Inject
   private ConfigManager       cm;

   @Execute
   public void execute(Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeJTBSession nodeJTBSession) {
      log.debug("execute. Selection : {}", nodeJTBSession);

      if (nodeJTBSession == null) {
         return;
      }

      JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();

      String newName = askForNewName(shell, cm, jtbSession.getName());
      if (newName == null) {
         return;
      }

      try {
         cm.sessionDuplicate(jtbSession, newName);

         // Refresh Session Browser asynchronously
         eventBroker.post(Constants.EVENT_REFRESH_SESSION_BROWSER, true);

         // Confirmation message
         MessageDialog.openInformation(shell, "Success", "The session has been successfully duplicated.");

      } catch (Exception e) {
         jtbStatusReporter.showError("Problem when duplicating Session", e, "An error occurred when duplicating the session");
         return;
      }
   }

   @CanExecute
   public boolean canExecute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object selection, @Optional MMenuItem menuItem) {

      // Show menu on Sessions only
      if (selection instanceof NodeJTBSession) {
         return Utils.enableMenu(menuItem);
      }

      return Utils.disableMenu(menuItem);
   }

   // --------
   // Helpers
   // --------

   private String askForNewName(Shell shell, ConfigManager cm, String sourceName) {

      String newName = sourceName + "_new";

      // Ask for new name
      InputDialog inputDialog = new InputDialog(shell, "Please enter the name of the new session", "Session Name:", newName, null);
      if (inputDialog.open() != Window.OK) {
         return null;
      }
      newName = inputDialog.getValue();

      // Do nothing if new name is empty
      if ((newName == null) || (newName.trim().isEmpty())) {
         return null;
      }
      newName = newName.trim();

      // Check for duplicate
      if (cm.getSessionDefByName(newName) != null) {
         MessageDialog.openInformation(shell, "Session already exist", "A session with this name already exist.");
         return null;
      }

      return newName;
   }
}

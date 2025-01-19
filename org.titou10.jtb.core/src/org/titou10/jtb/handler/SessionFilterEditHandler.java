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
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.navigator.NodeJTBSession;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Session Edit Filter" command
 * 
 * @author Denis Forveille
 * 
 */
public class SessionFilterEditHandler {

   private static final Logger log = LoggerFactory.getLogger(SessionFilterEditHandler.class);

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
      JTBConnection jtbConnection = jtbSession.getJTBConnection(JTBSessionClientType.GUI);

      String pattern = jtbConnection.getFilterPattern();
      if (pattern == null) {
         pattern = "";
      }

      // Ask for pattern
      InputDialog inputDialog = new InputDialog(shell,
                                                "Please enter a filter for destination name",
                                                "Use '*' or '?' as wildcards, separate patterns with ';'. ex 'ABC*;X?Z*'",
                                                pattern,
                                                null);
      if (inputDialog.open() != Window.OK) {
         return;
      }

      // Update sessionDef and live session
      pattern = inputDialog.getValue();
      pattern = pattern.trim().isEmpty() ? null : pattern.trim();
      boolean apply = pattern == null ? false : true;
      jtbConnection.updateFilterData(pattern, apply);

      // Save state in config
      try {
         cm.sessionFilterApply(jtbSession, pattern, apply);

      } catch (Exception e) {
         jtbStatusReporter.showError("Problem while saving filter", e, jtbSession.getName());
         return;
      }

      // Refresh Session Browser asynchronously
      eventBroker.post(Constants.EVENT_REFRESH_SESSION_BROWSER, true);

   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional Object selection, @Optional MMenuItem menuItem) {

      // Show menu on Sessions only
      if (selection instanceof NodeJTBSession nodeJTBSession) {
         // Show menu on "Connectable" session
         JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();
         if (jtbSession.isConnectable()) {
            return Utils.enableMenu(menuItem);
         } else {
            return Utils.disableMenu(menuItem);
         }
      }

      return Utils.disableMenu(menuItem);
   }
}

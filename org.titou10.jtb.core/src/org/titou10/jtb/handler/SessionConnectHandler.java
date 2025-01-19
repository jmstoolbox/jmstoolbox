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

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.xml.bind.JAXBException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.dialog.SessionConnectDialog;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.jms.model.JTBSessionClientType;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.navigator.NodeJTBSession;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Session Connect" command
 * 
 * @author Denis Forveille
 * 
 */

public class SessionConnectHandler {

   private static final Logger log = LoggerFactory.getLogger(SessionConnectHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Inject
   private ConfigManager       cm;

   @Execute
   public void execute(Shell shell, final @Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeJTBSession nodeJTBSession) {
      log.debug("execute. Selection : {}", nodeJTBSession);

      final JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();

      SessionDef sessionDef = jtbSession.getSessionDef();

      // Prompt user for credentials if set in session
      if (Utils.isTrue(sessionDef.isPromptForCredentials())) {
         SessionConnectDialog dialog = new SessionConnectDialog(shell,
                                                                jtbSession.getName(),
                                                                sessionDef.getUserid(),
                                                                sessionDef.getPassword());
         if (dialog.open() != Window.OK) {
            return;
         }

         // Save userid/password that will be used for the connection
         sessionDef.setActiveUserid(dialog.getUserID());
         sessionDef.setActivePassword(dialog.getPassword());

         // Handle remember actions
         try {
            if (dialog.isRememberUserid()) {
               sessionDef.setUserid(dialog.getUserID());
               cm.writeConfig();
            }
            if (dialog.isRememberPassword()) {
               sessionDef.setPassword(dialog.getPassword());
               cm.writeConfig();
            }
         } catch (JAXBException | CoreException | IOException e) {
            jtbStatusReporter.showError("Exception when writing configuration file", Utils.getCause(e), jtbSession.getName());
            return;
         }
      }

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

         @Override
         public void run() {
            try {
               jtbSession.getJTBConnection(JTBSessionClientType.GUI).connect();

               // Refresh Session Browser
               eventBroker.send(Constants.EVENT_REFRESH_SESSION_BROWSER, nodeJTBSession);

            } catch (Throwable e) {
               jtbStatusReporter.showError("Connect unsuccessful", Utils.getCause(e), jtbSession.getName());
               return;
            }
         }
      });

   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional Object selection, @Optional MMenuItem menuItem) {

      // Show menu on Sessions only
      if (selection instanceof NodeJTBSession nodeJTBSession) {

         JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();

         // Show menu only in the QM has been instantiated
         if (jtbSession.isConnectable()) {
            // Show menu on Disconnected Sessions only
            if (jtbSession.getJTBConnection(JTBSessionClientType.GUI).isConnected()) {
               return Utils.disableMenu(menuItem);
            } else {
               return Utils.enableMenu(menuItem);
            }
         }
      }

      return Utils.disableMenu(menuItem);
   }
}

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

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

   @Execute
   public void execute(final @Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeJTBSession nodeJTBSession) {
      log.debug("execute. Selection : {}", nodeJTBSession);

      final JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();

      BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

         @Override
         public void run() {
            try {
               jtbSession.getJTBConnection(JTBSessionClientType.GUI).connectOrDisconnect();

               // Refresh Session Browser
               eventBroker.send(Constants.EVENT_REFRESH_SESSION_BROWSER, nodeJTBSession);

            } catch (Throwable e) {
               jtbStatusReporter.showError("Connect unsuccessful", Utils.getCause(e), jtbSession.getName());
               return;
            }
         }
      });

      // Job job = new Job("Connect Job") {
      // @Override
      // protected IStatus run(IProgressMonitor monitor) {
      // try {
      // monitor.beginTask("Connecting", 100);
      // monitor.subTask("ss");
      // monitor.worked(20);
      // try {
      // jtbSession.connectOrDisconnect();
      // } catch (final Exception e) {
      // e.printStackTrace();
      // jtbStatusReporter.showError("Connect unsuccessful", e, jtbSession.getName());
      // return Status.OK_STATUS;
      // }
      // monitor.worked(20);
      // viewer.expandAll();
      // monitor.worked(20);
      // viewer.refresh();
      // monitor.worked(20);
      // } finally {
      // monitor.done();
      // }
      // return Status.OK_STATUS;
      // }
      // };
      //
      // Job.getJobManager().setProgressProvider(jtbProgressProvider);
      // job.schedule();

   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional Object selection, @Optional MMenuItem menuItem) {

      // Show menu on Sessions only
      if (selection instanceof NodeJTBSession) {

         NodeJTBSession nodeJTBSession = (NodeJTBSession) selection;
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

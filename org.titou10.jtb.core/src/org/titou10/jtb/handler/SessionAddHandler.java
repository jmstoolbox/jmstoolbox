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

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.config.gen.Properties;
import org.titou10.jtb.config.gen.Properties.Property;
import org.titou10.jtb.config.gen.PropertyKind;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.dialog.SessionAddOrEditDialog;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.UIProperty;
import org.titou10.jtb.util.Constants;

/**
 * Manage the "Add Session" command
 * 
 * @author Denis Forveille
 * 
 */
public class SessionAddHandler {

   private static final Logger log = LoggerFactory.getLogger(SessionAddHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private ConfigManager       cm;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell, IEclipseContext context) {
      log.debug("execute.");

      // SessionAddOrEditDialog dialog = ContextInjectionFactory.make(SessionAddOrEditDialog.class, context);

      SessionAddOrEditDialog dialog = new SessionAddOrEditDialog(shell, cm);
      if (dialog.open() != Window.OK) {
         return;
      }

      SessionDef newSessionDef = new SessionDef();
      newSessionDef.setFolder(dialog.getFolder());
      newSessionDef.setHost(dialog.getHost());
      newSessionDef.setName(dialog.getName());
      newSessionDef.setPassword(dialog.getPassword());
      newSessionDef.setPort(dialog.getPort());
      newSessionDef.setUserid(dialog.getUserId());

      if (!(dialog.getProperties().isEmpty())) {
         Properties x = new Properties();
         List<Property> properties = x.getProperty();
         Property p;
         for (UIProperty v : dialog.getProperties()) {
            p = new Property();
            p.setName(v.getName());
            p.setValue(v.getValue(), v.isRequiresEncoding());
            p.setKind(PropertyKind.valueOf(v.getKind().name()));
            properties.add(p);
         }
         newSessionDef.setProperties(x);
      }

      try {
         cm.addSession(dialog.getQueueManagerSelected(), newSessionDef);

         // Refresh Session Browser asynchronously
         eventBroker.post(Constants.EVENT_REFRESH_SESSION_BROWSER, true);

         // Confirmation message
         MessageDialog.openInformation(shell, "Success", "The session has been successfully added.");

      } catch (Exception e) {
         jtbStatusReporter.showError("Session Add or Update unsuccessful", e, newSessionDef.getName());
         return;
      }
   }
}

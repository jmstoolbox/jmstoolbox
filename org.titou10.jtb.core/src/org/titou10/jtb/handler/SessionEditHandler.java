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

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
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
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.UIProperty;
import org.titou10.jtb.ui.navigator.NodeJTBSession;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Edit Session" command
 * 
 * @author Denis Forveille
 * 
 */
public class SessionEditHandler {

   private static final Logger log = LoggerFactory.getLogger(SessionEditHandler.class);

   @Execute
   public void execute(Shell shell,
                       IEventBroker eventBroker,
                       ConfigManager cm,
                       JTBStatusReporter jtbStatusReporter,
                       @Optional @Named(IServiceConstants.ACTIVE_SELECTION) NodeJTBSession nodeJTBSession) {
      log.debug("execute. Selection : {}", nodeJTBSession);

      if (nodeJTBSession == null) {
         return;
      }

      JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();

      SessionAddOrEditDialog dialog = new SessionAddOrEditDialog(shell, cm, jtbSession);
      if (dialog.open() != Window.OK) {
         return;
      }

      SessionDef sessionDef = jtbSession.getSessionDef();
      sessionDef.setFolder(dialog.getFolder());
      sessionDef.setHost(dialog.getHost());
      sessionDef.setName(dialog.getName());
      sessionDef.setPassword(dialog.getPassword());
      sessionDef.setPort(dialog.getPort());
      sessionDef.setUserid(dialog.getUserId());

      jtbSession.setName(dialog.getName());

      if (!(dialog.getProperties().isEmpty())) {
         Properties x = new Properties();
         List<Property> properties = x.getProperty();
         Property p;
         for (UIProperty v : dialog.getProperties()) {
            if ((v.getValue() == null) || (v.getValue().trim().isEmpty())) {
               continue;
            }
            p = new Property();
            p.setName(v.getName().trim());
            p.setValue(v.getValue().trim(), v.isRequiresEncoding());
            p.setKind(PropertyKind.valueOf(v.getKind().name()));
            properties.add(p);
         }
         sessionDef.setProperties(x);
      }

      try {
         cm.editSession();

         // Refresh Session Browser asynchronously
         eventBroker.post(Constants.EVENT_REFRESH_SESSION_TREE, true);

         MessageDialog.openInformation(shell, "Success", "The session has been successfully updated.");
      } catch (Exception e) {
         jtbStatusReporter.showError("Problem while saving session", e, jtbSession.getName());
         return;
      }
   }

   @CanExecute
   public boolean canExecute(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object selection, @Optional MMenuItem menuItem) {
      log.debug("canExecute={}", selection);

      // Show menu on Sessions only
      if (selection instanceof NodeJTBSession) {
         // Show menu on "Connectable" session
         NodeJTBSession nodeJTBSession = (NodeJTBSession) selection;
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

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

import jakarta.inject.Inject;
import jakarta.inject.Named;

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
import org.titou10.jtb.config.MetaQManager;
import org.titou10.jtb.config.gen.Properties;
import org.titou10.jtb.config.gen.Properties.Property;
import org.titou10.jtb.config.gen.PropertyKind;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.dialog.SessionAddOrEditDialog;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.sessiontype.SessionTypeManager;
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

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Inject
   private ConfigManager       cm;

   @Inject
   private SessionTypeManager  sessionTypeManager;

   @Execute
   public void execute(Shell shell, @Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeJTBSession nodeJTBSession) {
      log.debug("execute. Selection : {}", nodeJTBSession);

      if (nodeJTBSession == null) {
         return;
      }

      JTBSession jtbSession = (JTBSession) nodeJTBSession.getBusinessObject();

      SessionAddOrEditDialog dialog = new SessionAddOrEditDialog(shell, cm, sessionTypeManager, jtbSession);
      if (dialog.open() != Window.OK) {
         return;
      }

      SessionDef sessionDef = jtbSession.getSessionDef();
      sessionDef.setName(dialog.getName());
      sessionDef.setFolder(dialog.getFolder());
      if (dialog.getSessionTypeSelected() != null) {
         sessionDef.setSessionType(dialog.getSessionTypeSelected().getName());
      } else {
         sessionDef.setSessionType(null);
      }

      sessionDef.setHost(dialog.getHost());
      sessionDef.setPort(dialog.getPort());
      sessionDef.setHost2(dialog.getHost2());
      sessionDef.setPort2(dialog.getPort2());
      sessionDef.setHost3(dialog.getHost3());
      sessionDef.setPort3(dialog.getPort3());

      sessionDef.setUserid(dialog.getUserId());
      sessionDef.setActiveUserid(dialog.getUserId());
      sessionDef.setPassword(dialog.getPassword());
      sessionDef.setActivePassword(dialog.getPassword());
      sessionDef.setPromptForCredentials(dialog.isPromptForCredentials());

      // MetaQManager from dialog
      MetaQManager mqm = cm.getMetaQManagerFromQManager(dialog.getQueueManagerSelected());
      String newQManagerDefId = mqm.getqManagerDef().getId();
      String qManagerDefId = sessionDef.getQManagerDef();

      // Qmanager def changed, set the new QManagerDef and set the QManager in JTBSession..
      if (!(newQManagerDefId.equals(qManagerDefId))) {
         sessionDef.setQManagerDef(newQManagerDefId);
         jtbSession.updateMetaQManager(mqm);
      }

      if (!(dialog.getProperties().isEmpty())) {
         Properties x = new Properties();
         List<Property> properties = x.getProperty();
         Property p;
         for (UIProperty v : dialog.getProperties()) {
            if (Utils.isEmpty(v.getValue())) {
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
         cm.writeConfig();

         // Refresh Session Browser asynchronously
         eventBroker.post(Constants.EVENT_REFRESH_SESSION_BROWSER, true);

         // Refresh Content partsasynchronously
         eventBroker.post(Constants.EVENT_REFRESH_BACKGROUND_COLOR, "useless");

         MessageDialog.openInformation(shell, "Success", "The session has been successfully updated.");
      } catch (Exception e) {
         jtbStatusReporter.showError("Problem while saving session", e, jtbSession.getName());
         return;
      }
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

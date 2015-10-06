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

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.config.MetaQManager;
import org.titou10.jtb.config.gen.QManagerDef;
import org.titou10.jtb.dialog.QManagerConfigurationDialog;
import org.titou10.jtb.ui.JTBStatusReporter;

/**
 * Configure a Queue Manager
 * 
 * @author Denis Forveille
 *
 */
public class QManagerConfigureHandler {

   private static final Logger log = LoggerFactory.getLogger(QManagerConfigureHandler.class);

   @Inject
   private ConfigManager       cm;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell, IWorkbench workbench, @Named(IServiceConstants.ACTIVE_SELECTION) MetaQManager metaQManager) {
      log.debug("execute");

      QManagerDef qManagerDef = metaQManager.getqManagerDef();
      if (qManagerDef == null) {
         qManagerDef = cm.createNewQManagerDef(metaQManager);
      }

      QManagerConfigurationDialog dialog = new QManagerConfigurationDialog(shell, metaQManager);

      if (dialog.open() != Window.OK) {
         return;
      }

      // Save Configuration
      // TODO Display a warning to the user saying the application will restart
      try {
         boolean res = cm.saveConfigQManager(metaQManager, dialog.getJarNames());
         if (res) {
            MessageDialog.openWarning(shell,
                                      "Restart Warning",
                                      "The configuration has been successfully changed. \nThe application will now restart.");
            workbench.restart();
         }
      } catch (Exception e) {
         jtbStatusReporter.showError("Saving unsuccessful", e, "");
         return;
      }

   }

}

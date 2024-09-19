/*
 * Copyright (C) 2015-2017 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.template.handler;

import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.template.dialog.TemplateManagerDialog;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;

/**
 * Manage the templates directory
 * 
 * @author Denis Forveille
 *
 */
public class TemplateManageHandler {

   private static final Logger log = LoggerFactory.getLogger(TemplateManageHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private TemplatesManager    templatesManager;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell) {
      log.debug("execute");

      TemplateManagerDialog dialog = new TemplateManagerDialog(shell, templatesManager);

      try {
         if (dialog.open() != Window.OK) {
            // Restore templates directories
            templatesManager.reload();
            return;
         }

         templatesManager.saveTemplates();
         eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);
      } catch (CoreException | JAXBException e) {
         jtbStatusReporter.showError("Save unsuccessful", e, "");
         return;
      }
   }

}

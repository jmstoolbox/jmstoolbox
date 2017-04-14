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
package org.titou10.jtb.script.handler;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.util.Constants;

/**
 * Manage the "Scripts Manager" command
 * 
 * @author Denis Forveille
 * 
 */
public class ScriptsManagerHandler {

   private static final Logger log = LoggerFactory.getLogger(ScriptsManagerHandler.class);

   @Inject
   private MApplication        app;

   @Inject
   private EModelService       modelService;

   @Execute
   public void execute(Display display) {
      log.debug("execute");

      // Reuse or create the Scripts Manager Dialog from the snippet
      MDialog part = (MDialog) modelService.find(Constants.SM_DIALOG_SNIPPET, app);
      if (part == null) {
         part = (MDialog) modelService.cloneSnippet(app, Constants.SM_DIALOG_SNIPPET, null);

         // Center Window (10px below on the right..
         Monitor monitor = display.getPrimaryMonitor();
         Rectangle monitorRect = monitor.getBounds();
         int windowx = monitorRect.width * 9 / 10;
         int windowy = monitorRect.height * 9 / 10;
         int x = monitorRect.x + (monitorRect.width - windowx) / 2;
         int y = monitorRect.y + (monitorRect.height - windowy) / 2;
         part.setWidth(windowx);
         part.setHeight(windowy);
         part.setX(x + 10);
         part.setY(y + 10);
      }

      // part.setOnTop(true);
      app.getChildren().add(part);
      modelService.bringToTop(part);
   }

}

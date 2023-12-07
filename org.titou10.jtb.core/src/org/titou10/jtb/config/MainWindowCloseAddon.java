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
package org.titou10.jtb.config;

import java.util.List;

import jakarta.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.util.Constants;

/**
 * 
 * E4 AddOn to intercept close events on the main Window
 * 
 * @author Denis Forveille
 *
 */
public class MainWindowCloseAddon implements IWindowCloseHandler {

   private static final Logger log = LoggerFactory.getLogger(MainWindowCloseAddon.class);

   private MApplication        application;

   @Inject
   @Optional
   public void startupComplete(@UIEventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) MApplication application,
                               EModelService modelService) {

      log.debug("register MainWindowCloseAddon Add On");

      MWindow window = (MWindow) modelService.find(Constants.MAIN_WINDOW, application);
      window.getContext().set(IWindowCloseHandler.class, this);
      this.application = application;
   }

   @Override
   public boolean close(MWindow window) {

      // Close Script Manager Dialog if it exists
      List<MWindow> children = application.getChildren();
      for (MWindow w : children) {
         if (w.getElementId().equals(Constants.SM_DIALOG_SNIPPET)) {
            log.debug("Found Scripts Manager Dialog. Close it");
            w.setVisible(false);
            w.setToBeRendered(false);
         }
      }

      return true;
   }
}

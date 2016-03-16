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
package org.titou10.jtb.rest.util;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.di.UISynchronize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Display information status to the user
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
@SuppressWarnings("restriction")
public class JTBStatusReporter {

   private static final Logger log = LoggerFactory.getLogger(JTBStatusReporter.class);

   @Inject
   private StatusReporter statusReporter;

   @Inject
   private UISynchronize sync;

   public void showError(String message, Throwable t, Object... information) {
      if (t == null) {
         log.error(message, information);
      } else {
         log.error(message, t);
      }
      show(StatusReporter.ERROR, message, t, information);
   }

   public void showInfo(String message, Throwable t, Object... information) {
      if (t == null) {
         log.info(message, information);
      } else {
         log.info(message, t);
      }
      show(StatusReporter.INFO, message, t, information);
   }

   public void showWarning(String message, Throwable t, Object... information) {
      if (t == null) {
         log.warn(message, information);
      } else {
         log.warn(message, t);
      }
      show(StatusReporter.WARNING, message, t, information);
   }

   // -------
   // Helpers
   // -------

   private void show(final int severity, final String message, final Throwable t, final Object... information) {
      sync.syncExec(new Runnable() {
         @Override
         public void run() {
            statusReporter.show(severity, message, t, information);
         }
      });
   }
}

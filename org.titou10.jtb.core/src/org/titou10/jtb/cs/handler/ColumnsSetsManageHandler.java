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
package org.titou10.jtb.cs.handler;

import jakarta.xml.bind.JAXBException; 

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.cs.ColumnsSetsManager;
import org.titou10.jtb.cs.dialog.ColumnsSetsManagerDialog;
import org.titou10.jtb.ui.JTBStatusReporter;

import jakarta.inject.Inject;

/**
 * Manage the ColumnsSets
 * 
 * @author Denis Forveille
 *
 */
public class ColumnsSetsManageHandler {
   private static final Logger log = LoggerFactory.getLogger(ColumnsSetsManageHandler.class);

   @Inject
   private ColumnsSetsManager  csManager;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell) {
      log.debug("execute");

      ColumnsSetsManagerDialog dialog = new ColumnsSetsManagerDialog(shell, csManager);
      if (dialog.open() != Window.OK) {
         // Restore columns sets
         csManager.reloadConfig();
         return;
      }

      try {
         csManager.saveConfig();
      } catch (CoreException | JAXBException e) {
         jtbStatusReporter.showError("Save unsuccessful", e, "");
         return;
      }
   }

}

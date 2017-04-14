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
package org.titou10.jtb.handler.script;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.program.Program;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.script.gen.DataFile;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Script Open Data File" command
 * 
 * @author Denis Forveille
 * 
 */
public class ScriptDataFileOpenHandler {

   private static final Logger log = LoggerFactory.getLogger(ScriptDataFileOpenHandler.class);

   @Execute
   public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) DataFile selection) {
      log.debug("execute.");

      String fileName = selection.getFileName();
      Program.launch(fileName);
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional DataFile selection,
                             @Optional MMenuItem menuItem) {
      if (selection != null) {
         if ((selection.getFileName() != null) && (selection.getFileName().trim().length() > 0)) {
            return Utils.enableMenu(menuItem);
         }
      }
      return Utils.disableMenu(menuItem);
   }
}

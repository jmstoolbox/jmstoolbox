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
package org.titou10.jtb.cs.ui;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.cs.gen.ColumnsSet;
import org.titou10.jtb.jms.model.JTBSession;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;

/**
 * Excecute the action linked to the dynamic menu displayed by the "Default Columns Set." menu
 * 
 * @author Denis Forveille
 *
 */
public class SessionSelectDefaultColumnsSetHandler {

   private static final Logger log = LoggerFactory.getLogger(SessionSelectDefaultColumnsSetHandler.class);

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Inject
   private ConfigManager       cm;

   @Execute
   public void execute(Shell shell, MMenuItem menuItem) {
      log.debug("execute");

      ColumnsSet columnsSet = (ColumnsSet) menuItem.getTransientData().get(Constants.COLUMNSSET_PARAM);
      JTBSession jtbSession = (JTBSession) menuItem.getTransientData().get(Constants.COLUMNSSET_PARAM_JTBSESSION);

      jtbSession.getSessionDef().setColumnsSetName(columnsSet.getName());

      // Save state in config
      try {
         cm.sessionEdit();
      } catch (Exception e) {
         jtbStatusReporter.showError("Problem while saving default columns set", e, jtbSession.getName());
         return;
      }
   }

}

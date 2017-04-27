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
package org.titou10.jtb.visualizer.ui;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.visualizer.VisualizersManager;

/**
 * Excecute the action linked to the dynamic menu displayed by the "Open Payload as..." menu
 * 
 * @author Denis Forveille
 *
 */
public class VisualizerShowPayloadAsHandler {

   private static final Logger log = LoggerFactory.getLogger(VisualizerShowPayloadAsHandler.class);

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Inject
   private VisualizersManager  visualizersManager;

   @Execute
   public void execute(Shell shell, MMenuItem menuItem) {
      log.debug("execute");

      String visualizerName = (String) menuItem.getTransientData().get(Constants.VISUALIZER_PARAM_NAME);
      JTBMessage jtbMessage = (JTBMessage) menuItem.getTransientData().get(Constants.VISUALIZER_PARAM_JTBMESSAGE);

      try {
         JTBMessageTemplate jtbMessageTemplate = new JTBMessageTemplate(jtbMessage);
         visualizersManager.launchVisualizer(shell,
                                             visualizerName,
                                             jtbMessage.getJtbMessageType(),
                                             jtbMessageTemplate.getPayloadText(),
                                             jtbMessageTemplate.getPayloadBytes(),
                                             jtbMessageTemplate.getPayloadMap());
      } catch (Exception e) {
         jtbStatusReporter.showError("An error occurred when launching the visualizer", e, "");
         return;
      }
   }

}

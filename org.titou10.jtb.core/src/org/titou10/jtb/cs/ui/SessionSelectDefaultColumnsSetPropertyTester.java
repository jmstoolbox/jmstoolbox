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

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Evaluate;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.ui.navigator.NodeAbstract;
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.ui.navigator.NodeJTBSession;
import org.titou10.jtb.ui.navigator.NodeJTBTopic;

/**
 * 
 * Enable the "Defult Columns Set" menu depending on the Node Type
 * 
 * @author Denis Forveille
 *
 */
public class SessionSelectDefaultColumnsSetPropertyTester {

   private static final Logger log = LoggerFactory.getLogger(SessionSelectDefaultColumnsSetPropertyTester.class);

   @Evaluate
   public boolean showDefaultColumnsSet(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional NodeAbstract nodeAbstract) {
      log.debug("showDefaultColumnsSet {}", nodeAbstract);

      if (nodeAbstract instanceof NodeJTBSession) {
         return true;
      }

      if (nodeAbstract instanceof NodeJTBQueue) {
         return true;
      }
      if (nodeAbstract instanceof NodeJTBTopic) {
         return true;
      }

      return false;
   }

}

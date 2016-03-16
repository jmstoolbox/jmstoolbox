/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.rest;

import javax.inject.Singleton;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.connector.ExternalConfigManager;
import org.titou10.jtb.connector.ExternalConnector;
import org.titou10.jtb.rest.util.Constants;

/**
 * Exposes JMSToolBox features as REST Services
 * 
 * @author Denis Forveille
 *
 */

@Creatable
@Singleton
public class RESTConnector implements ExternalConnector {

   private static final Logger log = LoggerFactory.getLogger(RESTConnector.class);

   private PreferencePage      preferencePage;

   // -----------------
   // Business contract
   // -----------------

   @Override
   public void initialize(ExternalConfigManager eConfigManager) throws Exception {
      log.debug("initialize: {}", eConfigManager);

      // Preferences

      PreferenceStore ps = eConfigManager.getPreferenceStore();
      ps.setDefault(Constants.PREF_REST_PORT, Constants.PREF_REST_PORT_DEFAULT);
      ps.setDefault(Constants.PREF_REST_AUTOSTART, Constants.PREF_REST_AUTOSTART_DEFAULT);

      preferencePage = new RESTPreferencePage(ps);

      // Create injectable object for e4
      Bundle b = FrameworkUtil.getBundle(RESTConnector.class);
      if (b.getState() != Bundle.ACTIVE) {
         b.start();
      }
      BundleContext bCtx = b.getBundleContext();
      IEclipseContext eCtx = EclipseContextFactory.getServiceContext(bCtx);
      RuntimeRESTConnector rrc = ContextInjectionFactory.make(RuntimeRESTConnector.class, eCtx);

      rrc.initialize(eConfigManager);

   }

   @Override
   public PreferencePage getPreferencePage() {
      return preferencePage;
   }
}

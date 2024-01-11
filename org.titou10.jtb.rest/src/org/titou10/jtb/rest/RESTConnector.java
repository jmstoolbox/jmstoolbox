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

import jakarta.inject.Singleton;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.connector.ExternalConnector;
import org.titou10.jtb.connector.ExternalConnectorManager;
import org.titou10.jtb.rest.preference.RESTPreferencePage;
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

   private IPreferenceStore    ps;

   // -----------------
   // Business contract
   // -----------------

   @Override
   public void initialize(ExternalConnectorManager eConfigManager) throws Exception {
      log.debug("initialize: {}", eConfigManager);

      // Preferences Management
      ps = eConfigManager.getIPreferenceStore();
      ps.setDefault(Constants.PREF_REST_PORT, Constants.PREF_REST_PORT_DEFAULT);
      ps.setDefault(Constants.PREF_REST_AUTOSTART, Constants.PREF_REST_AUTOSTART_DEFAULT);

      // Create an injectable object for e4 artefacts
      Bundle b = FrameworkUtil.getBundle(RESTConnector.class);

      if (b.getState() != Bundle.ACTIVE) {
         b.start();
      }

      BundleContext bCtx = b.getBundleContext();
      IEclipseContext eCtx = EclipseContextFactory.getServiceContext(bCtx);
      RuntimeRESTConnector rrc = ContextInjectionFactory.make(RuntimeRESTConnector.class, eCtx);

      // Initialize runtime object
      rrc.initialize(eConfigManager);
   }

   @Override
   public PreferencePage getPreferencePage() {
      // DF: do not put this in cache as the page must be recreated after each usage (ie disposed)
      return new RESTPreferencePage(ps);
   }
}

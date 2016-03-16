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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jetty.server.Server;
import org.eclipse.jface.preference.PreferenceStore;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.connector.ExternalConfigManager;
import org.titou10.jtb.rest.service.RESTServices;
import org.titou10.jtb.rest.util.Constants;

/**
 * REST Connector Runtime Object injectable in e4
 * 
 * @author Denis Forveille
 *
 */

@Creatable
@Singleton
public class RuntimeRESTConnector {

   private static final Logger   log       = LoggerFactory.getLogger(RuntimeRESTConnector.class);

   public static final String    ECM_PARAM = "ExternalConfigManager";

   private ExternalConfigManager eConfigManager;
   private PreferenceStore       ps;

   private Server                jettyServer;

   public void initialize(ExternalConfigManager eConfigManager) throws Exception {
      this.eConfigManager = eConfigManager;
      ps = eConfigManager.getPreferenceStore();

      boolean autostart = ps.getBoolean(Constants.PREF_REST_AUTOSTART);
      if (autostart) {
         start();
      }

   }

   public int getPort() {
      return ps.getInt(Constants.PREF_REST_PORT);
   }

   public void start() throws Exception {
      log.debug("starting Jetty Server on port {}", getPort());

      if (jettyServer == null) {

         // Save ExternalConfigManager to inject it into REST services via hk2
         Map<String, Object> applicationParams = new HashMap<>(1);
         applicationParams.put(ECM_PARAM, eConfigManager);

         // Initialize jetty server with jersey
         URI baseUri = UriBuilder.fromUri("http://localhost/").port(getPort()).build();
         ResourceConfig config = new ResourceConfig(RESTServices.class);
         config.setProperties(applicationParams);
         // config.register(JacksonFeature.class);
         jettyServer = JettyHttpContainerFactory.createServer(baseUri, config, true);
         jettyServer.setStopAtShutdown(true);
      }

      jettyServer.start();
   }

   public void stop() throws Exception {
      log.debug("stopping Jetty Server");

      // DF: start then stop then start generates a NPE, so we create a new instance each time
      if (jettyServer != null) {
         if (jettyServer.isStarted()) {
            jettyServer.stop();
         }
         jettyServer = null;
      }
   }

   public boolean isRunning() {
      if (jettyServer == null) {
         return false;
      }

      return jettyServer.isRunning();
   }

}

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
package org.titou10.jtb.rest;

import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.jetty.ee8.servlet.ServletContextHandler;
import org.eclipse.jetty.ee8.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.connector.ExternalConnectorManager;
import org.titou10.jtb.rest.util.Constants;

/**
 * REST Connector Runtime Object, injectable in e4
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class RuntimeRESTConnector {

   private static final Logger            log = LoggerFactory.getLogger(RuntimeRESTConnector.class);

   private IPreferenceStore               ps;

   private ServletContextHandler          servletCtxHandler;
   private Server                         jettyServer;

   public static ExternalConnectorManager E_CONNECTOR_MANAGER;

   public void initialize(ExternalConnectorManager eConfigManager) throws Exception {
      ps = eConfigManager.getIPreferenceStore();

      E_CONNECTOR_MANAGER = eConfigManager;

      // Manage Jetty
      ServletHolder servletHolder = new ServletHolder(new HttpServletDispatcher());
      servletHolder.setInitParameter("javax.ws.rs.Application", RestApplication.class.getCanonicalName());

      servletCtxHandler = new ServletContextHandler();
      servletCtxHandler.addServlet(servletHolder, "/rest/*");

      boolean autostart = ps.getBoolean(Constants.PREF_REST_AUTOSTART);
      if (autostart) {
         start();
      }
   }

   // -------
   // Actions
   // -------

   public void start() throws Exception {
      log.info("Starting Jetty Server on port {}", getPort());

      if (jettyServer == null) {
         jettyServer = new Server(getPort());
         jettyServer.setHandler(servletCtxHandler);
         jettyServer.start();
         jettyServer.setStopAtShutdown(true);
      }

      jettyServer.start();

      log.info("Jetty Server started on port {}", getPort());
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

   // -------
   // Information
   // -------

   public int getPort() {
      return ps.getInt(Constants.PREF_REST_PORT);
   }

   public String getStatus() {
      StringBuilder sb = new StringBuilder(128);
      sb.append("State: ");

      if (jettyServer == null) {
         sb.append(Server.STOPPED);
         sb.append("\n");
         sb.append("Configured port: ");
         sb.append(getPort());
      } else {
         sb.append(jettyServer.getState());
         sb.append("\n");
         sb.append("Listening on port: ");

         ServerConnector c = (ServerConnector) jettyServer.getConnectors()[0];
         sb.append(c.getPort());
      }
      return sb.toString();
   }

   public boolean isRunning() {
      return jettyServer == null ? false : jettyServer.isStarted();
   }

}

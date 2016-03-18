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
package org.titou10.jtb.rest.service;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.connector.ExternalConfigManager;
import org.titou10.jtb.rest.RuntimeRESTConnector;
import org.titou10.jtb.rest.util.Constants;

/**
 * 
 * Exposes JMSToolBox features related to Scripts as REST services
 * 
 * @author Denis Forveille
 *
 */
@Path("/rest/script")
@Singleton
public class ScriptServices {

   private static final Logger   log = LoggerFactory.getLogger(ScriptServices.class);

   @Context
   private Application           app;

   private ExternalConfigManager eConfigManager;

   @PostConstruct
   private void init() {
      this.eConfigManager = (ExternalConfigManager) app.getProperties().get(RuntimeRESTConnector.ECM_PARAM);
   }

   // -----------------------------------
   // Retrieve Destinations for a Session
   // POST /rest/script/<scriptName>
   // -----------------------------------

   @POST
   @Path("/{" + Constants.P_SCRIPT_NAME + "}")
   public Response executeScript(@PathParam(Constants.P_SCRIPT_NAME) String scriptName) {
      log.debug("getDestinationNames. scriptName={}", scriptName);

      if (scriptName == null) {
         return Response.status(Response.Status.BAD_REQUEST).build();
      }

      eConfigManager.executeScript(scriptName);

      return Response.ok().build();
   }
}

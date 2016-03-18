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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.connector.ExternalConfigManager;
import org.titou10.jtb.connector.ex.ExecutionException;
import org.titou10.jtb.connector.ex.UnknownSessionException;
import org.titou10.jtb.connector.transport.Destination;
import org.titou10.jtb.rest.RuntimeRESTConnector;
import org.titou10.jtb.rest.util.Constants;

/**
 * 
 * Exposes JMSToolBox features related to Session as REST services
 * 
 * @author Denis Forveille
 *
 */
@Path("/rest/session")
@Singleton
public class SessionServices {

   private static final Logger   log = LoggerFactory.getLogger(SessionServices.class);

   @Context
   private Application           app;

   private ExternalConfigManager eConfigManager;

   @PostConstruct
   private void init() {
      this.eConfigManager = (ExternalConfigManager) app.getProperties().get(RuntimeRESTConnector.ECM_PARAM);
   }

   // -----------------------------------
   // Retrieve Destinations for a Session
   // /rest/message/<sessionName>
   // -----------------------------------

   @GET
   @Path("/{" + Constants.P_SESSION_NAME + "}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response getDestinations(@PathParam(Constants.P_SESSION_NAME) String sessionName) {
      log.debug("getDestinationNames. sessionName={}", sessionName);

      try {

         List<Destination> destinations = eConfigManager.getDestination(sessionName);
         log.debug("nb destinations : {}", destinations.size());
         if (destinations.isEmpty()) {
            return Response.noContent().build();
         } else {
            return Response.ok(destinations).build();
         }

      } catch (ExecutionException e) {
         return Response.serverError().build();
      } catch (UnknownSessionException e) {
         return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }
   }
}

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
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.connector.ExternalConfigManager;
import org.titou10.jtb.connector.transport.Destination;
import org.titou10.jtb.connector.transport.Message;
import org.titou10.jtb.rest.RuntimeRESTConnector;

/**
 * 
 * Exposes JMSToolBox features as REST serices
 * 
 * @author Denis Forveille
 *
 */
@Path("/rest")
@Singleton
public class RESTServices {

   private static final Logger   log                = LoggerFactory.getLogger(RESTServices.class);

   private static final String   P_SESSION_NAME     = "sessionName";
   private static final String   P_DESTINATION_NAME = "destinationName";
   private static final String   P_MODE             = "mode";
   private static final String   P_LIMIT            = "limit";

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
   @Path("/message/{" + P_SESSION_NAME + "}")
   // @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
   @Produces(MediaType.APPLICATION_JSON)
   // @Produces(MediaType.APPLICATION_XML)
   public Response getDestinationNames(@PathParam(P_SESSION_NAME) String sessionName) {
      log.debug("getDestinationNames. sessionName={}", sessionName);

      if (sessionName == null) {
         return Response.status(Response.Status.BAD_REQUEST).build();
      }

      List<Destination> destinations = eConfigManager.getDestinationNames(sessionName);
      if (destinations == null) {
         return Response.status(Response.Status.BAD_REQUEST).build();
      }

      log.debug("nb destinations : {}", destinations.size());
      if (destinations.isEmpty()) {
         return Response.noContent().build();
      } else {
         // GenericEntity<List<Destination>> e = new GenericEntity<List<Destination>>(destinations) {
         // };
         // return Response.ok(e).build();
         return Response.status(Response.Status.OK).entity(destinations).build();
      }
   }

   // -----------------------------------------------------------------------
   // Retrieve (get/browse) Messages from a Session/Queue
   // /rest/message/<sessionName>/<destinationName>?mode=<get|browse>&limit=n
   // defaults: mode=browse limit=1
   // -----------------------------------------------------------------------

   @GET
   @Path("/message/{" + P_SESSION_NAME + "}/{" + P_DESTINATION_NAME + "}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response getMessage(@PathParam(P_SESSION_NAME) String sessionName,
                              @PathParam(P_DESTINATION_NAME) String destinationName,
                              @DefaultValue("browse") @QueryParam(P_MODE) String mode,
                              @DefaultValue("1") @QueryParam(P_LIMIT) int limit) {
      log.warn("getMessage. sessionName={} destinationName={} mode={} limit={}", sessionName, destinationName, mode, limit);

      if ((sessionName == null) || (destinationName == null)) {
         return Response.status(Response.Status.BAD_REQUEST).build();
      }

      List<Message> messages = eConfigManager.getMessage(sessionName, destinationName, mode, limit);
      if (messages == null) {
         return Response.status(Response.Status.BAD_REQUEST).build();
      }

      if (messages.isEmpty()) {
         return Response.noContent().build();
      } else {
         return Response.ok(messages).build();

      }
   }

   @POST
   @Path("/message/{" + P_SESSION_NAME + "}/{" + P_DESTINATION_NAME + "}")
   @Consumes(MediaType.APPLICATION_JSON)
   public Response postMessage(@PathParam(P_SESSION_NAME) String sessionName,
                               @PathParam(P_DESTINATION_NAME) String destinationName,
                               Message message) {
      log.warn("postMessage. sessionName={} destinationName={} message={}", sessionName, destinationName, message);

      if ((sessionName == null) || (destinationName == null) || (message == null)) {
         return Response.status(Response.Status.BAD_REQUEST).build();
      }

      eConfigManager.postMessage(sessionName, destinationName, message);

      return Response.ok().build();
   }

   @DELETE
   @Path("/message/{" + P_SESSION_NAME + "}/{" + P_DESTINATION_NAME + "}")
   public Response emptyDestination(@PathParam(P_SESSION_NAME) String sessionName,
                                    @PathParam(P_DESTINATION_NAME) String destinationName) {
      log.warn("emptyDestination. sessionName={} destinationName={} ", sessionName, destinationName);

      if ((sessionName == null) || (destinationName == null)) {
         return Response.status(Response.Status.BAD_REQUEST).build();
      }

      eConfigManager.emptyQueue(sessionName, destinationName);

      return Response.ok().build();
   }

}

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
import javax.ws.rs.PUT;
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
import org.titou10.jtb.connector.ex.EmptyMessageException;
import org.titou10.jtb.connector.ex.ExecutionException;
import org.titou10.jtb.connector.ex.UnknownDestinationException;
import org.titou10.jtb.connector.ex.UnknownQueueException;
import org.titou10.jtb.connector.ex.UnknownSessionException;
import org.titou10.jtb.connector.transport.Message;
import org.titou10.jtb.rest.RuntimeRESTConnector;
import org.titou10.jtb.rest.util.Constants;

/**
 * 
 * Exposes JMSToolBox features related to Messages as REST services
 * 
 * @author Denis Forveille
 *
 */
@Path("/rest/message")
@Singleton
public class MessageServices {

   private static final Logger   log = LoggerFactory.getLogger(MessageServices.class);

   @Context
   private Application           app;

   private ExternalConfigManager eConfigManager;

   @PostConstruct
   private void init() {
      this.eConfigManager = (ExternalConfigManager) app.getProperties().get(RuntimeRESTConnector.ECM_PARAM);
   }

   // -----------------------------------------------------------------------
   // Browse Messages from a Session:Queue
   // GET /rest/message/<sessionName>/<destinationName>?&limit=n
   // defaults: limit=200
   // -----------------------------------------------------------------------

   @GET
   @Path("/{" + Constants.P_SESSION_NAME + "}/{" + Constants.P_DESTINATION_NAME + "}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response browseMessages(@PathParam(Constants.P_SESSION_NAME) String sessionName,
                                  @PathParam(Constants.P_DESTINATION_NAME) String destinationName,
                                  @DefaultValue("200") @QueryParam(Constants.P_LIMIT) int limit) {
      log.warn("browseMessages. sessionName={} destinationName={} limit={}", sessionName, destinationName, limit);

      try {

         List<Message> messages = eConfigManager.browseMessages(sessionName, destinationName, limit);
         if (messages.isEmpty()) {
            return Response.noContent().build();
         } else {
            return Response.ok(messages).build();
         }

      } catch (ExecutionException e) {
         return Response.serverError().build();
      } catch (UnknownSessionException | UnknownDestinationException | UnknownQueueException e) {
         return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }
   }

   // -----------------------------------------------------------------------
   // Post/Publish a message to Session:Destination
   // POST /rest/message/<sessionName>/<destinationName>
   // -----------------------------------------------------------------------

   @POST
   @Path("/{" + Constants.P_SESSION_NAME + "}/{" + Constants.P_DESTINATION_NAME + "}")
   @Consumes(MediaType.APPLICATION_JSON)
   public Response postMessage(@PathParam(Constants.P_SESSION_NAME) String sessionName,
                               @PathParam(Constants.P_DESTINATION_NAME) String destinationName,
                               Message message) {
      log.warn("postMessage. sessionName={} destinationName={} message={}", sessionName, destinationName, message);

      try {

         eConfigManager.postMessage(sessionName, destinationName, message);
         return Response.ok().build();

      } catch (ExecutionException e) {
         return Response.serverError().build();
      } catch (UnknownSessionException | UnknownDestinationException | EmptyMessageException e) {
         return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }

   }

   // -----------------------------------------------------------------------
   // Post/Publish a message to Session:Destination from a Template
   // POST /rest/message/<sessionName>/<destinationName>/<templateName>
   // -----------------------------------------------------------------------

   @POST
   @Path("/{" + Constants.P_SESSION_NAME + "}/{" + Constants.P_DESTINATION_NAME + "}" + "}/{" + Constants.P_TEMPLATE_NAME + "}")
   @Consumes(MediaType.APPLICATION_JSON)
   public Response postMessageTemplate(@PathParam(Constants.P_SESSION_NAME) String sessionName,
                                       @PathParam(Constants.P_DESTINATION_NAME) String destinationName,
                                       @PathParam(Constants.P_TEMPLATE_NAME) String templateName) {
      log.warn("postMessageTemplate. sessionName={} destinationName={} templateName={}",
               sessionName,
               destinationName,
               templateName);

      eConfigManager.postMessageTemplate(sessionName, destinationName, templateName);

      return Response.ok().build();
   }

   // -----------------------------------------------------------------------
   // Remove messages from a Session:Destination
   // PUT /rest/message/<sessionName>/<destinationName>?limit=n
   // defaults: limit=1
   // -----------------------------------------------------------------------

   @PUT
   @Path("/{" + Constants.P_SESSION_NAME + "}/{" + Constants.P_DESTINATION_NAME + "}")
   public Response removeMessages(@PathParam(Constants.P_SESSION_NAME) String sessionName,
                                  @PathParam(Constants.P_DESTINATION_NAME) String destinationName,
                                  @DefaultValue("1") @QueryParam(Constants.P_LIMIT) int limit) {
      log.warn("removeMessages. sessionName={} destinationName={} limit={}", sessionName, destinationName, limit);

      try {

         List<Message> messages = eConfigManager.removeMessages(sessionName, destinationName, limit);
         if (messages.isEmpty()) {
            return Response.noContent().build();
         } else {
            return Response.ok(messages).build();
         }

      } catch (ExecutionException e) {
         return Response.serverError().build();
      } catch (UnknownSessionException | UnknownDestinationException | UnknownQueueException e) {
         return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }
   }

   // -----------------------------------------------------------------------
   // Remove all message from a Session:Destination
   // DELETE /rest/message/<sessionName>/<destinationName>
   // -----------------------------------------------------------------------

   @DELETE
   @Path("/{" + Constants.P_SESSION_NAME + "}/{" + Constants.P_DESTINATION_NAME + "}")
   public Response emptyDestination(@PathParam(Constants.P_SESSION_NAME) String sessionName,
                                    @PathParam(Constants.P_DESTINATION_NAME) String destinationName) {
      log.warn("emptyDestination. sessionName={} destinationName={} ", sessionName, destinationName);

      try {

         eConfigManager.emptyQueue(sessionName, destinationName);
         return Response.ok().build();

      } catch (ExecutionException e) {
         return Response.serverError().build();
      } catch (UnknownSessionException | UnknownDestinationException | UnknownQueueException e) {
         return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }
   }

}

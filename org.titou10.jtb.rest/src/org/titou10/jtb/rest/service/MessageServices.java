/*
 * Copyright (C) 2023 Denis Forveille titou10.titou10@gmail.com
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

import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.connector.ExternalConnectorManager;
import org.titou10.jtb.connector.ex.EmptyMessageException;
import org.titou10.jtb.connector.ex.ExecutionException;
import org.titou10.jtb.connector.ex.UnknownDestinationException;
import org.titou10.jtb.connector.ex.UnknownQueueException;
import org.titou10.jtb.connector.ex.UnknownSessionException;
import org.titou10.jtb.connector.ex.UnknownTemplateException;
import org.titou10.jtb.connector.transport.MessageInput;
import org.titou10.jtb.connector.transport.MessageOutput;
import org.titou10.jtb.rest.util.Constants;
import org.titou10.jtb.rest.util.Utils;

/**
 *
 * Exposes JMSToolBox features related to Messages, as REST services
 *
 * @author Denis Forveille
 *
 */
@Path("/rest/message")
@Singleton
public class MessageServices {

   private static final Logger      log                  = LoggerFactory.getLogger(MessageServices.class);
   private static final String      DEFAULT_BROWSE_LIMIT = "200";

   private ExternalConnectorManager eConfigManager;

   public MessageServices(ExternalConnectorManager eConfigManager) {
      this.eConfigManager = eConfigManager;
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
                                  @QueryParam(Constants.P_JMS_SELECTOR) String selectorsSearch,
                                  @QueryParam(Constants.P_PAYOAD_SEARCH) String payloadSearch,
                                  @DefaultValue(DEFAULT_BROWSE_LIMIT) @QueryParam(Constants.P_LIMIT) int limit) {
      log.debug("browseMessages. sessionName: {} destinationName: {} limit: {} selectorsSearch: {} payloadSearch: {} ",
                sessionName,
                destinationName,
                limit,
                selectorsSearch,
                payloadSearch);

      try {

         List<MessageOutput> messages = eConfigManager
                  .browseMessages(sessionName, destinationName, payloadSearch, selectorsSearch, limit);
         log.debug("nb messages : {}", messages.size());

         return messages.isEmpty() ? Response.noContent().build() : Response.ok(messages).build();

      } catch (ExecutionException e) {
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Utils.getCause(e).getMessage()).build();
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
   @Produces(MediaType.APPLICATION_JSON)
   public Response postMessage(@PathParam(Constants.P_SESSION_NAME) String sessionName,
                               @PathParam(Constants.P_DESTINATION_NAME) String destinationName,
                               MessageInput messageInput) {
      log.debug("postMessage. sessionName: {} destinationName: {} message: {}", sessionName, destinationName, messageInput);

      try {

         MessageOutput message = eConfigManager.postMessage(sessionName, destinationName, messageInput);
         log.debug("postMessage OK. message={}", message);
         return Response.status(Response.Status.CREATED).entity(message).build();

      } catch (ExecutionException e) {
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Utils.getCause(e).getMessage()).build();
      } catch (UnknownSessionException | UnknownDestinationException | EmptyMessageException e) {
         return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }

   }

   // -----------------------------------------------------------------------
   // Post/Publish a message to Session:Destination from a Template
   // POST /rest/message/<sessionName>/<destinationName>/<templateName>
   // -----------------------------------------------------------------------

   @POST
   @Path("/{" + Constants.P_SESSION_NAME + "}/{" + Constants.P_DESTINATION_NAME + "}/{" + Constants.P_TEMPLATE_NAME + "}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response postMessageTemplate(@PathParam(Constants.P_SESSION_NAME) String sessionName,
                                       @PathParam(Constants.P_DESTINATION_NAME) String destinationName,
                                       @PathParam(Constants.P_TEMPLATE_NAME) String templateName) {
      log.debug("postMessageTemplate. sessionName: {} destinationName: {} templateName: {}",
                sessionName,
                destinationName,
                templateName);

      try {

         MessageOutput message = eConfigManager.postMessageTemplate(sessionName, destinationName, templateName);
         log.debug("postMessage OK. message={}", message);
         return Response.status(Response.Status.CREATED).entity(message).build();

      } catch (ExecutionException e) {
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Utils.getCause(e).getMessage()).build();
      } catch (UnknownSessionException | UnknownDestinationException | UnknownTemplateException | EmptyMessageException e) {
         return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }

   }

   // -----------------------------------------------------------------------
   // Remove messages from a Session:Destination
   // PUT /rest/message/<sessionName>/<destinationName>?limit=n
   // defaults: limit=1
   // -----------------------------------------------------------------------

   @PUT
   @Path("/{" + Constants.P_SESSION_NAME + "}/{" + Constants.P_DESTINATION_NAME + "}")
   @Produces(MediaType.APPLICATION_JSON)
   public Response removeMessages(@PathParam(Constants.P_SESSION_NAME) String sessionName,
                                  @PathParam(Constants.P_DESTINATION_NAME) String destinationName,
                                  @QueryParam(Constants.P_JMS_SELECTOR) String selectorsSearch,
                                  @DefaultValue("1") @QueryParam(Constants.P_LIMIT) int limit) {
      log.debug("removeMessages. sessionName: {} destinationName: {} limit: {} selectorsSearch: {}",
                sessionName,
                destinationName,
                limit,
                selectorsSearch);

      try {

         List<MessageOutput> messages = eConfigManager.removeMessages(sessionName, destinationName, selectorsSearch, limit);
         log.debug("nb messages: {}", messages.size());
         return messages.isEmpty() ? Response.noContent().build() : Response.ok(messages).build();

      } catch (ExecutionException e) {
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Utils.getCause(e).getMessage()).build();
      } catch (UnknownSessionException | UnknownDestinationException | UnknownQueueException e) {
         return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }
   }

   // -----------------------------------------------------------------------
   // Remove all messages from a Session:Destination
   // DELETE /rest/message/<sessionName>/<destinationName>
   // -----------------------------------------------------------------------

   @DELETE
   @Path("/{" + Constants.P_SESSION_NAME + "}/{" + Constants.P_QUEUE_NAME + "}")
   public Response emptyDestination(@PathParam(Constants.P_SESSION_NAME) String sessionName,
                                    @PathParam(Constants.P_QUEUE_NAME) String queueName) {
      log.debug("emptyDestination. sessionName: {} queueName: {} ", sessionName, queueName);

      try {
         eConfigManager.emptyQueue(sessionName, queueName);
         log.debug("emptyDestination OK");
         return Response.ok().build();
      } catch (ExecutionException e) {
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Utils.getCause(e).getMessage()).build();
      } catch (UnknownSessionException | UnknownDestinationException | UnknownQueueException e) {
         return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }
   }

}

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

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.connector.ExternalConfigManager;
import org.titou10.jtb.rest.transport.MessageTransport;

/**
 * 
 * Exposes JMSToolBox features as REST serices
 * 
 * @author Denis Forveille
 *
 */
@Path("/rest/message")
@Singleton
public class RESTServices {

   private static final Logger   log = LoggerFactory.getLogger(RESTServices.class);

   @Context
   private Application           app;

   private ExternalConfigManager eConfigManager;

   @PostConstruct
   private void init() {
      this.eConfigManager = (ExternalConfigManager) app.getProperties().get(ExternalRESTConnector.ECM_PARAM);
   }

   // @GET
   // @Produces(MediaType.TEXT_PLAIN)
   // public String postMessage() {
   // log.debug("postMessage");
   //
   // eConfigManager.sendMessage("QMAAED_espaceClient", "ECP.INPUT", "super texte");
   //
   // return "Test " + new Date();
   // }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public Response postMessage2(MessageTransport mt) {
      log.debug("postMessage {}", mt);

      eConfigManager.postMessage(mt.getSessionName(), mt.getDestinationName(), mt.getPayload());

      return Response.status(Response.Status.OK).build();
   }

}

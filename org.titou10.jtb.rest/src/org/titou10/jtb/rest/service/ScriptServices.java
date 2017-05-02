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

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.connector.ExternalConnectorManager;
import org.titou10.jtb.connector.transport.ScriptInput;
import org.titou10.jtb.connector.transport.ScriptOutput;

/**
 * 
 * Exposes JMSToolBox features related to Scripts, as REST services
 * 
 * @author Denis Forveille
 *
 */
@Singleton
@Path("/rest/script")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ScriptServices {

   private static final Logger      log = LoggerFactory.getLogger(ScriptServices.class);

   private ExternalConnectorManager eConfigManager;

   public ScriptServices(ExternalConnectorManager eConfigManager) {
      this.eConfigManager = eConfigManager;
   }

   // -----------------------------------
   // Execute a script
   // -----------------------------------

   @POST
   public Response executeScript(ScriptInput scriptInput) {
      log.debug("executeScript. {}", scriptInput);

      boolean simulation = scriptInput.getSimulation() == null ? false : scriptInput.getSimulation();
      int nbMessagesMax = scriptInput.getNbMessagesMax() == null ? 0 : scriptInput.getNbMessagesMax();
      String scriptName = scriptInput.getScriptName();
      if ((scriptName == null) || (scriptName.trim().isEmpty())) {
         return Response.status(Response.Status.BAD_REQUEST).build();
      }

      try {
         int nbMessaqges = eConfigManager.executeScript(scriptName, simulation, nbMessagesMax);
         ScriptOutput scriptOutput = new ScriptOutput();
         scriptOutput.setNbMessages(nbMessaqges);
         return Response.ok(scriptOutput).build();
      } catch (Exception e) {
         log.error("An error occurred while executing the script", e);
         return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
      }
   }
}

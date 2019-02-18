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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.titou10.jtb.connector.ExternalConnectorManager;
import org.titou10.jtb.rest.service.MessageServices;
import org.titou10.jtb.rest.service.ScriptServices;
import org.titou10.jtb.rest.service.SessionServices;

/**
 * 
 * Define the jax-rs application components
 * 
 * @author Denis Forveille
 *
 */
public class RestApplication extends Application {

   ExternalConnectorManager eConfigManager;

   public RestApplication() {
      // DF: beurk..don't know how to pass this parameter correctly
      eConfigManager = RuntimeRESTConnector.E_CONNECTOR_MANAGER;
   }

   @Override
   public Set<Object> getSingletons() {
      Set<Object> r = new HashSet<>(3);
      r.add(new MessageServices(eConfigManager));
      r.add(new ScriptServices(eConfigManager));
      r.add(new SessionServices(eConfigManager));
      return r;
   }

   @Override
   public Set<Class<?>> getClasses() {
      return Collections.singleton(JacksonConfig.class);
   }
}

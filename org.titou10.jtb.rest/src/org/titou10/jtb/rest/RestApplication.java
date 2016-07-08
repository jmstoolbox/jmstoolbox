package org.titou10.jtb.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.titou10.jtb.connector.ExternalConnectorManager;
import org.titou10.jtb.rest.service.MessageServices;
import org.titou10.jtb.rest.service.ScriptServices;
import org.titou10.jtb.rest.service.SessionServices;

public class RestApplication extends Application {

   ExternalConnectorManager eConfigManager;

   public RestApplication() {
      // DF: beurk..don't know how to passe this parameter correctly
      eConfigManager = RuntimeRESTConnector.E_CONNECTOR_MANAGER;
   }

   @Override
   public Set<Object> getSingletons() {
      Set<Object> r = new HashSet<>();
      r.add(new MessageServices(eConfigManager));
      r.add(new ScriptServices(eConfigManager));
      r.add(new SessionServices(eConfigManager));
      return r;
   }
}

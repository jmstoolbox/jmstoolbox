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
package org.titou10.jtb.qm.websphere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.DestinationData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

/**
 * 
 * Implements IBM WebSphere full profile Q Provider via JNDI
 * 
 * @author Denis Forveille
 *
 */
public class WASQManager extends QManager {

   private static final Logger             log                = LoggerFactory.getLogger(WASQManager.class);

   private static final String             CR                 = "\n";

   private static final String             P_BINDING          = "binding";
   private static final String             P_ICF              = "initialContextFactory";
   private static final String             P_CONFIG_URL_SSL   = "com.ibm.SSL.ConfigURL";
   // private static final String P_PROVIDER_URL = "providerURL";

   private static final LinkedList<String> BYPASS_CLASS_NAMES = new LinkedList<>();

   private static final String             HELP_TEXT;

   private List<QManagerProperty>          parameters         = new ArrayList<QManagerProperty>();

   private final Map<Integer, Context>     jndiContexts       = new HashMap<>();

   public WASQManager() {
      log.debug("Instantiate WASQManager");

      parameters.add(new QManagerProperty(P_BINDING,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "Name of the Connection Factory (eg. jms/cf/xyz)"));
      parameters.add(new QManagerProperty(P_ICF,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "initialContextFactory : com.ibm.websphere.naming.WsnInitialContextFactory",
                                          "com.ibm.websphere.naming.WsnInitialContextFactory"));
      parameters.add(new QManagerProperty(P_CONFIG_URL_SSL,
                                          false,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "points to a 'ssl.client.props' client configuration file"));

      BYPASS_CLASS_NAMES.add("javax.resource.cci.ConnectionFactory");
      BYPASS_CLASS_NAMES.add("com.ibm.ejs.j2c.ActivationSpecBindingInfo");
      BYPASS_CLASS_NAMES.add("com.ibm.iscportal.portlet.service.PortletServiceHomeImpl");
      BYPASS_CLASS_NAMES.add("com.ibm.websphere.ejbquery.QueryLocalHome");
      BYPASS_CLASS_NAMES.add("com.ibm.ws.PluginRegistry");
      BYPASS_CLASS_NAMES.add("com.ibm.wkplc.extensionregistry.ExtensionRegistryProxy");

   }

   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      // Save System properties
      saveSystemProperties();
      try {

         // Extract properties
         Map<String, String> mapProperties = extractProperties(sessionDef);

         String binding = mapProperties.get(P_BINDING);
         String icf = mapProperties.get(P_ICF);
         String providerURL = "iiop://" + sessionDef.getHost() + ":" + sessionDef.getPort();
         String sslConfigURL = mapProperties.get(P_CONFIG_URL_SSL);
         // String providerURL = mapProperties.get( P_PROVIDER_URL);

         // Set System properties
         if (sslConfigURL != null) {
            System.setProperty(P_CONFIG_URL_SSL, sslConfigURL);
         }

         // Lookup Connection factory
         Hashtable<String, String> environment = new Hashtable<>();
         environment.put(Context.URL_PKG_PREFIXES, "com.ibm.ws.naming");
         if (sessionDef.getActiveUserid() != null) {
            environment.put(Context.SECURITY_PRINCIPAL, sessionDef.getActiveUserid());
         }
         if (sessionDef.getActivePassword() != null) {
            environment.put(Context.SECURITY_CREDENTIALS, sessionDef.getActivePassword());
         }

         environment.put(Context.PROVIDER_URL, providerURL);
         environment.put(Context.INITIAL_CONTEXT_FACTORY, icf);

         Context ctx = new InitialDirContext(environment);

         // Create JMS Connection
         ConnectionFactory cf = (ConnectionFactory) ctx.lookup(binding);
         Connection jmsConnection = cf.createConnection();
         jmsConnection.setClientID(clientID);
         jmsConnection.start();

         log.info("connected to {}", sessionDef.getName());

         // Store per connection related data
         Integer hash = jmsConnection.hashCode();
         jndiContexts.put(hash, ctx);

         return jmsConnection;
      } finally {
         restoreSystemProperties();
      }
   }

   @Override
   public DestinationData discoverDestinations(Connection jmsConnection, boolean showSystemObjects) throws Exception {
      log.debug("discoverDestinations : {} - {}", jmsConnection, showSystemObjects);

      Integer hash = jmsConnection.hashCode();
      Context ctx = jndiContexts.get(hash);

      // Build Queues/Topics lists
      SortedSet<QueueData> listQueueData = new TreeSet<>();
      SortedSet<TopicData> listTopicData = new TreeSet<>();

      listContext(null, ctx, new HashSet<String>(), listQueueData, listTopicData);

      return new DestinationData(listQueueData, listTopicData);
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occured while closing connection. Ignore it. Msg={}", e.getMessage());
      }

      Integer hash = jmsConnection.hashCode();
      jndiContexts.remove(hash);
   }

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      StringBuilder sb = new StringBuilder(2048);
      sb.append("Extra JARS:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("- com.ibm.ws.ejb.thinclient_x.y.z.jar      (from <was_full_home>/runtimes)").append(CR);
      sb.append("- com.ibm.ws.orb_x.y.z.jar                 (from <was_full_home>/runtimes)").append(CR);
      sb.append("- com.ibm.ws.sib.client.thin.jms_x.y.z.jar (from <was_full_home>/runtimes)").append(CR);
      sb.append(CR);
      sb.append("Prerequisites:").append(CR);
      sb.append("--------------").append(CR);
      sb.append("- define a 'JMS Connection factory' in WebSphere").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : WebSphere server host name").append(CR);
      sb.append("Port          : BOOTSTRAP_ADDRESS port of the server (eg 2819)").append(CR);
      sb.append("User/Password : User allowed to perform rest calls").append(CR);
      sb.append(CR);
      sb.append("Properties:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("- binding               : Name of the Connection Factory (eg. jms/cf/xyz)").append(CR);
      sb.append("- initialContextFactory : com.ibm.websphere.naming.WsnInitialContextFactory").append(CR);
      sb.append("- com.ibm.SSL.ConfigURL : points to a 'ssl.client.props' client configuration file");

      HELP_TEXT = sb.toString();
   }

   // -------
   // Helpers
   // -------

   private void listContext(String path,
                            Context ctx,
                            Set<String> visited,
                            SortedSet<QueueData> q,
                            SortedSet<TopicData> t) throws NamingException {
      log.trace("now scanning nameInNamespace '{}'", ctx.getNameInNamespace());

      if (visited.contains(ctx.getNameInNamespace())) {
         return;
      }

      visited.add(ctx.getNameInNamespace());

      NamingEnumeration<NameClassPair> list = ctx.list("");
      while (list.hasMore()) {
         NameClassPair item = (NameClassPair) list.next();
         String className = item.getClassName();
         String name = item.getName();
         String fn = ctx.getNameInNamespace() + "/" + name;
         log.debug("   {} name={} {}", item.toString(), fn, className);

         String ctxPath;
         if (path == null) {
            ctxPath = item.getName();
         } else {
            ctxPath = path + "/" + item.getName();
         }

         if (BYPASS_CLASS_NAMES.indexOf(className) != -1) {
            log.debug("   bypass");
            continue;
         }
         Object o;
         try {
            o = ctx.lookup(name);
            // String o = item.getNameInNamespace();
            if (o instanceof Context) {
               listContext(ctxPath, (Context) o, visited, q, t);
            }
            if (o instanceof Queue) {
               log.debug("   It's a Queue");
               Queue oq = (Queue) o;
               q.add(new QueueData(oq.getQueueName()));
            }
            if (o instanceof Topic) {
               log.debug("   It's a Topic");
               Topic ot = (Topic) o;
               t.add(new TopicData(ot.getTopicName()));
            }
         } catch (Throwable e) {
            log.debug("   !!! Exception when processing class '{}' : {}", className, e.getMessage());
            continue;
         }
      }
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }

}

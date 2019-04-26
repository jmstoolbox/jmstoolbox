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
package org.titou10.jtb.qm.solace;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.DestinationData;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

import com.solacesystems.jms.SupportedProperty;

/**
 * 
 * Implements Solace
 * 
 * @author Denis Forveille
 *
 */
public class SolaceQManager extends QManager {

   private static final org.slf4j.Logger log                            = LoggerFactory.getLogger(SolaceQManager.class);

   private static final String           SOLJMS_INITIAL_CONTEXT_FACTORY = "com.solacesystems.jndi.SolJNDIInitialContextFactory";

   private final Map<Integer, Session>   sessionJMSs                    = new HashMap<>();
   private final Map<Integer, Context>   jndiContexts                   = new HashMap<>();

   public SolaceQManager() {
      // TODO Auto-generated constructor stub
   }

   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {

      String jndiProviderURL = "smf://oc-node2.denis.prive:55555";
      String cfJNDIName = "/jms/cf/default";

      Hashtable<String, Object> env = new Hashtable<>();
      env.put(InitialContext.INITIAL_CONTEXT_FACTORY, SOLJMS_INITIAL_CONTEXT_FACTORY);
      env.put(InitialContext.PROVIDER_URL, jndiProviderURL);
      env.put(Context.SECURITY_PRINCIPAL, sessionDef.getActiveUserid());
      env.put(Context.SECURITY_CREDENTIALS, sessionDef.getActivePassword());
      // env.put(Context.SECURITY_PRINCIPAL, "default");
      // env.put(Context.SECURITY_CREDENTIALS, "default");

      env.put(SupportedProperty.SOLACE_JMS_SSL_VALIDATE_CERTIFICATE, false);

      // JMS Connections

      InitialContext ctx = new InitialContext(env);
      ConnectionFactory cf = (ConnectionFactory) ctx.lookup(cfJNDIName);

      Connection jmsConnection = cf.createConnection();
      jmsConnection.setClientID(clientID);
      jmsConnection.start();

      // Admin Objects

      Session sessionJMS = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      // Store per connection related data
      Integer hash = jmsConnection.hashCode();
      sessionJMSs.put(hash, sessionJMS);
      jndiContexts.put(hash, ctx);

      return jmsConnection;
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      log.debug("close connection {}", jmsConnection);

      Integer hash = jmsConnection.hashCode();
      Session sessionJMS = sessionJMSs.get(hash);

      if (sessionJMS != null) {
         try {
            sessionJMS.close();
         } catch (Exception e) {
            log.warn("Exception occurred while closing sessionJMS. Ignore it. Msg={}", e.getMessage());
         }
         sessionJMSs.remove(hash);
      }

      try {
         jmsConnection.close();
      } catch (Exception e) {
         log.warn("Exception occurred while closing jmsConnection. Ignore it. Msg={}", e.getMessage());
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

      // listContext(null, ctx, new HashSet<String>(), listQueueData, listTopicData);
      listQueueData.add(new QueueData("Test_Queue"));
      listTopicData.add(new TopicData("Test_Topic"));

      return new DestinationData(listQueueData, listTopicData);
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

         // if (BYPASS_CLASS_NAMES.indexOf(className) != -1) {
         // log.debug(" bypass");
         // continue;
         // }
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

}

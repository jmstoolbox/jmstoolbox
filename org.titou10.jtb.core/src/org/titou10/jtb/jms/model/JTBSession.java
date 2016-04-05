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
package org.titou10.jtb.jms.model;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.MetaQManager;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.QManager;

/**
 * 
 * A JTBSession represents a Session to a Q Manager, keep a collection of JTBConnections per client type to this Q Manager
 * 
 * @author Denis Forveille
 * 
 */
public class JTBSession implements JTBObject, Comparable<JTBSession> {

   private static final Logger                      log = LoggerFactory.getLogger(JTBSession.class);

   // Session config definition
   private SessionDef                               sessionDef;
   private MetaQManager                             mqm;
   private QManager                                 qm;

   // JTBConnection per client type
   private Map<JTBSessionClientType, JTBConnection> jtbConnections;

   // ------------------------
   // Constructor
   // ------------------------

   public JTBSession(SessionDef sessionDef, MetaQManager mqm) {
      this.sessionDef = sessionDef;

      jtbConnections = new HashMap<>();

      updateMetaQManager(mqm);
   }

   // ----------------------------
   // Manage Connections
   // ----------------------------
   public JTBConnection getJTBConnection(JTBSessionClientType jtbSessionClientType) {
      JTBConnection jtbConnection = jtbConnections.get(jtbSessionClientType);
      if (jtbConnection == null) {
         jtbConnection = new JTBConnection(jtbSessionClientType, sessionDef, mqm.getQmanager(), sessionDef.getDestinationFilter());
         jtbConnections.put(jtbSessionClientType, jtbConnection);
      }
      return jtbConnection;
   }

   public void disconnectAll() {
      log.debug("disconnectAll for '{}'", getName());
      for (Map.Entry<JTBSessionClientType, JTBConnection> e : jtbConnections.entrySet()) {
         JTBConnection jtbConnection = e.getValue();
         if (jtbConnection.isConnected()) {
            try {
               jtbConnection.connectOrDisconnect();
            } catch (Exception ex) {
               log.warn("Exception occurred when disconnecting '{}' for '{}'", jtbConnection.getSessionName(), e.getKey(), ex);
            }
         }
      }
   }

   // ----------------------------
   // Comparable
   // ----------------------------

   @Override
   public int compareTo(JTBSession o) {
      return this.sessionDef.getName().compareTo(o.getName());
   }

   // ------------------------
   // Helpers
   // ------------------------

   public void updateMetaQManager(MetaQManager mqm) {
      this.mqm = mqm;
      this.qm = mqm.getQmanager();
      for (JTBConnection jtbConnection : jtbConnections.values()) {
         jtbConnection.setQm(this.qm);
      }
   }

   public Boolean isConnectable() {
      return (qm != null);
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(256);
      builder.append("JTBSession [name=");
      builder.append(sessionDef.getName());
      builder.append("]");
      return builder.toString();
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   @Override
   public String getName() {
      return sessionDef.getName();
   }

   public QManager getQm() {
      return qm;
   }

   public SessionDef getSessionDef() {
      return sessionDef;
   }

   public MetaQManager getMqm() {
      return mqm;
   }

}

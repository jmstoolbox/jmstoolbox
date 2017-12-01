/* Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>. */
package org.titou10.jtb.jms.qm;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.jms.Connection;
import javax.jms.JMSException;

import org.titou10.jtb.config.gen.Properties;
import org.titou10.jtb.config.gen.Properties.Property;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.model.JTBObject;

/**
 * Interface for Q Managers
 * 
 * Must be implemented by plugins that provide a Q Manager implementation
 * 
 * @author Denis Forveille
 * 
 */
public abstract class QManager implements JTBObject, Comparable<QManager> {

   private String name;

   // ------------------------
   // Constructor
   // ------------------------
   public QManager() {
      // Required by Eclipse RCP Extension mecanism
   }

   // ------------------------
   // Name
   // ------------------------

   @Override
   public final String getName() {
      return this.name;
   }

   public final void setName(String name) {
      this.name = name;
   }

   // ------------------------
   // Business Contract
   // ------------------------

   // Connection related
   public abstract ConnectionData connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception;

   public abstract void close(Connection jmsConnection) throws JMSException;

   // Destination related
   @SuppressWarnings("unused")
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {
      return null;
   }

   @SuppressWarnings("unused")
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {
      return Collections.emptyMap();
   }

   @SuppressWarnings("unused")
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {
      return Collections.emptyMap();
   }

   // Q provider related
   public List<QManagerProperty> getQManagerProperties() {
      return Collections.emptyList();
   }

   public String getHelpText() {
      return null;
   }

   // Allows to define up to 3 host:port combinations
   public boolean supportsMultipleHosts() {
      return false;
   }

   // UniversalMessaging shows JMSMessagesID with the "ID:" prefix but does not store it internally
   // It has to be removed for selectors
   public boolean mustRemoveIDFromJMSMessageID() {
      return false;
   }

   // UniversalMessaging does not accept manual acknoledge on remove (?)
   public boolean manulAcknoledge() {
      return true;
   }

   // -------------------------
   // Comparator
   // -------------------------

   @Override
   public int compareTo(QManager qm) {
      return this.name.compareTo(qm.getName());
   }

   // ---------
   // Utilities
   // ---------

   protected Map<String, String> extractProperties(SessionDef sessionDef) {
      List<Properties.Property> p = sessionDef.getProperties().getProperty();
      Map<String, String> mapProperties = new HashMap<>(p.size());
      for (Property property : p) {
         mapProperties.put(property.getName(), property.getValue());
      }
      return mapProperties;
   }

   private SortedMap<Object, Object> systemProperties;

   protected void saveSystemProperties() {
      systemProperties = new TreeMap<>(System.getProperties());
   }

   protected void restoreSystemProperties() {
      for (Object o : new ConcurrentSkipListSet<>(System.getProperties().keySet())) {
         System.clearProperty((String) o);
      }
      for (Entry<Object, Object> e : systemProperties.entrySet()) {
         System.setProperty((String) e.getKey(), (String) e.getValue());
      }
   }
}

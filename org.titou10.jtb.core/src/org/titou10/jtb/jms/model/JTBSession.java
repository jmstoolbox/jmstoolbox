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
package org.titou10.jtb.jms.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.eclipse.jface.preference.PreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.config.MetaQManager;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.model.JTBQueue.JTBQueueComparator;
import org.titou10.jtb.jms.model.JTBTopic.JTBTopicComparator;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.util.Constants;

/**
 * 
 * A JTBSession represents a JMS Connection to a Q Manager
 * 
 * @author Denis Forveille
 * 
 */
public class JTBSession implements JTBObject, Comparable<JTBSession> {

   private static final Logger log                  = LoggerFactory.getLogger(JTBSession.class);

   private static final String UNKNOWN              = "Unknown";

   // JMS Provider Information
   private Connection          jmsConnection;
   private Session             jmsSession;
   private Boolean             connected;

   // Connection Metadata
   private String              metaJMSVersion       = UNKNOWN;
   private String              metaJMSProviderName  = UNKNOWN;
   private List<String>        metaJMSPropertyNames = new ArrayList<>(16);
   private String              metaProviderVersion  = UNKNOWN;

   // Session config definition
   private SessionDef          sessionDef;

   // JTBObject for display
   private MetaQManager        mdqm;
   private QManager            qm;
   private String              name;

   // Children
   private SortedSet<JTBQueue> jtbQueues;
   private SortedSet<JTBTopic> jtbTopics;

   // ------------------------
   // Constructor
   // ------------------------

   public JTBSession(SessionDef sessionDef, MetaQManager mdqm) {
      this.sessionDef = sessionDef;
      this.mdqm = mdqm;

      this.name = sessionDef.getName();
      this.jtbQueues = new TreeSet<>(new JTBQueueComparator());
      this.jtbTopics = new TreeSet<>(new JTBTopicComparator());

      this.qm = mdqm.getQmanager();

      this.connected = false;
   }

   // ----------------------------
   // Comparable
   // ----------------------------

   @Override
   public int compareTo(JTBSession o) {
      return this.name.compareTo(o.getName());
   }

   // ------------------------
   // Helpers
   // ------------------------

   public Boolean isConnectable() {
      return (qm != null);
   }

   public Boolean isConnected() {
      return connected;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("JTBSession [connected=");
      builder.append(connected);
      builder.append(", name=");
      builder.append(name);
      builder.append("]");
      return builder.toString();
   }

   // ------------------------
   // Actions
   // ------------------------
   public void connectOrDisconnect() throws Exception {
      if (this.isConnected()) {
         disConnect();
      } else {
         connect();
      }
   }

   // ----------------
   // JMS Interactions
   // ----------------
   public Message createJMSMessage(JTBMessageType jtbMessageType) throws JMSException {
      log.debug("createJMSMessage {}", jtbMessageType);
      switch (jtbMessageType) {
         case TEXT:
            return jmsSession.createTextMessage();

         case BYTES:
            return jmsSession.createBytesMessage();

         case MESSAGE:
            return jmsSession.createMessage();

         case MAP:
            return jmsSession.createMapMessage();

         case OBJECT:
            return jmsSession.createObjectMessage();

         case STREAM:
            return jmsSession.createStreamMessage();
      }
      return null; // Impossible
   }

   public void removeMessage(JTBMessage jtbMessage) throws JMSException {
      log.debug("Remove Message {}", jtbMessage);

      Message message = jtbMessage.getJmsMessage();
      JTBDestination jtbDestination = jtbMessage.getJtbDestination();

      StringBuilder sb = new StringBuilder(128);
      sb.append("JMSMessageID='");
      sb.append(message.getJMSMessageID());
      sb.append("'");

      try (MessageConsumer consumer = jmsSession.createConsumer(jtbDestination.getJmsDestination(), sb.toString());) {
         message = consumer.receiveNoWait();
      }

      jmsSession.commit();
   }

   public Integer emptyQueue(JTBQueue jtbQueue) throws JMSException {
      Message message = null;
      Integer nb = 0;
      try (MessageConsumer consumer = jmsSession.createConsumer(jtbQueue.getJmsQueue());) {
         do {
            message = consumer.receiveNoWait();
            if (message != null) {
               message.acknowledge();
               nb++;
            }
         } while (message != null);
      }
      jmsSession.commit();

      return nb;
   }

   public void sendMessage(JTBMessage jtbMessage) throws JMSException {
      Message m = jtbMessage.getJmsMessage();
      Destination d = jtbMessage.getJtbDestination().getJmsDestination();

      try (MessageProducer p = jmsSession.createProducer(d);) {
         p.setPriority(m.getJMSPriority());
         p.setDeliveryMode(m.getJMSDeliveryMode());
         p.setTimeToLive(m.getJMSExpiration());
         p.send(m);
      }

      jmsSession.commit();
   }

   public List<JTBMessage> browseQueue(JTBQueue jtbQueue, int maxMessages) throws JMSException {

      int limit = Integer.MAX_VALUE;
      if (maxMessages != 0) {
         limit = maxMessages - 2;
      }

      List<JTBMessage> jtbMessages = new ArrayList<>(128);
      try (QueueBrowser browser = jmsSession.createBrowser(jtbQueue.getJmsQueue());) {
         int n = 0;
         Enumeration<?> msgs = browser.getEnumeration();
         while (msgs.hasMoreElements()) {
            Message tempMsg = (Message) msgs.nextElement();
            jtbMessages.add(new JTBMessage(jtbQueue, tempMsg));
            if (n++ > limit) {
               break;
            }
         }
      }

      return jtbMessages;
   }

   public List<JTBMessage> searchQueue(JTBQueue jtbQueue, String searchString, int maxMessages) throws JMSException {

      int limit = Integer.MAX_VALUE;
      if (maxMessages != 0) {
         limit = maxMessages - 2;
      }

      List<JTBMessage> jtbMessages = new ArrayList<>(128);
      try (QueueBrowser browser = jmsSession.createBrowser(jtbQueue.getJmsQueue());) {
         int n = 0;
         Enumeration<?> msgs = browser.getEnumeration();
         while (msgs.hasMoreElements()) {
            Message tempMsg = (Message) msgs.nextElement();
            if (tempMsg instanceof TextMessage) {
               String text = ((TextMessage) tempMsg).getText();
               if (text.contains(searchString)) {
                  jtbMessages.add(new JTBMessage(jtbQueue, tempMsg));
                  if (n++ > limit) {
                     break;
                  }
               }
            }
         }
      }

      return jtbMessages;
   }

   public List<JTBMessage> browseQueueWithSelector(JTBQueue jtbQueue, String searchString, int maxMessages) throws JMSException {

      int limit = Integer.MAX_VALUE;
      if (maxMessages != 0) {
         limit = maxMessages - 2;
      }

      List<JTBMessage> jtbMessages = new ArrayList<>(64);
      try (QueueBrowser browser = jmsSession.createBrowser(jtbQueue.getJmsQueue(), searchString);) {
         int n = 0;
         Enumeration<?> msgs = browser.getEnumeration();
         while (msgs.hasMoreElements()) {
            Message tempMsg = (Message) msgs.nextElement();
            jtbMessages.add(new JTBMessage(jtbQueue, tempMsg));
            if (n++ > limit) {
               break;
            }
         }
      }

      return jtbMessages;
   }

   // ------------------------
   // Helpers
   // ------------------------
   @SuppressWarnings("unchecked")
   private void connect() throws Exception {
      log.debug("connect '{}'", this);

      PreferenceStore ps = ConfigManager.getPreferenceStore2();
      boolean showSystemObject = ps.getBoolean(Constants.PREF_SHOW_SYSTEM_OBJECTS);

      jmsConnection = qm.connect(sessionDef, showSystemObject);

      // TODO: Do not active...cause problems with MQ
      // jmsConnection.setClientID("JMSToolBox");

      jmsConnection.start();

      jmsSession = jmsConnection.createSession(true, Session.AUTO_ACKNOWLEDGE);

      Queue jmsQ;
      SortedSet<String> qNames = qm.getQueueNames();
      if (qNames != null) {
         for (String qName : qNames) {
            jmsQ = jmsSession.createQueue(qName);
            jtbQueues.add(new JTBQueue(this, qName, jmsQ));
         }
      }

      Topic jmsTopic;
      SortedSet<String> tNames = qm.getTopicNames();
      if (tNames != null) {
         for (String tName : tNames) {
            jmsTopic = jmsSession.createTopic(tName);
            jtbTopics.add(new JTBTopic(this, tName, jmsTopic));
         }
      }

      // Connection MetadaData
      ConnectionMetaData meta = jmsConnection.getMetaData();
      metaJMSProviderName = meta.getJMSProviderName();
      metaProviderVersion = meta.getProviderVersion();
      metaJMSVersion = meta.getJMSVersion();
      metaJMSPropertyNames = Collections.list(meta.getJMSXPropertyNames());
      Collections.sort(metaJMSPropertyNames);

      connected = true;
   }

   private void disConnect() throws JMSException {
      log.debug("disconnect : '{}'", this);

      try {
         jmsConnection.stop();
         qm.close(jmsConnection);
      } catch (Exception e) {
         log.warn("Exception occured when disconnecting. Ignoring", e);
      }

      connected = false;

      jtbQueues.clear();
      jtbTopics.clear();
      metaJMSVersion = UNKNOWN;
      metaJMSProviderName = UNKNOWN;
      metaProviderVersion = UNKNOWN;
      metaJMSPropertyNames.clear();
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public String getName() {
      return name;
   }

   public SortedSet<JTBQueue> getJtbQueues() {
      return jtbQueues;
   }

   public SortedSet<JTBTopic> getJtbTopics() {
      return jtbTopics;
   }

   public QManager getQm() {
      return qm;
   }

   public SessionDef getSessionDef() {
      return sessionDef;
   }

   public String getMetaJMSVersion() {
      return metaJMSVersion;
   }

   public String getMetaJMSProviderName() {
      return metaJMSProviderName;
   }

   public String getMetaProviderVersion() {
      return metaProviderVersion;
   }

   public List<String> getMetaJMSPropertyNames() {
      return metaJMSPropertyNames;
   }

   public void setName(String name) {
      this.name = name;
   }

   public MetaQManager getMdqm() {
      return mdqm;
   }

}

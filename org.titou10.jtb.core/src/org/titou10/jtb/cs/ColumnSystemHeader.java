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
package org.titou10.jtb.cs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.util.JTBDeliveryMode;
import org.titou10.jtb.util.Utils;

/**
 * 
 * JMS Artefacts (JMS Header + other) considered as "System" data that can be displayed as a column in the Message Browser
 * 
 * @author Denis Forveille
 *
 */
public enum ColumnSystemHeader {

                                JMS_CORRELATION_ID(
                                                   "JMSCorrelationID",
                                                   "JMS Correlation ID",
                                                   JMSPropertyKind.STRING,
                                                   150,
                                                   true,
                                                   false),
                                JMS_DELIVERY_MODE("JMSDeliveryMode", "Delivery Mode", JMSPropertyKind.INT, 120, true, false),
                                JMS_DELIVERY_TIME("JMSDeliveryTime", "Delivery Time", JMSPropertyKind.LONG, 180, false, true),
                                JMS_DESTINATION("JMSDestination", "Destination", null, 200, false, false),
                                JMS_EXPIRATION("JMSExpiration", "Expiration", JMSPropertyKind.LONG, 180, true, true),
                                JMS_MESSAGE_ID("JMSMessageID", "ID", JMSPropertyKind.STRING, 200, true, false),
                                JMS_PRIORITY("JMSPriority", "Priority", JMSPropertyKind.INT, 60, true, false),
                                JMS_REDELIVERED("JMSRedelivered", "Redelivered", JMSPropertyKind.BOOLEAN, 60, true, false),
                                JMS_REPLY_TO("JMSReplyTo", "Reply To", null, 200, false, false),
                                JMS_TIMESTAMP("JMSTimestamp", "JMS Timestamp", JMSPropertyKind.LONG, 180, true, true),
                                JMS_TYPE("JMSType", "JMS Type", JMSPropertyKind.STRING, 100, true, false),

                                MESSAGE_TYPE("Message Class", "Type", JMSPropertyKind.STRING, 60, false, false);

   public static final Logger                            log         = LoggerFactory.getLogger(ColumnSystemHeader.class);

   private String                                        headerName;
   private String                                        displayName;
   private int                                           displayWidth;
   private boolean                                       selector;
   private boolean                                       timestamp;
   private JMSPropertyKind                               jmsPropertyKind;

   // Map for performance: ColumnSystemHeader.getHeaderName().hashCode() -> ColumnSystemHeader
   private static final Map<Integer, ColumnSystemHeader> MAP_CSH     = new HashMap<>();

   private static final Map<String, ColumnSystemHeader>  MAP_CSH_JMS = new LinkedHashMap<>();;

   // -----------
   // Constructor
   // -----------

   private ColumnSystemHeader(String headerName,
                              String displayName,
                              JMSPropertyKind jmsPropertyKind,
                              int displayWidth,
                              boolean selector,
                              boolean timestamp) {
      this.headerName = headerName;
      this.displayName = displayName;
      this.jmsPropertyKind = jmsPropertyKind;
      this.displayWidth = displayWidth;
      this.selector = selector;
      this.timestamp = timestamp;
   }

   static {
      MAP_CSH.putAll(Arrays.stream(values()).collect(Collectors.toMap(csh -> csh.getHeaderName().hashCode(), csh -> csh)));

      MAP_CSH_JMS.put("JMSCorrelationID", ColumnSystemHeader.JMS_CORRELATION_ID);
      MAP_CSH_JMS.put("JMSMessageID", ColumnSystemHeader.JMS_MESSAGE_ID);
      MAP_CSH_JMS.put("JMSType", ColumnSystemHeader.JMS_TYPE);
      MAP_CSH_JMS.put("JMSDeliveryMode", ColumnSystemHeader.JMS_DELIVERY_MODE);
      MAP_CSH_JMS.put("JMSDestination", ColumnSystemHeader.JMS_DESTINATION);
      MAP_CSH_JMS.put("JMSDeliveryTime", ColumnSystemHeader.JMS_DELIVERY_TIME);
      MAP_CSH_JMS.put("JMSExpiration", ColumnSystemHeader.JMS_EXPIRATION);
      MAP_CSH_JMS.put("JMSPriority", ColumnSystemHeader.JMS_PRIORITY);
      MAP_CSH_JMS.put("JMSRedelivered", ColumnSystemHeader.JMS_REDELIVERED);
      MAP_CSH_JMS.put("JMSReplyTo", ColumnSystemHeader.JMS_REPLY_TO);
      MAP_CSH_JMS.put("JMSTimestamp", ColumnSystemHeader.JMS_TIMESTAMP);
   }

   // ----------------
   // Helpers
   // ----------------
   public static ColumnSystemHeader fromHeaderName(String columnSystemHeaderName) {
      return MAP_CSH.get(columnSystemHeaderName.hashCode());
   }

   public static boolean isSelector(String columnSystemHeaderName) {
      ColumnSystemHeader csh = MAP_CSH.get(columnSystemHeaderName.hashCode());
      return csh == null ? true : csh.isSelector();
   }

   public static boolean isTimestamp(String columnSystemHeaderName) {
      ColumnSystemHeader csh = MAP_CSH.get(columnSystemHeaderName.hashCode());
      return csh == null ? false : csh.isTimestamp();
   }

   public static Map<String, ColumnSystemHeader> getJMSColumnSystemHeader() {
      return MAP_CSH_JMS;
   }

   public Object getColumnSystemValue(Message m) {
      return getColumnSystemValue(m, false, true);
   }

   /**
    * 
    * @param m
    *           JMS Message
    * @param trueValue
    *           return the value directtly from the Message
    * @param timeStampsWithLong
    *           if trueValue = false, format the long value of timestamps with the human readable form
    * @return
    */
   public Object getColumnSystemValue(Message m, boolean trueValue, boolean timeStampsWithLong) {

      // DF: could probably better be implemented via a java 8 Function<>

      try {

         switch (this) {
            case JMS_CORRELATION_ID:
               return m.getJMSCorrelationID() == null ? "" : m.getJMSCorrelationID();

            case JMS_DELIVERY_MODE:
               return trueValue ? m.getJMSDeliveryMode()
                        : Utils.formatJTBDeliveryMode(JTBDeliveryMode.fromValue(m.getJMSDeliveryMode()));

            case JMS_DELIVERY_TIME:
               try {
                  return trueValue ? m.getJMSDeliveryTime()
                           : Utils.formatTimestamp(m.getJMSDeliveryTime(), timeStampsWithLong).toString();
               } catch (Throwable t) {
                  // JMS 2.0+ only..
                  return "";
               }
            case JMS_DESTINATION:
               return m.getJMSDestination() == null ? "" : m.getJMSDestination().toString();

            case JMS_EXPIRATION:
               return trueValue ? m.getJMSExpiration() : Utils.formatTimestamp(m.getJMSExpiration(), timeStampsWithLong);

            case JMS_MESSAGE_ID:
               return m.getJMSMessageID() == null ? "" : m.getJMSMessageID();

            case JMS_PRIORITY:
               return Integer.valueOf(m.getJMSPriority());

            case JMS_REDELIVERED:
               return Boolean.valueOf(m.getJMSRedelivered());

            case JMS_REPLY_TO:
               return m.getJMSReplyTo() == null ? "" : m.getJMSReplyTo().toString();

            case JMS_TIMESTAMP:
               return trueValue ? m.getJMSTimestamp() : Utils.formatTimestamp(m.getJMSTimestamp(), timeStampsWithLong);

            case JMS_TYPE:
               return m.getJMSType() == null ? "" : m.getJMSType();

            case MESSAGE_TYPE:
               return JTBMessageType.fromJMSMessage(m).getDescription();
         }
      } catch (JMSException e) {
         log.warn("JMSException occured when reading JMS header '{}' : {}", this.getHeaderName(), e.getMessage());
      }
      return "";

   }

   // ----------------
   // Standard Getters
   // ----------------
   public String getHeaderName() {
      return headerName;
   }

   public String getDisplayName() {
      return displayName;
   }

   public int getDisplayWidth() {
      return displayWidth;
   }

   public boolean isSelector() {
      return selector;
   }

   public boolean isTimestamp() {
      return timestamp;
   }

   public JMSPropertyKind getJmsPropertyKind() {
      return jmsPropertyKind;
   }

}

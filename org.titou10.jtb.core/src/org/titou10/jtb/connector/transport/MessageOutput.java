/*
 * Copyright (C) 2015-2022 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.connector.transport;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlInlineBinaryData;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.util.JTBDeliveryMode;
import org.titou10.jtb.util.Utils;

/**
 * Message exposed to an External Connector
 *
 * @author Denis Forveille
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@XmlType(propOrder = { "type" })
public class MessageOutput implements Serializable {
   private static final long   serialVersionUID = 1L;

   @XmlElement(name = "type")
   private JTBMessageType      jtbMessageType;

   private String              jmsMessageID;
   private Integer             jmsPriority;
   private String              jmsDestination;
   private String              jmsReplyTo;
   private String              jmsType;
   private String              jmsCorrelationID;
   private JTBDeliveryMode     jmsDeliveryMode;
   private String              jmsDeliveryTime;
   private Long                jmsExpiration;
   private String              jmsTimestamp;
   private boolean             jmsRedelivered;

   // Properties
   private Map<String, String> properties;

   // Payload
   private String              payloadText;
   private Map<String, Object> payloadMap;
   @XmlInlineBinaryData
   private byte[]              payloadBytesBase64;

   // ------------
   // Constructors
   // ------------
   public MessageOutput() {
      // JSON-B
   }

   public MessageOutput(JTBMessage jtbMessage, byte[] plb) throws JMSException {
      Message message = jtbMessage.getJmsMessage();

      this.jmsMessageID = message.getJMSMessageID();
      this.jmsCorrelationID = message.getJMSCorrelationID();
      this.jmsPriority = message.getJMSPriority();
      this.jmsType = message.getJMSType();
      this.jmsRedelivered = message.getJMSRedelivered();
      this.jmsDestination = message.getJMSDestination().toString();

      this.jmsDeliveryMode = jtbMessage.getDeliveryMode();
      this.jtbMessageType = jtbMessage.getJtbMessageType();
      this.jmsReplyTo = jtbMessage.getReplyToDestinationName();

      if (message.getJMSTimestamp() != 0) {
         this.jmsTimestamp = Utils.formatTimestamp(message.getJMSTimestamp(), false);
      }

      if (message.getJMSExpiration() != 0) {
         this.jmsExpiration = message.getJMSExpiration();
      }

      try {
         if (message.getJMSDeliveryTime() != 0) {
            this.jmsDeliveryTime = Utils.formatTimestamp(message.getJMSDeliveryTime(), false);
         }
      } catch (Throwable t) {
         // JMS 2.0+ only..
      }

      switch (jtbMessageType) {
         case TEXT:
            TextMessage tm = (TextMessage) message;
            if (tm.getText() != null) {
               this.payloadText = tm.getText();
            }
            break;

         case BYTES:
            if (plb != null) {
               payloadBytesBase64 = plb;
            } else {
               BytesMessage bm = (BytesMessage) message;
               payloadBytesBase64 = new byte[(int) bm.getBodyLength()];
               bm.reset();
               bm.readBytes(payloadBytesBase64);
            }
            break;

         case MESSAGE:
            break;

         case MAP:
            MapMessage mm = (MapMessage) message;
            payloadMap = new HashMap<>();

            @SuppressWarnings("rawtypes")
            Enumeration mapNames = mm.getMapNames();
            while (mapNames.hasMoreElements()) {
               String key = (String) mapNames.nextElement();
               payloadMap.put(key, mm.getObject(key));
            }

            break;

         case OBJECT:
         case STREAM:
            return;
      }

      // Properties: as for JMS specs, all properties can be read and written as strings: Great!
      properties = new TreeMap<>();
      @SuppressWarnings("unchecked")
      Enumeration<String> e = message.getPropertyNames();
      while (e.hasMoreElements()) {
         String key = e.nextElement();
         properties.put(key, message.getStringProperty(key));
      }
   }

   // -------------------------
   // Standard Getters/Setters
   // -------------------------

   public String getJmsMessageID() {
      return jmsMessageID;
   }

   public Integer getJmsPriority() {
      return jmsPriority;
   }

   public Map<String, String> getProperties() {
      return properties;
   }

   public String getJmsTimestamp() {
      return jmsTimestamp;
   }

   public String getJmsReplyTo() {
      return jmsReplyTo;
   }

   public String getJmsType() {
      return jmsType;
   }

   public String getJmsCorrelationID() {
      return jmsCorrelationID;
   }

   public Long getJmsExpiration() {
      return jmsExpiration;
   }

   public JTBDeliveryMode getJmsDeliveryMode() {
      return jmsDeliveryMode;
   }

   public void setJmsDeliveryMode(JTBDeliveryMode jmsDeliveryMode) {
      this.jmsDeliveryMode = jmsDeliveryMode;
   }

   public JTBMessageType getJtbMessageType() {
      return jtbMessageType;
   }

   public void setJtbMessageType(JTBMessageType jtbMessageType) {
      this.jtbMessageType = jtbMessageType;
   }

   public String getPayloadText() {
      return payloadText;
   }

   public Map<String, Object> getPayloadMap() {
      return payloadMap;
   }

   public String getJmsDeliveryTime() {
      return jmsDeliveryTime;
   }

   public byte[] getPayloadBytesBase64() {
      return payloadBytesBase64;
   }

   public boolean getJmsRedelivered() {
      return jmsRedelivered;
   }

   public String getJmsDestination() {
      return jmsDestination;
   }

}

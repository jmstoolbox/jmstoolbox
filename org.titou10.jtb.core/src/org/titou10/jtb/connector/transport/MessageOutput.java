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
package org.titou10.jtb.connector.transport;

import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.util.JMSDeliveryMode;
import org.titou10.jtb.util.Constants;

/**
 * Message Template
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
   private String              jmsReplyTo;
   private String              jmsType;
   private String              jmsCorrelationID;
   private Long                jmsExpiration;
   private JMSDeliveryMode     jmsDeliveryMode;
   private String              jmsTimestamp;

   // Properties
   private Map<String, String> properties;

   // Payload
   private String              payloadText;
   private Map<String, Object> payloadMap;
   @XmlInlineBinaryData
   private byte[]              payloadBytes;

   // ------------
   // Constructors
   // ------------
   public MessageOutput() {
   }

   public MessageOutput(JTBMessage jtbMessage, byte[] plb) throws JMSException {
      Message message = jtbMessage.getJmsMessage();

      this.jmsMessageID = message.getJMSMessageID();

      if (message.getJMSTimestamp() != 0) {
         Date d = new Date(message.getJMSTimestamp());
         this.jmsTimestamp = Constants.JMS_TIMESTAMP_SDF.format(d);
      }

      this.jmsCorrelationID = message.getJMSCorrelationID();
      this.jmsExpiration = message.getJMSExpiration();
      this.jmsPriority = message.getJMSPriority();
      this.jmsType = message.getJMSType();
      // this.jmsReplyTo=message.getJMSReplyTo();

      this.jmsDeliveryMode = jtbMessage.getJmsDeliveryMode();
      this.jtbMessageType = jtbMessage.getJtbMessageType();

      switch (jtbMessageType) {
         case TEXT:
            TextMessage tm = (TextMessage) message;
            if (tm.getText() != null) {
               this.payloadText = tm.getText();
            }
            break;

         case BYTES:
            if (plb != null) {
               payloadBytes = plb;
            } else {
               BytesMessage bm = (BytesMessage) message;
               payloadBytes = new byte[(int) bm.getBodyLength()];
               bm.reset();
               bm.readBytes(payloadBytes);
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
      properties = new HashMap<>();
      @SuppressWarnings("unchecked")
      Enumeration<String> e = message.getPropertyNames();
      while (e.hasMoreElements()) {
         String key = e.nextElement();
         // Do not store standard + Queue Manager properties
         if (!(key.startsWith("JMS"))) {
            properties.put(key, message.getStringProperty(key));
         }
      }
   }

   // -------------------------
   // Standard Getters/Setters
   // -------------------------
   public byte[] getPayloadBytes() {
      return payloadBytes;
   }

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

   public JMSDeliveryMode getJmsDeliveryMode() {
      return jmsDeliveryMode;
   }

   public void setJmsDeliveryMode(JMSDeliveryMode jmsDeliveryMode) {
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

}

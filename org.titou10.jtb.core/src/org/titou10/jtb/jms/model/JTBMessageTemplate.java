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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.util.JMSDeliveryMode;
import org.titou10.jtb.util.jaxb.Base64XmlAdapter;
import org.titou10.jtb.util.jaxb.MapPayloadXmlAdapter;
import org.titou10.jtb.util.jaxb.SerializableXmlAdapter;

/**
 * Message Template
 * 
 * @author Denis Forveille
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class JTBMessageTemplate implements Serializable {
   private static final long   serialVersionUID = 1L;

   private static final Logger log              = LoggerFactory.getLogger(JTBMessageTemplate.class);

   @XmlTransient
   private String              jmsMessageID;

   // Business data
   private Integer             jmsPriority;
   private String              jmsReplyTo;
   private String              jmsType;
   private String              jmsCorrelationID;
   private Long                jmsExpiration;
   private JMSDeliveryMode     jmsDeliveryMode;
   private Long                jmsTimestamp;

   private JTBMessageType      jtbMessageType;

   // Payload

   @XmlJavaTypeAdapter(Base64XmlAdapter.class)
   private String              payloadText;

   @XmlInlineBinaryData
   private byte[]              payloadBytes;

   @XmlJavaTypeAdapter(MapPayloadXmlAdapter.class)
   private Map<String, Object> payloadMap;

   @XmlJavaTypeAdapter(SerializableXmlAdapter.class)
   private Serializable        payloadObject;

   // Properties
   private Map<String, String> properties;

   // ------------
   // Constructors
   // ------------
   public JTBMessageTemplate() {
   }

   public JTBMessageTemplate(JTBMessage jtbMessage) throws JMSException {
      Message message = jtbMessage.getJmsMessage();

      this.jmsMessageID = message.getJMSMessageID();
      this.jmsTimestamp = message.getJMSTimestamp();

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
            this.payloadText = tm.getText();
            break;

         case BYTES:
            BytesMessage bm = (BytesMessage) message;
            payloadBytes = new byte[(int) bm.getBodyLength()];
            bm.reset();
            bm.readBytes(payloadBytes);
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
            ObjectMessage om = (ObjectMessage) message;
            payloadObject = om.getObject();
            break;

         case STREAM:
            // StreamMessage sm = (StreamMessage) message;
            log.warn("STREAM Message can not be transformed into JTBMessageTemplate");
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
   // Helper
   // -------------------------

   public void toJMSMessage(Message message) throws JMSException {
      switch (jtbMessageType) {
         case TEXT:
            TextMessage tm = (TextMessage) message;
            String txt = payloadText;
            if ((txt != null) && (txt.length() > 0)) {
               tm.setText(txt);
            }
            break;
         case BYTES:
            BytesMessage bm = (BytesMessage) message;
            byte[] b = payloadBytes;
            if ((b != null) && (b.length > 0)) {
               bm.writeBytes(b);
            }
            break;

         case MESSAGE:
            break;

         case MAP:
            MapMessage mm = (MapMessage) message;
            if (payloadMap != null) {
               for (Entry<String, Object> e : payloadMap.entrySet()) {
                  mm.setObject(e.getKey(), e.getValue());
               }
            }

            break;

         case OBJECT:
            ObjectMessage om = (ObjectMessage) message;
            if (payloadObject != null) {
               om.setObject(payloadObject);
            }
            break;

         case STREAM:
            // StreamMessage sm = (StreamMessage) message;
            log.warn("STREAM Message can not be transformed into JTBMessageTemplate");
            return;
      }

      if (jmsMessageID != null) {
         message.setJMSMessageID(jmsMessageID);
      }
      if (jmsTimestamp != null) {
         message.setJMSTimestamp(jmsTimestamp);
      }

      if (jmsCorrelationID != null) {
         message.setJMSCorrelationID(jmsCorrelationID);
      }
      message.setJMSDeliveryMode(jmsDeliveryMode.intValue());
      if (jmsExpiration != null) {
         message.setJMSExpiration(jmsExpiration);
      }
      message.setJMSPriority(jmsPriority);
      // m.setJMSReplyTo(jtbMessage.getReplyTo()); TODO Doit etre une destination..
      if (jmsType != null) {
         message.setJMSType(jmsType);
      }

      for (Map.Entry<String, String> property : properties.entrySet()) {
         message.setStringProperty(property.getKey(), property.getValue());
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

   public void setJmsMessageID(String jmsMessageID) {
      this.jmsMessageID = jmsMessageID;
   }

   public Integer getJmsPriority() {
      return jmsPriority;
   }

   public Map<String, String> getProperties() {
      return properties;
   }

   public Long getJmsTimestamp() {
      return jmsTimestamp;
   }

   public void setJmsTimestamp(Long jmsTimestamp) {
      this.jmsTimestamp = jmsTimestamp;
   }

   public void setProperties(Map<String, String> properties) {
      this.properties = properties;
   }

   public void setJmsPriority(Integer jmsPriority) {
      this.jmsPriority = jmsPriority;
   }

   public String getJmsReplyTo() {
      return jmsReplyTo;
   }

   public void setJmsReplyTo(String jmsReplyTo) {
      this.jmsReplyTo = jmsReplyTo;
   }

   public String getJmsType() {
      return jmsType;
   }

   public void setJmsType(String jmsType) {
      this.jmsType = jmsType;
   }

   public String getJmsCorrelationID() {
      return jmsCorrelationID;
   }

   public void setJmsCorrelationID(String jmsCorrelationID) {
      this.jmsCorrelationID = jmsCorrelationID;
   }

   public Long getJmsExpiration() {
      return jmsExpiration;
   }

   public void setJmsExpiration(Long jmsExpiration) {
      this.jmsExpiration = jmsExpiration;
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

   public void setPayloadText(String payloadText) {
      this.payloadText = payloadText;
   }

   public void setPayloadBytes(byte[] payloadBytes) {
      this.payloadBytes = payloadBytes;
   }

   public Map<String, Object> getPayloadMap() {
      return payloadMap;
   }

   public void setPayloadMap(Map<String, Object> payloadMap) {
      this.payloadMap = payloadMap;
   }

   public Serializable getPayloadObject() {
      return payloadObject;
   }

   public void setPayloadObject(Serializable payloadObject) {
      this.payloadObject = payloadObject;
   }

}

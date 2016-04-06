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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import org.titou10.jtb.jms.util.JTBDeliveryMode;
import org.titou10.jtb.util.Utils;
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

   private static final String CR               = "\n";
   private static final int    DEFAULT_PRIORITY = 4;

   // Business data
   private String              jmsReplyTo;
   private String              jmsType;
   private String              jmsCorrelationID;

   // Message Read Only Attributes
   @XmlTransient
   private String              jmsMessageID;
   @XmlTransient
   private Long                jmsTimestamp;
   @XmlTransient
   private Long                jmsDeliveryTime;
   @XmlTransient
   private Long                jmsExpiration;

   // Attributes not related to Messages but to MessageProducer
   private JTBDeliveryMode     deliveryMode;
   private Integer             priority;
   private Long                timeToLive;
   private Long                deliveryDelay;                                                       // JMS 2.0

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

      this.jtbMessageType = jtbMessage.getJtbMessageType();
      this.deliveryMode = jtbMessage.getDeliveryMode();
      this.priority = jtbMessage.getPriority();
      this.timeToLive = jtbMessage.getTimeToLive();
      this.deliveryDelay = jtbMessage.getDeliveryDelay();

      this.jmsCorrelationID = message.getJMSCorrelationID();
      this.jmsType = message.getJMSType();
      // this.jmsReplyTo=message.getJMSReplyTo();

      // Read Only
      this.jmsMessageID = message.getJMSMessageID();
      this.jmsTimestamp = message.getJMSTimestamp();
      this.jmsExpiration = message.getJMSExpiration();
      try {
         this.jmsDeliveryTime = message.getJMSDeliveryTime();
      } catch (Throwable t) {
         // JMS 2.0
      }

      switch (jtbMessageType) {
         case TEXT:
            TextMessage tm = (TextMessage) message;
            if (tm.getText() != null) {
               this.payloadText = tm.getText();
            }
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
            try {
               payloadObject = om.getObject();
            } catch (JMSException e) {
               StringBuilder sb = new StringBuilder(512);
               log.error("A JMSException occurred when reading Object Payload: {}", e.getMessage());

               sb.append("An exception occured while reading the ObjectMessage payload.");
               sb.append(CR).append(CR);
               sb.append("JMSToolBox needs to know the implementation of the class of the Object stored in the OnjectMessage in order to manage those messages.");
               sb.append(CR).append(CR);
               sb.append("Consider adding the implementation class of the Object stored in the ObjectMessage to the Q Manager configuration jars.");
               sb.append(CR).append(CR);
               sb.append("Cause: ").append(Utils.getCause(e).getMessage());
               throw new JMSException(sb.toString());
            }
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
   public JTBMessage toJTBMessage(JTBDestination jtbDestination, Message jmsMessage) throws JMSException {

      // Set JTBMessage Properties
      JTBMessage jtbMessage = new JTBMessage(jtbDestination, jmsMessage);
      jtbMessage.setDeliveryDelay(this.deliveryDelay);
      jtbMessage.setDeliveryMode(this.deliveryMode);
      jtbMessage.setJmsMessage(jmsMessage);
      jtbMessage.setJtbDestination(jtbDestination);
      jtbMessage.setJtbMessageType(this.jtbMessageType);
      jtbMessage.setTimeToLive(this.timeToLive);
      jtbMessage.setPriority(getPriority());

      // Set JMS Message Properties
      if (this.jmsType != null) {
         jmsMessage.setJMSType(this.jmsType);
      }
      if (this.jmsCorrelationID != null) {
         jmsMessage.setJMSCorrelationID(this.jmsCorrelationID);
      }

      for (Map.Entry<String, String> property : this.properties.entrySet()) {
         jmsMessage.setStringProperty(property.getKey(), property.getValue());
      }

      switch (jtbMessageType) {
         case TEXT:
            TextMessage tm = (TextMessage) jmsMessage;
            String txt = payloadText;
            if ((txt != null) && (txt.length() > 0)) {
               tm.setText(txt);
            }
            break;
         case BYTES:
            BytesMessage bm = (BytesMessage) jmsMessage;
            byte[] b = payloadBytes;
            if ((b != null) && (b.length > 0)) {
               bm.writeBytes(b);
            }
            break;

         case MESSAGE:
            break;

         case MAP:
            MapMessage mm = (MapMessage) jmsMessage;
            if (payloadMap != null) {
               for (Entry<String, Object> e : payloadMap.entrySet()) {
                  mm.setObject(e.getKey(), e.getValue());
               }
            }

            break;

         case OBJECT:
            ObjectMessage om = (ObjectMessage) jmsMessage;
            if (payloadObject != null) {
               om.setObject(payloadObject);
            }
            break;

         case STREAM:
            // StreamMessage sm = (StreamMessage) message;
            log.warn("STREAM Message can not be transformed into JTBMessageTemplate");
            break;
      }

      return jtbMessage;

   }

   public static JTBMessageTemplate deepClone(JTBMessageTemplate object) {
      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(object);
         ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
         ObjectInputStream ois = new ObjectInputStream(bais);
         return (JTBMessageTemplate) ois.readObject();
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public Integer getPriority() {
      // DF: This to handle "old" templates where the priority was held in the JMSPriority field
      if (this.priority == null) {
         this.priority = DEFAULT_PRIORITY;
      }
      return priority;
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

   public Map<String, String> getProperties() {
      return properties;
   }

   public Long getJmsTimestamp() {
      return jmsTimestamp;
   }

   public void setProperties(Map<String, String> properties) {
      this.properties = properties;
   }

   public String getJmsReplyTo() {
      return jmsReplyTo;
   }

   public void setDeliveryMode(JTBDeliveryMode deliveryMode) {
      this.deliveryMode = deliveryMode;
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

   public JTBDeliveryMode getDeliveryMode() {
      return deliveryMode;
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

   public void setPriority(Integer priority) {
      this.priority = priority;
   }

   public Long getTimeToLive() {
      return timeToLive;
   }

   public void setTimeToLive(Long timeToLive) {
      this.timeToLive = timeToLive;
   }

   public Long getDeliveryDelay() {
      return deliveryDelay;
   }

   public void setDeliveryDelay(Long deliveryDelay) {
      this.deliveryDelay = deliveryDelay;
   }

   public Long getJmsDeliveryTime() {
      return jmsDeliveryTime;
   }

   public Long getJmsExpiration() {
      return jmsExpiration;
   }

}

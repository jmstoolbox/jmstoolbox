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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlInlineBinaryData;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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

   // Business data
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
   private String              replyToDestinationName;

   // Payload

   @XmlJavaTypeAdapter(Base64XmlAdapter.class)
   private String              payloadText;

   @XmlInlineBinaryData
   private byte[]              payloadBytes;

   @XmlJavaTypeAdapter(MapPayloadXmlAdapter.class)
   private Map<String, Object> payloadMap;

   @XmlJavaTypeAdapter(SerializableXmlAdapter.class)
   private Serializable        payloadObject;

   private List<JTBProperty>   jtbProperties;

   // ------------
   // Constructors
   // ------------
   public JTBMessageTemplate() {
      // JAX-B
   }

   public JTBMessageTemplate(JTBMessage jtbMessage) throws JMSException {
      Message message = jtbMessage.getJmsMessage();

      this.jtbMessageType = jtbMessage.getJtbMessageType();
      this.replyToDestinationName = jtbMessage.getReplyToDestinationName();
      this.deliveryMode = jtbMessage.getDeliveryMode();
      this.priority = jtbMessage.getPriority();
      this.timeToLive = jtbMessage.getTimeToLive();
      this.deliveryDelay = jtbMessage.getDeliveryDelay();

      this.jmsCorrelationID = message.getJMSCorrelationID();
      this.jmsType = message.getJMSType();

      // Read Only
      this.jmsMessageID = message.getJMSMessageID();
      this.jmsTimestamp = message.getJMSTimestamp();
      this.jmsExpiration = message.getJMSExpiration();
      try {
         this.jmsDeliveryTime = message.getJMSDeliveryTime();
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
            } catch (Throwable e1) {
               // } catch (JMSException e1) {
               // DF: Catching JMSException is not sufficient for classes that are used by the class of the Object Body..
               // } catch (JMSException e1) {

               StringBuilder sb = new StringBuilder(512);
               log.error("A JMSException occurred when reading Object Payload: {}", e1);

               sb.append("An exception occured while reading the ObjectMessage payload.");
               sb.append(CR).append(CR);
               sb.append("JMSToolBox needs to know the implementation of the class of the Object stored in the OnjectMessage in order to manage those messages.");
               sb.append(CR).append(CR);
               sb.append("Consider adding the implementation class of the Object stored in the ObjectMessage to the Q Manager configuration jars.");
               sb.append(CR).append(CR);
               sb.append("Cause: ").append(Utils.getCause(e1));
               throw new JMSException(sb.toString());
            }
            break;

         case STREAM:
            // StreamMessage sm = (StreamMessage) message;
            log.warn("STREAM Message can not be transformed into JTBMessageTemplate");
            return;
      }

      // Properties
      jtbProperties = new ArrayList<>();
      @SuppressWarnings("unchecked")
      Enumeration<String> e = message.getPropertyNames();
      while (e.hasMoreElements()) {
         String key = e.nextElement();
         // Do not store standard + Queue Manager properties
         if (!(key.startsWith("JMS"))) {
            jtbProperties.add(new JTBProperty(key, message.getObjectProperty(key)));
         }
      }
   }

   // -------------------------
   // toString()
   // -------------------------

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(512);
      builder.append("JTBMessageTemplate [jtbMessageType=");
      builder.append(jtbMessageType);
      builder.append(", jmsMessageID=");
      builder.append(jmsMessageID);
      builder.append(", jmsCorrelationID=");
      builder.append(jmsCorrelationID);
      builder.append(", jmsType=");
      builder.append(jmsType);
      builder.append(", replyToDestinationName=");
      builder.append(replyToDestinationName);
      builder.append(", jmsTimestamp=");
      builder.append(jmsTimestamp);
      builder.append(", jmsDeliveryTime=");
      builder.append(jmsDeliveryTime);
      builder.append(", jmsExpiration=");
      builder.append(jmsExpiration);
      builder.append(", deliveryMode=");
      builder.append(deliveryMode);
      builder.append(", priority=");
      builder.append(priority);
      builder.append(", timeToLive=");
      builder.append(timeToLive);
      builder.append(", deliveryDelay=");
      builder.append(deliveryDelay);
      builder.append("]");
      return builder.toString();
   }

   // -------------------------
   // Helper
   // -------------------------
   public JTBMessage toJTBMessage(JTBDestination jtbDestination, Message jmsMessage) throws JMSException {

      // Set JTBMessage Properties
      JTBMessage jtbMessage = new JTBMessage(jtbDestination, jmsMessage);
      jtbMessage.setDeliveryDelay(this.deliveryDelay);
      jtbMessage.setJmsMessage(jmsMessage);
      jtbMessage.setJtbDestination(jtbDestination);
      jtbMessage.setJtbMessageType(this.jtbMessageType);
      jtbMessage.setTimeToLive(this.timeToLive);
      jtbMessage.setDeliveryMode(this.deliveryMode);
      jtbMessage.setPriority(this.priority);
      jtbMessage.setReplyToDestinationName(this.replyToDestinationName);

      // Set JMS Message Properties
      if (this.jmsType != null) {
         jmsMessage.setJMSType(this.jmsType);
      }
      if (this.jmsCorrelationID != null) {
         jmsMessage.setJMSCorrelationID(this.jmsCorrelationID);
      }

      List<JTBProperty> props = getJtbProperties();
      if (props != null) {
         for (JTBProperty jtbProperty : props) {
            String value = jtbProperty.getValue().toString();
            switch (jtbProperty.getKind()) {
               case STRING:
                  jmsMessage.setStringProperty(jtbProperty.getName(), value);
                  break;
               case BOOLEAN:
                  jmsMessage.setBooleanProperty(jtbProperty.getName(), Boolean.parseBoolean(value));
                  break;
               case DOUBLE:
                  jmsMessage.setDoubleProperty(jtbProperty.getName(), Double.parseDouble(value));
                  break;
               case FLOAT:
                  jmsMessage.setFloatProperty(jtbProperty.getName(), Float.parseFloat(value));
                  break;
               case INT:
                  jmsMessage.setIntProperty(jtbProperty.getName(), Integer.parseInt(value));
                  break;
               case LONG:
                  jmsMessage.setLongProperty(jtbProperty.getName(), Long.parseLong(value));
                  break;
               case SHORT:
                  jmsMessage.setShortProperty(jtbProperty.getName(), Short.parseShort(value));
                  break;
               default:
                  jmsMessage.setObjectProperty(jtbProperty.getName(), jtbProperty.getValue());
                  break;
            }
         }
      }

      switch (jtbMessageType) {
         case TEXT:
            TextMessage tm = (TextMessage) jmsMessage;
            String txt = payloadText;
            if (Utils.isNotEmpty(txt)) {
               tm.setText(txt);
            }
            break;
         case BYTES:
            BytesMessage bm = (BytesMessage) jmsMessage;
            byte[] b = payloadBytes;
            if (Utils.isNotEmpty(b)) {
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

   public boolean hasPayload() {
      switch (jtbMessageType) {
         case TEXT:
            return Utils.isNotEmpty(payloadText);
         case BYTES:
            return Utils.isNotEmpty(payloadBytes);
         case MAP:
            return Utils.isNotEmpty(payloadMap);
         case MESSAGE:
         case OBJECT:
         case STREAM:
            return false;
         default:
            return false;
      }
   }

   // -------------------------
   // Standard Getters/Setters
   // -------------------------

   public List<JTBProperty> getJtbProperties() {
      return jtbProperties;
   }

   public void setJtbProperties(List<JTBProperty> jtbProperties) {
      this.jtbProperties = jtbProperties;
   }

   public Integer getPriority() {
      return priority;
   }

   public JTBDeliveryMode getDeliveryMode() {
      return deliveryMode;
   }

   public byte[] getPayloadBytes() {
      return payloadBytes;
   }

   public String getJmsMessageID() {
      return jmsMessageID;
   }

   public Long getJmsTimestamp() {
      return jmsTimestamp;
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

   public void setDeliveryMode(JTBDeliveryMode deliveryMode) {
      this.deliveryMode = deliveryMode;
   }

   public String getReplyToDestinationName() {
      return replyToDestinationName;
   }

   public void setReplyToDestinationName(String replyToDestinationName) {
      this.replyToDestinationName = replyToDestinationName;
   }

}

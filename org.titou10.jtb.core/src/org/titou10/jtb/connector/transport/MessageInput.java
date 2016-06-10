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
package org.titou10.jtb.connector.transport;

import java.util.Map;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.bind.annotation.XmlRootElement;

import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.util.JTBDeliveryMode;

/**
 * Input Message coming from an External Connector
 * 
 * @author Denis Forveille
 *
 */
@XmlRootElement
public class MessageInput {

   public enum MessageInputType {
                                 TEXT,
                                 MESSAGE
   }

   private MessageInputType    type;

   // JTM Message Producer properties
   private JTBDeliveryMode     deliveryMode;
   private Integer             priority;
   private Long                timeToLive;
   private Long                deliveryDelay;   // JMS 2.0

   // JMS Message Properties
   private String              jmsType;
   private String              jmsCorrelationID;
   private String              payloadText;
   private Map<String, String> properties;

   public JTBMessage toJTBMessage(JTBConnection jtbConnection, JTBDestination jtbDestination) throws JMSException {
      Message jmsMessage = (TextMessage) jtbConnection.createJMSMessage(JTBMessageType.valueOf(type.name()));

      JTBMessage jtbMessage = new JTBMessage(jtbDestination, jmsMessage);
      if (this.deliveryMode != null) {
         jtbMessage.setDeliveryMode(this.deliveryMode);
      }
      if (this.priority != null) {
         jtbMessage.setPriority(this.priority);
      }
      if (this.timeToLive != null) {
         jtbMessage.setTimeToLive(this.timeToLive);
      }
      if (this.deliveryDelay != null) {
         jtbMessage.setDeliveryDelay(this.deliveryDelay);
      }

      jmsMessage.setJMSType(this.jmsType);
      jmsMessage.setJMSCorrelationID(this.jmsCorrelationID);
      if (properties != null) {
         for (Entry<String, String> e : properties.entrySet()) {
            jmsMessage.setStringProperty(e.getKey(), e.getValue());
         }
      }

      switch (type) {
         case TEXT:
            if (payloadText != null) {
               TextMessage tm = (TextMessage) jmsMessage;
               tm.setText(payloadText);
            }
            break;

         case MESSAGE:
            break;
      }

      return jtbMessage;
   }

   // ------------------------
   // toString()
   // ------------------------

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(512);
      builder.append("MessageInput [type=");
      builder.append(type);
      builder.append(", deliveryMode=");
      builder.append(deliveryMode);
      builder.append(", priority=");
      builder.append(priority);
      builder.append(", timeToLive=");
      builder.append(timeToLive);
      builder.append(", deliveryDelay=");
      builder.append(deliveryDelay);
      builder.append(", jmsType=");
      builder.append(jmsType);
      builder.append(", jmsCorrelationID=");
      builder.append(jmsCorrelationID);
      builder.append(", payloadText=");
      builder.append(payloadText);
      builder.append(", properties=");
      builder.append(properties);
      builder.append("]");
      return builder.toString();
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public void setType(MessageInputType type) {
      this.type = type;
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

   public Map<String, String> getProperties() {
      return properties;
   }

   public void setProperties(Map<String, String> properties) {
      this.properties = properties;
   }

   public JTBDeliveryMode getDeliveryMode() {
      return deliveryMode;
   }

   public void setDeliveryMode(JTBDeliveryMode deliveryMode) {
      this.deliveryMode = deliveryMode;
   }

   public String getPayloadText() {
      return payloadText;
   }

   public void setPayloadText(String payloadText) {
      this.payloadText = payloadText;
   }

   public MessageInputType getType() {
      return type;
   }

   public Integer getPriority() {
      return priority;
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

}

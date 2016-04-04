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
import org.titou10.jtb.jms.util.JMSDeliveryMode;

@XmlRootElement
public class MessageInput {

   public enum MessageInputType {
                                 TEXT,
                                 MESSAGE
   }

   private MessageInputType    type;

   private JMSDeliveryMode     jmsDeliveryMode;

   private Integer             jmsPriority;
   private String              jmsType;
   private String              jmsCorrelationID;
   private Long                jmsDeliveryTime;
   private Long                jmsExpiration;

   private String              payloadText;

   private Map<String, String> properties;

   public JTBMessage toJTBMessage(JTBConnection jtbConnection, JTBDestination jtbDestination) throws JMSException {
      Message jmsMessage = (TextMessage) jtbConnection.createJMSMessage(JTBMessageType.valueOf(type.name()));

      JTBMessage jtbMessage = new JTBMessage(jtbDestination, jmsMessage);

      jmsMessage.setJMSCorrelationID(this.jmsCorrelationID);
      if (this.jmsDeliveryTime != null) {
         jmsMessage.setJMSDeliveryTime(this.jmsDeliveryTime);
      }
      if (this.jmsExpiration != null) {
         jmsMessage.setJMSExpiration(this.jmsExpiration);
      }
      if (this.jmsPriority != null) {
         jmsMessage.setJMSPriority(this.jmsPriority);
      }
      jmsMessage.setJMSType(this.jmsType);
      if (this.jmsDeliveryMode != null) {
         jmsMessage.setJMSDeliveryMode(this.jmsDeliveryMode.intValue());
      }

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
      StringBuilder builder = new StringBuilder(256);
      builder.append("MessageTransport [jmsPriority=");
      builder.append(jmsPriority);
      builder.append(", jmsType=");
      builder.append(jmsType);
      builder.append(", jmsCorrelationID=");
      builder.append(jmsCorrelationID);
      builder.append(", jmsDeliveryTime=");
      builder.append(jmsDeliveryTime);
      builder.append(", jmsExpiration=");
      builder.append(jmsExpiration);
      builder.append(", payloadText=");
      builder.append(payloadText);
      builder.append("]");
      return builder.toString();
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------
   public Integer getJmsPriority() {
      return jmsPriority;
   }

   public void setType(MessageInputType type) {
      this.type = type;
   }

   public void setJmsPriority(Integer jmsPriority) {
      this.jmsPriority = jmsPriority;
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

   public Map<String, String> getProperties() {
      return properties;
   }

   public void setProperties(Map<String, String> properties) {
      this.properties = properties;
   }

   public JMSDeliveryMode getJmsDeliveryMode() {
      return jmsDeliveryMode;
   }

   public void setJmsDeliveryMode(JMSDeliveryMode jmsDeliveryMode) {
      this.jmsDeliveryMode = jmsDeliveryMode;
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

   public Long getJmsDeliveryTime() {
      return jmsDeliveryTime;
   }

   public void setJmsDeliveryTime(Long jmsDeliveryTime) {
      this.jmsDeliveryTime = jmsDeliveryTime;
   }

}

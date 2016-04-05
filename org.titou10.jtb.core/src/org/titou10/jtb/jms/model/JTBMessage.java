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

import javax.jms.JMSException;
import javax.jms.Message;

import org.titou10.jtb.jms.util.JMSDeliveryMode;

/**
 * Encapsulates a JMS Message
 * 
 * @author Denis Forveille
 * 
 */
public class JTBMessage {

   // JMS Object
   private Message         jmsMessage;

   // Owner Queue
   private JTBDestination  jtbDestination;

   // Helpers
   private JTBMessageType  jtbMessageType;

   // Attributes not related to Messages but to MessageProducer
   private JMSDeliveryMode jmsDeliveryMode;
   private Integer         priority;
   private Long            timeToLive;
   private Long            deliveryDelay;  // JMS 2.0

   // ------------------------
   // Constructor
   // ------------------------
   public JTBMessage(JTBDestination jtbDestination, Message jmsMessage) throws JMSException {
      this.jtbDestination = jtbDestination;
      this.jmsMessage = jmsMessage;
      this.jtbMessageType = JTBMessageType.fromJMSMessage(jmsMessage);
      this.jmsDeliveryMode = JMSDeliveryMode.fromValue(jmsMessage.getJMSDeliveryMode());
      this.priority = jmsMessage.getJMSPriority();
   }

   // ------------------------
   // Helpers
   // ------------------------
   @Override
   public String toString() {
      try {
         StringBuilder builder = new StringBuilder(256);
         builder.append("JTBMessage [jtbDestination=");
         builder.append(jtbDestination.getName());
         builder.append(", jmsMessage ID=");
         builder.append(jmsMessage.getJMSMessageID());
         builder.append(", jtbMessageType=");
         builder.append(jtbMessageType);
         builder.append(", jmsDeliveryMode=");
         builder.append(jmsDeliveryMode);
         builder.append(", priority=");
         builder.append(priority);
         builder.append(", timeToLive=");
         builder.append(timeToLive);
         builder.append(", deliveryDelay=");
         builder.append(deliveryDelay);
         builder.append("]");
         return builder.toString();
      } catch (JMSException e) {
         return super.toString();
      }
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public Message getJmsMessage() {
      return jmsMessage;
   }

   public JMSDeliveryMode getJmsDeliveryMode() {
      return jmsDeliveryMode;
   }

   public void setJmsDeliveryMode(JMSDeliveryMode jmsDeliveryMode) {
      this.jmsDeliveryMode = jmsDeliveryMode;
   }

   public void setJmsMessage(Message jmsMessage) {
      this.jmsMessage = jmsMessage;
   }

   public JTBDestination getJtbDestination() {
      return jtbDestination;
   }

   public void setJtbDestination(JTBDestination jtbDestination) {
      this.jtbDestination = jtbDestination;
   }

   public JTBMessageType getJtbMessageType() {
      return jtbMessageType;
   }

   public void setJtbMessageType(JTBMessageType jtbMessageType) {
      this.jtbMessageType = jtbMessageType;
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

package org.titou10.jtb.cs;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.jms.util.JTBDeliveryMode;

public enum JTBStandardHeader {

                               JMS_DESTINATION("JMSDestination", "Destination"),
                               JMS_DELIVERY_MODE("JMSDeliveryMode", "Delivery Mode"),
                               JMS_EXPIRATION("JMSExpiration", "Expiration"),
                               JMS_DELIVERY_TIME("DeliveryTime", "Delivery Time"),
                               JMS_PRIORITY("JMSPriority", "Priority"),
                               JMS_MESSAGE_ID("JMSMessageID", "ID"),
                               JMS_TIMESTAMP("JMSTimestamp", "JMS Timestamp"),
                               JMS_CORRELATION_ID("JMSCorrelationID", "JMS Correlation ID"),
                               JMS_REPLY_TO("JMSReplyTo", "Reply To"),
                               JMS_TYPE("JMSType", "JMS Type"),
                               JMS_REDELIVERED("JMSRedelivered", "Redelivered"),

                               MESSAGE_TYPE(null, "Type");

   private static final Logger           log = LoggerFactory.getLogger(JTBStandardHeader.class);
   private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

   private String                        headerName;
   private String                        displayName;

   // -----------
   // Constructor
   // -----------

   private JTBStandardHeader(String headerName, String displayName) {
      this.headerName = headerName;
      this.displayName = displayName;
   }

   // ----------------
   // Helpers
   // ----------------
   public JTBStandardHeader fromName(String name) {
      return Arrays.stream(values()).filter(x -> name.equals(x.headerName)).findFirst().orElse(null);
   }

   public String formatValue(Message m) {

      // DF: could probably better be implemented via a java 8 Function<>

      try {
         switch (this) {
            case JMS_CORRELATION_ID:
               return m.getJMSCorrelationID();

            case JMS_DELIVERY_MODE:
               StringBuilder deliveryMode = new StringBuilder(32);
               deliveryMode.append(JTBDeliveryMode.fromValue(m.getJMSDeliveryMode()).name());
               deliveryMode.append(" (");
               deliveryMode.append(m.getJMSDeliveryMode());
               deliveryMode.append(")");
               return deliveryMode.toString();

            case JMS_DELIVERY_TIME:
               return m.getJMSDeliveryTime() == 0 ? "" : SDF.format(new Date(m.getJMSDeliveryTime()));

            case JMS_DESTINATION:
               return m.getJMSDestination() == null ? "" : m.getJMSDestination().toString();

            case JMS_EXPIRATION:
               return m.getJMSExpiration() == 0 ? "" : SDF.format(new Date(m.getJMSExpiration()));

            case JMS_MESSAGE_ID:
               return m.getJMSMessageID();

            case JMS_PRIORITY:
               return String.valueOf(m.getJMSPriority());

            case JMS_REDELIVERED:
               return String.valueOf(m.getJMSRedelivered());

            case JMS_REPLY_TO:
               return m.getJMSReplyTo() == null ? "" : m.getJMSReplyTo().toString();

            case JMS_TIMESTAMP:
               return m.getJMSTimestamp() == 0 ? "" : SDF.format(new Date(m.getJMSTimestamp()));

            case JMS_TYPE:
               return m.getJMSType();

            case MESSAGE_TYPE:
               return JTBMessageType.fromJMSMessage(m).getDescription();
         }
      } catch (JMSException e) {
         log.warn("JMSException occured when reading JMSCorrelationID : {}", e.getMessage());
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

}

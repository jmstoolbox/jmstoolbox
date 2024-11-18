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
package org.titou10.jtb.qm.oracleaq;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.gen.SessionDef;
import org.titou10.jtb.jms.qm.DestinationData;
import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.jms.qm.QManagerProperty;
import org.titou10.jtb.jms.qm.QueueData;
import org.titou10.jtb.jms.qm.TopicData;

import oracle.jdbc.driver.OracleDriver;
import oracle.jms.AQjmsFactory;

/**
 * 
 * Implements Oracle Advanced Queuing (AQ) Q Provider
 * 
 * @author Denis Forveille
 * @author Ihar Kuzniatsou
 *
 */
public class OracleAqManager extends QManager {
   private static final Logger                 log                 = LoggerFactory.getLogger(OracleAqManager.class);

   // private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SSS");
   private static final String                 CR                  = "\n";

   private final static String                 P_SID               = "sid";
   private final static String                 P_DRIVER_TYPE       = "driverType";

   private static final String                 URL_OCI             = "jdbc:oracle:%s:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(HOST=%s)(PORT=%s)(CONNECT_DATA=(SID=%s))))";
   private static final String                 URL_THIN            = "jdbc:oracle:thin:@%s:%s/%s";

   private static final String                 C_NAME              = "NAME";
   private static final String                 C_OWNER             = "OWNER";
   private static final String                 C_RECIPIENTS        = "RECIPIENTS";
   private static final String                 C_QUEUE_TYPE        = "QUEUE_TYPE";
   private static final String                 V_QUEUE_MARKER      = "SINGLE";
   // private static final String V_EXCEPTION_QUEUE = "EXCEPTION_QUEUE";

   private static final String                 QUERY_GET_DEST      = "select qu.*" +
                                                                     "      ,qt.*" +
                                                                     "      ,qu.queue_type" +
                                                                     "  from all_queues qu" +
                                                                     "      ,all_queue_tables qt" +
                                                                     " where qt.queue_table = qu.queue_table" +
                                                                     "   and qt.object_type like 'SYS.AQ$_JMS%'";
   private static final String                 QUERY_GET_DEST_INFO = QUERY_GET_DEST + "   and qu.owner = ?" + "   and qu.name = ?";

   private static final String                 QUERY_Q_DEPTH1      = "select qu.queue_table from all_queues qu where qu.owner = ? and qu.name = ?";
   private static final String                 QUERY_Q_DEPTH2      = "select count(*) from %s where q_name = '%s'";

   private static final String                 HELP_TEXT;

   private static final List<QManagerProperty> parameters          = new ArrayList<>();

   private final Map<Integer, JDBCData>        jdbcDatas           = new HashMap<>();

   // -----------
   // Constructor
   // -----------

   public OracleAqManager() {
      log.debug("Instantiate OracleAQ");

      parameters.add(new QManagerProperty(P_SID, true, JMSPropertyKind.STRING, false, "Oracle SID", "ORCLCDB"));
      parameters.add(new QManagerProperty(P_DRIVER_TYPE, true, JMSPropertyKind.STRING, false, "driver (thin, oci, oci8 )", "thin"));
   }

   // ------------------
   // Business Interface
   // ------------------

   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);

      var mapProperties = extractProperties(sessionDef);

      var sid = mapProperties.get(P_SID);
      var driverType = mapProperties.get(P_DRIVER_TYPE);

      var url = "";
      switch (driverType) {
         case "oci":
         case "oci8":
            url = String.format(URL_OCI, driverType, sessionDef.getHost(), sessionDef.getPort(), sid);
            break;
         case "thin":
            url = String.format(URL_THIN, sessionDef.getHost(), sessionDef.getPort(), sid);
            break;
         default:
            throw new Exception("Unsupported driver type: " + driverType);
      }

      log.debug("url: {}", url);

      // Load DB Driver
      DriverManager.registerDriver(new OracleDriver());

      // ------------------
      // Get JMS Connection
      // ------------------
      final ConnectionFactory connectionFactory = AQjmsFactory.getConnectionFactory(url, null);
      Connection jmsConnection = connectionFactory.createConnection(sessionDef.getActiveUserid(), sessionDef.getActivePassword());
      jmsConnection.start();

      log.info("connected to {} - {}", sessionDef.getName(), jmsConnection.getClientID());

      // Store per connection related data
      jdbcDatas.put(jmsConnection.hashCode(),
                    new JDBCData(jdbcConnect(url, sessionDef.getActiveUserid(), sessionDef.getActivePassword()),
                                 url,
                                 sessionDef.getActiveUserid(),
                                 sessionDef.getActivePassword()));

      return jmsConnection;
   }

   @Override
   public DestinationData discoverDestinations(Connection jmsConnection, boolean showSystemObjects) throws SQLException {
      log.debug("discoverDestinations : {} - {}", jmsConnection, showSystemObjects);

      var jdbcData = jdbcDatas.get(jmsConnection.hashCode());

      SortedSet<QueueData> queues = new TreeSet<>();
      SortedSet<TopicData> topics = new TreeSet<>();
      var name = "";
      var owner = "";
      var recipients = "";
      var queueType = "";

      var destinationName = "";

      // Read database to get Destinations
      try (PreparedStatement preparedStatement = jdbcData.jdbcConnection.prepareStatement(QUERY_GET_DEST)) {
         ResultSet resultSet = preparedStatement.executeQuery();
         while (resultSet.next()) {
            name = resultSet.getString(C_NAME);
            owner = resultSet.getString(C_OWNER);
            recipients = resultSet.getString(C_RECIPIENTS);
            queueType = resultSet.getString(C_QUEUE_TYPE);

            destinationName = owner + "." + name;

            // Queues of type "EXCEPTION_QUEUE" are not system queues...
            // if (!showSystemObjects && queueType.equals(V_EXCEPTION_QUEUE)) {
            // continue;
            // }

            if (V_QUEUE_MARKER.equals(recipients)) {
               log.debug("Found Queue '{}'.", destinationName);
               queues.add(new QueueData(destinationName));
            } else {
               log.debug("Found Topic '{}'.", destinationName);
               topics.add(new TopicData(destinationName));
            }

         }
      }

      return new DestinationData(queues, topics);
   }

   @Override
   public Integer getQueueDepth(Connection jmsConnection, String queueName) {
      // log.debug("getQueueDepth for '{}'", queueName);

      var jdbcData = jdbcDatas.get(jmsConnection.hashCode());

      // Reconnect if connection closed
      try {
         if (!jdbcData.jdbcConnection.isValid(0)) {
            jdbcData.jdbcConnection = jdbcConnect(jdbcData.jdbcURL, jdbcData.jdbcUserid, jdbcData.jdbcPassword);
         }
      } catch (SQLException e) {
         log.error("JDBC connection is closed, and test/reconnect failed!: {}", e.getMessage());
         return null;
      }

      String[] q = queueName.split("[.]");
      var owner = q[0];
      var name = q[1];

      var queue_table_name = "";

      // Read database to get queue_table for queue
      try (PreparedStatement preparedStatement = jdbcData.jdbcConnection.prepareStatement(QUERY_Q_DEPTH1)) {
         preparedStatement.setString(1, owner);
         preparedStatement.setString(2, name);
         ResultSet resultSet = preparedStatement.executeQuery();
         resultSet.next();

         queue_table_name = resultSet.getString("QUEUE_TABLE");
      } catch (SQLException e) {
         log.error("SQLException when reading queue for '{}': {}", queueName, e.getMessage());
         return null;
      }

      // Count nb messages
      String query = String.format(QUERY_Q_DEPTH2, queue_table_name, name);
      try (PreparedStatement preparedStatement = jdbcData.jdbcConnection.prepareStatement(query)) {
         ResultSet resultSet = preparedStatement.executeQuery();
         resultSet.next();

         return resultSet.getInt(1);
      } catch (SQLException e) {
         log.error("SQLException when reading Q Depth for '{}': {}", queueName, e.getMessage());
         return null;
      }
   }

   @Override
   public Map<String, Object> getQueueInformation(Connection jmsConnection, String queueName) {
      log.debug("Get Destination Information for '{}'", queueName);

      SortedMap<String, Object> properties = new TreeMap<>();

      var jdbcData = jdbcDatas.get(jmsConnection.hashCode());

      // Reconnect if connection closed
      try {
         if (!jdbcData.jdbcConnection.isValid(0)) {
            jdbcData.jdbcConnection = jdbcConnect(jdbcData.jdbcURL, jdbcData.jdbcUserid, jdbcData.jdbcPassword);
         }
      } catch (SQLException e1) {
         log.error("JDBC connection is closed, and test/reconnect failed!");
         return properties;
      }

      String[] q = queueName.split("[.]");
      var owner = q[0];
      var name = q[1];

      // Read database to get Destinations
      try (PreparedStatement preparedStatement = jdbcData.jdbcConnection.prepareStatement(QUERY_GET_DEST_INFO)) {
         preparedStatement.setString(1, owner);
         preparedStatement.setString(2, name);
         ResultSet resultSet = preparedStatement.executeQuery();
         resultSet.next();

         // ALL_QUEUE
         properties.put("QUEUE_TABLE", resultSet.getString("QUEUE_TABLE"));
         properties.put("QID", resultSet.getString("QID"));
         properties.put("QUEUE_TYPE", resultSet.getString("QUEUE_TYPE"));
         properties.put("MAX_RETRIES", resultSet.getString("MAX_RETRIES"));
         properties.put("RETRY_DELAY", resultSet.getString("RETRY_DELAY"));
         properties.put("ENQUEUE_ENABLED", resultSet.getString("ENQUEUE_ENABLED").trim());
         properties.put("DEQUEUE_ENABLED", resultSet.getString("DEQUEUE_ENABLED").trim());
         properties.put("RETENTION", resultSet.getString("RETENTION"));
         properties.put("USER_COMMENT", resultSet.getString("USER_COMMENT"));
         properties.put("SHARDED", resultSet.getString("SHARDED"));

         // ALL_QUEUE_TABLES
         properties.put("TYPE", resultSet.getString("TYPE"));
         properties.put("OBJECT_TYPE", resultSet.getString("OBJECT_TYPE"));
         properties.put("SORT_ORDER", resultSet.getString("SORT_ORDER"));
         properties.put("MESSAGE_GROUPING", resultSet.getString("MESSAGE_GROUPING"));
         properties.put("REPLICATION_MODE", resultSet.getString("REPLICATION_MODE"));
         properties.put("COMPATIBLE", resultSet.getString("COMPATIBLE"));
         properties.put("PRIMARY_INSTANCE", resultSet.getString("PRIMARY_INSTANCE"));
         properties.put("SECONDARY_INSTANCE", resultSet.getString("SECONDARY_INSTANCE"));
         properties.put("SECURE", resultSet.getString("SECURE"));

      } catch (SQLException e) {
         log.error("Exception when reading Queue Information. Ignoring", e);
      }

      log.debug("Queue Information : {}", properties);
      return properties;
   }

   @Override
   public Map<String, Object> getTopicInformation(Connection jmsConnection, String topicName) {
      return getQueueInformation(jmsConnection, topicName);
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      Integer hash = jmsConnection.hashCode();
      var jdbcData = jdbcDatas.get(hash);

      try {
         jmsConnection.close();
      } catch (JMSException e) {
         log.warn("Exception occured while closing JMS connection. Ignore it. Msg={}", e.getMessage());
      }

      try {
         jdbcData.jdbcConnection.close();
      } catch (SQLException e) {
         log.warn("Exception occured while closing JDBC connection. Ignore it. Msg={}", e.getMessage());
      }

      jdbcDatas.remove(hash);
   }

   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }

   @Override
   public String getHelpText() {
      return HELP_TEXT;
   }

   static {
      var sb = new StringBuilder(2048);
      sb.append("Extra JARS :").append(CR);
      sb.append("------------").append(CR);
      sb.append("No extra jar is needed as JMSToolBox is bundled with the latest Apache ActiveMQ jars").append(CR);
      sb.append(CR);
      sb.append("Connection:").append(CR);
      sb.append("-----------").append(CR);
      sb.append("Host          : Oracle AQ server host name").append(CR);
      sb.append("Port          : Oracle AQ server port (ie 1521)").append(CR);
      sb.append("User/Password : User allowed to connect to Oracle AQ").append(CR);
      sb.append(CR);
      sb.append("Properties values:").append(CR);
      sb.append("------------------").append(CR);
      sb.append("sid                : sid.").append(CR);
      sb.append("driverType         : 'thin', 'oci', 'oci8'").append(CR);

      HELP_TEXT = sb.toString();
   }

   // JDBC Helpers
   private java.sql.Connection jdbcConnect(String url, String userid, String password) throws SQLException {
      return DriverManager.getConnection(url, userid, password);
   }

   private final class JDBCData {
      public java.sql.Connection jdbcConnection;
      public final String        jdbcURL;
      public final String        jdbcUserid;
      public final String        jdbcPassword;

      public JDBCData(java.sql.Connection jdbcConnection, String jdbcURL, String jdbcUserid, String jdbcPassword) {
         this.jdbcConnection = jdbcConnection;
         this.jdbcURL = jdbcURL;
         this.jdbcUserid = jdbcUserid;
         this.jdbcPassword = jdbcPassword;
      }
   }
}

package org.titou10.jtb.qm.oracleaq;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

import oracle.jms.AQjmsFactory;

public class OracleAqManager extends QManager {
   private static final Logger    log                      = LoggerFactory.getLogger(OracleAqManager.class);

   private List<QManagerProperty> parameters               = new ArrayList<QManagerProperty>();

   private final static String    ORACLE_SID_PROPERTY_NAME = "ORACLE_SID";
   private final static String    DRIVER_PROPERTY_NAME     = "DRIVER";

   private String                 url                      = "";

   private String                 host_property_value      = "";
   private String                 sid_property_value       = "";
   private Integer                port_property_value      = null;
   private String                 driver_property_value    = "";

   private String                 user_property_value      = "";
   private String                 password_property_value  = "";

   public OracleAqManager() {
      log.debug("Instantiate OracleAQ");
      parameters.add(new QManagerProperty(ORACLE_SID_PROPERTY_NAME, true, JMSPropertyKind.STRING, false, "SID", "xe"));
      parameters.add(new QManagerProperty(DRIVER_PROPERTY_NAME,
                                          true,
                                          JMSPropertyKind.STRING,
                                          false,
                                          "driver (thin, kprb, oci8 )",
                                          "thin"));
   }

   @Override
   public Connection connect(SessionDef sessionDef, boolean showSystemObjects, String clientID) throws Exception {
      log.info("connecting to {} - {}", sessionDef.getName(), clientID);
      establish_properties(sessionDef);
      final ConnectionFactory connectionFactory = AQjmsFactory.getConnectionFactory(url, null);
      Connection connection = connectionFactory.createConnection(user_property_value, password_property_value);
      connection.start();
      return connection;
   }

   private void establish_properties(SessionDef sessionDef) throws JMSException {
      Map<String, String> mapProperties = extractProperties(sessionDef);

      sid_property_value = mapProperties.get(ORACLE_SID_PROPERTY_NAME);
      driver_property_value = mapProperties.get(DRIVER_PROPERTY_NAME);

      host_property_value = sessionDef.getHost();
      port_property_value = sessionDef.getPort();
      user_property_value = sessionDef.getActiveUserid();
      password_property_value = sessionDef.getActivePassword();

      url = createUrl(host_property_value, sid_property_value, port_property_value, driver_property_value);
   }

   @Override
   public DestinationData discoverDestinations(Connection jmsConnection, boolean showSystemObjects) throws Exception {
      log.debug("discoverDestinations : {} - {}", jmsConnection, showSystemObjects);

      List<OracleAqDestination> listOfOracleAqDestinations = findOracleAqDestinations(url,
                                                                                      user_property_value,
                                                                                      password_property_value);

      SortedSet<QueueData> listQueueData = listOfOracleAqDestinations.stream().filter(OracleAqDestination::isQueue)
               .map(destination -> new QueueData(destination.getName())).collect(Collectors.toCollection(() -> new TreeSet<>()));

      SortedSet<TopicData> listTopicData = listOfOracleAqDestinations.stream().filter(OracleAqDestination::isTopic)
               .map(destination -> new TopicData(destination.getName())).collect(Collectors.toCollection(() -> new TreeSet<>()));

      return new DestinationData(listQueueData, listTopicData);
   }

   private List<OracleAqDestination> findOracleAqDestinations(String jdbcUrl,
                                                              String jdbcUser,
                                                              String jdbcPassword) throws SQLException {
      List<OracleAqDestination> listOfDestinations = new ArrayList<>();

      DriverManager.registerDriver(new oracle.jdbc.OracleDriver());

      try (java.sql.Connection jdbcConnection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)) {
         String schema = user_property_value.toUpperCase();

         String query = "select NAME,RECIPIENTS from dba_queues qu "
                        + " left join dba_queue_tables qt on qt.queue_table=qu.queue_table "
                        + " where qu.queue_type=? and qu.owner =? ";

         PreparedStatement preparedStatement = jdbcConnection.prepareStatement(query);
         preparedStatement.setString(1, "NORMAL_QUEUE");
         preparedStatement.setString(2, schema);
         ResultSet resultSet = preparedStatement.executeQuery();

         while (resultSet.next()) {
            listOfDestinations.add(new OracleAqDestination(resultSet.getString("NAME"), resultSet.getString("RECIPIENTS")));
         }
      }

      return listOfDestinations;
   }

   @Override
   public void close(Connection jmsConnection) throws JMSException {
      jmsConnection.close();

   }

   @Override
   public List<QManagerProperty> getQManagerProperties() {
      return parameters;
   }

   private static String createUrl(String host, String sid, int port, String driver) throws JMSException {
      String url;
      if (!"oci8".equals(driver) && !"oci".equals(driver)) {
         if (!"thin".equals(driver)) {
            throw new JMSException("unsupported driver");
         }
         url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
      } else {
         url = "jdbc:oracle:oci8:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp)(PORT=" + port + ")(HOST=" + host + "))(CONNECT_DATA=(SID="
               + sid + ")))";
      }
      return url;
   }

   private class OracleAqDestination {

      private String name;
      private String recipients;

      public OracleAqDestination(String name, String recipients) {
         this.name = name;
         this.recipients = recipients;
      }

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getRecipients() {
         return recipients;
      }

      public void setRecipients(String recipients) {
         this.recipients = recipients;
      }

      public boolean isQueue() {
         return "SINGLE".equals(recipients);
      }

      public boolean isTopic() {
         return "MULTIPLE".equals(recipients);
      }

   }
}

/*
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
package org.titou10.jtb.uitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.titou10.jtb.uitest.UITestReport.step;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.StreamMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTabItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/**
 * UI tests of JMSToolBox, run in the real application by Tycho with SWTBot.
 *
 * The sessions come from org.titou10.jtb.build/smoketest/config.xml, the brokers from smoketest/docker-compose.yml. The message tests
 * use the queue JTB.TEST.ACTIVEMQ of the ActiveMQ Classic session and empty it first. With the other brokers, a text message is
 * sent and browsed with JMSToolBox only
 *
 */
public class JMSToolBoxUITest {

   private static final String SESSION          = "ActiveMQ Classic";
   private static final String SESSION_ARTEMIS2 = "Artemis 2.x";
   private static final String SESSION_ARTEMIS1 = "Artemis 1.x";
   private static final String SESSION_ORACLE   = "Oracle AQ";
   private static final String SESSION_SOLACE   = "Solace";
   // One session per broker of smoketest/config.xml (the "Discovery" sessions use ActiveMQ Classic too)
   private static final List<String> SESSIONS   = List.of(SESSION, SESSION_ARTEMIS2, SESSION_ARTEMIS1, SESSION_ORACLE,
                                                          SESSION_SOLACE);
   // Test queues created by setup.sh: a name of its own per broker, so that a test can not see the queue of another broker
   private static final String QUEUE            = "JTB.TEST.ACTIVEMQ";
   private static final String QUEUE_ARTEMIS2   = "JTB.TEST.ARTEMIS2";
   private static final String QUEUE_ARTEMIS1   = "JTB.TEST.ARTEMIS1";
   private static final String QUEUE_SOLACE     = "JTB.TEST.SOLACE";
   // Oracle AQ doesn't allow dots in queue names. JMSToolBox shows the queue with its owner: JTB.JTB_TEST_ORACLE
   private static final String QUEUE_ORACLE     = "JTB_TEST_ORACLE";
   private static final String VISUALIZER       = "SWTBot visualizer";

   // REST API (Jolokia) of the ActiveMQ Classic broker, used to prepare the queue
   private static final String JOLOKIA_URL      = "http://localhost:8162/api/jolokia/";
   private static final String QUEUE_MBEAN      = "org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName="
                                                  + QUEUE;

   private static SWTBot       bot;

   // Records the result of each test in the report (see UITestReport)
   @Rule
   public TestWatcher          report           = UITestReport.watcher();

   @BeforeClass
   public static void setUp() {
      SWTBotPreferences.TIMEOUT = 30_000;
      // Pause between SWTBot actions, to watch the tests (uitest.sh --slow)
      SWTBotPreferences.PLAYBACK_DELAY = Long.getLong("uitest.delay", 0);
      bot = new SWTBot();
      step("JMSToolBox started");
   }

   // Closes the dialogs a failed test may have left open, so that they don't disturb the next tests
   @Before
   public void closeLeftoverDialogs() {
      UIThreadRunnable.syncExec(() -> {
         for (Shell shell : Display.getDefault().getShells()) {
            if (shell.getParent() != null && shell.isVisible()) {
               shell.close();
            }
         }
      });
   }

   @AfterClass
   public static void writeReport() throws Exception {
      UITestReport.write();
   }

   @Test
   public void connectSessionAndListQueues() {
      SWTBotTreeItem session = connect(SESSION);

      SWTBotTreeItem queues = session.getNode("Queues").expand();
      step("Expanded the queues of " + SESSION + ": " + queues.getNodes());
      assertTrue("Queue " + QUEUE + " not found in " + queues.getNodes(), queues.getNodes().contains(QUEUE));
   }

   @Test
   public void sendMessageWithDialog() throws Exception {
      String payload = uniquePayload("sent with the dialog");
      purgeQueue();

      // Right-click on the queue > "Send New Message..."
      connect(SESSION).getNode("Queues").expand().getNode(QUEUE).contextMenu("Send New Message...").click();
      SWTBotShell dialog = bot.shell("Send Message to " + SESSION + ":" + QUEUE);
      SWTBot dialogBot = dialog.bot();
      step("Opened the dialog '" + dialog.getText() + "'");

      // Payload tab: the multi-line text field of the payload
      inTab(dialogBot, "Payload").text(0).setText(payload);
      // Properties tab: name and value of a new property, then "Add"
      SWTBot properties = inTab(dialogBot, "Properties");
      properties.text(0).setText("jtbUiTest");
      properties.text(1).setText("dialog");
      properties.button("Add").click();
      step("Entered the payload and the property jtbUiTest=dialog");

      dialogBot.button("Send and Close").click();
      step("Clicked 'Send and Close'");

      // The message shows up when browsing the queue, with the property
      browseQueueUntilShown(payload);
      assertEquals("dialog", valueInTable(inTab(bot, "Properties").table(), "jtbUiTest"));
      step("Browsed the queue: the message has the payload and the property jtbUiTest=dialog");
   }

   @Test
   public void showMessageDetails() throws Exception {
      String payload = uniquePayload("details");
      String correlationId = "corr-" + System.currentTimeMillis();
      purgeQueue();
      jolokia("{\"type\":\"exec\",\"mbean\":\"" + QUEUE_MBEAN + "\",\"operation\":\"sendTextMessage(java.util.Map,java.lang.String)\","
              + "\"arguments\":[{\"JMSCorrelationID\":\"" + correlationId + "\",\"jtbUiTest\":\"details\"},\"" + payload + "\"]}");

      browseQueueUntilShown(payload);
      step("Selected the message: toString shows the payload");

      assertEquals(correlationId, valueInTable(inTab(bot, "JMS Headers").table(), "JMSCorrelationID"));
      step("JMS Headers tab: JMSCorrelationID=" + correlationId);

      assertEquals("details", valueInTable(inTab(bot, "Properties").table(), "jtbUiTest"));
      step("Properties tab: jtbUiTest=details");
   }

   @Test
   public void openMessageWithJavaScriptVisualizer() throws Exception {
      String payload = uniquePayload("visualizer");
      purgeQueue();
      jolokia("{\"type\":\"exec\",\"mbean\":\"" + QUEUE_MBEAN
              + "\",\"operation\":\"sendTextMessage(java.lang.String)\",\"arguments\":[\"" + payload + "\"]}");

      SWTBotTable messages = browseQueueUntilShown(payload);

      // Run the inline JavaScript visualizer (workspace/visualizers.xml) on the message
      messages.getTableItem(0).select().contextMenu("Open Payload as...").menu(VISUALIZER).click();

      SWTBotShell logs = bot.shell("Script logs");
      step("Clicked 'Open Payload as... > " + VISUALIZER + "' on the message: dialog '" + logs.getText() + "' opened");
      String expected = "SWTBOT:" + payload.toUpperCase();
      bot.waitUntil(new DefaultCondition() {
         @Override
         public boolean test() {
            return logs.bot().text().getText().contains(expected);
         }

         @Override
         public String getFailureMessage() {
            return "'" + expected + "' not found in the script logs: " + logs.bot().text().getText();
         }
      });
      step("Script logs show: " + logs.bot().text().getText().trim());
      logs.close();
   }

   // ---------------------------------------------------------------------------------------------------------
   // Message viewer: every JMS message type, shown in the details below the message table and in the dialog that
   // opens on double-click. The object message contains a plain JDK object (java.util.ArrayList)
   // ---------------------------------------------------------------------------------------------------------

   @Test
   public void viewTextMessage() throws Exception {
      viewMessage("TEXT", "ActiveMQTextMessage", s -> s.createTextMessage("swtbot text message"), (title, payload) -> {
         assertEquals("Payload: 19 chars", title);
         assertEquals("swtbot text message", payload.text(0).getText());
      });
   }

   @Test
   public void viewBytesMessage() throws Exception {
      viewMessage("BYTES", "ActiveMQBytesMessage", s -> {
         BytesMessage m = s.createBytesMessage();
         m.writeBytes("swtbot bytes message".getBytes(StandardCharsets.UTF_8));
         return m;
      }, (title, payload) -> {
         assertEquals("Payload: 20 bytes", title);
         // The hex viewer has 3 columns: offsets, hex and raw. The raw column shows 16 bytes per line
         String raw = payload.styledText(2).getText().replace("\n", "");
         assertEquals("swtbot bytes message", raw);
      });
   }

   @Test
   public void viewMapMessage() throws Exception {
      viewMessage("MAP", "ActiveMQMapMessage", s -> {
         MapMessage m = s.createMapMessage();
         m.setString("text", "swtbot map message");
         m.setInt("number", 42);
         return m;
      }, (title, payload) -> {
         SWTBotTable map = payload.table(0);
         assertEquals(2, map.rowCount());
         assertEquals("swtbot map message", valueInTable(map, "text"));
         assertEquals("42", valueInTable(map, "number"));
      });
   }

   // The ActiveMQ client only deserializes classes of its trusted packages (by default org.apache.activemq,
   // org.fusesource.hawtbuf, com.thoughtworks.xstream.mapper), unless the session sets "trustAllPackages". So the payload of
   // this object message (a java.util.ArrayList) can't be read. Today, JMSToolBox shows the details of the message, logs the
   // error and doesn't open the dialog on double-click.
   // This test documents the current behaviour, not the desired one: once JMSToolBox shows such payloads (or an error
   // dialog), it fails and must be changed to check the new behaviour
   @Test
   public void viewObjectMessageWithUntrustedPayload() throws Exception {
      purgeQueue();
      String id = sendWithJms(s -> s.createObjectMessage(new ArrayList<>(List.of("swtbot", "object", "message"))));

      SWTBotTable messages = browseQueueUntilShown(id);
      String toString = inTab(bot, "toString").text(0).getText();
      assertTrue("The toString doesn't mention ActiveMQObjectMessage: " + toString, toString.contains("ActiveMQObjectMessage"));
      step("Details of the OBJECT message (ActiveMQObjectMessage)");

      messages.getTableItem(0).doubleClick();
      bot.sleep(3000);
      for (SWTBotShell shell : bot.shells()) {
         assertFalse("Unexpected dialog '" + shell.getText() + "'", shell.getText().contains("OBJECT"));
      }
      assertTrue("The error reading the payload is not in the log", appLog().contains("when reading Object Payload"));
      step("Double-clicked the OBJECT message: no dialog, the error reading the payload is logged");
   }

   // A JSON payload is shown formatted in the "Payload (JSON)" tab and as a tree in "Payload (JSON Tree)" (jakarta.json)
   @Test
   public void viewJsonMessage() throws Exception {
      String json = "{\"order\":{\"id\":4711,\"customer\":\"swtbot\",\"items\":[\"json\",\"tree\"]}}";
      purgeQueue();
      String id = sendWithJms(s -> s.createTextMessage(json));

      browseQueueUntilShown(id);
      String formatted = inTab(bot, "Payload (JSON)").text(0).getText();
      assertTrue("The JSON is not formatted: " + formatted, formatted.lines().count() > 3 && formatted.contains("\"customer\""));
      step("Payload (JSON) tab: the payload is formatted");

      List<String> nodes = allItemTexts(inTab(bot, "Payload (JSON Tree)").tree());
      String all = String.join(" | ", nodes);
      assertTrue("The JSON tree doesn't contain the payload: " + all, all.contains("customer") && all.contains("swtbot")
                                                                       && all.contains("4711"));
      step("Payload (JSON Tree) tab: " + all);
   }

   // Saves a message as template (JAXB writes the template file) and sends a new message from it (JAXB reads it back)
   @Test
   public void saveAsTemplateAndSendFromTemplate() throws Exception {
      String payload = uniquePayload("template");
      String templateName = "swtbot-" + System.currentTimeMillis();
      purgeQueue();
      sendWithJms(s -> s.createTextMessage(payload));

      // Double-click on the message > "Save as Template..."
      browseQueueUntilShown(payload).getTableItem(0).doubleClick();
      SWTBotShell view = waitForShellWithTitleContaining("TEXT");
      view.bot().button("Save as Template...").click();
      SWTBotShell save = bot.shell("Save Template");
      save.bot().text(0).setText(templateName);
      step("Clicked 'Save as Template...' and entered the name " + templateName);
      save.bot().button("OK").click();

      // The template is an XML file; the payload is stored Base64 encoded with the prefix {##}
      Path file = Path.of(System.getProperty("uitest.workspace", "target/workspace"), "JMSToolBox", "Templates",
                          templateName + ".jtb");
      String encodedPayload = "{##}" + Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
      bot.waitUntil(new DefaultCondition() {
         @Override
         public boolean test() throws Exception {
            return Files.exists(file) && Files.readString(file, StandardCharsets.UTF_8).contains(encodedPayload);
         }

         @Override
         public String getFailureMessage() {
            return "Template file " + file + " not written or without the payload";
         }
      });
      step("Template file " + file.getFileName() + " written, it contains the payload");

      // Right-click on the queue > "Send Message from Template..." > select the template > "Send"
      purgeQueue();
      connect(SESSION).getNode("Queues").expand().getNode(QUEUE).contextMenu("Send Message from Template...").click();
      SWTBotShell chooser = bot.shell("Select a Template");
      selectTreeItemContaining(chooser.bot().tree(), templateName);
      step("Clicked 'Send Message from Template...' and selected the template");
      chooser.bot().button("OK").click();
      SWTBotShell send = bot.shell("Send Message to " + SESSION + ":" + QUEUE);
      step("Dialog '" + send.getText() + "' opened with the template");
      send.bot().button("Send").click();

      browseQueueUntilShown(payload);
      step("Browsed the queue: the message sent from the template has the payload");
   }

   // ----------------------------------------------------
   // Other brokers
   // ----------------------------------------------------

   // JMSToolBox shows a session in red if its Q Manager plugin is not installed or failed to start
   @Test
   public void allSessionsHaveAQManager() {
      Tree tree = sessionsTree().widget;
      List<String> shown = new ArrayList<>();
      List<String> red = new ArrayList<>();
      UIThreadRunnable.syncExec(() -> {
         Color colorRed = Display.getDefault().getSystemColor(SWT.COLOR_RED);
         for (TreeItem item : tree.getItems()) {
            shown.add(item.getText());
            // The viewer sets the color of the cell (column 0), not of the whole item
            if (colorRed.equals(item.getForeground(0))) {
               red.add(item.getText());
            }
         }
      });
      assertTrue("Sessions missing in " + shown, shown.containsAll(SESSIONS));
      assertTrue("Sessions shown in red (Q Manager plugin missing or not started): " + red, red.isEmpty());
      step("All sessions are shown with a Q Manager: " + SESSIONS);
   }

   @Test
   public void sendAndBrowseMessageArtemis2() {
      sendAndBrowseMessage(SESSION_ARTEMIS2, QUEUE_ARTEMIS2);
   }

   @Test
   public void sendAndBrowseMessageArtemis1() {
      sendAndBrowseMessage(SESSION_ARTEMIS1, QUEUE_ARTEMIS1);
   }

   @Test
   public void sendAndBrowseMessageOracleAq() {
      sendAndBrowseMessage(SESSION_ORACLE, QUEUE_ORACLE);
   }

   @Test
   public void sendAndBrowseMessageSolace() {
      sendAndBrowseMessage(SESSION_SOLACE, QUEUE_SOLACE);
   }

   // A round trip with a broker, only with JMSToolBox: connects the session, finds the test queue of setup.sh (with or
   // without the owner as prefix), empties it, sends a text message with a property with the dialog, browses the queue in
   // the tab of the session until the message shows up, checks its payload and property, empties the queue and
   // disconnects. The payload names the broker, so each tab shows a different message. The message tests above only use
   // the ActiveMQ Classic session
   private static void sendAndBrowseMessage(String sessionName, String queue) {
      try {
         List<String> names = connect(sessionName).getNode("Queues").expand().getNodes();
         step("Expanded the queues of " + sessionName + ": " + names);
         String found = names.stream().filter(n -> n.equals(queue) || n.endsWith("." + queue)).findFirst()
                  .orElseThrow(() -> new AssertionError("Queue " + queue + " not found in " + names));

         emptyQueue(sessionName, found);

         // Right-click on the queue > "Send New Message..."
         String payload = "swtbot message for " + sessionName + " " + System.currentTimeMillis();
         queueNode(sessionName, found).contextMenu("Send New Message...").click();
         SWTBotShell dialog = bot.shell("Send Message to " + sessionName + ":" + found);
         SWTBot dialogBot = dialog.bot();
         inTab(dialogBot, "Payload").text(0).setText(payload);
         SWTBot properties = inTab(dialogBot, "Properties");
         properties.text(0).setText("jtbUiTest");
         properties.text(1).setText(sessionName);
         properties.button("Add").click();
         step("Opened the dialog '" + dialog.getText() + "', entered the payload '" + payload + "' and the property jtbUiTest="
              + sessionName);
         dialogBot.button("Send and Close").click();

         browseUntilShown(sessionName, found, payload);
         assertEquals(sessionName, valueInTable(inTab(bot, "Properties").table(), "jtbUiTest"));
         // Show the payload in the details, for the picture of this step
         new SWTBotTabItem(payloadTab()).activate();
         step("The message is shown in the tab of " + sessionName + ", with its payload and the property jtbUiTest=" + sessionName);

         emptyQueue(sessionName, found);
      } finally {
         disconnect(sessionName);
      }
   }

   // The node of a queue in the sessions browser. Looked up again each time, as JMSToolBox rebuilds the tree on refreshes
   private static SWTBotTreeItem queueNode(String sessionName, String queue) {
      return sessionsTree().getTreeItem(sessionName).expand().getNode("Queues").expand().getNode(queue);
   }

   // Right-click on the queue > "Empty Queue", and "OK" in the confirmation dialog
   private static void emptyQueue(String sessionName, String queue) {
      queueNode(sessionName, queue).select().contextMenu("Empty Queue").click();
      SWTBotShell confirmation = bot.shell("Confirmation");
      confirmation.bot().button("OK").click();
      bot.waitUntil(Conditions.shellCloses(confirmation));
      step("Emptied queue " + queue + " of " + sessionName);
   }

   // Double-clicks on the queue and waits until the tab of the session shows exactly one message in the tab of the queue,
   // with the given text payload. Browses again from time to time, as some brokers make a message visible to browsers
   // only after a short delay
   private static void browseUntilShown(String sessionName, String queue, String payload) {
      queueNode(sessionName, queue).doubleClick();
      int[] attempts = new int[1];
      bot.waitUntil(new DefaultCondition() {
         @Override
         public boolean test() {
            SWTBotTable table = messageTable(sessionName);
            if (table != null && table.rowCount() == 1) {
               table.select(0);
               if (payload.equals(shownTextPayload())) {
                  return true;
               }
            }
            if (++attempts[0] % 4 == 0) {
               queueNode(sessionName, queue).doubleClick();
            }
            return false;
         }

         @Override
         public String getFailureMessage() {
            return "Message '" + payload + "' not shown when browsing " + queue + " in the tab of " + sessionName;
         }
      });
      step("Double-clicked queue " + queue + " and selected the message '" + payload + "' in the tab of " + sessionName);
   }

   // The text in the payload tab of the message details. null if not shown
   private static String shownTextPayload() {
      return UIThreadRunnable.syncExec(() -> {
         TabItem item = payloadTab();
         Text text = item == null || item.getControl() == null ? null
                  : findWidget(List.of(item.getControl()), Text.class, t -> true);
         return text == null ? null : text.getText();
      });
   }

   // The payload tab of the message details of a text message ("Payload: <n> chars"). null if not shown
   private static TabItem payloadTab() {
      return UIThreadRunnable.syncExec(() -> {
         TabFolder folder = findWidget(List.of(Display.getDefault().getShells()), TabFolder.class,
                                       f -> f.isVisible() && Arrays.stream(f.getItems()).anyMatch(i -> i.getText().startsWith("Payload: ")));
         return folder == null ? null
                  : Arrays.stream(folder.getItems()).filter(i -> i.getText().startsWith("Payload: ")).findFirst().get();
      });
   }

   @Test
   public void viewStreamMessage() throws Exception {
      viewMessage("STREAM", "ActiveMQStreamMessage", s -> {
         StreamMessage m = s.createStreamMessage();
         m.writeString("swtbot stream message");
         m.writeInt(42);
         return m;
      }, JMSToolBoxUITest::assertNoPayloadShown);
   }

   @Test
   public void viewMessageWithoutPayload() throws Exception {
      viewMessage("MESSAGE", "ActiveMQMessage", Session::createMessage, JMSToolBoxUITest::assertNoPayloadShown);
   }

   // Today the dialog doesn't show the payload of STREAM messages (and MESSAGE has none): the payload area is hidden.
   // For STREAM messages, this documents the current behaviour, not the desired one: once JMSToolBox shows their payload,
   // viewStreamMessage fails and must check the payload instead
   private static void assertNoPayloadShown(String title, SWTBot payload) {
      assertEquals("Payload", title);
      assertTrue("A text field is shown in the payload tab",
                 payload.getFinder().findControls(WidgetMatcherFactory.widgetOfType(Text.class)).isEmpty());
      assertTrue("A table is shown in the payload tab",
                 payload.getFinder().findControls(WidgetMatcherFactory.widgetOfType(Table.class)).isEmpty());
   }

   // Sends a message of the given type, browses the queue, checks its details, opens it with a double-click and checks the
   // payload tab of the dialog
   private static void viewMessage(String type, String jmsClass, MessageFactory factory, PayloadCheck payloadCheck)
                                                                                                                  throws Exception {
      purgeQueue();
      String id = sendWithJms(factory);

      SWTBotTable messages = browseQueueUntilShown(id);
      String toString = inTab(bot, "toString").text(0).getText();
      assertTrue("The toString of the " + type + " message doesn't mention " + jmsClass + ": " + toString,
                 toString.contains(jmsClass));
      step("Details of the " + type + " message (" + jmsClass + ")");

      messages.getTableItem(0).doubleClick();
      SWTBotShell dialog = waitForShellWithTitleContaining(type);
      step("Double-clicked the " + type + " message: dialog '" + dialog.getText() + "' opened");
      // The name of the payload tab depends on the payload, eg "Payload: 20 bytes"
      SWTBotTabItem tab = tabStartingWith(dialog, "Payload");
      payloadCheck.check(tab.getText(), inTab(tab));
      step("Payload tab '" + tab.getText() + "' of the " + type + " message checked");
      dialog.close();
   }

   // The first tab of the window whose text starts with the given text. Fails if there is none
   private static SWTBotTabItem tabStartingWith(SWTBotShell shell, String text) {
      TabItem item = UIThreadRunnable.syncExec(() -> {
         TabFolder folder = findWidget(List.of(shell.widget), TabFolder.class,
                                       f -> Arrays.stream(f.getItems()).anyMatch(i -> i.getText().startsWith(text)));
         return folder == null ? null
                  : Arrays.stream(folder.getItems()).filter(i -> i.getText().startsWith(text)).findFirst().get();
      });
      if (item == null) {
         throw new AssertionError("No tab starting with '" + text + "' in '" + shell.getText() + "'");
      }
      return new SWTBotTabItem(item);
   }

   @FunctionalInterface
   private interface PayloadCheck {
      // title: text of the payload tab, payload: bot that only finds the widgets of that tab
      void check(String title, SWTBot payload);
   }

   @FunctionalInterface
   private interface MessageFactory {
      Message create(Session session) throws JMSException;
   }

   // Sends a message with the JMS client of ActiveMQ. Returns the value of its property "jtbUiTestId", which its toString
   // contains, so that browseQueueUntilShown() can recognize it
   private static String sendWithJms(MessageFactory factory) throws Exception {
      String id = "swtbot-" + System.nanoTime();
      var cf = new ActiveMQConnectionFactory("admin", "admin", "tcp://localhost:61617");
      try (Connection connection = cf.createConnection()) {
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         Message message = factory.create(session);
         message.setStringProperty("jtbUiTestId", id);
         session.createProducer(session.createQueue(QUEUE)).send(message);
      }
      return id;
   }

   private static SWTBotShell waitForShellWithTitleContaining(String text) {
      SWTBotShell[] found = new SWTBotShell[1];
      bot.waitUntil(new DefaultCondition() {
         @Override
         public boolean test() {
            for (SWTBotShell shell : bot.shells()) {
               if (shell.getText().contains(text)) {
                  found[0] = shell;
                  return true;
               }
            }
            return false;
         }

         @Override
         public String getFailureMessage() {
            return "No window with '" + text + "' in its title";
         }
      });
      return found[0];
   }

   // -------
   // Helpers
   // -------

   // Double clicks on the session if it is not connected yet, and waits until its "Queues" folder shows up
   private static SWTBotTreeItem connect(String sessionName) {
      SWTBotTreeItem session = sessionsTree().getTreeItem(sessionName);
      if (!session.expand().getNodes().contains("Queues")) {
         session.doubleClick();
         step("Double-clicked session " + sessionName);
      }
      bot.waitUntil(new DefaultCondition() {
         @Override
         public boolean test() {
            return session.expand().getNodes().contains("Queues");
         }

         @Override
         public String getFailureMessage() {
            return "Session '" + sessionName + "' did not connect. Nodes: " + session.getNodes();
         }
      });
      step("Session " + sessionName + " connected: " + session.getNodes());
      return session;
   }

   // Right-click on the session > "Disconnect", if it is connected, and waits until its folders are gone
   private static void disconnect(String sessionName) {
      SWTBotTreeItem session = sessionsTree().getTreeItem(sessionName);
      if (!session.expand().getNodes().contains("Queues")) {
         return;
      }
      session.select().contextMenu("Disconnect").click();
      bot.waitUntil(new DefaultCondition() {
         @Override
         public boolean test() {
            return !session.expand().getNodes().contains("Queues");
         }

         @Override
         public String getFailureMessage() {
            return "Session '" + sessionName + "' did not disconnect. Nodes: " + session.getNodes();
         }
      });
      step("Disconnected session " + sessionName);
   }

   // Double clicks on the queue, waits until the table shows exactly one message, selects it and waits until its toString
   // contains the payload. Waiting for the payload makes sure that it's not the table of a previous test
   private static SWTBotTable browseQueueUntilShown(String payload) {
      connect(SESSION).getNode("Queues").expand().getNode(QUEUE).doubleClick();
      SWTBotTable[] found = new SWTBotTable[1];
      bot.waitUntil(new DefaultCondition() {
         @Override
         public boolean test() {
            SWTBotTable table = messageTable(SESSION);
            if (table == null || table.rowCount() != 1) {
               return false;
            }
            table.select(0);
            if (!inTab(bot, "toString").text(0).getText().contains(payload)) {
               return false;
            }
            found[0] = table;
            return true;
         }

         @Override
         public String getFailureMessage() {
            return "Message '" + payload + "' not shown when browsing " + QUEUE;
         }
      });
      step("Double-clicked queue " + QUEUE + " and selected the message '" + payload + "'");
      return found[0];
   }

   // The sessions browser is one of several trees in the window: find the one containing the session.
   // The widgets are searched directly: bot.tree(index) waits SWTBotPreferences.TIMEOUT for each index that doesn't exist
   private static SWTBotTree sessionsTree() {
      Tree tree = findWidget(Tree.class, t -> Arrays.stream(t.getItems()).anyMatch(i -> i.getText().equals(SESSION)));
      if (tree == null) {
         throw new WidgetNotFoundException("No tree contains the session '" + SESSION + "'");
      }
      return new SWTBotTree(tree);
   }

   // The table of the messages of the queue shown in the tab of the session, recognized by its "JMS Timestamp" column.
   // null if the tab of the session is not the one shown or has no queue tab (yet)
   private static SWTBotTable messageTable(String sessionName) {
      Table table = UIThreadRunnable.syncExec(() -> {
         CTabFolder sessions = findWidget(List.of(Display.getDefault().getShells()), CTabFolder.class,
                                          f -> f.getSelection() != null && f.getSelection().getText().equals(sessionName));
         // The tabs of the other sessions and queues are hidden, so only the table of the queue shown is visible
         return sessions == null ? null
                  : findWidget(List.of(sessions), Table.class, t -> t.isVisible() && Arrays.stream(t.getColumns())
                           .anyMatch(c -> c.getText().equals("JMS Timestamp")));
      });
      return table == null ? null : new SWTBotTable(table);
   }

   // First widget of the given type in any open window that matches the condition, without waiting. null if none
   private static <T extends Widget> T findWidget(Class<T> type, Predicate<T> condition) {
      return UIThreadRunnable.syncExec(() -> findWidget(List.of(Display.getDefault().getShells()), type, condition));
   }

   // First widget of the given type below the given controls that matches the condition. To be called in the UI thread
   private static <T extends Widget> T findWidget(List<? extends Control> roots, Class<T> type, Predicate<T> condition) {
      Deque<Control> controls = new ArrayDeque<>(roots);
      while (!controls.isEmpty()) {
         Control control = controls.pop();
         if (control.isDisposed()) {
            continue;
         }
         if (type.isInstance(control) && condition.test(type.cast(control))) {
            return type.cast(control);
         }
         if (control instanceof Composite composite) {
            controls.addAll(List.of(composite.getChildren()));
         }
      }
      return null;
   }

   // Activates a tab and returns a bot that only finds the widgets of that tab
   private static SWTBot inTab(SWTBot parent, String tabText) {
      return inTab(parent.tabItem(tabText));
   }

   private static SWTBot inTab(SWTBotTabItem tab) {
      tab.activate();
      Control control = UIThreadRunnable.syncExec(() -> tab.widget.getControl());
      return new SWTBot(control);
   }

   // Value in the second column of the row whose first column is the given name (tables "Header | Value")
   private static String valueInTable(SWTBotTable table, String name) {
      for (int row = 0; row < table.rowCount(); row++) {
         if (name.equals(table.cell(row, 0))) {
            return table.cell(row, 1);
         }
      }
      throw new AssertionError("'" + name + "' not found in the table");
   }

   // Texts of all items of a tree, at all levels
   private static List<String> allItemTexts(SWTBotTree tree) {
      return UIThreadRunnable.syncExec(() -> {
         List<String> texts = new ArrayList<>();
         Deque<TreeItem> items = new ArrayDeque<>(List.of(tree.widget.getItems()));
         while (!items.isEmpty()) {
            TreeItem item = items.pop();
            texts.add(item.getText());
            items.addAll(List.of(item.getItems()));
         }
         return texts;
      });
   }

   // Expands the tree and selects the first item whose text contains the given text
   private static void selectTreeItemContaining(SWTBotTree tree, String text) {
      for (SWTBotTreeItem item : tree.getAllItems()) {
         item.expand();
         for (String node : item.getNodes()) {
            if (node.contains(text)) {
               item.getNode(node).select();
               return;
            }
         }
      }
      throw new WidgetNotFoundException("No item '" + text + "' in the tree");
   }

   // The log of JMSToolBox, in the workspace of the UI tests
   private static String appLog() throws Exception {
      Path log = Path.of(System.getProperty("uitest.workspace", "target/workspace"), "JMSToolBox", "jmstoolbox.log");
      return Files.readString(log, StandardCharsets.UTF_8);
   }

   private static String uniquePayload(String text) {
      return "swtbot " + text + " " + System.currentTimeMillis();
   }

   private static void purgeQueue() throws Exception {
      jolokia("{\"type\":\"exec\",\"mbean\":\"" + QUEUE_MBEAN + "\",\"operation\":\"purge\"}");
   }

   private static void jolokia(String request) throws Exception {
      String auth = Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));
      HttpRequest r = HttpRequest.newBuilder(URI.create(JOLOKIA_URL)).header("Authorization", "Basic " + auth)
               .header("Origin", "http://localhost").header("Content-Type", "application/json")
               .POST(HttpRequest.BodyPublishers.ofString(request)).build();
      HttpResponse<String> response = HttpClient.newHttpClient().send(r, HttpResponse.BodyHandlers.ofString());
      if (!response.body().contains("\"status\":200")) {
         throw new IllegalStateException("Jolokia request failed: " + response.body());
      }
   }
}

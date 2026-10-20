/*
 * Copyright (C) 2026 Stephan Rauh
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
package org.titou10.jtb.uitest;

import static org.junit.Assert.assertTrue;
import static org.titou10.jtb.uitest.UITestReport.step;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/**
 * UI tests of JMSToolBox, run in the real application by Tycho with SWTBot.
 *
 * The sessions come from org.titou10.jtb.build/smoketest/config.xml, the brokers from smoketest/docker-compose.yml
 *
 */
public class JMSToolBoxUITest {

   private static final String SESSION        = "ActiveMQ Classic";
   private static final String QUEUE          = "JTB.TEST";
   private static final String VISUALIZER     = "SWTBot visualizer";

   // REST API (Jolokia) of the ActiveMQ Classic broker, used to prepare the queue
   private static final String JOLOKIA_URL    = "http://localhost:8162/api/jolokia/";
   private static final String QUEUE_MBEAN    = "org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName="
                                                + QUEUE;

   private static SWTBot       bot;

   // Records the result of each test in the report (see UITestReport)
   @Rule
   public TestWatcher          report         = UITestReport.watcher();

   @BeforeClass
   public static void setUp() {
      SWTBotPreferences.TIMEOUT = 30_000;
      // Pause between SWTBot actions, to watch the tests (uitest.sh --slow)
      SWTBotPreferences.PLAYBACK_DELAY = Long.getLong("uitest.delay", 0);
      bot = new SWTBot();
      step("JMSToolBox started");
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
   public void openMessageWithJavaScriptVisualizer() throws Exception {
      String payload = "hello from swtbot " + System.currentTimeMillis();
      jolokia("{\"type\":\"exec\",\"mbean\":\"" + QUEUE_MBEAN + "\",\"operation\":\"purge\"}");
      jolokia("{\"type\":\"exec\",\"mbean\":\"" + QUEUE_MBEAN
              + "\",\"operation\":\"sendTextMessage(java.lang.String)\",\"arguments\":[\"" + payload + "\"]}");

      // Browse the queue: its messages are shown in a table on the right
      SWTBotTreeItem session = connect(SESSION);
      session.getNode("Queues").expand().getNode(QUEUE).doubleClick();
      SWTBotTable messages = waitForTableWithRows();
      step("Double-clicked queue " + QUEUE + ": " + messages.rowCount() + " message(s) shown");

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

   // The sessions browser is one of several trees in the window: find the one containing the session
   private static SWTBotTree sessionsTree() {
      for (int i = 0; i < 10; i++) {
         try {
            SWTBotTree tree = bot.tree(i);
            tree.getTreeItem(SESSION);
            return tree;
         } catch (WidgetNotFoundException e) {
            // Not this tree, or no more trees
         }
      }
      throw new WidgetNotFoundException("No tree contains the session '" + SESSION + "'");
   }

   private static SWTBotTable waitForTableWithRows() {
      SWTBotTable[] found = new SWTBotTable[1];
      bot.waitUntil(new DefaultCondition() {
         @Override
         public boolean test() {
            for (int i = 0; i < 10; i++) {
               try {
                  SWTBotTable table = bot.table(i);
                  if (table.rowCount() > 0) {
                     found[0] = table;
                     return true;
                  }
               } catch (WidgetNotFoundException e) {
                  return false;
               }
            }
            return false;
         }

         @Override
         public String getFailureMessage() {
            return "No message table with rows found";
         }
      });
      return found[0];
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

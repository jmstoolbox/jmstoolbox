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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Report of the UI tests: the steps of each test with a picture of the JMSToolBox windows after each step.
 *
 * Writes index.html (for people) and summary.md (for the GitHub job summary) into the directory set by the system property
 * "uitest.report".
 *
 * The windows are drawn into images by SWT (Control.print) instead of taking screenshots: screenshots need the "Screen
 * Recording" permission on macOS and are blank without it.
 */
public final class UITestReport {

   private static final File       DIR     = new File(System.getProperty("uitest.report", "target/uitest-report"));

   private static final List<Test> TESTS   = new ArrayList<>();
   private static Test             current = new Test("Startup");
   private static int              imageNr;

   private UITestReport() {
   }

   // Records a step of the current test, with pictures of the open windows
   public static void step(String description) {
      System.out.println("UI: " + description);
      current.steps.add(new Step(description, captureWindows()));
   }

   // JUnit rule that records the result of each test
   public static TestWatcher watcher() {
      return new TestWatcher() {
         @Override
         protected void starting(Description d) {
            if (!current.steps.isEmpty()) {
               TESTS.add(current);
            }
            current = new Test(d.getMethodName());
         }

         @Override
         protected void failed(Throwable e, Description d) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            current.failure = sw.toString();
            current.steps.add(new Step("Failed: " + e.getMessage(), captureWindows()));
         }

         @Override
         protected void finished(Description d) {
            current.millis = System.currentTimeMillis() - current.start;
            TESTS.add(current);
            current = new Test("After " + d.getMethodName());
         }
      };
   }

   public static void write() throws IOException {
      StringBuilder html = new StringBuilder();
      html.append("<!doctype html>\n<html><head><meta charset=\"utf-8\"><title>JMSToolBox UI tests</title><style>\n")
               .append("body{font-family:sans-serif;margin:24px;color:#222}h2{margin-top:32px}")
               .append(".pass{color:#1a7f37}.fail{color:#cf222e}li{margin:12px 0}")
               .append("img{max-width:420px;border:1px solid #ccc;margin:6px 6px 0 0;vertical-align:top}")
               .append("pre{background:#f6f8fa;padding:12px;overflow-x:auto;font-size:12px}\n</style></head><body>\n")
               .append("<h1>JMSToolBox UI tests</h1>\n");

      StringBuilder md = new StringBuilder("### JMSToolBox UI tests\n\n| Test | Result | Duration | Steps |\n|---|---|---|---|\n");

      for (Test t : TESTS) {
         if (t.isSetup() && t.steps.isEmpty()) {
            continue;
         }
         String result = t.isSetup() ? "" : (t.failure == null ? "PASS" : "FAIL");
         html.append("<h2>").append(esc(t.name)).append(" <span class=\"").append(t.failure == null ? "pass" : "fail")
                  .append("\">").append(result).append("</span></h2>\n");
         if (!t.isSetup()) {
            html.append("<p>").append(t.millis).append(" ms</p>\n");
            md.append("| ").append(t.name).append(" | ").append(t.failure == null ? "✅ pass" : "❌ fail").append(" | ")
                     .append(t.millis).append(" ms | ").append(t.steps.size()).append(" |\n");
         }
         html.append("<ol>\n");
         for (Step s : t.steps) {
            html.append("<li>").append(esc(s.description)).append("<br>");
            for (String image : s.images) {
               html.append("<a href=\"").append(image).append("\"><img src=\"").append(image).append("\" alt=\"\"></a>");
            }
            html.append("</li>\n");
         }
         html.append("</ol>\n");
         if (t.failure != null) {
            html.append("<pre>").append(esc(t.failure)).append("</pre>\n");
            md.append("\n<details><summary>").append(t.name).append("</summary>\n\n```\n").append(t.failure).append("```\n</details>\n\n");
         }
      }
      html.append("</body></html>\n");

      DIR.mkdirs();
      Files.writeString(new File(DIR, "index.html").toPath(), html, StandardCharsets.UTF_8);
      Files.writeString(new File(DIR, "summary.md").toPath(), md, StandardCharsets.UTF_8);
      System.out.println("UI test report: " + new File(DIR, "index.html").getAbsolutePath());
   }

   // Draws each visible window (main window, dialogs) into a PNG file. Returns the file names
   private static List<String> captureWindows() {
      DIR.mkdirs();
      int nr = ++imageNr;
      return UIThreadRunnable.syncExec(() -> {
         List<String> files = new ArrayList<>();
         Display display = Display.getDefault();
         for (Shell shell : display.getShells()) {
            Point size = shell.getSize();
            if (!shell.isVisible() || size.x <= 0 || size.y <= 0) {
               continue;
            }
            Image image = new Image(display, size.x, size.y);
            GC gc = new GC(image);
            try {
               shell.print(gc);
            } finally {
               gc.dispose();
            }
            String file = String.format("%02d-%d.png", nr, files.size() + 1);
            ImageLoader loader = new ImageLoader();
            loader.data = new ImageData[] { image.getImageData() };
            loader.save(new File(DIR, file).getPath(), SWT.IMAGE_PNG);
            image.dispose();
            files.add(file);
         }
         return files;
      });
   }

   private static String esc(String s) {
      return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
   }

   private static final class Test {
      final String     name;
      final long       start = System.currentTimeMillis();
      final List<Step> steps = new ArrayList<>();
      long             millis;
      String           failure;

      Test(String name) {
         this.name = name;
      }

      boolean isSetup() {
         return name.equals("Startup") || name.startsWith("After ");
      }
   }

   private record Step(String description, List<String> images) {
   }
}

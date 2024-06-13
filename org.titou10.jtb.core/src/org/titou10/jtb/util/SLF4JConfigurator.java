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
package org.titou10.jtb.util;

/**
 * Configure SLF4J
 * 
 * @author Denis Forveille
 * 
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter2;

public final class SLF4JConfigurator {

   private static final String BUNDLE_NAME = FrameworkUtil.getBundle(SLF4JConfigurator.class).getSymbolicName();
   private static final String LOGBACK_XML = "platform:/plugin/" + BUNDLE_NAME + "/config/logback.xml";

   public static void configure() {

      // Reset Current SLF4J config
      JoranConfigurator configurator = new JoranConfigurator();
      LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
      configurator.setContext(loggerContext);
      loggerContext.reset();

      // Read Configuration file
      try (InputStream configurationStream = new URL(LOGBACK_XML).openStream()) {
         configurator.doConfigure(configurationStream);
      } catch (JoranException | IOException e) {
         // Problem when reading file...
         e.printStackTrace();
      }
      new StatusPrinter2().printInCaseOfErrorsOrWarnings(loggerContext);
      // new StatusPrinter2().print(loggerContext);

      // Confirm message
      Logger logger = LoggerFactory.getLogger(SLF4JConfigurator.class);
      logger.info("Logging configuration initialised.");
   }
}

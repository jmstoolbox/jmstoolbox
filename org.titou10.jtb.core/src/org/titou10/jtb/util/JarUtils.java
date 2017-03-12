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

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Create an OSGI Bundle/Plugin with external Q Managers Jars as dependencies
 * 
 * @author Denis Forveille
 *
 */
public final class JarUtils {

   private static final Logger log              = LoggerFactory.getLogger(JarUtils.class);

   private static final String NON_EXISTING_JAR = "\nJAR file '%s' not found for plugin '%s'.\nSessions for this Q Manager will probably not work.";

   public static String createBundle(String workDirectry, String pluginId, List<String> jarFileNames) throws Exception {

      // Only create a resource bundle if jars are defined
      if ((jarFileNames == null) || (jarFileNames.isEmpty())) {
         log.warn("pluginId '{}': No jars found in 'QManagerDef' found in config file. No resource Bundle will be created for it.",
                  pluginId);
         return null;
      }

      // Build classpath with external jars
      boolean notFirst = false;
      StringBuilder sb = new StringBuilder(512);
      for (String jarFileName : jarFileNames) {
         String usableJarFileName = jarFileName.replaceAll("\\\\", "/");

         // Throw exception to user if a jar does not exist..
         File f = new File(usableJarFileName);
         if (!(f.exists()) || (f.isDirectory())) {
            String msg = String.format(NON_EXISTING_JAR, jarFileName, pluginId);
            log.warn(msg);
            throw new Exception(msg);
         }

         if (notFirst) {
            sb.append(", ");
         }
         sb.append("external:" + usableJarFileName);
         notFirst = true;
      }

      log.debug("pluginId '{}': Creating a resource bundle with jars: '{}'", pluginId, sb.toString());

      String resourcePluginId = pluginId + ".resource";
      String resourceFileName = workDirectry + "/" + resourcePluginId + ".jar";

      // Bundle Manifest
      Manifest manifest = new Manifest();
      Attributes a = manifest.getMainAttributes();
      a.put(Attributes.Name.MANIFEST_VERSION, "1.0");
      a.putValue("Bundle-ManifestVersion", "2");
      a.putValue("Bundle-Name", resourcePluginId);
      a.putValue("Bundle-SymbolicName", resourcePluginId + ";singleton:=true");
      a.putValue("Bundle-Version", "1.0.0");
      a.putValue("Import-Package", "javax.jms");
      a.putValue("Require-Bundle", pluginId + ";resolution:=optional");
      a.putValue("Eclipse-RegisterBuddy", pluginId);
      a.putValue("Bundle-ClassPath", sb.toString());

      // Write the bundle/jar file
      JarOutputStream target = new JarOutputStream(new FileOutputStream(resourceFileName), manifest);
      target.close();

      log.debug("pluginId '{}' Bundle created: {}", pluginId, resourceFileName);

      return resourceFileName;
   }

   // ------------------
   // Pure Utility Class
   // ------------------
   private JarUtils() {
      // NOP
   }
}

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
package org.titou10.jtb.hook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.internal.hookregistry.ClassLoaderHook;
import org.eclipse.osgi.internal.loader.classpath.ClasspathEntry;
import org.eclipse.osgi.internal.loader.classpath.ClasspathManager;
import org.eclipse.osgi.storage.BundleInfo.Generation;

/**
 * Add extra jars to the QM bundles as defined in config file
 * 
 * @author Denis Forveille
 *
 */
public class JTBClassLoaderHook extends ClassLoaderHook {

   private static final String       JTB_QM_PLUGIN_PREFIX = "org.titou10.jtb.qm";

   private static final List<String> PLUGINS_DONE         = new ArrayList<String>();

   private Map<String, List<String>> jarsPerPlugin;

   public JTBClassLoaderHook(Map<String, List<String>> jarsPerPlugin) {
      this.jarsPerPlugin = jarsPerPlugin;
      System.out.println("JTB ClassLoaderHook initialized");
   }

   @Override
   public boolean addClassPathEntry(ArrayList<ClasspathEntry> cpEntries,
                                    String cp,
                                    ClasspathManager hostmanager,
                                    Generation sourceGeneration) {

      // Fast fail
      if (jarsPerPlugin == null) {
         return super.addClassPathEntry(cpEntries, cp, hostmanager, sourceGeneration);
      }

      // Add jars depending on the pluginId
      String pluginId = sourceGeneration.getRevision().getSymbolicName();

      // Process only JTB QM plugins
      if (pluginId.startsWith(JTB_QM_PLUGIN_PREFIX)) {
         System.out.println("cp=" + cp);
         System.out.println("cpEntries:");
         for (ClasspathEntry classpathEntry : cpEntries) {
            System.out.println("   " + classpathEntry.getBundleFile().getBaseFile());
         }

         // Process each plugin only once
         if (!(PLUGINS_DONE.contains(pluginId))) {
            PLUGINS_DONE.add(pluginId);
            List<String> jars = jarsPerPlugin.get(pluginId);
            if (jars == null) {
               System.out.println("No extra jars to add to " + pluginId);
            } else {
               for (String jarFileName : jars) {
                  String j = jarFileName.replaceAll("\\\\", "/");
                  System.out.println("Adding jar '" + j + "' to " + pluginId);
                  cpEntries.add(hostmanager.getExternalClassPath(j, sourceGeneration));
               }
            }
         }
      }

      return super.addClassPathEntry(cpEntries, cp, hostmanager, sourceGeneration);
   }
}

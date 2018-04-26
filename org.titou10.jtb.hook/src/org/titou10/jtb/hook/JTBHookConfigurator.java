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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.eclipse.osgi.internal.framework.EquinoxContainer;
import org.eclipse.osgi.internal.hookregistry.HookConfigurator;
import org.eclipse.osgi.internal.hookregistry.HookRegistry;
import org.titou10.jtb.config.gen.Config;
import org.titou10.jtb.config.gen.QManagerDef;

/**
 * 
 * Classloader hook to add extra jars to QM bundles as defined in config file
 * 
 * To run : - add in VM arguments: -Dosgi.framework.extensions=org.titou10.jtb.hook
 * 
 * In dev: - import plugin/fragment org.eclipse.osgi in workspace - add org.eclipse.osgi in config
 * 
 * @author Denis Forveille
 *
 */
@Deprecated
public class JTBHookConfigurator implements HookConfigurator {

   private static final String JTB_CONFIG_FILE = "JMSToolBox/config.xml";

   @Override
   public void addHooks(HookRegistry registry) {
      System.out.println("JTBHookConfigurator initializing...");

      Map<String, List<String>> jarsPerPlugin = new HashMap<>();

      EquinoxContainer eco = registry.getContainer();
      URL url = eco.getLocations().getInstanceLocation().getURL();
      try {
         URI uri = url.toURI().resolve(JTB_CONFIG_FILE);
         System.out.println("Looking for config file : " + uri);
         File f = new File(uri);
         if (f.exists()) {
            System.out.println("Config file found. Parsing it");
            JAXBContext jc = JAXBContext.newInstance(Config.class);
            Unmarshaller u = jc.createUnmarshaller();
            Config cfg = (Config) u.unmarshal(f);
            for (QManagerDef qManagerDef : cfg.getQManagerDef()) {
               String id = qManagerDef.getId();
               jarsPerPlugin.put(id, qManagerDef.getJar());
            }
         }
      } catch (URISyntaxException | JAXBException e) {
         System.out.println("A problem occurred when parsing config file : " + e.getMessage());
      }

      registry.addClassLoaderHook(new JTBClassLoaderHook(jarsPerPlugin));

   }
}

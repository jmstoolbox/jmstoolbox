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
package org.titou10.jtb.config;

import org.eclipse.core.runtime.IConfigurationElement;
import org.titou10.jtb.config.gen.QManagerDef;
import org.titou10.jtb.jms.qm.QManager;
import org.titou10.jtb.util.Constants;

/**
 * Links a QManagerDef with the equivalent plugin and extension
 * 
 * @author Denis Forveille
 *
 */
public class MetaQManager implements Comparable<MetaQManager> {

   private String                id;              // Plugin id
   private String                displayName;     // "displayName" attribute as defined in the Extension point for the plugin
   private String                pluginClassName; // Class name of the QM in the plugin

   private IConfigurationElement ice;             // Plugin configuration
   private QManager              qmanager;        // Instance of the QM for a plugin
   private QManagerDef           qManagerDef;     // Definition of a QM in the config file

   // -------------------------
   // Constructeurs
   // -------------------------

   public MetaQManager(IConfigurationElement ice) {
      this.ice = ice;
      this.id = ice.getNamespaceIdentifier();
      this.displayName = ice.getAttribute(Constants.JTB_EXTENSION_POINT_NAME_ATTR);
      this.pluginClassName = ice.getAttribute(Constants.JTB_EXTENSION_POINT_CLASS_ATTR);
   }

   public MetaQManager(QManagerDef qManagerDef) {
      this.qManagerDef = qManagerDef;
      this.id = qManagerDef.getId();
      this.displayName = qManagerDef.getName();
   }

   // -------------------------
   // Comparator
   // -------------------------

   @Override
   public int compareTo(MetaQManager m) {
      return this.displayName.compareTo(m.getDisplayName());
   }

   // -------------------------
   // toString
   // -------------------------

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(256);
      builder.append("WorkingQManager [id=");
      builder.append(id);
      builder.append(", displayName=");
      builder.append(displayName);
      builder.append("]");
      return builder.toString();
   }

   public void setIce(IConfigurationElement ice) {
      this.ice = ice;
      this.displayName = ice.getAttribute(Constants.JTB_EXTENSION_POINT_NAME_ATTR);
      this.pluginClassName = ice.getAttribute(Constants.JTB_EXTENSION_POINT_CLASS_ATTR);
   }

   // -------------------------
   // Getters/Setters Standards
   // -------------------------

   public QManager getQmanager() {
      return qmanager;
   }

   public void setQmanager(QManager qmanager) {
      this.qmanager = qmanager;
   }

   public QManagerDef getqManagerDef() {
      return qManagerDef;
   }

   public void setqManagerDef(QManagerDef qManagerDef) {
      this.qManagerDef = qManagerDef;
   }

   public String getId() {
      return id;
   }

   public IConfigurationElement getIce() {
      return ice;
   }

   public String getDisplayName() {
      return displayName;
   }

   public void setId(String id) {
      this.id = id;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public String getPluginClassName() {
      return pluginClassName;
   }

   public void setPluginClassName(String pluginClassName) {
      this.pluginClassName = pluginClassName;
   }

}

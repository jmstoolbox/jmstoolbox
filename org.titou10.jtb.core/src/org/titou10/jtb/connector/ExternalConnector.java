/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.connector;

import org.eclipse.jface.preference.PreferencePage;

/**
 * Exposes JMSToolBox engine to plugins
 * 
 * Must be implemented by plugins that provides external connectors to JMSToolBox
 * 
 * @author Denis Forveille
 * 
 */
public interface ExternalConnector {

   void initialize(ExternalConfigManager eConfigManager) throws Exception;

   PreferencePage getPreferencePage();
}

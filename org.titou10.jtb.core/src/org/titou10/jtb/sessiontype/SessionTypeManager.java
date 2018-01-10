/*
 * Copyright (C) 2018 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.sessiontype;

import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;

/**
 * Manage all things related to "Session Types"
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class SessionTypeManager {

   private static final Logger               log                     = LoggerFactory.getLogger(SessionTypeManager.class);

   public static final SessionTypeComparator SESSION_TYPE_COMPARATOR = new SessionTypeComparator();

   @Inject
   private ConfigManager                     cm;

   private List<SessionType>                 sessionTypes;

   @PostConstruct
   private void initialize() throws Exception {
      log.debug("Initializing SessionTypeManager");

      log.debug("SessionTypeManager initialized");
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public List<SessionType> getSessionTypes() {
      return sessionTypes;
   }

   // -------
   // Helpers
   // -------

   public final static class SessionTypeComparator implements Comparator<SessionType> {

      @Override
      public int compare(SessionType o1, SessionType o2) {
         // System variables first
         boolean sameSystem = o1.isSystem() == o2.isSystem();
         if (!(sameSystem)) {
            if (o1.isSystem()) {
               return -1;
            } else {
               return 1;
            }
         }
         return o1.getName().compareTo(o2.getName());
      }
   }

}

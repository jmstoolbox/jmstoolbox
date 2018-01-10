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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.JTBPreferenceStore;
import org.titou10.jtb.util.Constants;

/**
 * Manage all things related to "Session Types"
 * 
 * @author Denis Forveille
 *
 */
@Creatable
@Singleton
public class SessionTypeManager {

   private static final Logger               log                      = LoggerFactory.getLogger(SessionTypeManager.class);

   public static final String                PREF_SESSION_TYPE_PREFIX = "sessionttype.";
   public static final SessionTypeComparator SESSION_TYPE_COMPARATOR  = new SessionTypeComparator();

   @Inject
   private JTBPreferenceStore                ps;

   private List<SessionType>                 sessionTypes;
   private SessionType                       stDefault;

   @PostConstruct
   private void initialize() throws Exception {
      log.debug("Initializing SessionTypeManager");

      stDefault = new SessionType(true, "default", "Default session type", SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));

      sessionTypes = new ArrayList<>();
      sessionTypes.add(stDefault);

      // Format cle.<nom>=<description>||<R>,<G>,<B>

      Map<String, String> sessionTypesFromPref = ps.getAllWithPrefix(Constants.PREF_SESSION_TYPE_PREFIX);
      for (Entry<String, String> e : sessionTypesFromPref.entrySet()) {
         String key = e.getKey();
         String value = e.getValue();

         String name = key.substring(Constants.PREF_SESSION_TYPE_PREFIX_LEN);
         try (Scanner s = new Scanner(value);) {
            s.useDelimiter(Constants.PREF_SESSION_TYPE_DELIMITER);
            String description = s.next();
            s.useDelimiter(Constants.PREF_SESSION_TYPE_DELIMITER_RGB);
            int r = s.nextInt();
            int g = s.nextInt();
            int b = s.nextInt();

            sessionTypes.add(new SessionType(false, name, description, SWTResourceManager.getColor(r, g, b)));
         }
      }

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

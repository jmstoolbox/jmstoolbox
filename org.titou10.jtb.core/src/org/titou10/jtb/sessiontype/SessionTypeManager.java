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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
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

   private static final Logger               log                     = LoggerFactory.getLogger(SessionTypeManager.class);

   private static final String               NAME_DESC_DEL           = "||";
   private static final int                  NAME_DESC_DEL_LEN       = NAME_DESC_DEL.length();
   private static final String               RGB_DEL                 = ",";
   private static final String               VALUE_PATTERN           = "%s" + NAME_DESC_DEL + "%d" + RGB_DEL + "%d" + RGB_DEL
                                                                       + "%d";

   public static final SessionTypeComparator SESSION_TYPE_COMPARATOR = new SessionTypeComparator();

   @Inject
   private JTBPreferenceStore                ps;

   private List<SessionType>                 sessionTypes;
   private SessionType                       defaultSessionType;

   @PostConstruct
   private void initialize() throws Exception {
      log.debug("Initializing SessionTypeManager");

      defaultSessionType = new SessionType(true,
                                           "default",
                                           "Default session type",
                                           SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND));

      sessionTypes = new ArrayList<>();
      sessionTypes.add(defaultSessionType);

      // Format cle.<nom>=<description>||<R>,<G>,<B>

      Map<String, String> sessionTypesFromPref = ps.getAllWithPrefix(Constants.PREF_SESSION_TYPE_PREFIX);
      for (Entry<String, String> e : sessionTypesFromPref.entrySet()) {
         String key = e.getKey();
         String value = e.getValue();

         String name = key.substring(Constants.PREF_SESSION_TYPE_PREFIX_LEN);

         int n = value.indexOf(NAME_DESC_DEL);
         String description = value.substring(0, n);
         String rgb = value.substring(description.length() + NAME_DESC_DEL_LEN, value.length());
         String[] rgbs = rgb.split(RGB_DEL);

         int r = Integer.parseInt(rgbs[0]);
         int g = Integer.parseInt(rgbs[1]);
         int b = Integer.parseInt(rgbs[2]);

         sessionTypes.add(new SessionType(false, name, description, SWTResourceManager.getColor(r, g, b)));
      }

      log.debug("SessionTypeManager initialized");

   }

   public void saveValues(List<SessionType> sessionTypes) throws IOException {

      ps.removeAllWithPrefix(Constants.PREF_SESSION_TYPE_PREFIX);
      for (SessionType st : sessionTypes) {
         if (st.equals(defaultSessionType)) {
            continue;
         }
         ps.setValue(buildPreferenceName(st), buildPreferenceValue(st));
      }

      ps.save();
   }

   public List<SessionType> restoreDefault() {
      sessionTypes = new ArrayList<>();
      sessionTypes.add(defaultSessionType);
      return sessionTypes;
   }

   public String buildPreferenceName(SessionType st) {
      return Constants.PREF_SESSION_TYPE_PREFIX + st.getName();
   }

   public String buildPreferenceValue(SessionType st) {
      RGB rgb = st.getColor().getRGB();
      return String.format(VALUE_PATTERN, st.getDescription(), rgb.red, rgb.green, rgb.blue);
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public List<SessionType> getSessionTypes() {
      return sessionTypes;
   }

   public SessionType getDefaultSessionType() {
      return defaultSessionType;
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

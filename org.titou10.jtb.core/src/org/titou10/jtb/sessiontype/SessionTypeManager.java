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
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
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

   public static final Color                 DEFAULT_COLOR           = SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND);

   private static final String               NAME_DESC_DEL           = "&&&";
   private static final String               VALUE_PATTERN           = "%s" + NAME_DESC_DEL + "%s";

   public static final SessionTypeComparator SESSION_TYPE_COMPARATOR = new SessionTypeComparator();

   @Inject
   private JTBPreferenceStore                ps;

   private List<SessionType>                 sessionTypes;

   @PostConstruct
   private void initialize() throws Exception {
      log.debug("Initializing SessionTypeManager");

      sessionTypes = new ArrayList<>();

      // Format: sessionttype.definition.<id>=<name>|||<R>,<G>,<B>
      // Read custom session types
      Map<String, String> sessionTypesFromPref = ps.getAllWithPrefix(Constants.PREF_SESSION_TYPE_PREFIX);
      for (Entry<String, String> e : sessionTypesFromPref.entrySet()) {
         String key = e.getKey();
         String value = e.getValue();

         String id = key.substring(Constants.PREF_SESSION_TYPE_PREFIX_LEN);

         String[] parts = value.split(NAME_DESC_DEL);
         sessionTypes.add(new SessionType(id, parts[0], SWTResourceManager.getColor(StringConverter.asRGB(parts[1]))));
      }

      // Add the standard type only once and save them
      boolean initialized = ps.getBoolean(Constants.PREF_SESSION_TYPE_INITIALIZED);
      if (!initialized) {
         ps.setValue(Constants.PREF_SESSION_TYPE_INITIALIZED, true);
         sessionTypes.addAll(getStandardSessionTypes());
         saveValues(sessionTypes);
      }

      log.debug("SessionTypeManager initialized");

   }

   public void saveValues(List<SessionType> sessionTypes) throws IOException {
      log.debug("saveValues: {}", sessionTypes);

      this.sessionTypes = sessionTypes;

      ps.removeAllWithPrefix(Constants.PREF_SESSION_TYPE_PREFIX);
      for (SessionType st : sessionTypes) {
         ps.setValue(buildPreferenceName(st), buildPreferenceValue(st));
      }

      ps.save();
   }

   public List<SessionType> getStandardSessionTypes() {
      List<SessionType> standardST = new ArrayList<>(3);
      standardST.add(new SessionType("dev", "Development", DEFAULT_COLOR));
      standardST.add(new SessionType("prd", "Production", SWTResourceManager.getColor(247, 159, 129)));
      standardST.add(new SessionType("qa", "QA", SWTResourceManager.getColor(196, 255, 181)));
      return standardST;
   }

   public String buildPreferenceName(SessionType st) {
      return Constants.PREF_SESSION_TYPE_PREFIX + st.getId();
   }

   public String buildPreferenceValue(SessionType st) {
      return String.format(VALUE_PATTERN, st.getName().trim(), StringConverter.asString(st.getColor().getRGB()));
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
         return o1.getName().compareTo(o2.getName());
      }
   }

}

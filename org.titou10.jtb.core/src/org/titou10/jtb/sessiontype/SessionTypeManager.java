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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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

   private static final Logger      log           = LoggerFactory.getLogger(SessionTypeManager.class);

   public static final Color        DEFAULT_COLOR = SWTResourceManager.getColor(SWT.COLOR_LIST_BACKGROUND);

   @Inject
   private JTBPreferenceStore       ps;

   private List<SessionType>        sessionTypes;
   private Map<String, SessionType> sessionTypesPerName;

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

         String name = key.substring(Constants.PREF_SESSION_TYPE_PREFIX_LEN);
         sessionTypes.add(new SessionType(name, SWTResourceManager.getColor(StringConverter.asRGB(value))));
      }

      // Add the standard type only once and save them
      boolean initialized = ps.getBoolean(Constants.PREF_SESSION_TYPE_INITIALIZED);
      if (!initialized) {
         ps.setValue(Constants.PREF_SESSION_TYPE_INITIALIZED, true);
         sessionTypes.addAll(getStandardSessionTypes());
         saveValues(sessionTypes);
      }

      Collections.sort(sessionTypes);
      sessionTypesPerName = sessionTypes.stream().collect(Collectors.toMap(SessionType::getName, st -> st));

      log.debug("SessionTypeManager initialized");

   }

   public void saveValues(List<SessionType> sessionTypes) throws IOException {
      log.debug("saveValues: {}", sessionTypes);

      this.sessionTypes = sessionTypes;
      this.sessionTypesPerName = sessionTypes.stream().collect(Collectors.toMap(SessionType::getName, st -> st));

      ps.removeAllWithPrefix(Constants.PREF_SESSION_TYPE_PREFIX);
      for (SessionType st : sessionTypes) {
         ps.setValue(Constants.PREF_SESSION_TYPE_PREFIX + st.getName(), StringConverter.asString(st.getColor().getRGB()));
      }

      ps.save();
   }

   public List<SessionType> getStandardSessionTypes() {
      List<SessionType> standardST = new ArrayList<>(3);
      standardST.add(new SessionType("Production", SWTResourceManager.getColor(247, 159, 129)));
      standardST.add(new SessionType("QA", SWTResourceManager.getColor(255, 255, 128)));
      standardST.add(new SessionType("Test", SWTResourceManager.getColor(196, 255, 181)));
      return standardST;
   }

   public Color getBackgroundColorForSessionTypeName(String sessionTypeName) {
      SessionType st = sessionTypesPerName.get(sessionTypeName);
      return st == null ? null : st.getColor();
   }

   public SessionType getSessionTypeFromSessionTypeName(String sessionTypeName) {
      return sessionTypesPerName.get(sessionTypeName);
   }

   // ------------------------
   // Standard Getters/Setters
   // ------------------------

   public List<SessionType> getSessionTypes() {
      return sessionTypes;
   }

}

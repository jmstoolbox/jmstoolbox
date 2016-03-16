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
package org.titou10.jtb.rest.util;

/**
 * 
 * Global constants
 * 
 * @author Denis Forveille
 *
 */
public final class Constants {

   public static final String  BASE                         = "org.titou10.jtb.rest.";

   // Preferences
   public static final String  PREF_REST_PORT               = "rest.port";
   public static final int     PREF_REST_PORT_DEFAULT       = 9998;
   public static final String  PREF_REST_AUTOSTART          = "rest.autostart";
   public static final boolean PREF_REST_AUTOSTART_DEFAULT  = false;

   // E4 Events

   public static final String  COMMAND_REST_STARTSTOP_PARAM = BASE + "rest.startstop.mode";
   public static final String  COMMAND_REST_STARTSTOP_START = "start";
   public static final String  COMMAND_REST_STARTSTOP_STOP  = "stop";

   private Constants() {
      // NOP
   }

}

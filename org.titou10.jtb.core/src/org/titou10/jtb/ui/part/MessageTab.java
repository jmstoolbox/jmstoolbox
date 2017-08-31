/*
 * Copyright (C) 2015-2017 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.ui.part;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum with Tab name included in the Message Viewer part
 * 
 * @author Denis Forveille
 *
 */
public enum MessageTab {
                        TO_STRING("toString"),
                        JMS_HEADERS("JMS Headers"),
                        PROPERTIES("Properties"),
                        PAYLOAD("Payload");

   private String          text;

   private static String[] displayTexts;

   static {
      List<String> list = new ArrayList<>();
      for (MessageTab messageTab : values()) {
         list.add(messageTab.text);
      }
      displayTexts = list.toArray(new String[list.size()]);
   }

   // -----------
   // Constructor
   // -----------
   private MessageTab(String text) {
      this.text = text;
   }

   // -----------
   // Helpers
   // -----------
   public static MessageTab fromText(String text) {
      for (MessageTab messageTab : values()) {
         if (messageTab.text.equals(text)) {
            return messageTab;
         }
      }
      return null;
   }

   public static int getIndexFromDisplayTexts(String messageTabName) {
      String messageTabText = valueOf(messageTabName).text;
      for (int i = 0; i < displayTexts.length; i++) {
         String string = displayTexts[i];
         if (string.equals(messageTabText)) {
            return i;
         }
      }
      return 0;
   }

   // -----------
   // Getters
   // -----------

   public String getText() {
      return text;
   }

   public static String[] getDisplayTexts() {
      return displayTexts;
   }

}

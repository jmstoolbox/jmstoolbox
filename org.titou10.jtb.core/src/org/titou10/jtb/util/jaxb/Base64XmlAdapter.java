/*
 * Copyright (C) 2025 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.util.jaxb;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB Adapter to transform a String in a base64 encoded String
 * 
 * @author Denis Forveille
 *
 */
public class Base64XmlAdapter extends XmlAdapter<String, String> {

   private static final String ENC_PREFIX     = "{##}";             // For old templates with standard serialization
   private static final int    ENC_PREFIX_LEN = ENC_PREFIX.length();

   @Override
   public String marshal(String xmlText) throws Exception {
      return (xmlText == null) ? null : ENC_PREFIX + Base64.getEncoder().encodeToString(xmlText.getBytes());
   }

   @Override
   public String unmarshal(String encodedValue) throws Exception {
      if (encodedValue == null) {
         return null;
      }

      if (encodedValue.startsWith(ENC_PREFIX)) {
         String d = encodedValue.substring(ENC_PREFIX_LEN);
         return new String(Base64.getDecoder().decode(d), StandardCharsets.UTF_8);
      } else {
         return encodedValue;
      }
   }

}

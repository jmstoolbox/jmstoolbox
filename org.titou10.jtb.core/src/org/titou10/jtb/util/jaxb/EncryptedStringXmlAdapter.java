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
package org.titou10.jtb.util.jaxb;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import org.titou10.jtb.util.EncryptUtils;

/**
 * JAXB Adapter to Manage Encrypted Strings (Passwords)
 * 
 * @author Denis Forveille
 *
 */
public class EncryptedStringXmlAdapter extends XmlAdapter<String, String> {

   @Override
   public String marshal(String text) throws Exception {
      // Encrypt Text
      return EncryptUtils.encrypt(text);
   }

   @Override
   public String unmarshal(String xmlText) throws Exception {
      return EncryptUtils.decrypt(xmlText);
   }

}

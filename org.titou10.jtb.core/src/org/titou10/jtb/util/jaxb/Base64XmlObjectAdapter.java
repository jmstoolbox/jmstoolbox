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
package org.titou10.jtb.util.jaxb;

import jakarta.xml.bind.DatatypeConverter;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB Adapter to transform a String in a base64 encoded String
 * 
 * @author Denis Forveille
 *
 */
public class Base64XmlObjectAdapter extends XmlAdapter<String, Object> {

   @Override
   public String marshal(Object object) throws Exception {
      if (object == null) {
         return null;
      }

      return DatatypeConverter.printBase64Binary(object.toString().getBytes());
   }

   @Override
   public Object unmarshal(String encodedValue) throws Exception {
      if (encodedValue == null) {
         return null;
      }

      byte[] decodedValue = DatatypeConverter.parseBase64Binary(encodedValue);
      return new String(decodedValue);
   }

}

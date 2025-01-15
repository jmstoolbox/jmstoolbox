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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Map;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB Adapter to Manage MapMessage payload (Map<String,Object>)
 * 
 * @author Denis Forveille
 *
 */
public class MapPayloadXmlAdapter extends XmlAdapter<String, Map<String, Object>> {

   @Override
   public String marshal(Map<String, Object> map) throws Exception {

      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos);) {
         oos.writeObject(map);
         return Base64.getEncoder().encodeToString(baos.toByteArray());
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   public Map<String, Object> unmarshal(String encodedValue) throws Exception {

      byte[] decodedValue = Base64.getDecoder().decode(encodedValue);
      try (ByteArrayInputStream bis = new ByteArrayInputStream(decodedValue); ObjectInputStream ois = new ObjectInputStream(bis);) {
         return (Map<String, Object>) ois.readObject();
      }
   }

}

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
import java.io.Serializable;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * JAXB Adapter to Manage ObjectMessage payload (Serializable object)
 * 
 * @author Denis Forveille
 *
 */
public class SerializableXmlAdapter extends XmlAdapter<String, Serializable> {

   @Override
   public String marshal(Serializable o) throws Exception {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos);) {
         oos.writeObject(o);
         String base64 = DatatypeConverter.printBase64Binary(baos.toByteArray());
         return base64;
      }
   }

   @Override
   public Serializable unmarshal(String xmlText) throws Exception {
      byte[] data = DatatypeConverter.parseBase64Binary(xmlText);
      try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));) {
         Object o = ois.readObject();
         return (Serializable) o;
      }
   }

}

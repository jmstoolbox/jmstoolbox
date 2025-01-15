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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAXB Adapter to Manage ObjectMessage payload (Serializable object)
 * 
 * @author Denis Forveille
 *
 */
public class SerializableXmlAdapter extends XmlAdapter<String, Serializable> {

   private static final Logger log = LoggerFactory.getLogger(SerializableXmlAdapter.class);

   @Override
   public String marshal(Serializable o) throws Exception {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos);) {
         oos.writeObject(o);
         return Base64.getEncoder().encodeToString(baos.toByteArray().toString().getBytes());
      } catch (Exception e) {
         log.error("Exception when marshalling object to JTBMessageTemplate", e);
         throw e;
      }
   }

   @Override
   public Serializable unmarshal(String xmlText) throws Exception {
      // FIXME: DF will ALWAYS fail because the implementation class, if defined, is in a classloder of a QM bundle...
      // Don't have a solution for this now...
      // Q: how to create an instance of "Serialization" without the implementaiton class on the classpath?
      byte[] data = Base64.getDecoder().decode(xmlText);
      try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));) {
         Object o = ois.readObject();
         return (Serializable) o;
      } catch (Exception e) {
         log.error("Exception when unmarshalling object from JTBMessageTemplate", e);
         throw e;
      }
   }

}

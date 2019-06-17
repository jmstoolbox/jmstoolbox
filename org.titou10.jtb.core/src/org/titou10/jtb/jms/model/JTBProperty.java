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
package org.titou10.jtb.jms.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.titou10.jtb.jms.qm.JMSPropertyKind;
import org.titou10.jtb.util.jaxb.Base64XmlObjectAdapter;

/**
 * Hold a JMS Property
 * 
 * @author Denis Forveille
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JTBProperty implements Serializable {
   private static final long serialVersionUID = 1L;

   private JMSPropertyKind   kind;

   private String            name;

   @XmlJavaTypeAdapter(Base64XmlObjectAdapter.class)
   private Object            value;

   // ------------
   // Constructors
   // ------------
   public JTBProperty() {
      // JAX-B
   }

   public JTBProperty(String name, Object objectProperty) {
      this(name, objectProperty, JMSPropertyKind.fromObjectClassname(objectProperty));
   }

   public JTBProperty(String name, Object value, JMSPropertyKind kind) {
      this.name = name;
      this.value = value;
      this.kind = kind;
   }

   // ------------
   // toString
   // ------------

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(128);
      builder.append("JTBProperty [name=");
      builder.append(name);
      builder.append(", value=");
      builder.append(value);
      builder.append(", kind=");
      builder.append(kind);
      builder.append("]");
      return builder.toString();
   }

   // ----------------------------
   // Standard Getters and Setters
   // ----------------------------

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Object getValue() {
      return value;
   }

   public void setValue(Object value) {
      this.value = value;
   }

   public JMSPropertyKind getKind() {
      return kind;
   }

   public void setKind(JMSPropertyKind kind) {
      this.kind = kind;
   }

}

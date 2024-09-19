/*
 * Copyright (C) 2021- Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.connector.transport;

import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import org.titou10.jtb.jms.model.JTBQueue;

/**
 * Queue exposed to an External Connector
 *
 * @author Denis Forveille
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@XmlType(propOrder = { "type" })
public class QueueOutput implements Serializable {
   private static final long serialVersionUID = 1L;

   private String            name;
   private Long              depth;

   // ------------
   // Constructors
   // ------------
   public QueueOutput() {
      // JAXB
   }

   public QueueOutput(JTBQueue jtbQueue, Long depth) {
      this.name = jtbQueue.getName();
      this.depth = depth;
   }

   // -------------------------
   // Standard Getters/Setters
   // -------------------------

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Long getDepth() {
      return depth;
   }

   public void setDepth(Long depth) {
      this.depth = depth;
   }
}

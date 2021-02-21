/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.titou10.jtb.qm.solace.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * From Apache v: https://johnzon.apache.org/
 *
 * https://raw.githubusercontent.com/apache/johnzon/master/johnzon-mapper/src/main/java/org/apache/johnzon/mapper/reflection/JohnzonParameterizedType.java
 *
 */
public class PType implements ParameterizedType {
   private final Type   rawType;
   private final Type[] types;

   public PType(final Type raw, final Type... types) {
      if (raw == null) {
         final PType userFinalType = findUserParameterizedType();
         this.rawType = userFinalType.getRawType();
         this.types = userFinalType.getActualTypeArguments();
      } else {
         this.rawType = raw;
         this.types = types;
      }
   }

   private PType findUserParameterizedType() {
      final Type genericSuperclass = getClass().getGenericSuperclass();
      if (!PType.class.isInstance(genericSuperclass)) {
         throw new IllegalArgumentException("raw can be null only for children classes");
      }
      final PType pt = PType.class.cast(genericSuperclass); // our type, then unwrap it

      final Type userType = pt.getActualTypeArguments()[0];
      if (!PType.class.isInstance(userType)) {
         throw new IllegalArgumentException("You need to pass a parameterized type to Johnzon*Types");
      }

      return PType.class.cast(userType);
   }

   @Override
   public Type[] getActualTypeArguments() {
      return types.clone();
   }

   @Override
   public Type getOwnerType() {
      return null;
   }

   @Override
   public Type getRawType() {
      return rawType;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = (prime * result) + Arrays.hashCode(types);
      result = (prime * result) + Objects.hash(rawType);
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (!(obj instanceof PType)) {
         return false;
      }
      PType other = (PType) obj;
      return Objects.equals(rawType, other.rawType) && Arrays.equals(types, other.types);
   }

   @Override
   public String toString() {
      final StringBuilder buffer = new StringBuilder(512);
      buffer.append(((Class<?>) rawType).getSimpleName());
      final Type[] actualTypes = getActualTypeArguments();
      if (actualTypes.length > 0) {
         buffer.append("<");
         int length = actualTypes.length;
         for (int i = 0; i < length; i++) {
            buffer.append(actualTypes[i].toString());
            if (i != (actualTypes.length - 1)) {
               buffer.append(",");
            }
         }

         buffer.append(">");
      }
      return buffer.toString();
   }
}

/*
 * Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
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
package org.titou10.jtb.rest;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Configure Jackson
 * 
 * https://github.com/FasterXML/jackson-databind/wiki
 * 
 * @author Denis Forveille
 *
 */
@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {
   private final ObjectMapper objectMapper;

   public JacksonConfig() throws Exception {
      objectMapper = new ObjectMapper();

      // Disable null fields
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

      // Do not print empty arrays
      objectMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);

      // Format output
      objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
   }

   @Override
   public ObjectMapper getContext(Class<?> ctx) {
      return objectMapper;
   }
}

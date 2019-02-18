/*
 * Copyright (C) 2019 Denis Forveille titou10.titou10@gmail.com
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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

/**
 * 
 * Convert Emty Strings to null
 * 
 * https://github.com/FasterXML/jackson-databind/issues/768
 * 
 * @author Denis Forveille
 *
 */
public class EmptyStringDeserializer extends JsonDeserializer<String> {

   @Override
   public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

      if ((jp.getCurrentToken() == JsonToken.VALUE_STRING) && (jp.getText().isEmpty())) {
         return null;
      }

      return StringDeserializer.instance.deserialize(jp, ctxt);
   }

}

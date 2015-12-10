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
package org.titou10.jtb.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Utility class for text formatting
 * 
 * @author Denis Forveille
 *
 */
public final class FormatUtils {

   private static final Logger log                    = LoggerFactory.getLogger(FormatUtils.class);

   private static final String XML_DECLARATION_PREFIX = "<?xml";
   private static final String XML_DECLARATION_SUFFIX = ">";
   private static final String CR                     = "\n";
   private static final String INDENT_STRING          = "{http://xml.apache.org/xslt}indent-amount";
   private static final String INDENT                 = "3";

   private static final String NOT_XML                = "(A problem occured when formatting the text as xml. Was it valid xml?)";
   private static final String EMPTY_XML              = "(No xml text to show. Was it valid xml?)";

   public static String jsonPrettyFormat(String unformattedText, boolean sourceIfError) {

      if (unformattedText == null) {
         return "";
      }

      try {
         JsonReader jr = Json.createReader(new StringReader(unformattedText));
         JsonObject jobj = jr.readObject();

         Map<String, Boolean> config = new HashMap<>();
         config.put(JsonGenerator.PRETTY_PRINTING, true);
         JsonWriterFactory jwf = Json.createWriterFactory(config);

         StringWriter sw = new StringWriter();
         try (JsonWriter jsonWriter = jwf.createWriter(sw)) {
            jsonWriter.writeObject(jobj);
         }
         return sw.toString();
      } catch (Exception e) {
         log.warn("Probleme when parsing json : {}", e.getMessage());
         return unformattedText;
      }

   }

   public static String xmlPrettyFormat(String unformattedText, boolean sourceIfError) {

      if (unformattedText == null) {
         return "";
      }

      // Fast Fail 
      if (!(unformattedText.trim().startsWith("<"))) {
         if (sourceIfError) {
            return unformattedText;
         } else {
            return EMPTY_XML;
         }
      }

      try {
         Source xmlInput = new StreamSource(new StringReader(unformattedText));
         StringWriter stringWriter = new StringWriter();
         StreamResult xmlOutput = new StreamResult(stringWriter);

         TransformerFactory transformerFactory = TransformerFactory.newInstance();

         Transformer transformer = transformerFactory.newTransformer();
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         transformer.setOutputProperty(INDENT_STRING, INDENT);

         transformer.transform(xmlInput, xmlOutput);

         String output = xmlOutput.getWriter().toString();

         // Copy back the XML Declaration if present
         if (unformattedText.startsWith(XML_DECLARATION_PREFIX)) {
            int n = unformattedText.indexOf(XML_DECLARATION_SUFFIX);
            String prefix = unformattedText.substring(0, n + 1);
            output = prefix + CR + output;
         }
         if (output.isEmpty()) {
            if (sourceIfError) {
               return unformattedText;
            } else {
               return EMPTY_XML;
            }
         } else {
            return output;
         }
      } catch (Exception e) {
         log.warn("Exception when formatting XML : {}", e.getMessage());
         if (sourceIfError) {
            return unformattedText;
         } else {
            return NOT_XML;
         }
      }
   }

   // ------------------
   // Pure Utility Class
   // ------------------
   private FormatUtils() {
      //NOP
   }

}

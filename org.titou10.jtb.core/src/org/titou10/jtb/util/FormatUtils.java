/*
 * Copyright (C) 2024 Denis Forveille titou10.titou10@gmail.com
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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.stream.JsonGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.JTBPreferenceStore;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * Utility class for text formatting
 *
 * @author Denis Forveille
 *
 */
public final class FormatUtils {

   private static final Logger                 log                    = LoggerFactory.getLogger(FormatUtils.class);

   private static final String                 XML_DECLARATION_PREFIX = "<?xml";
   private static final String                 XML_DECLARATION_SUFFIX = ">";
   private static final String                 XPATH_REMOTE_SPACES    = "//text()[normalize-space()='']";

   private static final String                 CR                     = "\n";
   private static final String                 INDENT_STRING          = "{http://xml.apache.org/xslt}indent-amount";

   private static final String                 NOT_XML                = "(A problem occured when formatting the text as xml. The payload was probably not valid xml)";
   private static final String                 EMPTY_XML              = "(No xml text to show. The payload was probably not valid xml)";
   private static final String                 EMPTY_JSON             = "(No JSON text to show. The payload was probably not valid JSON)";

   private static final DocumentBuilderFactory DB_FACTORY             = DocumentBuilderFactory.newInstance();
   private static final XPath                  X_PATH                 = XPathFactory.newInstance().newXPath();
   private static final TransformerFactory     T_FACTORY              = TransformerFactory.newInstance();

   public static String jsonPrettyFormat(String unformattedText, boolean sourceIfError) {

      if (unformattedText == null) {
         return "";
      }

      var sw = new StringWriter();
      var jwf = Json.createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));

      try {
         JsonValue jsonValue;
         try (JsonReader jsonReader = Json.createReader(new StringReader(unformattedText))) {
            jsonValue = jsonReader.readValue();
         }
         try (JsonWriter jsonWriter = jwf.createWriter(sw)) {
            jsonWriter.write(jsonValue);
         }
         return sw.toString();

      } catch (Exception e) {
         log.warn("Problem occurred when parsing json : {}", e.getMessage());
         if (sourceIfError) {
            return unformattedText;
         } else {
            return EMPTY_JSON;
         }
      }

   }

   public static String xmlPrettyFormat(JTBPreferenceStore ps, String unformattedText, boolean sourceIfError) {

      // Fast Fail
      if (unformattedText == null) {
         return "";
      }
      if (!(unformattedText.trim().startsWith("<"))) {
         if (sourceIfError) {
            return unformattedText;
         } else {
            return EMPTY_XML;
         }
      }

      try {
         // http://stackoverflow.com/questions/25864316/pretty-print-xml-in-java-8/33541820#33541820

         // Turn xml string into a document
         var documentBuilder = DB_FACTORY.newDocumentBuilder();
         var document = documentBuilder.parse(new InputSource(new ByteArrayInputStream(unformattedText.getBytes("UTF-8"))));

         // Remove whitespaces outside tags
         var nodeList = (NodeList) X_PATH.evaluate(XPATH_REMOTE_SPACES, document, XPathConstants.NODESET);
         for (var i = 0; i < nodeList.getLength(); ++i) {
            var node = nodeList.item(i);
            node.getParentNode().removeChild(node);
         }

         // Pretty Format
         Source xmlInput = new DOMSource(document);
         var stringWriter = new StringWriter();
         var xmlOutput = new StreamResult(stringWriter);

         var transformer = T_FACTORY.newTransformer();
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
         transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
         transformer.setOutputProperty(INDENT_STRING, String.valueOf(ps.getInt(Constants.PREF_XML_INDENT)));

         transformer.transform(xmlInput, xmlOutput);

         var output = xmlOutput.getWriter().toString();

         // Copy back the XML Declaration if present
         if (unformattedText.startsWith(XML_DECLARATION_PREFIX)) {
            var n = unformattedText.indexOf(XML_DECLARATION_SUFFIX);
            var prefix = unformattedText.substring(0, n + 1);
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
      // NOP
   }

}

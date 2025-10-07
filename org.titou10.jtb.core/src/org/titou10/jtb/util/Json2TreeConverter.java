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
package org.titou10.jtb.util;

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to convert string as JSON and insert it into an SWT {@link Tree}.
 * 
 * <pre>
 * Tree tree = ...;
 * String propablyJsonText = ...;
 * 
 * Json2TreeConverter.parseTree(treePayloadJSON, propablyJsonText);
 * </pre>
 * 
 * @author Thomas Jancar (pmias)
 * @version 1.0 - 25.09.2025
 */
public final class Json2TreeConverter {

   private static final Logger log = LoggerFactory.getLogger(Json2TreeConverter.class);

   private Json2TreeConverter() {
   }

   /**
    * Parse a string as a JSON structure and convert it to a tree structure. If string cannot be parsed as JSON, a single tree item
    * with an error message will be shown.
    * 
    * @param tree
    *           SWT tree.
    * @param txt
    *           string (probably JSON)
    */
   public static void parseTree(Tree tree, String txt) {

      tree.removeAll();

      // Fast Fail
      if (Utils.isEmpty(txt)) {
         return;
      }

      // The root item - either with the parsed JSON content or for error message.
      TreeItem root = new TreeItem(tree, SWT.NONE);

      // Parse string as JSON and insert it into root tree item.
      try (JsonReader reader = Json.createReader(new StringReader(txt))) {
         insertJsonType(root, reader.read());
      } catch (Exception e) {
         log.warn("Problem occurred when parsing json : {}", e.getMessage());
         root.removeAll();
         root.setText("(No JSON tree to show. The payload was probably not valid JSON)");
      }

      expandAll(root, true);
   }

   /**
    * Expand or collapse a {@link TreeItem} and all its children.
    * 
    * @param treeItem
    *           tree item to expand/collapse.
    * @param expand
    *           if true, expand; else collapse.
    */
   private static void expandAll(TreeItem treeItem, boolean expand) {
      treeItem.setExpanded(expand);
      for (TreeItem ti : treeItem.getItems()) {
         expandAll(ti, expand);
      }
   }

   /**
    * Make new {@link TreeItem} with text.
    * 
    * @param parentItem
    *           parent tree item to add the new tree item as a child.
    * @param text
    *           optional text of the new tree item.
    * @return generated tree item.
    */
   private static TreeItem newItem(TreeItem parentItem, String text) {
      TreeItem childItem = new TreeItem(parentItem, SWT.NONE);
      if (null != text) {
         childItem.setText(text);
      }
      return childItem;
   }

   /**
    * Append a string with a space to an existing title of an {@link TreeItem}.
    * 
    * @param treeItem
    *           tree item to change the title.
    * @param append
    *           append string for the title.
    */
   private static void appendTitle(TreeItem treeItem, String append) {
      String txt = treeItem.getText();
      txt = null == txt ? append : txt + ' ' + append;
      treeItem.setText(txt);
   }

   /**
    * Append a {@link JsonValue} to a {@link TreeItem}, either as string value to its title or as a new child.
    * 
    * @param parentItem
    *           tree item to add the JSÖN value, depending on the value's type.
    * @param jsonValue
    *           JSON value.
    */
   private static void insertJsonType(TreeItem parentItem, JsonValue jsonValue) {

      // Add these JSON values into the parent's title.
      switch (jsonValue.getValueType()) {
         case STRING -> appendTitle(parentItem, ((JsonString) jsonValue).getString());
         case NUMBER -> appendTitle(parentItem, jsonValue.toString());
         case FALSE, TRUE, NULL -> appendTitle(parentItem, jsonValue.getValueType().toString());
         case ARRAY -> ((JsonArray) jsonValue).forEach(val -> {
            TreeItem child = newItem(parentItem, "[ ]");
            insertJsonType(child, val);
         });
         case OBJECT -> ((JsonObject) jsonValue).forEach((key, val) -> {
            TreeItem child = newItem(parentItem, key + ':');
            insertJsonType(child, val);
         });
      }
   }
}

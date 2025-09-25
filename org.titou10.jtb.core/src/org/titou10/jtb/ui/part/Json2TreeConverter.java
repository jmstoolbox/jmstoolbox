package org.titou10.jtb.ui.part;

import java.io.StringReader;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;


/**
 * Helper class to convert string as JSON and insert
 * it into an SWT {@link Tree}.
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

   /** No instance - only one public static method. */
   private Json2TreeConverter() {}
   
   /**
    * Parse a string as a JSON structure and convert
    * it to a tree structure. If string cannot be parsed
    * as JSON, a single tree item with an error message
    * will be shown.
    * 
    * @param tree SWT tree.
    * @param txt string (probably JSON)
    */
   public static void parseTree(Tree tree, String txt) {

      // Remove all previous structures.
      tree.removeAll();
      
      // Nothing to view.
      if (null == txt || txt.isBlank()) return;

      // The root item - either with the parsed JSON content or for error message.
      TreeItem root = new TreeItem(tree, SWT.NONE);

      // Parse string as JSON and insert it into root tree item.
      // If string wasn't JSON, a JsonParsingException will be thrown;
      // in this case, show an error message in the root tree item.
      try {
         JsonReader reader = Json.createReader(new StringReader(txt));
         JsonStructure struct = reader.read();

         insertJsonType(root, struct);
      } catch (JsonParsingException e) {
         root.removeAll();
         // root.setText(e.toString());
         root.setText("(No json tree to show. The payload was probably not valid json)");
      }

      expandAll(root, true);
   }

   
   /**
    * Expand or collapse a {@link TreeItem} and all its children.
    * 
    * @param treeItem tree item to expand/collapse.
    * @param expand if true, expand; else collapse.
    */
   private static void expandAll(TreeItem treeItem, boolean expand) {
      treeItem.setExpanded(expand);
      for (TreeItem ti : treeItem.getItems())
         expandAll(ti, expand);
   }


   /**
    * Make new {@link TreeItem} with text.
    * 
    * @param parentItem parent tree item to add the new tree item as a child.
    * @param text optional text of the new tree item.
    * @return generated tree item.
    */
   private static TreeItem newItem(TreeItem parentItem, String text) {
      TreeItem childItem = new TreeItem(parentItem, SWT.NONE);
      if (null != text)
         childItem.setText(text);
      return childItem;
   }

   
   /**
    * Append a string with a space to an existing title of an {@link TreeItem}.
    * 
    * @param treeItem tree item to change the title.
    * @param append append string for the title.
    */
   private static void appendTitle(TreeItem treeItem, String append) {
      String txt = treeItem.getText();
      txt = null == txt ? append : txt + ' ' + append;
      treeItem.setText(txt);
   }
   

   /**
    * Append a {@link JsonValue} to a {@link TreeItem}, either as string value
    * to its title or as a new child.
    * 
    * @param parentItem tree item to add the JSÖN value, depending on the value's type.
    * @param jsonValue JSON value.
    */
   private static void insertJsonType(TreeItem parentItem, JsonValue jsonValue) {
      switch (jsonValue.getValueType()) {
         
         // Add these JSON values into the parent's title.
         case STRING:
            appendTitle(parentItem, ((JsonString) jsonValue).getString());
            break;

         case NUMBER:
            appendTitle(parentItem, jsonValue.toString());
            break;

         case FALSE: // fallthrough;
         case TRUE:  // fallthrough;
         case NULL:
            appendTitle(parentItem, jsonValue.getValueType().toString());
            break;

         // Add array elements as new children.
         case ARRAY:
            JsonArray array = (JsonArray) jsonValue;
            for (JsonValue val : array) {
               TreeItem childItem = newItem(parentItem, "[ ]");
               insertJsonType(childItem, val);
            }
            break;

         // Add sub elements of JSON object as new childen.
         case OBJECT:
            JsonObject object = (JsonObject) jsonValue;
            for (String key : object.keySet()) {
               TreeItem childItem = newItem(parentItem, key + ':');
               insertJsonType(childItem, object.get(key));
            }
            break;
      }

   }

}

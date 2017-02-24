package org.titou10.jtb.ui.hex;

public class StringDataProvider extends AbstractDataProvider {

   public StringDataProvider(String text) {
      size = text.length();
      data = text.getBytes();
   }
}

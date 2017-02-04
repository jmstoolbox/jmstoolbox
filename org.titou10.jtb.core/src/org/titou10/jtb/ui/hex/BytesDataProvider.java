package org.titou10.jtb.ui.hex;

public class BytesDataProvider extends AbstractDataProvider {

   public BytesDataProvider(byte[] bytes) {
      size = bytes.length;
      data = bytes;
   }
}

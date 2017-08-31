package org.titou10.jtb.ui.hex;

import java.io.FileInputStream;

public class FileDataProvider extends AbstractDataProvider {

   public FileDataProvider(String filename) {
      try {
         FileInputStream fis = new FileInputStream(filename);
         size = fis.available();
         data = new byte[size];
         fis.read(data);
         fis.close();
      } catch (Exception e) {
         data = new byte[0];
         size = 0;
      }
   }
}

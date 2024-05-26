package org.titou10.jtb.ui.hex;

public class AbstractDataProvider implements IDataProvider {

   protected byte[] data;
   protected int    size;
   protected int    bytesPerRow;

   @Override
   public void setBytesPerRow(int bpr) {
      this.bytesPerRow = bpr;
   }

   public int getRowCount() {
      // use the following code to calculate the number of rows needed:
      // 0 : 0
      // 1 - 16 : 1
      // 17 - 32: 2
      // ...
      return (size + bytesPerRow - 1) / bytesPerRow;
   }

   public int getDataSize() {
      return size;
   }

   public String getRowDescriptor(int rowNumber) {
      String rowStr = Integer.toString(rowNumber * bytesPerRow, 16).toUpperCase();
      while (rowStr.length() < 8) {
         rowStr = "0" + rowStr;
      }
      return rowStr;
   }

   public int getData(Byte[] arr, int rowNumber) {
      int pos = rowNumber * bytesPerRow;
      int i = 0;
      for (i = 0; i < bytesPerRow; i++) {
         if (pos >= data.length) {
            break;
         }
         arr[i] = Byte.valueOf(data[pos]);
         pos++;
      }
      int res = i;
      for (; i < bytesPerRow; i++) {
         arr[i] = null;
      }
      return res;
   }

}

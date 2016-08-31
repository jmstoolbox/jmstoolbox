package org.titou10.jtb.ui.hex;

public interface IDataProvider {

   void setBytesPerRow(int bpr);

   int getRowCount();

   int getDataSize();

   String getRowDescriptor(int rowNumber);

   int getData(Byte[] arr, int rowNumber);

}

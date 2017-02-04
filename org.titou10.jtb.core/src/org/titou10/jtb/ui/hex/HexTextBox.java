package org.titou10.jtb.ui.hex;

import org.eclipse.swt.custom.StyleRange;

class HexTextBox extends BinaryTextBox {
   static String   HEX_VALS   = "0123456789ABCDEF";
   static String[] BYTE_2_STR = null;

   public HexTextBox(HexViewer hex, int bpr) {
      super(hex, bpr);
      if (BYTE_2_STR == null) {
         BYTE_2_STR = new String[256];
         char[] arr = new char[2];
         for (int i = 0; i < 256; i++) {
            arr[0] = HEX_VALS.charAt((i >> 4) & 0x0F);
            arr[1] = HEX_VALS.charAt(i & 0x0F);
            BYTE_2_STR[i] = String.copyValueOf(arr);
         }
      }
   }

   protected void calcPositions() {
      int pos = 0;
      for (int i = 0; i < bytesPerRow; i++) {
         beforePos[i] = pos;
         pos += 2;
         afterPos[i] = pos;
         pos++;
      }
      charsPerRow = afterPos[bytesPerRow - 1] + 1;
   }

   public void appendRow(IDataProvider idp, int row, boolean isLastRow) {
      int bytes = idp.getData(rowTemp, row);
      for (int i = 0; i < bytes; i++) {
         Byte b = rowTemp[i];
         if (b == null) {
            styleRanges.add(new StyleRange(sbTemp.length(), 2, null, HexViewer.red));
            sbTemp.append("??");
         } else {
            sbTemp.append(BYTE_2_STR[b.intValue() & 0x0FF]);
         }
         if (i == bytesPerRow - 1) {
            // add new line, unless it is the last row
            if (!isLastRow) {
               sbTemp.append('\n');
            }
         } else {
            if (i != bytes - 1) {
               sbTemp.append(' ');
            }
         }
      }
   }
}

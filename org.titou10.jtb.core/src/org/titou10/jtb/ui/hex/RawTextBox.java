package org.titou10.jtb.ui.hex;

import org.eclipse.swt.custom.StyleRange;

class RawTextBox extends BinaryTextBox {
   char UNKNOWN_CHAR      = '?';
   char NONPRINTABLE_CHAR = '.';

   public RawTextBox(HexViewer hex, int bpr) {
      super(hex, bpr);
   }

   protected void calcPositions() {
      int pos = 0;
      for (int i = 0; i < bytesPerRow; i++) {
         beforePos[i] = pos;
         pos += 1;
         afterPos[i] = pos;
      }
      charsPerRow = afterPos[bytesPerRow - 1] + 1;
   }

   public void appendRow(IDataProvider idp, int row, boolean isLastRow) {
      int bytes = idp.getData(rowTemp, row);
      for (int i = 0; i < bytes; i++) {
         Byte b = rowTemp[i];
         if (b == null) {
            styleRanges.add(new StyleRange(sbTemp.length(), 1, null, HexViewer.red));
            sbTemp.append(UNKNOWN_CHAR);
         } else {
            int x = b.intValue();
            if (x < 32 || x > 126) {
               // non-printable char
               sbTemp.append(NONPRINTABLE_CHAR);
            } else {
               sbTemp.append((char) x);
            }
         }

         if (i == bytesPerRow - 1) {
            // add new line, unless it is the last row
            if (!isLastRow) {
               sbTemp.append('\n');
            }
         }
      }
   }
}

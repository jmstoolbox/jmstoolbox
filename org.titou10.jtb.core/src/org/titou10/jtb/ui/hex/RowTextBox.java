package org.titou10.jtb.ui.hex;

public class RowTextBox extends BinaryTextBox {

   // StyledText txt;

   public RowTextBox(final HexViewer hex, int bpr) {
      super(hex, bpr);
   }

   protected void calcPositions() {
      for (int i = 0; i < bytesPerRow; i++) {
         beforePos[i] = 0;
         afterPos[i] = 8;
      }
      charsPerRow = afterPos[bytesPerRow - 1] + 1;
   }

   public void appendRow(IDataProvider idp, int row, boolean isLastRow) {
      sbTemp.append(idp.getRowDescriptor(row));
      if (!isLastRow) {
         sbTemp.append('\n');
      }
   }
}

package org.titou10.jtb.ui.hex;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.wb.swt.SWTResourceManager;

public class HexViewer extends Composite {

   static Font           fnt         = SWTResourceManager.getFont("Courier New", 10, 0);
   static Color          defaultBack = SWTResourceManager.getColor(255, 255, 255);
   static Color          defaultFore = SWTResourceManager.getColor(0, 0, 0);
   static Color          red         = SWTResourceManager.getColor(SWT.COLOR_RED);

   private RowTextBox    rowText;
   private HexTextBox    hexText;
   private RawTextBox    rawText;
   private Slider        sld;
   private Color         backcolor;
   private Color         forecolor;

   private IDataProvider idp;
   private int           rows;                                                          // number of rows in the data

   private int           showStartRow;                                                  // first line shown in the text boxes
   private int           maxShowStart;                                                  // maximal start row to show
   private int           rowsInView;                                                    // number of rows fit in the text
                                                                                        // boxes
   private int           selectStartByte;                                               // selection start
   private int           selectEndByte;                                                 // selection end
   private int           bytesPerRow;                                                   // number of bytes in a row to show

   public HexViewer(Composite parent, int style, IDataProvider idp, int bytesPerRow) {
      super(parent, style);
      this.setLayout(new GridLayout(4, false));
      // create all widgest with a standard 4-columns grid-layout

      this.bytesPerRow = bytesPerRow;
      rowText = new RowTextBox(this, bytesPerRow);
      hexText = new HexTextBox(this, bytesPerRow);
      rawText = new RawTextBox(this, bytesPerRow);
      initSlider();
      setBackground(defaultBack);
      setForeground(defaultFore);

      this.idp = idp;
      if (idp == null) {
         rows = 0;
      } else {
         idp.setBytesPerRow(bytesPerRow);
         rows = idp.getRowCount();
      }

      showStartRow = 0;
      selectStartByte = 0;
      selectEndByte = 0;
      this.addControlListener(new ControlListener() {
         public void controlMoved(ControlEvent e) {
            doResizeCalc();
         }

         public void controlResized(ControlEvent e) {
            doResizeCalc();
         }
      });
      doResizeCalc();
   }

   /// on resize, recalculate sizes, and draw what necessary.
   protected void doResizeCalc() {
      rowsInView = hexText.calcRowsInView();
      rawText.setRowsInView(rowsInView);

      maxShowStart = rows - rowsInView;
      if (maxShowStart < 0) {
         maxShowStart = 0;
      }
      showStartRow = fix(showStartRow, maxShowStart);

      if (rows <= rowsInView) {
         // all rows will fit
         sld.setEnabled(false);
      } else {
         // minimum row start is 0
         // last row start is rows - rowsInView, but we use +1 in order to include the last row too
         sld.setEnabled(true);
         sld.setValues(showStartRow, 0, maxShowStart + 1, 1, 1, rowsInView);
      }
      showData();
      showSelection();
   }

   /// make sure a specific address is shown
   protected void showCaret(int ca) {
      int row = ca / bytesPerRow;

      if (row >= rows) {
         // do not continue one line down, if it's the last byte
         // and it's in the end of a line
         row--;
      }
      if (showStartRow + rowsInView <= row) {
         // row is ahead
         showStartRow = row - rowsInView + 1;
         sld.setSelection(showStartRow);
         showData();
      } else
         if (row < showStartRow) {
            // row is behind
            showStartRow = row;
            sld.setSelection(showStartRow);
            showData();
         }
   }

   /// write all the needed data to the text-boxes
   protected void showData() {
      if (idp == null) {
         hexText.clearText();
         rawText.clearText();
         rowText.clearText();
         return;
      }

      // iterate over all shown rows
      int maxRow = showStartRow + rowsInView;
      if (maxRow > rows) {
         maxRow = rows;
      }
      hexText.initText();
      rawText.initText();
      rowText.initText();

      for (int r = showStartRow; r < maxRow; r++) {
         // iterate over all bytes in the row
         boolean isLastRow = (r == maxRow - 1);
         hexText.appendRow(idp, r, isLastRow);
         rawText.appendRow(idp, r, isLastRow);
         rowText.appendRow(idp, r, isLastRow);
      }
      hexText.showText();
      rawText.showText();
      rowText.showText();
   }

   /// fix a selection position to fit in range [0,maxpos]
   static int fix(int pos, int maxpos) {
      if (pos < 0) {
         pos = 0;
      }
      if (pos > maxpos) {
         pos = maxpos;
      }
      return pos;
   }

   /// shows the current selection in both text boxes
   protected void showSelection() {
      hexText.showSelection(selectStartByte, selectEndByte);
      rawText.showSelection(selectStartByte, selectEndByte);
      rowText.showSelection(selectStartByte, selectEndByte);
   }

   /// initialize the vertical slider
   private void initSlider() {
      sld = new Slider(this, SWT.V_SCROLL);
      GridData gd = new GridData();
      gd.widthHint = 18;
      gd.verticalAlignment = SWT.FILL;
      gd.grabExcessVerticalSpace = true;
      sld.setLayoutData(gd);
      sld.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            showStartRow = sld.getSelection();
            showData();
            showSelection();
         }
      });
   }

   public void setSelectEnd(int ca) {
      selectEndByte = ca;
   }

   public int getSelectEnd() {
      return selectEndByte;
   }

   public void setSelectStart(int ca) {
      selectStartByte = ca;
   }

   public int getSelectStart() {
      return selectStartByte;
   }

   public void setShowStart(int i) {
      i = fix(i, maxShowStart);
      if (showStartRow != i) {
         showStartRow = i;
         sld.setSelection(showStartRow);
         showData();
         showSelection();
      }
   }

   public int getShowStart() {
      return showStartRow;
   }

   public int getBytesPerRow() {
      return bytesPerRow;
   }

   public int getDataSize() {
      if (idp == null) {
         return 0;
      }
      return idp.getDataSize();
   }

   public int getRowsInView() {
      return rowsInView;
   }

   public void setDataProvider(IDataProvider idp) {
      this.idp = idp;
      if (idp == null) {
         rows = 0;
      } else {
         idp.setBytesPerRow(bytesPerRow);
         rows = idp.getRowCount();
      }

      showStartRow = 0;
      selectStartByte = 0;
      selectEndByte = 0;
      doResizeCalc();
      showData();
      showSelection();
   }

   public void setBackground(Color color) {
      this.backcolor = color;
      rowText.setBackground(color);
      hexText.setBackground(color);
      rawText.setBackground(color);
   }

   public void setForeground(Color color) {
      this.forecolor = color;
      rowText.setForeground(color);
      hexText.setForeground(color);
      rawText.setForeground(color);
   }

   public Color getBackground() {
      return this.backcolor;
   }

   public Color getForeground() {
      return this.forecolor;
   }
}

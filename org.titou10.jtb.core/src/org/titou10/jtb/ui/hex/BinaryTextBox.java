package org.titou10.jtb.ui.hex;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.titou10.jtb.util.FontUtils;

abstract class BinaryTextBox {
   int txtCharHeight;
   double txtCharWidth;

   protected StyledText txt;
   protected HexViewer hex;

   protected int rowsInView;
   protected int bytesPerRow;
   protected int charsPerRow;
   protected int beforePos[];
   protected int afterPos[];

   protected Byte rowTemp[];
   protected StringBuilder sbTemp;
   protected List<StyleRange> styleRanges;

   public BinaryTextBox(final HexViewer hex, int bpr) {
      this.hex = hex;
      this.bytesPerRow = bpr;
      beforePos = new int[bpr];
      afterPos = new int[bpr];
      rowTemp = new Byte[bpr];
      calcPositions();

      txt = new StyledText(hex, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI);
      txt.setFont(HexViewer.fnt);
      GridData gd = new GridData();
      gd.widthHint = getWidth();
      gd.verticalAlignment = SWT.FILL;
      gd.grabExcessVerticalSpace = true;
      txt.setLayoutData(gd);
      txt.addMouseListener(new MouseListener() {
         public void mouseDoubleClick(MouseEvent e) {
            // NOP
         }

         public void mouseDown(MouseEvent e) {
            if (txt.isFocusControl()) {
               int ca = getAddressByPos(getCaretPos(e.x, e.y));
               hex.setSelectEnd(ca);
               if ((e.stateMask & SWT.SHIFT) == 0) {
                  hex.setSelectStart(ca);
               }
               hex.showSelection();
               txt.setFocus();
            }
         }

         public void mouseUp(MouseEvent e) {
            if (txt.isFocusControl()) {
               int ca = getAddressByPos(getCaretPos(e.x, e.y));
               hex.setSelectEnd(ca);
               hex.showSelection();
               txt.setFocus();
            }
         }
      });
      txt.addMouseMoveListener(new MouseMoveListener() {
         public void mouseMove(MouseEvent e) {
            if (txt.isFocusControl()) {
               if ((e.stateMask & SWT.BUTTON1) != 0) {
                  int ca = getAddressByPos(getCaretPos(e.x, e.y));
                  hex.setSelectEnd(ca);
                  // scroll if out of bounds
                  if (e.y < 0) {
                     hex.setShowStart(hex.getShowStart() - 1);
                  } else
                     if (e.y > txt.getSize().y) {
                     hex.setShowStart(hex.getShowStart() + 1);
                  }
                  hex.showSelection();
                  txt.setFocus();
               }
            }
         }
      });
      txt.addListener(SWT.MouseWheel, new Listener() {
         public void handleEvent(Event event) {
            hex.setShowStart(hex.getShowStart() - event.count);
            txt.setFocus();
         }
      });
      txt.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
         // abort all traversal keys
         e.doit = false;
         int caretAddress = hex.getSelectEnd();

         // move the caret according to key
         switch (e.keyCode) {
         case SWT.ARROW_DOWN:
            caretAddress = caretAddress + bytesPerRow;
            break;
         case SWT.ARROW_UP:
            caretAddress = caretAddress - bytesPerRow;
         case SWT.ARROW_RIGHT:
            caretAddress = caretAddress + 1;
            break;
         case SWT.ARROW_LEFT:
            caretAddress = caretAddress - 1;
            break;
         case SWT.PAGE_DOWN:
            caretAddress = caretAddress + bytesPerRow * rowsInView;
            break;
         case SWT.PAGE_UP:
            caretAddress = caretAddress - bytesPerRow * rowsInView;
            break;
         case SWT.HOME:
            caretAddress = 0;
            break;
         case SWT.END:
            caretAddress = hex.getDataSize();
            break;
         default:
            // no traversal key
            return;
         }

         caretAddress = HexViewer.fix(caretAddress, hex.getDataSize());

         hex.setSelectEnd(caretAddress);
         if ((e.stateMask & SWT.SHIFT) == 0) {
            // if shift is not pressed, change both selection end and start
            hex.setSelectStart(caretAddress);
         }
         hex.showCaret(hex.getSelectEnd());
         hex.showSelection();
         txt.setFocus();
      }));
   }

   protected int getCaretPos(int x, int y) {
      int row = y / getCharHeight();
      int col = (int) Math.round(x / getCharWidht());
      return row * charsPerRow + col;
   }

   protected int getAddressByPos(int pos) {
      int row = pos / charsPerRow;
      int col = pos % charsPerRow;
      int addr = (hex.getShowStart() + row) * bytesPerRow;
      // simulate the printing method, and count position
      for (int i = 0; i < bytesPerRow; i++) {
         if (afterPos[i] > col) {
            break;
         }
         addr++;
      }
      return addr;
   }

   protected int getPosByAddress(int address, boolean isForward) {
      int row = address / bytesPerRow;
      int col = address % bytesPerRow;
      int pos = (row - hex.getShowStart()) * charsPerRow;
      if (isForward) {
         pos += afterPos[col];
      } else {
         pos += beforePos[col];
      }
      return pos;
   }

   public abstract void appendRow(IDataProvider idp, int row, boolean isLastRow);

   public void initText() {
      sbTemp = new StringBuilder(1024);
      styleRanges = new ArrayList<>();
   }

   public void showText() {
      txt.setText(sbTemp.toString());
      for (StyleRange style : styleRanges) {
         txt.setStyleRange(style);
      }
   }

   public void clearText() {
      txt.setText("");
   }

   protected abstract void calcPositions();

   public void setBackground(Color color) {
      txt.setBackground(color);
   }

   public void setForeground(Color color) {
      txt.setForeground(color);
   }

   public void showSelection(int startByte, int endByte) {
      if (startByte == endByte) {
         int pos = getPosByAddress(startByte, false);
         pos = HexViewer.fix(pos, txt.getText().length());
         txt.setSelection(pos);
         return;
      }
      int startPos;
      int endPos;
      if (startByte < endByte) {
         startPos = getPosByAddress(startByte, false);
         endPos = getPosByAddress(endByte - 1, true);
      } else {
         startPos = getPosByAddress(startByte - 1, true);
         endPos = getPosByAddress(endByte, false);
      }
      startPos = HexViewer.fix(startPos, txt.getText().length());
      endPos = HexViewer.fix(endPos, txt.getText().length());
      txt.setSelection(startPos, endPos);
   }

   public int calcRowsInView() {
      // use -10 due to border width, and some extra spacing
      // rowsInView = (txt.getSize().y - 10) / CHAR_HEIGHT;
      rowsInView = (txt.getParent().getSize().y - 10) / getCharHeight();
      return rowsInView;
   }

   public void setRowsInView(int rows) {
      this.rowsInView = rows;
   }

   private double getCharWidht() {
      if (txtCharWidth <= 0) {
         txtCharWidth = FontUtils.getFontCharWidth(txt);
      }
      return txtCharWidth;
   }

   private int getCharHeight() {
      if (txtCharHeight <= 0) {
         txtCharHeight = FontUtils.getFontCharHeight(txt);
      }
      return txtCharHeight;
   }

   private int getWidth() {
      if (afterPos == null) {
         return 0;
      }
      return (int) Math.round(afterPos[bytesPerRow - 1] * getCharWidht() + 5);
   }
}

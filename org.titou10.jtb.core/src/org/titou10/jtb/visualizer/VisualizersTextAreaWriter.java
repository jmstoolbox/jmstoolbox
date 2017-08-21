/*
 * Copyright (C) 2015-2017 Denis Forveille titou10.titou10@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.titou10.jtb.visualizer;

import java.io.IOException;
import java.io.StringWriter;

import org.eclipse.swt.widgets.Text;

/**
 * Specialized Writer that delegates to a SWT TextArea
 * 
 * 
 * @author Denis Forveille
 *
 */
public class VisualizersTextAreaWriter extends StringWriter {

   private Text         textLog;
   private StringBuffer buf = new StringBuffer(512);

   public VisualizersTextAreaWriter(Text textLog) {
      this.textLog = textLog;
      this.lock = buf;

   }

   private void writeLog() {
      if (buf.length() <= 0) {
         return;
      }
      textLog.append(buf.toString());
      buf.setLength(0);

   }

   @Override
   public void write(char cbuf[], int off, int len) {
      // log.debug("write1");
      if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
         throw new IndexOutOfBoundsException();
      } else
         if (len == 0) {
            return;
         }
      buf.append(cbuf, off, len);
   }

   @Override
   public void write(String str) {
      // log.debug("write2 '{}'", str);
      buf.append(str);
   }

   @Override
   public void write(String str, int off, int len) {
      // log.debug("write3 {} {} {}", str, off, len);
      buf.append(str.substring(off, off + len));
   }

   @Override
   public StringWriter append(CharSequence csq) {
      // log.debug("append3");
      if (csq == null) {
         write("null");
      } else {
         write(csq.toString());
      }
      return this;
   }

   @Override
   public StringWriter append(CharSequence csq, int start, int end) {
      // debug("append2");
      CharSequence cs = (csq == null ? "null" : csq);
      write(cs.subSequence(start, end).toString());
      return this;
   }

   @Override
   public StringWriter append(char c) {
      // log.debug("append1");
      write(c);
      return this;
   }

   @Override
   public void write(int c) {
      // log.debug("write4");
      buf.append((char) c);
   }

   @Override
   public String toString() {
      return buf.toString();
   }

   @Override
   public StringBuffer getBuffer() {
      return buf;
   }

   @Override
   public void flush() {
      // log.debug("close");
      writeLog();
   }

   @Override
   public void close() throws IOException {
      // log.debug("close");
      writeLog();
   }
}

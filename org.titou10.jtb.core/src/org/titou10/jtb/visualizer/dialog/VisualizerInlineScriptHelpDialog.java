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
package org.titou10.jtb.visualizer.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 
 * Display help text for the Inline Script creation dialog
 * 
 * @author Denis Forveille
 *
 */
public class VisualizerInlineScriptHelpDialog extends Dialog {

   private static final String CR = "\n";
   private static final String helpText;

   static {
      StringBuilder sb = new StringBuilder(1024);
      sb.append("Global Objects availbale to the scripts:").append(CR);
      sb.append("========================================").append(CR);
      sb.append("String jtb_jmsMessageType : Type of JMS Message : TEXT, BYTES or MAP").append(CR);
      sb.append("Object jtb_visualizer     : Exposes some utility method (See below)").append(CR);
      sb.append("String jtb_payloadText    : The payload of a JMS TextMessage").append(CR);
      sb.append("byte[] jtb_payloadBytes   : The payload of a JMS BytesMessage").append(CR);
      sb.append("Map    jtb_payloadMap     : The payload of a JMS MapMessage").append(CR);
      sb.append(CR);
      sb.append("Methods exposed by the 'jtb_visualizer' object:").append(CR);
      sb.append("===============================================").append(CR);
      sb.append("void showContent(String ext, String payload) : Show the content as for the 'OS Extension' visualizer").append(CR);
      sb.append("void showContent(String ext, byte[] payload) : Show the content as for the 'OS Extension' visualizer").append(CR);
      sb.append("void showContent(String ext,Map payload)     : Show the content as for the 'OS Extension' visualizer").append(CR);
      sb.append(CR);
      sb.append("byte[] decodeBase64(String stringToDecode)   : Decodes a base64 encoded String").append(CR);
      sb.append("byte[] decodeBase64(byte[] bytesToDecode)    : Decodes base64 encoded bytes").append(CR);
      sb.append("byte[] encodeBase64(byte[] bytesToDecode)    : Encodes bytes to base64").append(CR);
      sb.append("String encodeToStringBase64(byte[] b)        : Encodes bytes to a base64 String").append(CR);
      sb.append(CR);
      sb.append("byte[] compress(byte[] b)                    : Uses the Java 'Deflater' class to compress the bytes").append(CR);
      sb.append("byte[] decompress(byte[] b)                  : Uses the Java 'Inflater' class to compress the bytes").append(CR);
      sb.append(CR);

      helpText = sb.toString();
   }

   public VisualizerInlineScriptHelpDialog(Shell parentShell) {
      super(parentShell);
   }

   @Override
   protected void setShellStyle(int newShellStyle) {
      super.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
      setBlockOnOpen(false);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      super.createDialogArea(parent);

      Composite container = (Composite) super.createDialogArea(parent);

      StyledText txt = new StyledText(container, SWT.NONE);
      txt.setText(helpText);
      txt.setEditable(false);
      txt.setFont(SWTResourceManager.getFont("Courier New", 9, SWT.NORMAL));

      return container;
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
   }

   @Override
   protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText("Help");
   }
}

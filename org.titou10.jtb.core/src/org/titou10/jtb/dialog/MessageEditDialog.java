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
package org.titou10.jtb.dialog;

import javax.jms.JMSException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBMessageType;
import org.titou10.jtb.ui.JTBStatusReporter;

/**
 * Dialog for viewing and "Editing" a Message
 * 
 * @author Denis Forveille
 *
 */
public class MessageEditDialog extends MessageDialogAbstract {

   public static final int     BUTTON_SAVE_TEMPLATE = IDialogConstants.NEXT_ID + 2;
   public static final int     BUTTON_REMOVE        = IDialogConstants.NEXT_ID + 3;

   private static final String TITLE_1              = "View Message %s of type %s";
   private static final String TITLE_2              = "View Message %s : %s (%s)";

   private String              jtbDestinationName;
   private String              jmsMessageID;
   private JTBMessageType      jtbMessageType;

   // -----------
   // Constructor
   // -----------
   public MessageEditDialog(Shell parentShell,
                            JTBStatusReporter jtbStatusReporter,
                            ConfigManager cm,
                            JTBMessage jtbMessage) throws JMSException {
      super(parentShell, jtbStatusReporter, cm, new JTBMessageTemplate(jtbMessage));
      jtbDestinationName = jtbMessage.getJtbDestination().getName();
      jmsMessageID = jtbMessage.getJmsMessage().getJMSMessageID();
      jtbMessageType = jtbMessage.getJtbMessageType();
   }

   // ----------------
   // Business Methods
   // ----------------

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, BUTTON_SAVE_TEMPLATE, "Save as Template...", false);
      createButton(parent, BUTTON_REMOVE, "Remove", false);
      createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", true);
   }

   @Override
   public String getDialogTitle() {
      if (jmsMessageID == null) {
         return String.format(TITLE_1, jtbDestinationName, jtbMessageType.name());
      } else {
         return String.format(TITLE_2, jtbDestinationName, jmsMessageID, jtbMessageType.name());
      }
   }

   @Override
   public boolean isReadOnly() {
      return true;
   }

   @Override
   protected void buttonPressed(int buttonId) {
      switch (buttonId) {
         case BUTTON_SAVE_TEMPLATE:
         case BUTTON_REMOVE:
            updateTemplate();
            setReturnCode(buttonId);
            close();
            break;
         default:
            super.buttonPressed(buttonId);
            break;
      }
   }

}

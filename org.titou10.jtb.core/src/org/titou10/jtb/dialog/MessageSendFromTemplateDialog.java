/*
 * Copyright (C) 2015 Denis Forveille titou10.titou10@gmail.com
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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.variable.VariablesUtils;

/**
 * Dialog for sending a message from a template
 * 
 * @author Denis Forveille
 *
 */
public class MessageSendFromTemplateDialog extends MessageDialogAbstract {

   private static final String TITLE = "Send Message to %s:%s";

   private ConfigManager       cm;
   private JTBDestination      jtbDestination;

   // -----------
   // Constructor
   // -----------
   public MessageSendFromTemplateDialog(Shell parentShell,
                                        ConfigManager cm,
                                        JTBMessageTemplate template,
                                        JTBDestination jtbDestination) {
      super(parentShell, cm, template);
      this.cm = cm;
      this.jtbDestination = jtbDestination;
   }

   // ----------------
   // Business Methods
   // ----------------

   @Override
   protected Control createDialogArea(Composite parent) {

      // Replace variables
      JTBMessageTemplate template = getTemplate();
      template.setPayloadText(VariablesUtils.replaceTemplateVariables(cm.getVariables(), template.getPayloadText()));

      Control c = super.createDialogArea(parent);

      // Disable Content Assist
      disableContentAssist();

      return c;
   }

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.OK_ID, "Send", false);
      createButton(parent, IDialogConstants.CANCEL_ID, "Cancel", true);
   }

   @Override
   public String getDialogTitle() {
      return String.format(TITLE, jtbDestination.getJtbConnection().getSessionName(), jtbDestination.getName());
   }

}

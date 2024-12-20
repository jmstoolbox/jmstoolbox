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
import org.eclipse.swt.widgets.Shell;
import org.titou10.jtb.config.JTBPreferenceStore;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.variable.VariablesManager;
import org.titou10.jtb.visualizer.VisualizersManager;

/**
 * Dialog for creating/sending a new message
 *
 * @author Denis Forveille
 *
 */
public class MessageSendDialog extends MessageDialogAbstract {

   private static final String TITLE = "Send Message to %s:%s";

   private JTBDestination      jtbDestination;

   // ------------
   // Constructor
   // ------------
   public MessageSendDialog(Shell parentShell,
                            JTBStatusReporter jtbStatusReporter,
                            JTBPreferenceStore ps,
                            VariablesManager variablesManager,
                            VisualizersManager visualizersManager,
                            JTBMessageTemplate template,
                            JTBDestination jtbDestination) {
      super(parentShell,
            jtbStatusReporter,
            ps,
            variablesManager,
            visualizersManager,
            template,
            jtbDestination.getJtbConnection().getMetaJMSPropertyNames());
      this.jtbDestination = jtbDestination;
   }

   // ----------------
   // Business Methods
   // ----------------

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.OK_ID, "Send and Close", false);
      createButton(parent, IDialogConstants.CANCEL_ID, "Close", true);
   }

   @Override
   public String getDialogTitle() {
      return String.format(TITLE, jtbDestination.getJtbConnection().getSessionName(), jtbDestination.getName());
   }

   @Override
   public boolean isReadOnly() {
      return false;
   }
}

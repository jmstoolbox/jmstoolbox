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
package org.titou10.jtb.template.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.titou10.jtb.config.JTBPreferenceStore;
import org.titou10.jtb.dialog.MessageDialogAbstract;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.variable.VariablesManager;
import org.titou10.jtb.visualizer.VisualizersManager;

/**
 * Dialog for creating or editing a template
 * 
 * @author Denis Forveille
 *
 */
public class TemplateAddOrEditDialog extends MessageDialogAbstract {

   private String templateName;

   // ------------
   // Constructor
   // ------------

   public TemplateAddOrEditDialog(Shell parentShell,
                                  JTBStatusReporter jtbStatusReporter,
                                  JTBPreferenceStore ps,
                                  VariablesManager variablesManager,
                                  VisualizersManager visualizersManager,
                                  JTBMessageTemplate template,
                                  String templateName) {
      super(parentShell, jtbStatusReporter, ps, variablesManager, visualizersManager, template);
      this.templateName = templateName;
   }

   // ----------------
   // Business Methods
   // ----------------

   @Override
   protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, IDialogConstants.OK_ID, "Save Template", false);
      createButton(parent, IDialogConstants.CANCEL_ID, "Close", true);
   }

   @Override
   public String getDialogTitle() {
      if (templateName == null) {
         return "Add a new Message Template";
      } else {
         return "Edit Message Template : '" + templateName + "'";
      }
   }

   @Override
   public boolean isReadOnly() {
      return false;
   }

}

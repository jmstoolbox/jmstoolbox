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
package org.titou10.jtb.template.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.template.dialog.TemplateAddOrEditDialog;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.dnd.DNDData;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.JTBPreferenceStore;
import org.titou10.jtb.util.Utils;
import org.titou10.jtb.variable.VariablesManager;
import org.titou10.jtb.visualizer.VisualizersManager;

/**
 * Manage the "Add or Edit Template" command
 * 
 * @author Denis Forveille
 * 
 */
public class TemplateAddOrEditHandler {

   private static final Logger log = LoggerFactory.getLogger(TemplateAddOrEditHandler.class);

   @Inject
   @Named(IServiceConstants.ACTIVE_SHELL)
   private Shell               shell;

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Inject
   private JTBPreferenceStore  ps;

   @Inject
   private TemplatesManager    templatesManager;

   @Inject
   private VariablesManager    variablesManager;

   @Inject
   private VisualizersManager  visualizersManager;

   @Execute
   public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<IFileStore> templateFiles,
                       @Named(Constants.COMMAND_TEMPLATE_ADDEDIT_PARAM) String mode) {
      log.debug("execute template={} mode={}", templateFiles, mode);

      if (mode == null) {
         return;
      }

      IFileStore templateFile;
      if ((templateFiles == null) || (templateFiles.isEmpty())) {
         // Case of the "New Template" menu without any template selected
         templateFile = templatesManager.getSystemTemplateDirectoryFileStore();
      } else {
         // When a message is selected and CTRL-T is pressed, templateFiles contains a JTBMessage...
         if (templateFiles.get(0) instanceof IFileStore) {
            templateFile = templateFiles.get(0);
         } else {
            templateFile = templatesManager.getSystemTemplateDirectoryFileStore();
         }
      }
      IFileStore parentFolder;
      if (templateFile.fetchInfo().isDirectory()) {
         parentFolder = templateFile;
      } else {
         parentFolder = templateFile.getParent();
      }

      TemplateAddOrEditDialog dialog;
      JTBMessageTemplate template = new JTBMessageTemplate();

      switch (mode) {
         case Constants.COMMAND_TEMPLATE_ADDEDIT_ADD:
            dialog = new TemplateAddOrEditDialog(shell,
                                                 jtbStatusReporter,
                                                 ps,
                                                 variablesManager,
                                                 visualizersManager,
                                                 template,
                                                 null);
            if (dialog.open() != Window.OK) {
               return;
            }

            try {
               boolean res = templatesManager.createNewTemplate(shell, template, parentFolder, "Template");
               if (res) {
                  // Refresh Template Browser asynchronously
                  eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);
               }
            } catch (Exception e) {
               jtbStatusReporter.showError("Save unsuccessful", e, "");
               return;
            }
            break;

         case Constants.COMMAND_TEMPLATE_ADDEDIT_EDIT:
            // Read template
            try {
               template = templatesManager.readTemplate(templateFile);
            } catch (Exception e) {
               jtbStatusReporter.showError("A problem occurred when reading the template", e, "");
               return;
            }

            dialog = new TemplateAddOrEditDialog(shell,
                                                 jtbStatusReporter,
                                                 ps,
                                                 variablesManager,
                                                 visualizersManager,
                                                 template,
                                                 templateFile.getName());

            if (dialog.open() != Window.OK) {
               return;
            }
            try {

               templatesManager.updateTemplate(templateFile, template);
            } catch (Exception e) {
               jtbStatusReporter.showError("Save unsuccessful", e, "");
               return;
            }
            break;

         case Constants.COMMAND_TEMPLATE_ADDEDIT_EDIT_SCRIPT:

            // Template is in DNDData structure...
            template = DNDData.getSelectedJTBMessageTemplate();

            dialog = new TemplateAddOrEditDialog(shell,
                                                 jtbStatusReporter,
                                                 ps,
                                                 variablesManager,
                                                 visualizersManager,
                                                 template,
                                                 null);
            if (dialog.open() != Window.OK) {
               return;
            }

            try {
               boolean res = templatesManager.createNewTemplate(shell, template, parentFolder, "Template");
               if (res) {
                  // Refresh Template Browser asynchronously
                  eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);
               }
            } catch (Exception e) {
               jtbStatusReporter.showError("Save unsuccessful", e, "");
               return;
            }

         default:
            break;
      }

   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<IFileStore> selection,
                             @Named(Constants.COMMAND_TEMPLATE_ADDEDIT_PARAM) String mode,
                             @Optional MMenuItem menuItem) {

      switch (mode) {
         case Constants.COMMAND_TEMPLATE_ADDEDIT_ADD:
            return Utils.enableMenu(menuItem);

         case Constants.COMMAND_TEMPLATE_ADDEDIT_EDIT_SCRIPT:
            return Utils.enableMenu(menuItem);

         case Constants.COMMAND_TEMPLATE_ADDEDIT_EDIT:

            // Show only if maximum one template is selected
            if ((selection != null) && (selection.size() == 1)) {
               IFileStore ifs = (IFileStore) selection.get(0);
               if (!ifs.fetchInfo().isDirectory()) {
                  return Utils.enableMenu(menuItem);
               }
            }
            break;

      }

      return Utils.disableMenu(menuItem);
   }

}

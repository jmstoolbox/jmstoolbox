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
package org.titou10.jtb.handler;

import java.util.List;

import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.dialog.TemplateAddOrEditDialog;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

/**
 * Manage the "Add or Edit Template" command
 * 
 * @author Denis Forveille
 * 
 */
public class TemplateAddOrEditHandler {

   private static final Logger log = LoggerFactory.getLogger(TemplateAddOrEditHandler.class);

   @Execute
   public void execute(Shell shell,
                       IEventBroker eventBroker,
                       JTBStatusReporter jtbStatusReporter,
                       ConfigManager cm,
                       @Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<IResource> templateFiles,
                       @Named(Constants.COMMAND_TEMPLATE_ADDEDIT_PARAM) String mode) {
      log.debug("execute .template={} mode={}", templateFiles, mode);

      if (mode == null) {
         return;
      }

      IFile templateFile = null;
      IFolder parentFolder = null;
      if ((templateFiles == null) || (templateFiles.isEmpty())) {
         parentFolder = cm.getTemplateFolder();
      } else {
         if (templateFiles.get(0) instanceof IFile) {
            parentFolder = (IFolder) templateFiles.get(0).getParent();
         } else {
            if (templateFiles.get(0) instanceof IFolder) {
               parentFolder = (IFolder) templateFiles.get(0);
            } else {
               parentFolder = cm.getTemplateFolder();
            }
         }
      }

      TemplateAddOrEditDialog dialog;
      JTBMessageTemplate template = new JTBMessageTemplate();

      if (mode.equals(Constants.COMMAND_TEMPLATE_ADDEDIT_ADD)) {
         dialog = new TemplateAddOrEditDialog(shell, cm, template, null);
      } else {
         // Read template
         templateFile = (IFile) templateFiles.get(0);
         try {
            template = Utils.readTemplate(templateFile);
         } catch (JAXBException | CoreException e) {
            jtbStatusReporter.showError("A problem occurred when reading the template", e, "");
            return;
         }
         dialog = new TemplateAddOrEditDialog(shell, cm, template, templateFile.getName());
      }

      if (dialog.open() != Window.OK) {
         return;
      }

      try {
         if (mode.equals(Constants.COMMAND_TEMPLATE_ADDEDIT_EDIT)) {
            Utils.updateTemplate(templateFile, template);
         } else {
            boolean res = Utils.createNewTemplate(shell, template, cm.getTemplateFolder(), parentFolder, "Template");
            if (res) {
               // Refresh Template Browser asynchronously
               eventBroker.post(Constants.EVENT_TEMPLATES, null);
            }
         }
      } catch (Exception e) {
         jtbStatusReporter.showError("Save unsuccessful", e, "");
         return;
      }
   }

   @CanExecute
   public boolean canExecute(@Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<IResource> selection,
                             @Named(Constants.COMMAND_TEMPLATE_ADDEDIT_PARAM) String mode,
                             @Optional MMenuItem menuItem,
                             MApplication app,
                             EModelService modelService,
                             EPartService partService) {
      log.debug("canExecute={} mode={}", selection, mode);

      switch (mode) {
         case Constants.COMMAND_TEMPLATE_ADDEDIT_ADD:
            return Utils.enableMenu(menuItem);

         case Constants.COMMAND_TEMPLATE_ADDEDIT_EDIT:

            // Show only if maximum one template is selected
            if ((selection != null) && (selection.size() == 1)) {
               if (selection.get(0) instanceof IFile) {
                  return Utils.enableMenu(menuItem);
               }
            }
            break;
      }

      return Utils.disableMenu(menuItem);
   }

}

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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.dnd.DNDData;
import org.titou10.jtb.ui.dnd.DNDData.DNDElement;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;
import org.titou10.jtb.variable.VariablesManager;

/**
 * Manage the "Save Message as Template" command
 * 
 * @author Denis Forveille
 * 
 */
public class MessageSaveAsTemplateHandler {

   private static final Logger log = LoggerFactory.getLogger(MessageSaveAsTemplateHandler.class);

   @Inject
   private IEventBroker        eventBroker;

   @Inject
   private VariablesManager    variablesManager;

   @Inject
   private TemplatesManager    templatesManager;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell,
                       @Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                       @Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<JTBMessage> selection) {
      log.debug("execute");

      try {

         switch (context) {
            case Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP:

               log.debug("Drag & Drop operation in progress...");

               IFileStore initialFolder;
               IFileStore targetFileStore = DNDData.getTargetTemplateFileStore();
               if (DNDData.getDrop() == DNDElement.DROP_ON_TEMPLATE_FILESTORE_FILE) {
                  initialFolder = targetFileStore.getParent();
               } else {
                  initialFolder = targetFileStore;
               }

               // Messages from the Messages Browser
               switch (DNDData.getDrag()) {
                  case JTB_MESSAGES:
                     List<JTBMessage> jtbMessages = DNDData.getSourceJTBMessages();
                     boolean atLeastOne1 = false;
                     for (JTBMessage jtbMessage1 : jtbMessages) {
                        JTBMessageTemplate template1 = new JTBMessageTemplate(jtbMessage1);
                        String destinationName1 = jtbMessage1.getJtbDestination().getName();
                        if (templatesManager.createNewTemplate(shell, template1, initialFolder, destinationName1)) {
                           atLeastOne1 = true;
                        }
                     }
                     // Refresh Template Browser asynchronously
                     if (atLeastOne1) {
                        eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);
                     }
                     return;

                  // Files from the OS
                  case TEMPLATES_FILENAMES:

                     // Read files from the OS and save them in JTB
                     List<String> fileNames = DNDData.getSourceTemplatesFileNames();
                     boolean atLeastOne2 = false;
                     for (String fileName : fileNames) {
                        Path p = new Path(fileName);
                        String destinationName2 = p.removeFileExtension().lastSegment();
                        JTBMessageTemplate template2 = templatesManager.readTemplate(fileName);
                        template2.setPayloadText(variablesManager.replaceTemplateVariables(template2.getPayloadText()));
                        // Show the "save as" dialog
                        if (templatesManager.createNewTemplate(shell, template2, initialFolder, destinationName2)) {
                           atLeastOne2 = true;
                           eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);
                        }
                     }

                     // Refresh Template Browser asynchronously
                     if (atLeastOne2) {
                        eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);
                     }
                     return;

                  default:
                     return;
               }

            default:

               // Only 1 message can be selected
               JTBMessage jtbMessage = selection.get(0);
               JTBMessageTemplate template = new JTBMessageTemplate(jtbMessage);
               String destinationName = jtbMessage.getJtbDestination().getName();

               // Show the "save as" dialog
               if (templatesManager.createNewTemplate(shell, template, null, destinationName)) {
                  // Refresh Template Browser asynchronously
                  eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);
               }
         }
      } catch (Exception e) {
         jtbStatusReporter.showError("An error occurred when saving template", e, "");
         return;
      }

   }

   @CanExecute
   public boolean canExecute(@Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                             @Optional @Named(IServiceConstants.ACTIVE_SELECTION) List<JTBMessage> selection,
                             @Named(Constants.CURRENT_TAB_JTBDESTINATION) JTBDestination jtbDestination,
                             @Optional MMenuItem menuItem) {

      if (context.equals(Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP)) {
         return Utils.enableMenu(menuItem);
      }

      // Show the menu only if one message is selected
      if (Utils.nullOrMoreThanOne(selection)) {
         return Utils.disableMenu(menuItem);
      }

      // Enable menu only if the selected message is from the active tab
      JTBMessage selected = selection.get(0);
      if (selected.getJtbDestination().getName().equals(jtbDestination.getName())) {
         return Utils.enableMenu(menuItem);
      }

      return Utils.disableMenu(menuItem);
   }
}

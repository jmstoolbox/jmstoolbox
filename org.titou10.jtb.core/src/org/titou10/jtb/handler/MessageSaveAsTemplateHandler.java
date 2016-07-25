/* Copyright (C) 2015-2016 Denis Forveille titou10.titou10@gmail.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>. */
package org.titou10.jtb.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFolder;
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
import org.titou10.jtb.config.ConfigManager;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.template.TemplatesUtils;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.dnd.DNDData;
import org.titou10.jtb.ui.dnd.DNDData.DNDElement;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;

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
   private ConfigManager       cm;

   @Inject
   private JTBStatusReporter   jtbStatusReporter;

   @Execute
   public void execute(Shell shell,
                       @Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<JTBMessage> selection) {
      log.debug("execute");

      JTBMessage jtbMessage;
      IFolder initialFolder = cm.getTemplateFolder();
      String destinationName;

      try {

         JTBMessageTemplate template;

         if (context.equals(Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP)) {
            log.debug("Drag & Drop operation in progress...");
            if (DNDData.getDrop() == DNDElement.TEMPLATE_FOLDER) {
               initialFolder = DNDData.getTargetTemplateIFolder();
            }
            if (DNDData.getDrop() == DNDElement.TEMPLATE) {
               initialFolder = (IFolder) DNDData.getTargetTemplateIFile().getParent();
            }

            switch (DNDData.getDrag()) {
               case JTBMESSAGE:
                  jtbMessage = DNDData.getSourceJTBMessages().get(0);
                  template = new JTBMessageTemplate(jtbMessage);
                  destinationName = jtbMessage.getJtbDestination().getName();
                  break;

               case EXTERNAL_FILE_NAME:
                  template = TemplatesUtils.readTemplateFromOS(DNDData.getSourceExternalFileName());
                  Path p1 = new Path(DNDData.getSourceExternalFileName());
                  destinationName = p1.removeFileExtension().lastSegment();
                  break;

               case TEMPLATE_EXTERNAL:
                  template = TemplatesUtils.readTemplate(DNDData.getSourceTemplateExternal());
                  Path p2 = new Path(DNDData.getSourceTemplateExternal());
                  destinationName = p2.removeFileExtension().lastSegment();
                  break;

               default:
                  return;
            }

         } else {
            // Only 1 message can be selected
            jtbMessage = selection.get(0);
            template = new JTBMessageTemplate(jtbMessage);
            destinationName = jtbMessage.getJtbDestination().getName();
         }

         // Show the "save as" dialog
         boolean res = TemplatesUtils.createNewTemplate(shell, template, cm.getTemplateFolder(), initialFolder, destinationName);
         if (res) {

            // Refresh Template Browser asynchronously
            eventBroker.post(Constants.EVENT_REFRESH_TEMPLATES_BROWSER, null);
         }
      } catch (Exception e) {
         jtbStatusReporter.showError("An error occurred when saving template", e, "");
         return;
      }

   }

   @CanExecute
   public boolean canExecute(@Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                             @Named(IServiceConstants.ACTIVE_SELECTION) @Optional List<JTBMessage> selection,
                             @Named(Constants.CURRENT_TAB_JTBDESTINATION) JTBDestination jtbDestination,
                             @Optional MMenuItem menuItem) {

      if (context.equals(Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP)) {
         return Utils.enableMenu(menuItem);
      }

      // Show the menu only if one message is selected
      if ((selection == null) || (selection.size() != 1)) {
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

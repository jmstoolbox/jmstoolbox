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
package org.titou10.jtb.handler;

import java.io.IOException;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.xml.bind.JAXBException;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.titou10.jtb.config.JTBPreferenceStore;
import org.titou10.jtb.dialog.MessageSendFromTemplateDialog;
import org.titou10.jtb.jms.model.JTBConnection;
import org.titou10.jtb.jms.model.JTBDestination;
import org.titou10.jtb.jms.model.JTBMessage;
import org.titou10.jtb.jms.model.JTBMessageTemplate;
import org.titou10.jtb.jms.model.JTBObject;
import org.titou10.jtb.jms.model.JTBQueue;
import org.titou10.jtb.jms.model.JTBTopic;
import org.titou10.jtb.template.TemplatesManager;
import org.titou10.jtb.template.dialog.TemplateChooserDialog;
import org.titou10.jtb.ui.JTBStatusReporter;
import org.titou10.jtb.ui.dnd.DNDData;
import org.titou10.jtb.ui.navigator.NodeJTBQueue;
import org.titou10.jtb.ui.navigator.NodeJTBTopic;
import org.titou10.jtb.util.Constants;
import org.titou10.jtb.util.Utils;
import org.titou10.jtb.variable.VariablesManager;
import org.titou10.jtb.visualizer.VisualizersManager;

/**
 * Manage the "Send Message From Template" command
 * 
 * @author Denis Forveille
 * 
 */
public class MessageSendFromTemplateHandler {

   private static final Logger log                  = LoggerFactory.getLogger(MessageSendFromTemplateHandler.class);

   private static final String TEMPLATES_COPY_MULTI = "Are you sure to blindly post those %d templates to '%s' ?";

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

   // This can be called in various contexts depending on parameter "context":
   // - right click on a session = QUEUE : -> use selection
   // - right click on message browser = MESSAGE : -> use tabJTBQueue
   // - drag & drop

   @Execute
   public void execute(Shell shell,
                       @Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                       @Named(IServiceConstants.ACTIVE_SELECTION) @Optional JTBObject selection,
                       @Named(Constants.CURRENT_TAB_JTBDESTINATION) @Optional JTBDestination jtbDestination) {
      log.debug("execute context={} selection={} jtbDestination={}", context, selection, jtbDestination);

      JTBMessageTemplate template = null;
      IFileStore selectedTemplateFile = null;

      switch (context) {
         case Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP:
            jtbDestination = DNDData.getTargetJTBDestination();
            log.debug("'Send from template' initiated from Drag & Drop: {} Destination: {}", DNDData.getDrag(), jtbDestination);

            // Source of drag = Templates or Messages?
            switch (DNDData.getDrag()) {

               case TEMPLATE_FILESTORES:
                  List<IFileStore> templates = DNDData.getSourceTemplatesFileStores();

                  if (templates.size() == 1) {
                     selectedTemplateFile = templates.get(0);
                     break;
                  }

                  String msg2 = String.format(TEMPLATES_COPY_MULTI, templates.size(), jtbDestination.getName());
                  if (!(MessageDialog.openConfirm(shell, "Confirmation", msg2))) {
                     return;
                  }
                  try {
                     // Post Messages
                     JTBConnection jtbConnection2 = jtbDestination.getJtbConnection();
                     for (IFileStore ifs : templates) {
                        JTBMessageTemplate t = templatesManager.readTemplate(ifs);
                        t.setPayloadText(variablesManager.replaceTemplateVariables(t.getPayloadText()));
                        Message m = jtbConnection2.createJMSMessage(t.getJtbMessageType());
                        JTBMessage jtbMessage = t.toJTBMessage(jtbDestination, m);
                        jtbDestination.getJtbConnection().sendMessage(jtbMessage);
                     }
                     // Refresh List if the destination is browsable
                     if ((jtbDestination.isJTBQueue()) && (!jtbDestination.getAsJTBQueue().isBrowsable())) {
                        return;
                     }
                     eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, jtbDestination);
                  } catch (Exception e) {
                     jtbStatusReporter.showError("Problem occurred while sending the messages", e, jtbDestination.getName());
                  }
                  return;

               default:
                  break;
            }
            break;

         case Constants.COMMAND_CONTEXT_PARAM_QUEUE:
            log.debug("'Send from template' initiated from Destination...");

            selectedTemplateFile = chooseTemplate(shell);
            if (selectedTemplateFile == null) {
               return;
            }

            // Queue or Topic?

            if (selection instanceof NodeJTBQueue nodeJTBQueue) {
               jtbDestination = (JTBQueue) nodeJTBQueue.getBusinessObject();
            } else {
               NodeJTBTopic nodeJTBTopic = (NodeJTBTopic) selection;
               jtbDestination = (JTBTopic) nodeJTBTopic.getBusinessObject();
            }
            break;

         case Constants.COMMAND_CONTEXT_PARAM_MESSAGE:
            log.debug("'Send from template' initiated from Message Browser...");

            selectedTemplateFile = chooseTemplate(shell);
            if (selectedTemplateFile == null) {
               return;
            }

            break;

         default:
            log.error("Invalid value : {}", context);
            return;
      }

      // Read template from IFileStore
      if (template == null) {
         try {
            template = templatesManager.readTemplate(selectedTemplateFile);
         } catch (JAXBException | CoreException | IOException e) {
            jtbStatusReporter.showError("A problem occurred when reading the template", e, "");
            return;
         }
      }

      JTBConnection jtbConnection = jtbDestination.getJtbConnection();

      // Show the "edit template" dialog with a send button..
      MessageSendFromTemplateDialog dialog = new MessageSendFromTemplateDialog(shell,
                                                                               jtbStatusReporter,
                                                                               ps,
                                                                               variablesManager,
                                                                               visualizersManager,
                                                                               template,
                                                                               jtbDestination);
      if (dialog.open() != Window.OK) {
         return;
      }

      template = dialog.getTemplate();

      log.debug("OK {}", template.getJtbMessageType());

      try {
         // Send Message
         Message m = jtbConnection.createJMSMessage(template.getJtbMessageType());
         JTBMessage jtbMessage = template.toJTBMessage(jtbDestination, m);
         jtbDestination.getJtbConnection().sendMessage(jtbMessage);

         // Refresh List if the destination is browsable
         if ((jtbDestination.isJTBQueue()) && (!jtbDestination.getAsJTBQueue().isBrowsable())) {
            return;
         }

         eventBroker.send(Constants.EVENT_REFRESH_QUEUE_MESSAGES, jtbDestination);

      } catch (JMSException e) {
         jtbStatusReporter.showError("Problem occurred while sending the message", e, jtbDestination.getName());
         return;
      }

   }

   private IFileStore chooseTemplate(Shell shell) {

      TemplateChooserDialog dialog1 = new TemplateChooserDialog(shell, templatesManager, false);
      if (dialog1.open() != Window.OK) {
         return null;
      }

      return dialog1.getSelectedTemplate();
   }

   @CanExecute
   public boolean canExecute(@Named(Constants.COMMAND_CONTEXT_PARAM) String context,
                             @Named(IServiceConstants.ACTIVE_SELECTION) @Optional JTBObject selection,
                             @Optional MMenuItem menuItem) {
      // log.debug("canExecute context={} selection={} jtbDestination={}", context, selection, jtbDestination);

      switch (context) {
         case Constants.COMMAND_CONTEXT_PARAM_DRAG_DROP:
            return Utils.enableMenu(menuItem);

         case Constants.COMMAND_CONTEXT_PARAM_QUEUE:
            // Show menu on Queues and Topics only
            if ((!(selection instanceof NodeJTBQueue)) && (!(selection instanceof NodeJTBTopic))) {
               return Utils.disableMenu(menuItem);
            }

            // At least one template must exits
            // FIXME DF
            // try {
            // if (cm.getTemplateFolder().members().length == 0) {
            // return Utils.disableMenu(menuItem);
            // }
            // } catch (CoreException e) {
            // jtbStatusReporter.showError("Problem occurred while reading the template folder", e, "");
            // return Utils.disableMenu(menuItem);
            // }
            return Utils.enableMenu(menuItem);

         case Constants.COMMAND_CONTEXT_PARAM_MESSAGE:
            return Utils.enableMenu(menuItem);

         default:
            log.error("Invalid value : {}", context);
            return Utils.disableMenu(menuItem);
      }

   }
}

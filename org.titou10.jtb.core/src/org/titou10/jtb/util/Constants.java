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
package org.titou10.jtb.util;

import java.text.SimpleDateFormat;

/**
 * 
 * Global constants
 * 
 * @author Denis Forveille
 *
 */
public final class Constants {

   public static final String BASE = "org.titou10.jtb.core.";

   // Extension Point
   public static final String JTB_EXTENSION_POINT            = BASE + "QManagerProvider";
   public static final String JTB_EXTENSION_POINT_CLASS_ATTR = "class";
   public static final String JTB_EXTENSION_POINT_NAME_ATTR  = "displayName";

   // JTB Config
   public static final String JTB_CONFIG_PROJECT           = "JMSToolBox";
   public static final String JTB_CONFIG_FILE_NAME         = "config.xml";
   public static final String JTB_LOG_FILE_NAME            = "jmstoolbox";
   public static final String JTB_PROPERTY_FILE_NAME       = "jmstoolbox.log";
   public static final String CONFIG_FILE_EXTENSION_FILTER = "*.xml";

   public static final String JTB_VARIABLE_FILE_NAME         = "variables.xml";
   public static final String VARIABLE_FILE_EXTENSION_FILTER = "*.xml";

   public static final String TEMPLATE_FOLDER = "Templates";
   public static final String SCRIPTS_FOLDER  = "Scripts";

   public static final String JTB_SCRIPT_FILE_NAME         = "scripts.xml";
   public static final String SCRIPT_FILE_EXTENSION_FILTER = "*.xml";

   // Preferences
   public static final String  PREFERENCE_FILE_NAME             = "jmstoolbox.properties";
   public static final String  PREF_AUTO_REFRESH_DELAY          = "auto.refresh.delay";
   public static final int     PREF_AUTO_REFRESH_DELAY_DEFAULT  = 30;
   public static final String  PREF_MAX_MESSAGES                = "max.messages";
   public static final int     PREF_MAX_MESSAGES_DEFAULT        = 200;
   public static final String  PREF_SHOW_SYSTEM_OBJECTS         = "show.system.objects";
   public static final boolean PREF_SHOW_SYSTEM_OBJECTS_DEFAULT = false;

   // QM Configuration
   public static final String[] JAR_FILE_EXTENSION_FILTER = { "*.jar" };

   // Various
   public static final SimpleDateFormat JMS_TIMESTAMP_SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

   // Handle Message tab selection
   public static final String CURRENT_TAB_JTBQUEUE = "CURRENT_TAB_JTBQUEUE";

   // Current Selected Script
   public static final String CURRENT_WORKING_SCRIPT = "CURRENT_WORKING_SCRIPT";
   public static final String WORKING_SCRIPT_TO_SAVE = "WORKING_SCRIPT_TO_SAVE";

   public static final String COMMAND_CONTEXT_PARAM           = BASE + "command.context.param";
   public static final String COMMAND_CONTEXT_PARAM_QUEUE     = "queue";
   public static final String COMMAND_CONTEXT_PARAM_MESSAGE   = "message";
   public static final String COMMAND_CONTEXT_PARAM_DRAG_DROP = "dragdrop";

   // E4 Events
   public static final String EVENT_REFRESH_JTBMESSAGE_PART = "event_jtbmessage";
   public static final String EVENT_REFRESH_MESSAGES        = "event_refresh_messages";
   public static final String EVENT_REFRESH_EXECUTION_LOG   = "event_refresh_execution_log";
   public static final String EVENT_CLEAR_EXECUTION_LOG     = "event_clear_execution_log";
   public static final String EVENT_TEMPLATES               = "event_templates";
   public static final String EVENT_REFRESH_SESSION_TREE    = "event_refresh_session_tree";
   public static final String EVENT_ADD_SEARCH_STRING       = "event_add_search_string";
   public static final String EVENT_FOCUS_CTABITEM          = "event_focus_ctabitem";
   public static final String EVENT_REFRESH_SCRIPTS_BROWSER = "event_scripts_browser";
   public static final String EVENT_REFRESH_SCRIPT_EDIT     = "event_script_edit";

   // E4 artefacts

   public static final String MAIN_WINDOW            = BASE + "main.window";
   public static final String PARTSTACK_QCONTENT     = BASE + "partstack.qcontent";
   public static final String PART_QCONTENT_PREFIX   = BASE + "part.qcontent.";
   public static final String PARTDESCRITOR_MESSAGES = BASE + "partdescriptor.messages";
   public static final String PART_SESSIONS          = BASE + "part.sessions";
   public static final String SM_DIALOG_SNIPPET      = BASE + "dialog.scripts.manager";

   public static final String COMMAND_QM_CONFIGURE = BASE + "command.qm.configure";
   public static final String COMMAND_QUEUE_BROWSE = BASE + "command.queue.browse";

   public static final String COMMAND_MESSAGE_VIEW          = BASE + "command.message.view";
   public static final String COMMAND_MESSAGE_REMOVE        = BASE + "command.message.remove";
   public static final String COMMAND_MESSAGE_COPY_MOVE     = BASE + "command.message.copyormove";
   public static final String COMMAND_MESSAGE_SAVE_TEMPLATE = BASE + "command.message.saveastemplate";

   public static final String COMMAND_MESSAGE_SEND_TEMPLATE = BASE + "command.message.sendtemplate";

   public static final String COMMAND_SESSION_CONNECT    = BASE + "session.connect";
   public static final String COMMAND_SESSION_DISCONNECT = BASE + "session.disconnect";
   public static final String COMMAND_SESSION_REMOVE     = BASE + "command.session.remove";

   public static final String COMMAND_TEMPLATE_RDD           = BASE + "command.template.rdd";
   public static final String COMMAND_TEMPLATE_RDD_PARAM     = BASE + "template.rdd.parameter.mode";
   public static final String COMMAND_TEMPLATE_RDD_RENAME    = "rename";
   public static final String COMMAND_TEMPLATE_RDD_DUPLICATE = "duplicate";
   public static final String COMMAND_TEMPLATE_RDD_DELETE    = "delete";

   public static final String COMMAND_TEMPLATE_ADDEDIT             = BASE + "command.template.addoredit";
   public static final String COMMAND_TEMPLATE_ADDEDIT_PARAM       = BASE + "command.template.addoredit.parameter";
   public static final String COMMAND_TEMPLATE_ADDEDIT_ADD         = "add";
   public static final String COMMAND_TEMPLATE_ADDEDIT_EDIT        = "edit";
   public static final String COMMAND_TEMPLATE_ADDEDIT_EDIT_SCRIPT = "script";

   public static final String COMMAND_SCRIPTS_RDD           = BASE + "command.scripts.rdd";
   public static final String COMMAND_SCRIPTS_RDD_PARAM     = BASE + "scripts.rdd.parameter.mode";
   public static final String COMMAND_SCRIPTS_RDD_RENAME    = "rename";
   public static final String COMMAND_SCRIPTS_RDD_DUPLICATE = "duplicate";
   public static final String COMMAND_SCRIPTS_RDD_DELETE    = "delete";

   public static final String COMMAND_SCRIPTS_ADDEDIT       = BASE + "command.scripts.addoredit";
   public static final String COMMAND_SCRIPTS_ADDEDIT_PARAM = BASE + "command.scripts.addoredit.parameter";
   public static final String COMMAND_SCRIPTS_ADDEDIT_ADD   = "add";
   public static final String COMMAND_SCRIPTS_ADDEDIT_EDIT  = "edit";

   public static final String COMMAND_SCRIPT_NEWSTEP       = BASE + "command.script.newstep";
   public static final String COMMAND_SCRIPT_NEWSTEP_PARAM = BASE + "script.newstep.parameter.mode";
   public static final String COMMAND_SCRIPT_NEWSTEP_STEP  = "step";
   public static final String COMMAND_SCRIPT_NEWSTEP_PAUSE = "pause";

   public static final String COMMAND_SCRIPT_EXECUTE          = BASE + "command.script.execute";
   public static final String COMMAND_SCRIPT_EXECUTE_PARAM    = BASE + "script.execute.parameter.mode";
   public static final String COMMAND_SCRIPT_EXECUTE_SIMULATE = "simulate";
   public static final String COMMAND_SCRIPT_EXECUTE_STEP     = "step";
   public static final String COMMAND_SCRIPT_EXECUTE_EXECUTE  = "execute";

   public static final String COMMAND_SCRIPT_SAVE = BASE + "command.script.save";

   public static final String SESSION_POPUP_MENU       = BASE + "popupmenu.sessions";
   public static final String TEMPLATES_POPUP_MENU     = BASE + "popupmenu.templates";
   public static final String SCRIPTS_POPUP_MENU       = BASE + "popupmenu.scripts";
   public static final String QMANAGER_POPUP_MENU      = BASE + "popupmenu.qmanagers";
   public static final String QUEUE_CONTENT_POPUP_MENU = BASE + "popupmenu.message.actions";
   public static final String MESSAGE_VIEW_POPUP_MENU  = BASE + "popupmenu.property.table";

   public static final String TOOLCONTROL_STATUS_CONTROL = BASE + "toolcontrol.statut.control";

   private Constants() {
      // NOP
   }

}

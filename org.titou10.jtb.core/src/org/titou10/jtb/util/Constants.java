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

import org.titou10.jtb.cs.ui.SessionSelectDefaultColumnsSetHandler;
import org.titou10.jtb.selectors.FilterHandler;
import org.titou10.jtb.selectors.PropertyBuildSelectorHandler;
import org.titou10.jtb.ui.part.MessageTab;
import org.titou10.jtb.visualizer.ui.VisualizerShowPayloadAsHandler;

/**
 *
 * Global constants
 *
 * @author Denis Forveille
 *
 */
public final class Constants {

   public static final String   BASE_CORE                                  = "org.titou10.jtb.core";
   public static final String   BASE                                       = BASE_CORE + ".";
   public static final String   BASE_COMMAND                               = BASE + "command.";
   public static final String   BASE_CORE_PLUGIN                           = "platform:/plugin/" + BASE_CORE;
   public static final String   BASE_CORE_BUNDLE                           = "bundleclass://" + BASE_CORE;

   // QM Extension Points
   public static final String   JTB_EXTENSION_POINT_QM                     = BASE + "QManagerProvider";
   public static final String   JTB_EXTENSION_POINT_QM_CLASS_ATTR          = "class";
   public static final String   JTB_EXTENSION_POINT_QM_NAME_ATTR           = "displayName";

   // Connectors Extension Points
   public static final String   JTB_EXTENSION_POINT_EC                     = BASE + "ExternalConnectorProvider";
   public static final String   JTB_EXTENSION_POINT_EC_CLASS_ATTR          = "class";

   // JTB Config
   public static final String   JTB_CONFIG_PROJECT                         = "JMSToolBox";
   public static final String   JTB_LOG_FILE_NAME                          = "jmstoolbox.log";
   public static final String   JTB_CONFIG_FILE_NAME                       = "config.xml";
   public static final String   JTB_CONFIG_FILE_EXTENSION                  = "*.xml";

   public static final String   JTB_EXPORT_CONFIG_FILE_NAME                = "jmstoolbox_config.zip";
   public static final String   JTB_EXPORT_CONFIG_FILE_EXTENSION           = "*.zip";

   public static final String   JTB_VARIABLE_CONFIG_FILE_NAME              = "variables.xml";
   public static final String   JTB_VARIABLE_CONFIG_FILE_EXTENSION         = "*.xml";

   public static final String   JTB_TEMPLATE_CONFIG_FOLDER_NAME            = "Templates";
   public static final String   JTB_TEMPLATE_CONFIG_FILE_NAME              = "templates.xml";
   public static final String   JTB_TEMPLATE_CONFIG_FILE_EXTENSION         = "*.xml";
   public static final String   JTB_TEMPLATE_FILE_EXTENSION                = ".jtb";

   public static final String   JTB_SCRIPTS_FOLDER_NAME                    = "Scripts";
   public static final String   JTB_SCRIPT_CONFIG_FILE_NAME                = "scripts.xml";
   public static final String   JTB_SCRIPT_CONFIG_FILE_EXTENSION           = "*.xml";

   public static final String   JTB_VISUALIZER_CONFIG_FILE_NAME            = "visualizers.xml";
   public static final String   JTB_VISUALIZER_CONFIG_FILE_EXTENSION       = "*.xml";

   public static final String   JTB_COLUMNSSETS_CONFIG_FILE_NAME           = "columnssets.xml";
   public static final String   JTB_COLUMNSSETS_CONFIG_FILE_EXTENSION      = "*.xml";
   public static final String   JTB_COLUMNSSETS_SYSTEM_CS_NAME             = "System";

   // Preferences
   public static final String   PREFERENCE_FILE_NAME                       = "jmstoolbox.properties";
   public static final String   PREF_AUTO_REFRESH_DELAY                    = "auto.refresh.delay";
   public static final int      PREF_AUTO_REFRESH_DELAY_DEFAULT            = 30;
   public static final String   PREF_MAX_MESSAGES                          = "max.messages";
   public static final int      PREF_MAX_MESSAGES_DEFAULT                  = 200;
   public static final String   PREF_SHOW_SYSTEM_OBJECTS                   = "show.system.objects";
   public static final boolean  PREF_SHOW_SYSTEM_OBJECTS_DEFAULT           = false;
   public static final String   PREF_SHOW_NON_BROWSABLE_Q                  = "hide.non.browsable.queues";
   public static final boolean  PREF_SHOW_NON_BROWSABLE_Q_DEFAULT          = true;
   public static final String   PREF_TRUST_ALL_CERTIFICATES                = "trust.all.certificates";
   public static final boolean  PREF_TRUST_ALL_CERTIFICATES_DEFAULT        = false;
   public static final String   PREF_CLEAR_LOGS_EXECUTION                  = "clear.logs.execution";
   public static final boolean  PREF_CLEAR_LOGS_EXECUTION_DEFAULT          = false;
   public static final String   PREF_MAX_MESSAGES_TOPIC                    = "max.messages.topic";
   public static final int      PREF_MAX_MESSAGES_TOPIC_DEFAULT            = 100;
   public static final String   PREF_CONN_CLIENT_ID_PREFIX                 = "connection.client.id.prefix";
   public static final String   PREF_CONN_CLIENT_ID_PREFIX_DEFAULT         = "JMSToolBox";
   public static final String   PREF_XML_INDENT                            = "xml.indent";
   public static final int      PREF_XML_INDENT_DEFAULT                    = 3;
   public static final String   PREF_SYNCHRONIZE_SESSIONS_MESSAGES         = "synchronize.sessions.messages";
   public static final boolean  PREF_SYNCHRONIZE_SESSIONS_MESSAGES_DEFAULT = true;
   public static final String   PREF_MESSAGE_TAB_DISPLAY                   = "message.tab.display";
   public static final String   PREF_MESSAGE_TAB_DISPLAY_DEFAULT           = MessageTab.PAYLOAD.name();
   public static final String   PREF_MESSAGE_TEXT_MONOSPACED               = "message.text.monospaced";
   public static final boolean  PREF_MESSAGE_TEXT_MONOSPACED_DEFAULT       = false;
   public static final String   PREF_AUTO_RESIZE_COLS_BROWSER              = "message.browser.autoresize";
   public static final boolean  PREF_AUTO_RESIZE_COLS_BROWSER_DEFAULT      = false;
   public static final String   PREF_EDIT_MESSAGE_DND                      = "message.browser.edit.message.dnd";
   public static final boolean  PREF_EDIT_MESSAGE_DND_DEFAULT              = false;
   public static final String   PREF_COLUMNSSET_DEFAULT_NAME               = "columnsset.default.name";

   public static final String   PREF_COLUMNSSET_DEFAULT_DEST_PREFIX        = "columnsset.default.dest.prefix.";
   public static final String   PREF_Q_DEPTH_FILTER_KEY_PREFIX             = "jtb.queue.depth.filter.";

   public static final String   PREF_SESSION_TYPE_BASE                     = "sessionttype.";
   public static final String   PREF_SESSION_TYPE_PREFIX                   = PREF_SESSION_TYPE_BASE + "definition.";
   public static final String   PREF_SESSION_TYPE_INITIALIZED              = PREF_SESSION_TYPE_BASE + "initialized";
   public static final int      PREF_SESSION_TYPE_PREFIX_LEN               = PREF_SESSION_TYPE_PREFIX.length();

   public static final int      MINIMUM_AUTO_REFRESH                       = 2;

   // QM Configuration
   public static final String[] JAR_FILE_EXTENSION_FILTER                  = { "*.jar" };

   // Various
   public static final String   TS_FORMAT                                  = "yyyy-MM-dd HH:mm:ss.SSS";

   // Handle Message tab selection
   public static final String   CURRENT_TAB_JTBDESTINATION                 = "CURRENT_TAB_JTBDESTINATION";
   public static final String   CURRENT_TAB_JTBSESSION                     = "CURRENT_TAB_JTBSESSION";
   public static final String   CURRENT_COLUMNSSET                         = "CURRENT_COLUMNSSET";

   // Current Selected Script
   public static final String   CURRENT_WORKING_SCRIPT                     = "CURRENT_WORKING_SCRIPT";

   // Selectors
   public static final String   COLUMN_TYPE_COLUMN_SYSTEM_HEADER           = "COLUMN_TYPE_COLUMN_SYSTEM_HEADER";
   public static final String   COLUMN_TYPE_USER_PROPERTY                  = "COLUMN_TYPE_USER_PROPERTY";
   public static final String   FILTER_MENU_ICON                           = BASE_CORE_PLUGIN + "/icons/filter.png";
   public static final String   FILTER_MENU_URI                            = BASE_CORE_BUNDLE +
                                                                             "/" +
                                                                             FilterHandler.class.getCanonicalName();
   public static final String   FILTER_PARAM_SELECTOR                      = "FILTER_PARAM_SELECTOR";
   public static final String   FILTER_PARAM_BUILD_SELECTOR_CSH            = "FILTER_PARAM_BUILD_SELECTOR_CSH";
   public static final String   FILTER_PARAM_BUILD_SELECTOR_VALUE          = "FILTER_PARAM_BUILD_SELECTOR_VALUE";
   public static final String   FILTER_BUILD_SELECTOR_MENU_URI             = BASE_CORE_BUNDLE +
                                                                             "/" +
                                                                             PropertyBuildSelectorHandler.class.getCanonicalName();

   public static final String   COMMAND_CONTEXT_PARAM                      = BASE_COMMAND + "context.param";
   public static final String   COMMAND_CONTEXT_PARAM_QUEUE                = "queue";
   public static final String   COMMAND_CONTEXT_PARAM_MESSAGE              = "message";
   public static final String   COMMAND_CONTEXT_PARAM_DRAG_DROP            = "dragdrop";
   public static final String   COMMAND_CONTEXT_PARAM_SYNTHETIC            = "synthetic";

   // E4 Events
   public static final String   EVENT_BASE                                 = "org/titou10/jtb/event/";
   public static final String   EVENT_REFRESH_SESSION_BROWSER              = EVENT_BASE + "refresh_session_browser";
   public static final String   EVENT_REFRESH_TEMPLATES_BROWSER            = EVENT_BASE + "refresh_templates_browser";
   public static final String   EVENT_REFRESH_SCRIPTS_BROWSER              = EVENT_BASE + "scripts_browser";
   public static final String   EVENT_JTBMESSAGE_PART_REFRESH              = EVENT_BASE + "jtbmessage_refresh";
   public static final String   EVENT_REFRESH_SESSION_SYNTHETIC_VIEW       = EVENT_BASE + "refresh_session_synthetic_view";
   public static final String   EVENT_REFRESH_QUEUE_MESSAGES               = EVENT_BASE + "refresh_queue_messages";
   public static final String   EVENT_REFRESH_TOPIC_SHOW_MESSAGES          = EVENT_BASE + "refresh_topic_show_messages";
   public static final String   EVENT_TOPIC_CLEAR_MESSAGES                 = EVENT_BASE + "topic_clear_messages";
   public static final String   EVENT_TOPIC_REMOVE_MESSAGES                = EVENT_BASE + "topic_remove_messages";
   public static final String   EVENT_REFRESH_EXECUTION_LOG                = EVENT_BASE + "refresh_execution_log";
   public static final String   EVENT_CLEAR_EXECUTION_LOG                  = EVENT_BASE + "clear_execution_log";
   public static final String   EVENT_ADD_SELECTOR_CLAUSE                  = EVENT_BASE + "add_selector_clause";
   public static final String   EVENT_REBUILD_VIEW_NEW_CS                  = EVENT_BASE + "rebuild_view_new_cs";
   public static final String   EVENT_FOCUS_CTABITEM                       = EVENT_BASE + "focus_ctabitem";
   public static final String   EVENT_REFRESH_SCRIPT_EDIT                  = EVENT_BASE + "script_edit";
   public static final String   EVENT_FOCUS_SYNTHETIC                      = EVENT_BASE + "focus_synthetic";
   public static final String   EVENT_SELECT_OBJECT_SESSION_BROWSER        = EVENT_BASE + "select_object_session_browser";
   public static final String   EVENT_REFRESH_BACKGROUND_COLOR             = EVENT_BASE + "refresh_background_color";

   // E4 artefacts

   public static final String   MAIN_WINDOW                                = BASE + "main.window";
   public static final String   PARTSTACK_QCONTENT                         = BASE + "partstack.qcontent";
   public static final String   PART_SESSION_CONTENT_PREFIX                = BASE + "part.session.content.";
   public static final String   PARTDESCRITOR_SESSION_CONTENT              = BASE + "partdescriptor.session.content";
   public static final String   PART_SESSIONS                              = BASE + "part.sessions";
   public static final String   SM_DIALOG_SNIPPET                          = BASE + "dialog.scripts.manager";

   public static final String   PART_SCRIPT_PREFIX                         = BASE + "part.script.";
   public static final String   PARTDESCRITOR_SCRIPT                       = BASE + "partdescriptor.script";
   public static final String   PARTSTACK_SCRIPTT                          = BASE + "partstack.scripts";

   public static final String   COMMAND_QM_CONFIGURE                       = BASE_COMMAND + "qm.configure";
   public static final String   COMMAND_QUEUE_BROWSE                       = BASE_COMMAND + "queue.browse";

   public static final String   COMMAND_SESSION_CONNECT                    = BASE_COMMAND + "session.connect";
   public static final String   COMMAND_SESSION_DISCONNECT                 = BASE_COMMAND + "session.disconnect";
   public static final String   COMMAND_SESSION_RESCAN                     = BASE_COMMAND + "session.rescan";
   public static final String   COMMAND_SESSION_REMOVE                     = BASE_COMMAND + "session.remove";
   public static final String   COMMAND_SESSION_SYNTHETIC_VIEW             = BASE_COMMAND + "session.synthetic.view";

   public static final String   COMMAND_TOPIC_SUBSCRIBE                    = BASE_COMMAND + "topic.subscribe";
   public static final String   COMMAND_TOPIC_SUBSCRIBE_PARAM              = COMMAND_TOPIC_SUBSCRIBE + ".param";
   public static final String   COMMAND_TOPIC_SUBSCRIBE_PARAM_TOPIC        = "topic";
   public static final String   COMMAND_TOPIC_SUBSCRIBE_PARAM_MSG          = "message";

   public static final String   COMMAND_TOPIC_MESSAGE_REMOVE               = BASE_COMMAND + "topic.remove.messages";

   public static final String   COMMAND_MESSAGE_VIEW                       = BASE_COMMAND + "message.view";
   public static final String   COMMAND_MESSAGE_REMOVE                     = BASE_COMMAND + "message.remove";
   public static final String   COMMAND_MESSAGE_COPY_MOVE                  = BASE_COMMAND + "message.copyormove";
   public static final String   COMMAND_MESSAGE_SAVE_TEMPLATE              = BASE_COMMAND + "message.saveastemplate";

   public static final String   COMMAND_MESSAGE_SEND                       = BASE_COMMAND + "message.send";
   public static final String   COMMAND_MESSAGE_SEND_TEMPLATE              = BASE_COMMAND + "message.sendtemplate";

   public static final String   COMMAND_TEMPLATE_RDD                       = BASE_COMMAND + "template.rdd";
   public static final String   COMMAND_TEMPLATE_RDD_PARAM                 = BASE + "template.rdd.parameter.mode";
   public static final String   COMMAND_TEMPLATE_RDD_RENAME                = "rename";
   public static final String   COMMAND_TEMPLATE_RDD_DUPLICATE             = "duplicate";
   public static final String   COMMAND_TEMPLATE_RDD_DELETE                = "delete";

   public static final String   COMMAND_TEMPLATE_ADDEDIT                   = BASE_COMMAND + "template.addoredit";
   public static final String   COMMAND_TEMPLATE_ADDEDIT_PARAM             = BASE_COMMAND + "template.addoredit.parameter";
   public static final String   COMMAND_TEMPLATE_ADDEDIT_ADD               = "add";
   public static final String   COMMAND_TEMPLATE_ADDEDIT_EDIT              = "edit";
   public static final String   COMMAND_TEMPLATE_ADDEDIT_EDIT_SCRIPT       = "script";

   public static final String   COMMAND_SCRIPTS_RDD                        = BASE_COMMAND + "scripts.rdd";
   public static final String   COMMAND_SCRIPTS_RDD_PARAM                  = BASE + "scripts.rdd.parameter.mode";
   public static final String   COMMAND_SCRIPTS_RDD_RENAME                 = "rename";
   public static final String   COMMAND_SCRIPTS_RDD_DUPLICATE              = "duplicate";
   public static final String   COMMAND_SCRIPTS_RDD_DELETE                 = "delete";

   public static final String   COMMAND_SCRIPTS_ADDEDIT                    = BASE_COMMAND + "scripts.addoredit";
   public static final String   COMMAND_SCRIPTS_ADDEDIT_PARAM              = BASE_COMMAND + "scripts.addoredit.parameter";
   public static final String   COMMAND_SCRIPTS_ADDEDIT_ADD                = "add";
   public static final String   COMMAND_SCRIPTS_ADDEDIT_EDIT               = "edit";

   public static final String   COMMAND_SCRIPT_NEWSTEP                     = BASE_COMMAND + "script.newstep";
   public static final String   COMMAND_SCRIPT_NEWSTEP_PARAM               = BASE + "script.newstep.parameter.mode";
   public static final String   COMMAND_SCRIPT_NEWSTEP_STEP                = "step";
   public static final String   COMMAND_SCRIPT_NEWSTEP_PAUSE               = "pause";
   public static final String   COMMAND_SCRIPT_NEWSTEP_EDIT                = "edit";

   public static final String   COMMAND_SCRIPT_NEWDF                       = BASE_COMMAND + "script.newdatafile";
   public static final String   COMMAND_SCRIPT_NEWDF_PARAM                 = BASE + "script.newdatafile.parameter.mode";
   public static final String   COMMAND_SCRIPT_NEWDF_ADD                   = "add";
   public static final String   COMMAND_SCRIPT_NEWDF_EDIT                  = "edit";

   public static final String   COMMAND_SCRIPT_EXECUTE                     = BASE_COMMAND + "script.execute";
   public static final String   COMMAND_SCRIPT_EXECUTE_PARAM               = BASE + "script.execute.parameter.mode";
   public static final String   COMMAND_SCRIPT_EXECUTE_SIMULATE            = "simulate";
   public static final String   COMMAND_SCRIPT_EXECUTE_EXECUTE             = "execute";

   public static final String   COMMAND_SCRIPT_SAVE                        = BASE_COMMAND + "script.save";

   public static final String   COMMAND_SESSION_FILTER_PARAM               = BASE + "session.filter.apply.mode";
   public static final String   COMMAND_SESSION_FILTER_APPLY               = "apply";
   public static final String   COMMAND_SESSION_FILTER_UNAPPLY             = "unapply";

   public static final String   SESSION_POPUP_MENU                         = BASE + "popupmenu.sessions";
   public static final String   TEMPLATES_POPUP_MENU                       = BASE + "popupmenu.templates";
   public static final String   SCRIPTS_POPUP_MENU                         = BASE + "popupmenu.scripts";
   public static final String   QMANAGER_POPUP_MENU                        = BASE + "popupmenu.qmanagers";
   public static final String   QUEUE_CONTENT_POPUP_MENU                   = BASE + "popupmenu.message.actions";
   public static final String   SYNTHETIC_VIEW_POPUP_MENU                  = BASE + "popupmenu.synthetic";
   public static final String   MESSAGE_VIEW_POPUP_MENU                    = BASE + "popupmenu.property.table";
   public static final String   EXECUTION_LOG_POPUP_MENU                   = BASE + "popupmenu.executionlog";
   public static final String   SCRIPT_POPUP_MENU                          = BASE + "popupmenu.script";
   public static final String   SCRIPT_DATAFILE_POPUP_MENU                 = BASE + "popupmenu.script.datafile";

   public static final String   TOOLCONTROL_STATUS_CONTROL                 = BASE + "toolcontrol.statut.control";

   public static final String   PAYLOAD_BYTES_TITLE                        = "Payload: %,d bytes";
   public static final String   PAYLOAD_TEXT_TITLE                         = "Payload: %,d chars";

   public static final String   VISUALIZER_PARAM_NAME                      = "visualizer.param.name";
   public static final String   VISUALIZER_PARAM_JTBMESSAGE                = "visualizer.param.jtbmessage";
   public static final String   VISUALIZER_MENU_ICON                       = BASE_CORE_PLUGIN + "/icons/visualizers/camera.png";
   public static final String   VISUALIZER_MENU_URI                        = BASE_CORE_BUNDLE +
                                                                             "/" +
                                                                             VisualizerShowPayloadAsHandler.class
                                                                                      .getCanonicalName();

   public static final String   COLUMNSSET_PARAM                           = "columnsset.param";
   public static final String   COLUMNSSET_PARAM_JTBSESSION                = "columnsset.param.jtbsession";
   public static final String   COLUMNSSET_PARAM_JTBDESTINATION            = "columnsset.param.jtbdestination";
   public static final String   COLUMNSSET_MENU_ICON                       = BASE_CORE_PLUGIN + "/icons/columnsSets/monitor.png";
   public static final String   COLUMNSSET_MENU_URI                        = BASE_CORE_BUNDLE +
                                                                             "/" +
                                                                             SessionSelectDefaultColumnsSetHandler.class
                                                                                      .getCanonicalName();

   public static final String   SESSION_TYPE_SESSION_DEF                   = "sessiontype.sessiondef";

   public static final String   CHARSET_DEFAULT_PREFIX                     = "Default";
   public static final String   CHARSET_DEFAULT                            = CHARSET_DEFAULT_PREFIX +
                                                                             " (inherited from container: ";

   public static final String   NODE_FOLDER_QUEUES_NAME                    = "Queues";
   public static final String   NODE_FOLDER_TOPICS_NAME                    = "Topics";

   public static final String   JTB_JOBS_FAMILY                            = "JMSToolBox";

   private Constants() {
      // NOP
   }

}

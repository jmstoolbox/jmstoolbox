2024-??-?? v6.6.0
------------------
- Important Changes
    - 
- New features:
    - 
- Bugs fixed:
    - Fixes #166: Azure ServiceBus: missing netty dependencies
    - Many fixes related to bytes messages thanks to Thomas Raddatz 
- Other:  
    - Upgraded Apache Artemis 2.x jars to v2.34.0
    - Upgraded eclipse RCP to v2024-06 (v4.32)
    - Upgraded eclipse tycho to v4.0.8 (build system)
    - Upgraded JRE to v17.0.11

2024-03-26 v6.5.0
------------------
- Bugs fixed:
    - Fixes #163: ActiveMQ: browser.getEnumeration().nextElement() can be null where it shouldn't
- Other:    
    - Upgraded Apache Artemis 2.x jars to v2.33.0
    - Upgraded Solace PubSub+ jars to v10.22.0
    - Upgraded eclipse RCP to v2024-03 (v4.31)
    - Upgraded eclipse tycho to v4.0.6 (build system)
    - Upgraded JRE to v17.0.10

2024-01-18 v6.4.0
------------------
- Bugs fixed:
    - Fixes #154: Software AG Universal Messaging: Fix public changes with v10.15+
- Other:    
    - Upgraded Apache Artemis 2.x jars to v2.31.2
    - Upgraded eclipse RCP to v2023-12 (v4.30)
    - Upgraded eclipse tycho to v4.0.4 (build system)
    - Upgraded JRE to v17.0.9

2023-09-14 v6.3.0
------------------
- New features:
    - Export message payload in batch
    - Export message as templates in batch (Closes #52)
    - REST: it is now possible select messages to delete via a JMS selector (Closes #152)
- Bugs fixed:
    - Fix various D&D messages problems, especially on linux/mac
- Other:    
    - Upgraded Apache Artemis 2.x jars to v2.30.0
    - Upgraded eclipse RCP to v2023-09 (v4.29)
    - Upgraded JRE to v17.0.8 (Windows)
    - Upgraded eclipse tycho to v4.0.2 (build system)
    - Upgraded slf4j to v2.0 (logging system)
    - Refactored export message payload
    
2023-05-18 v6.2.0
------------------
- Important Changes
    - All flavors of JMSToolBox (linux, macos, windows) are now bundled with a JRE
    - The executable bit on the JMSToolBox executable for linux and macos is now set, so not need to set it manually after install
- Bugs fixed:
    - Fixes #149: ActiveMQ: use JMS over HTTP
    - Fixes #33: The application "JMSToolBox.app" can't be opened
- Other:    
    - Upgraded Azure ServiceBus dependencies
    - Upgraded Apache ActiveMQ jars to v5.18.1
    - Upgraded Solace PubSub+ jars to v10.19.0
    - Upgraded eclipse tycho to v3.0.4 (build system)

2023-03-16 v6.1.0
------------------
- Bugs fixed:
    - Message Browser: JMS Headers and properties were not updated properly in the 
    - Message Browser: Wrong colors in dark mode on linux 
    - Fixes #146: OpenLiberty: Recent versions of JTB requires a custom SSLFactory to connect to Liberty
    - Fixes #147: Solace: Added AUTHENTICATION_SCHEME property for client certification authentication 
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.28.0
    - Upgraded eclipse RCP to v2023-03 (v4.27)
    - Upgraded JRE to v17.0.6 (Windows)
    - Upgraded eclipse tycho to v3.0.3 (build system)

2022-12-29 v6.0.0
------------------
- Important Changes
    - JMSToolBox v6.x now requires Java 17 to run
- Bugs fixed:
    - Fixes #143: Oracle AQ: ORA-12505, TNS:listener does not currently know of SID given in connect descriptor
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.27.1
    - Upgraded eclipse RCP to v2022-12 (v4.26)
    - Upgraded JRE to v17.0.5 (Windows)
    - Upgraded eclipse tycho to v3.0.1 (build system)

2022-10-26 v5.16.0
------------------
- New feature:
    - Initial implementation of Oracle AQ Queue Manager plugin
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.26.0
    - Upgraded eclipse RCP to v2022-09 (v4.25)
    - Upgraded JRE to v11.0.17 (Windows)
    - Upgraded eclipse tycho to v3.0.0 (build system)

2022-08-17 v5.15.0
------------------
- Improvements:
    - REST: return all JMS headers and properties with Messages (eg. JMSMessageID...)
- Bugs fixed:
    - REST: POSTing a message from a template is broken and silently fails: "unable to locate the template"
    - REST: add selector + payload text selection when retrieving messages
    - The "new Folder" menu in the Template pane must only be available when a parent file or directory is selected
    - Fix "flacky" delete icons in Variables/ColumnSets/Vizualizers/Template Dir lists (Linux)
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.24.0
    - Upgraded Azure ServiceBus dependencies
    - Upgraded Solace PubSub+ jars to v10.15.0
    - Upgraded eclipse RCP to v2022-06 (v4.24)
    - Upgraded JRE to v11.0.16 (Windows)
    - Upgraded eclipse tycho to v2.7.4 (build system)

2022-06-07 v5.14.0
------------------
- Bugs fixed:
    - Fixes #134: IBM MQ "JMSXAppID" not overridden by the "Client ID" string set in preferences
    - Fixes #133: Properties with a name starting with "JMS_<vendor>_<name>" flagged as invalid where they are allowed by the JMS spec
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.22.0
    - Added a link to ko-fi in the "About" dialog: https://ko-fi.com/titou10titou10
    - Upgraded JRE to v11.0.15.1 (Windows) 
    - Upgraded eclipse tycho to v2.7.3 (build system)

2022-04-04 v5.13.0
------------------
- New feature:
    - First distribution of JMSToolBox for "Apple Silicon Mac" / arm64 (Feature Request #131)
- Improvements:
    - Feature Request #126: ActiveMQ: manage different credentials for JMX connection and session connection 
- Bugs fixed:
    - Fixes #127: NullPointerException since v5.12.0 when a session property is set to null
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.21.0
    - Upgraded eclipse RCP to v2022-03 (v4.23)
    - Upgraded eclipse tycho to v2.7.0 (build system)
    - Upgraded JRE to v11.0.14.1 (Windows) 
    - Upgraded GraalVM jars to v21.3.0 
    - Replaced abandonned AdoptOpenJDK by Azul Zulu JDK (AdoptOpenJDK is replaced by Eclipe Temurin that does not provide JREs)
   
2021-12-06 v5.12.0
------------------
- Bugs fixed:
    - Fixes #115: GraalVM Visualizer interoperability with Java is broken
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.19.0
    - Upgraded Apache ActiveMQ jars to v5.16.3
    - Upgraded Azure ServiceBus jars to v3.6.6 /v0.0.9
    - Upgraded eclipse RCP to v2021-09 (v4.21)
    - Upgraded eclipse tycho to v2.5.0 (build system)
    - Upgraded JRE to v11.0.12 (Windows) 

2021-05-08 v5.11.0
------------------
- Improvements:
    - Feature Request #114: Show the binary representation of text payload for TextMessage 
    - Feature Request #112: IBM MQ: Added "CCSID" property on sessions properties 
    - Solace PubSub+: Implement pagination when retrieving destinations
- Bugs fixed:
    - Do not try to get Q Depth if the Queue is not browsable
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.17.0
    - Upgraded Apache ActiveMQ jars to v5.16.1
    - Upgraded Azure ServiceBus jars to v3.6.3
    - Upgraded Solace PubSub+ jars to v10.10.0
    - Upgraded JRE to v11.0.11 (Windows)
    - Upgraded eclipse RCP to v2021-03 (v4.19)
    - Upgraded eclipse tycho to v2.3.0 (build system) 

2021-01-22 v5.10.0
------------------
- Improvements:
    - REST: added services to retrieve queues depth 
- Bugs fixed:
    - Fixes #102: Properties are convered to type String when copying messages
- Other:
    - Upgraded Azure ServiceBus jars to v3.6.0
    - Upgraded Azure ServiceBus JMS jars to v0.7.0
    - Upgraded eclipse RCP to v2020-12 (v4.18)
    - Upgraded eclipse tycho to v2.2.0 (build system) 

2020-12-01 v5.9.0
-----------------
- New feature:
    - Feature Request #94: Added initial support for Azure Service Bus
- Bugs fixed:
    - Fixes #92: Make Destination Information text selectable
    - Fixes #93: "Trouble setting up the development environment"
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.16.0
    - Upgraded Apache ActiveMQ jars to v5.16.0
    - Upgraded JRE to v11.0.9 (Windows)
    - Upgraded eclipse RCP to v2020-09 (v4.17)
    - Upgraded eclipse tycho to v2.1.0 (build system) 
    
2020-08-08 v5.8.0
-----------------
- Improvements:
    - Implements #84: Added a tab in message viewer pane to display text JSON messages pretty formatted  
    - Script vizualisers: GraalVM (ECMA 2020) in replacment of Nashorn (Soon to be removed from JDK) 
- Bugs fixed:
    - Fixes #83: IBM MQ: Discard "Ghost Queues
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.14.0
    - Upgraded Apache ActiveMQ jars to v5.15.12
    - Upgraded JRE to v11.0.8 (Windows)
    - Upgraded eclipse RCP to v2020-06 (v4.16)

2020-05-09 v5.7.0
-----------------
- Bugs fixed:
    - Fixes #72: Bypassing SSL hostname verifications does not work for some version of Java    
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.12.0
    - Upgraded JRE to v11.0.7 (Windows)
    - Upgraded eclipse RCP to v2020-03 (v4.15)
    - Upgraded eclipse tycho to v1.7.0 (build system) 
    
2019-12-25 v5.6.0
-----------------
- Bugs fixed:
    - Fixes #65: "Show Queues depth" context menu does not appear in some circunstances 
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.10.1
    - Upgraded eclipse RCP to v2019-12 (v4.14)
    - Upgraded JRE to v11.0.5 (Windows)
    - Upgraded eclipse tycho to v1.5.1 (build system) 

2019-08-23 v5.5.0
-----------------
- New feature:
    - Added initial support for Solace PubSub+
- Other:
    - Upgraded JRE to v11.0.4 OpenJ9 v0.15.1 (Windows)

2019-07-14 v5.4.0
-----------------
- Bugs fixed:
    - Fixes #55: Do not try to remove JMSMessage with no "JMSMessageID" property 
    - Fixes #56: ActiveMQ: Connect unsuccessful 
    - Fixes #58: Not able to clear a non required session property after a value has been set 
    - Fixes #59: NullPointerException when using a template with properties in a script  
    
2019-06-11 v5.3.0
-----------------
- Bugs fixed:
    - Artemis 2.x: "minLargeMessageSize" was hard coded to 107857600 !
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.9.0
    - Upgraded JRE to v11.0.3 OpenJ9 v0.14.3 (Windows)
    
2019-04-18 v5.2.0
-----------------
- Improvements:
    - Artemis 2.x: Added "minLargeMessageSize" and "compressLargeMessage" connection factory parameters
    - Sort alphabetically destination information attributes 
- Bugs fixed:
    - Fixes #46: "Open Payload As" contextual menu not shown on MapMessage with payload 
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.7.0
    - Upgraded JAXB API libs to v2.3.2
    - Upgraded eclipse RCP to v2019-03 (v4.11)
    - Upgraded eclipse tycho to v1.4.0 (build system) 
    - Upgraded JRE to v11.0.3 (Windows)

2019-02-22 v5.1.0
------------------
- Improvements:
    - Use Map value as column source in a Column Set (Feature Request #40)
- Bugs fixed:
    - Fixes #44: REST: Empty queue service returns "destination not found"
    - Fixes #43: ClassCastException when selecting a template and right click on message browser
- Other:
    - Upgraded Resteasy modules to v3.6.3 and jackson to v2.9.8 (REST plugin)
    - Upgraded Apache Artemis 2.x jars to v2.6.4
    - Upgraded JAXB to v2.3.2
    - Upgraded JRE to v11.0.2 (Windows)
    
2019-01-03 v5.0.0
------------------
- Important Changes
    - JMSToolBox now requires Java 11. A good source for Java 11 JRE is Adopt OpenJDK: https://adoptopenjdk.net/
    - JMSToolBox is only available for 64 bits OS (Windows, Linux, Mac OS) from now on. 
     -> This because eclipse RCP  2018-12+ is no more available for 32 bits OS and JMSToolbox is based on the eclipse RCP platform  
- Bugs fixed:
    - Fixes #37: Scripts: steps of kind "pause" appear as a blank line in the script editor
- Other:
    - Upgraded eclipse RCP to v2018-12 (v4.10)
    - Upgraded eclipse tycho to v1.3.0 (build system) 
    - Embeds AdoptOpenJDK/J9 v11.0.1 in place of v1.8.0_192 (windows) 
    
2018-12-07 v4.14.0
------------------
- New features:
    - Fixes #14: Add a "Filter..." contextual menu to generate JMS Selectors
- Improvements:
    - Fixes #36: Add a menu to delete selected messages in a topic subscription
    - PR #17: Refresh message browser when max messages is modified
- Other:
    - Upgraded Apache ActiveMQ jars to v5.15.8
    - Replaced Oracle JDK by OpenJDK/J9 in Windows distributions from Adopt OpenJDK

2018-10-30 v4.13.0
------------------
- Improvements:
    - Issue #29: IBM MQ: Make "queueManager" property optional to allow to connect to the default Q Manager
- Bugs fixed:
    - Issue #30: Regression: Boolean session properties value is required even if they are not mandatory
    - Issue #32: NPE while running scripts against templates without properties
- Other:
    - Upgraded Apache ActiveMQ jars to v5.15.7
    - Upgraded eclipse RCP to v2018-09
    - Upgraded JRE to v1.8.0_192 (Windows)
    - Various other bugs fixes

2018-09-12 v4.12.1
------------------
- Bugs fixed:
    - Fixes #26: D&D of 2 or more templates to a destination generates a NPE 
    
2018-09-12 v4.12.0
------------------
- New features:
    - Issue #23. Allow the possibility to connect to an ActiveMQ server without JMX with limited functionnalities
- Improvements:
    - Issue #24: IBM MQ: Add properties to specify client side certificate information
    - Issue #25: The "use as selector" feature generates bad JMS selectors for String properties that contains quotes
- Bugs fixed:
    - Fixes #21: Manage the JMS user properties name/values, along with their class (Double, Integer, String etc.) 
    - Fixes #22: Wrong value generated for selector for header "JMSDeliveryMode"
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.6.3
    - Upgraded Apache ActiveMQ jars to v5.15.6
�
2018-07-20 v4.11.0
------------------
- Improvements:
    - PR #16: OpenMQ: do not use authentication if user is null or empty
- Bugs fixed:
    - Fixes #17: No queues are visible when session is opened on an embedded ActiveMQ in some setups
    - Fixes #18: ActiveMQ: Connection with failover does not work properly
    - Fixes #19: Topic subscription does not work properly, not correclty capturing all messages
- Other:
    - Upgraded OpenMQ jars to v5.1.1
    - Upgraded JRE to v1.8.0_181 (Windows)
    - Replaced class 'MDialog' deprecated in eclipse 4.8 by class 'MWindow'

2018-06-28 v4.10.0
------------------
- Improvements:
    - Fixes #8: Combine JMS-Selector with payload-search
- Bugs fixed:
    - Fixes #9: Backgound color related to "Session Type" not set for Topic browser & Queue depth view
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.6.2
    - Upgraded Apache ActiveMQ jars to v5.15.4
    - Upgraded eclipse RCP to v4.8.0
    - Upgraded eclipse tycho (build system) to v1.2.0
    - Upgraded JRE to v1.8.0_172 (Windows)
    - Fixes #10: Added the "Automatic-Module-Name" stanza in all MANIFEST.MF to prepare for Java 9+
    - Some other minor UI improvements
    
2018-03-27 v4.9.0
-----------------
- New features:
    - Added a menu to easily export JMSToolBox log files for debugging 
- Bugs fixed:
    - Fixes #1: Allow any netty connector property on Artemis 1.x and 2.x sessions
    - Fixes #4: NPE when editing session attributes while a destination is being browsed in the message browser
    - Fixes #5: Updates of the userid or password in a session settings are only used after JMSToolBox is restarted 
    - Fixes #7: "Destination information" for "topics" in Artemis 2.x is broken 
- Other:
    - Upgraded Apache Artemis 2.x jars to v2.5.0
    - Upgraded Apache Artemis legacy jars to v1.5.6
    - Upgraded eclipse RCP to v4.7.3
    - Upgraded eclipse tycho (build system) to v1.1.0
    - Some other minor UI improvements
    
2018-02-20 v4.8.0
-----------------
- Important Change
    - JMSToolBox migrated from SourceForge/subversion to GitHub/git 
- Bugs fixed:
    - D&D messages from one destination to another causes unexpected behavior in some cases (eg Timeouts in IBM MQ)
    - NullPointerException when showing the "Queue Depth" view causing the contextual menu to be wrong in the Message browsing view 
- Other:
    - First release from Git/GitHub
    - Upgraded Apache ActiveMQ jars to v5.15.3

2018-02-01 v4.7.2
-----------------
- Emergency bug fixed:
    - Scripts: Queue names do not appear when associating a destination to a step
    - Scripts: NullPointerException when a session is associated with a step that does not exist anymore

2018-01-30 v4.7.1
-----------------
- Emergency bug fixed:
  - import "sessions config" does not import sessions if import preferences is also selected
    
2018-01-28 v4.7.0
-----------------
- New features:
    - Option to prompt for credentials on connect (feature Request #26) 
    - Ability to refresh the list of queues and topics (Feature Request #27)
    - Assign a "background color" per session to visually identify sessions per their roles: "production", "QA", "dev"... (Feature Request #25) 
- Improvements:
    - Complete redesign of the export/import feature
    - Redesign of the session configuration dialog
- Other:
    - Upgraded eclipse RCP to v4.7.2
    - Upgraded JRE to v1.8.0_162 (Windows)
    - Many other minor bug fixes and UI improvements

2017-11-30 v4.6.0
-----------------
- New features:
    - Added initial support for Software AG Universal Messaging
- Improvements:
    - Sort the content of the Queues depth view by clicking on the column header (Feature Request #24) 
- Bugs fixed:
    - Bug #19: "Use as selector" does not take into account the type of the property 
    - Bug #18: Unable to close JMSToolBox in some cases when there is a Topic Listener active (User gets "Widget is disposed" errors)
    - TIBCO EMS: JMSToolBox fails to connect to a server with protocol=ssl 
- Other:
    - Do not try to instantiate a QM for a plugin declared in config file but not installed in JMSToolBox
    - Upgraded Apache Artemis jars to v2.4.0
    - Upgraded Apache ActiveMQ jars to v5.15.2

2017-10-22 v4.5.0
-----------------
- New features:
    - Added a new contextual menu to generate selector clauses for JMS properties of king timestamp
- Improvements:
    - Allow tab "Payload (XML)" to be selected as default tab selection in Message Viewer (Feature Request #19)
    - Added a preference to automatically adjust columns width based on the content in the Message Browser
    - Display real value (long) for JMS Timestamps in addition to the human readable form + generate correct JMS Selector for Timestamps  (Feature Request #22)
    - Greatly improved the dialog to create a Date Variable
    - Augment the maximum number of message displayed from 9999 to 99999 (Feature Request #21)
- Bugs fixed:
    - Bug #17 : NullPointerException in some situations when using the "Copy as selector" menu
    - Do not display the "Copy as selector" and "Add to current Column Set" menu when no message is displayed
    - Search text in Message payload gives a NullPointerException for TextMessages with null payload
    - Restrict JMSHeader to be used as selector clauses to the ones allowed by the JMS specifications
    - Many other minor bug fixes and UI improvements
- Other:
    - Upgraded eclipse RCP to v4.7.1a
    - Upgraded JRE to v1.8.0_152 (Windows)
    - Upgraded Apache Artemis jars to v2.3.0
    - Upgraded Apache ActiveMQ jars to v5.15.1
    
2017-08-30 v4.4.1
-----------------
- Bugs fixed:
    - JMSToolBox does not start if the file "jmstoolbox.properties" holding the JMSToolBox preferences does not exist
    
2017-08-21 v4.4.0
-----------------
- New features:
    - Columns Sets: Allows to select what header/properties are shown in the Message Browser (Feature Request #18)
- Bugs fixed:
    - Reading the JMSDeliveryTime header results in "AbstractMethodError" exception in some cases. This header is only valid since JMS v2.0 
- Other:
    - Added Ralf Lehmann as a JMSToolBox contributor
    - Added a dynamic splash screen
    - Many other minor bug fixes and UI improvements
    - Major internal refactoring on the way preferences are managed
    
2017-08-02 v4.3.0
-----------------
- Improvements:
    - Use the previously selected tab in the Message Viewer when showing a new message (Feature Request #17)
    - Renamed tab "Overview" to "toString" in the message viewer 
    - Display the build timestamp in the "About" dialog
- Bugs fixed:
    - Bugs #15, #16: Oracle WLS: "InvalidClassException: javax.jms.JMSException; local class incompatible" fixed 
- Other:
    - Replaced j2ee.jar (from JEE v7.0) by javax.jms-api-2.0.1.jar to fix bugs #15 and #16
    - Upgraded JRE to v1.8.0_144 (Windows)
    - Upgraded Apache Artemis jars to v2.2.0

2017-07-09 v4.2.0
-----------------
- Improvements:
    - 'Queues Depth' view: Add the possibility to set the refresh delay when activating auto refresh
    - 'Queues Depth' view: Use background job for collecting data (Feature Request #14) 
    - 'Queues Depth' view: Preserve the current queue filter (Feature Request #14)
- Bugs fixed:
    - Bug #14: Selecting a message or a Queue in the synthetic view then pressing CTRL-T (new Template) causes a ClassCastException
    - Dropping an external file on an empty area in the Template viewer causes an exception
    - Dropping an external file on an empty area or on an object different than a Queue or a Topic causes an exception 
- Other:
    - Upgraded eclipse RCP to v4.7.0
    - Upgraded Apache ActiveMQ jars to v5.15.0

2017-05-26 v4.1.0
-----------------
- Important
    - Scripts: The feature that allowed to read and post messages from a list of templates stored in a directory has been removed
- New features:
    - Scripts: generate messages based on files from a directory with one file per payload (with a Template of type "Text" or "Bytes") 
    - Templates: Added the possibility to defined many Template directories in addition of the "system internal" template directory
    - Message Viever: Added a button to "Duplicate and Post " the message edited to the same Destination  
    - REST: Execute a script via a REST call 
- Improvements:
    - Linux: Force the usage of GTK v2.x for a way better visual experience (instead of GTK v3.x on some OS like Ubuntu)
    - Drag & Drop: it is now possible to Drag & Drop multiple messages/templates/external files to/from template/message/session browsers
    - Double click on the "Queue" folder in a session to display the "Queue Depth" view 
    - MapMessages: Allow variable replacement for values 
    - Scripts: Ask to save/discard "dirty" scripts when the main windoww is closed and some Scripts have not been saved
    - Linux/GTK: Many UI improvements and fixes (Dialogs dimensions, size of columns in tables etc.)
- Bugs fixed:
    - Scripts: Fixed the progress monitor dialog when executing/simulating a script
    - Show the error dialog before the JMSToolBox main window pops up when a problem is detected while initializing plugins
    - Try to initialize all plugins on startup and do not stop after the first failing one     
- Other:
    - Complete refactoring of the way templates are stored
    - Upgraded Apache Artemis jars to v2.1.0
    - Upgraded Apache Artemis legacy jars to v1.5.5
    - Various other minor bug fixes and UI improvements

2017-04-28 v4.0.0
-----------------
- New features:
    - "Payload visualizers": 
     - Visualize the payload directly from JMSToolBox by calling the OS tools associated with a file extension (zip, pds, libre office, etc.)
     - Define custom payload visualizers that associate a vizualiser to an OS file extension
     - Define custom inline or external scripts to process the payload (in JavaScript / nashorn)
     - Define custom external OS commands to process the payload
     - Expose services to custom scripts to display the content based on a file extension
- Improvements:
    - Ask what type of message to create (Text, Bytes) when an external file is dropped on the message browser 
    - Message Auto Refresh button: Replace the "dynamic" tooltip by a popup to capture the refresh delya to be more gtk friendly
    - Variables can now be edited
    - Scripts: Specify charset for datafiles
    - Added a busy indicator for the "Empty Queue" and "Remove Messages" actions  
    - Added a preference setting to choose what tab to display in the Message View Part
    - Added buttons to 
     - delete variables in the "Variable Manager" dialog, in addition to the "delete" button
     - delete a value from the values of the "List" kind of variable, in addition to the "delete" button
     - delete jars "Q Manager" configuration dialog, in addition to the "delete" button (Feature Request #1)
     - delete scripts steps, in addition to the "delete" button 
     - delete scripts global variables, in addition to the "delete" button
     - delete scripts data files, in addition to the "delete" button
    - Apache ActiveMQ Artemis: Added connector property TransportConstants.HTTP_UPGRADE_ENABLED_PROP_NAME ("httpUpgradeEnabled")
- Bugs fixed:
    - Apache ActiveMQ: Display the right exception message when JMSToolBox can't connect to the server 
    - Make the "Message Type" combo box read only and hide the "Import Payload" button when viewing a message 
    - Do not show the "Export Payload" submenu on message when there is no payload
    - Exception occurs when selecting no file in the "Import Payload" dialog 
    - Exception occurs when importing binary payload in a new message when computing the size of the payload for the dialog title text 
- Other:
    - Set minimum JVM size to 512m (-Xms512m)
    - Upgraded JRE to v1.8.0_131 (Windows)
    - Upgraded Apache ActiveMQ jars to v5.14.5
    - Major internal code refactoring for Variables/Visualizers/Scripts/Templates...
    - Many, many other minor bug fixes and UI improvements
    

2017-04-11 v3.12.0
------------------
- New features:
    - Added initial support for Apache ActiveMQ Artemis v2.x
    - Added support for IBM MQ v6.0 (mqseries v6) with no broker configured
- Improvements:
    - Apache ActiveMQ: Allow failover connections (JMX+JMS)  (Feature Request #13)
    - Added a preference setting to disable automatic destination synchronization selection with the selected destination in the message browser
    - Added a scrollbar on the new/edit session dialog to use in case all the fields are not visible in the dialog 
- Bugs fixed:
    - The Session Treeviever is not synchronized when the Message browser is opened from the 'Queue Depth' view
- Other:
    - Oracle WebLogic server: Changed the way credentials are passed to the JMX connection to match official documentation
    - Upgraded eclipse RCP to v4.6.3
    - Upgraded Apache ActiveMQ jars to v5.14.4
    - Upgraded Apache Artemis legacy jars to v1.5.4
    - Various other minor bug fixes and UI improvements

2017-02-24 v3.11.0
------------------
- Improvements:
    - Display the length of the body for Text and Bytes messages in the Message Viewer
    - By default, show the payload tab in the Message Viewer
    - Synchronise the destination in the Session Browser with the tab selected in the Queue Browser (Feature Request #10.1)
    - Synchronise the Message Viewer content with the message selected in the Message Browser (Feature Request #10.2)  
    - Changed the visual of "non browsable" destination from a gray background to specific icon
    - Add the possibility to export multiple messages as templates at the same time from the message browser
    - IBM MQ: Add the possibility to connect to IBM MQ without a userid (Feature Request #11)
- Bugs fixed:
    - Do not try to show the message browser after dropping a message on a "non browsable" destination
    - Binary Messages: only 13 bytes where visible in the hex viewer where it should have been 16
- Other:
    - Upgraded Apache Artemis jars to v1.5.3
    - Upgraded eclipse tycho (build system) to v1.0.0
    - Various other minor bug fixes and UI improvements

2017-02-03 v3.10.0
------------------
- Improvements:
    - Do not allow to "browse" Queues that can not be browsed (like the IBM MQ "alias" or "model" queues)
    - Added a preference key to hide or show 'non browsable' queue in the Queue Depth browser
    - IBM MQ: Added many properties in the "Destination Information" popup
    - Message Viewer: Added CTRL+A key shortcut to select text and xml payload
    - Do not restart if no changes have been made while configuring jars for a Q Manager (Feature Request #2) 
    - The preference page has been restructured
    - Various other minor bug fixes and UI improvements
- Bugs fixed:
    - One 'synchronous' session was used for all Topic subscriptions. This is in violation of JMS specs   
- Other:
    - Upgraded eclipse RCP to v4.6.2
    - Upgraded JRE to v1.8.0_121 (Windows)
    - Upgraded Apache ActiveMQ jars to v5.14.3
    - Upgraded Apache Artemis jars to v1.5.2

2016-12-04 v3.9.0
-----------------
- New features:
    - Added support for Oracle WebLogic server as a Q provider (Feature Request #9) 
- Improvements:
    - Added a preferences to set the number of characters used for XML indentation
- Bugs fixed:
    - Bug #12: Destination name filtering: the '.' character was badly interpreted
    - Bug #13: XML pretty print added additional whitespaces (Thanks Ralf!)
- Other:
    - Upgraded Apache Artemis jars to v1.5.0

2016-11-02 v3.8.0
-----------------
- New features:
    - Queue Depth View: added the possibility to filter Queues shown based on name
- Improvements:
    - Apache ActiveMQ: Added the possibility to specify the JMX context to access the server(ie karaf-xxx')
- Bugs fixed:
    - Apache Artemis, HornetQ, OpenMQ: Impossible to vizualize ObjectMessages even if the jar with the implementation class is added to the QM config
    - Bug #9: Downgraded HornetQ jar to v2.3.25 due to an imcopatibility with HornetQ v2.3.x embedded in JBoss EAP v6.3.x
    - Bug #11. Apache Active MQ: Added a protection to exclude Destinations with an empty name 
- Other:
    - Upgraded Apache ActiveMQ jars to v5.14.1
    - Windows: Upgraded JRE to v1.8.0_112


2016-09-29 v3.7.0
-----------------
- Improvements:
    - Added the timestamp of the first message to be consumed in the "Queue Depth" view
    - Added compatibility for older versions of Apache ActiveMQ (v5.8.0-)
- Bugs fixed:
    - the "Queue Depth" view was not getting focus on second time selection
    - Apache Artemis: The Queue depth type changed in v1.4.0 from Integer to Long causing an exception
    - Various minor bug fixes and UI improvements
- Other:
    - Upgraded Apache Artemis jars to v1.4.0
    - Upgraded eclipse RCP to v4.6.1
    - Upgraded eclipse tycho (build system) to v0.26.0


2016-08-15 v3.6.0
-----------------
- New features:
    - Added a "Synthetic View" that shows depth of Queues for a session in one view
    - Allow Failover/Load balancing connections for selected Q Managers (SonicMQ, Tibco ems)
- Bugs fixed:
    - "F5" (Refresh) on Message Browser was broken
    - Drag a message from a Topic and Drop it to OS was not enabled
    - Correctly "dispose" images and icons used by JMSToolBox  
    - Various minor bug fixes and UI improvements
- Other:
    - Upgraded Apache ActiveMQ jars to v5.14.0
    

2016-07-26 v3.5.0
-----------------
- New features:
    - Added an Hex viewer for BytesMessage
    - HornetQ : Added a 'CORE mode' that let JMSToolBox to work with core defined queues instead of JMS defined queues
    - Export message to OS as Templates
    - Drag & Drop message to OS to export payload (Text, Bytes, Map)
    - Drag & Drop templates from OS to destination browser, destination content browser, template browser
    - Drag & Drop templates to OS
- Improvements:
    - Handling of the "jmsReplyTo" JMS Header
    - REST: do not output null properties or empty arrays + pretty print
- Bugs fixed:
    - REST: Posting a MapMessage was broken
    - Droping an external file (from OS) on a destination in the destination browser was broken
    - Various minor bug fixes and UI improvements
- Other:
    - REST: Replaced JAX-RS implementation from "jersey" to "RESTEasy" for Jetty + Java 8 compatibility
    - Renamed "IBM WebSphere MQ" to "IBM MQ" as IBM changed (again!) the name of the product for v9.0.0 
    - Upgraded Apache ActiveMQ jars to v5.13.4
    - Upgraded Jackson (JSON-REST) jars to v2.8.1
    

2016-07-07 v3.4.0
-----------------
- Important
    - JMSToolBox now requires Java 8 to run. Windows versions are now bundled with Java 8
      Sessions associated with the "WebSphere" Q Manager may cause problems due to jar incompatibilities with Java 8. Three possible options:
     - update the required dependent jar to WebSphere version >= 8.5.5.9. Previous versions of jars are not Java 8 compatible
     - or switch the session to the "WebSphere SIB" Q provider
     - or stick to JMSToolBox v3.3.0
- Improvements:
    - Scripts: added an option to disable the feedback in log viewer of messages post to greatly speed up  (x100) script execution
    - WebSphere MQ: Added suport for the "com.ibm.mq.cfg.useIBMCipherMappings" property
    - HornetQ: set "ConnectionTTL = -1" on ConnectionFactory
    - Added a contextual menu entry in the Message browser to "Export payload" 
    - Added the possibility to export Map Messages payload as text files 
    - Added a "New Session..." contextual menu on session tree browser
    - Added tooltips on properties session/ Q Managers properties
    - Added default values for some session/ Q Managers properties
- Bugs fixed:
    - Inline editing of headers fixed when editing a message
    - Various minor bug fixes and UI improvements
- Other:
    - JMSToolBox now requires Java 8 to run
    - Upgraded Apache Artemis jars to v1.3.0
    - Upgraded to eclipse-rcp to v4.6.0 ("Neon") planned but postponed for 3 reasons
     - a bug in v4.6.0: https://bugs.eclipse.org/bugs/show_bug.cgi?id=496695
     - incompatibility between latest version of java 8 + jersey + jetty from eclipse v4.6.0 
     - no WindowBuilder comptabible version is yet available: http://www.eclipse.org/windowbuilder/download.php

2016-06-09 v3.3.0
------------------
- New features:
    - Added support for TIBCO EMS as a Q provider
    - Create a new Text Message by dropping a file onto a Destination
- Bugs fixed:
    - Multiple opened sessions for the same Q provider were not handled correctly
    - Bad handling of destination content viewer when viewing a Queue and a Topic with the same name for the same session     
    - Various minor bug fixes and UI improvements
- Improvements:
    - SonicMQ: Display temporary queues only if set in preferences
    - Minium refresh time in seconds for automatic Q browsing is now 2 seconds
- Other:
    - JMSToolBox REST services are no more considered experimental 
    - Added Raymond Meester as contributor for his help on the SonicMQ plugin 


2016-05-24 v3.2.0
------------------
- New features:
    - Added basic support for SonicMQ (aka CX Messenger) as a Q provider
    - Possibility to set the clientID prefix string to identify JMSToolBox connections on the server side  
- Bugs fixed:
    - NPE when closing a Topic browsing tab when the Topic Consumer is in pause state 
    - Preferences are not saved if the "REST" preference page has not been visited
    - Browsing a topic, closing the session, the reopening the session an dbrowsing another topic caused an Exception
    - Various minor bug fixes and UI improvements
- Improvements:
    - Apache ActiveMQ : Show system Queues/Topics based on preference choice
- Other:
    - Upgraded ActiveMQ jars to v5.13.3

2016-04-29 v3.1.0
------------------
- Bugs fixed:
    - Apache ActiveMQ : It is not possible to connect to a jmx secured instance
    - Contextual menus "Empty Queue" and "Clear Messages" are missing in the Message Browser view
    - Queue Browsing auto refresh: It is possible to start multiple background refreshing jobs simultaneously for a single Q  
    
2016-04-12 v3.0.0
------------------
- New features:
    - Topic "browsing": Create a topic subscription and show messages published to that topic. 
- Bugs fixed:
    - Preferences were not saved in many situations
- Improvements:
    - Displaying TextMessages with "large" (>1K) payload is very slow
    - Apache ActiveMQ: Display Topic information
    - HornetQ: Display Topic information
    - Liberty: Display Topic information
    - OpenMQ: Display Topic information
    - Various minor bug fixes and UI improvements
    

2016-04-06 v2.2.1
------------------
- New features:
    - Provides executables for Mac OS X
- Bugs fixed:
    - ! JMSType was not attached to message published
    - Various minor bug fixes and UI improvements

2016-05-04 v2.2.0
------------------
- New features:
    - The message search feature has been reworked, visually and internally
    - The message Edit/Add dialog has been redesigned to group properties by they role (Message Producer, Message, read only properties)  
    - Added handling of the JMS 2.0 "Delay Delivery" feature 
- Bugs fixed:
    - #4 "auto-refresh doesn't respect selector" 
    - Script: Do not apply session filter name in the session chooser dialog when chhosing the session in a step
    - JMSExpiration, JMSPriority, JMSDeliveryMode, JMSDeliveryTime are no more set on JMS Messages as the JMS specification disallow it
    - The initial splash screen disappeared in v2.1.0
    - Various minor bug fixes and UI improvements
- Other:
    - Major Refactoring: Clearly separated properties related to JMS Messages from the ones related to JMS Producers

2016-03-26 v2.1.0
------------------
- New features:
    - Added support for Apache ActiveMQ Artemis as a Q provider
    - Display Queue information for HornetQ destinations
- Bugs fixed:
    - Session Edit: Changing the QManager when editing a session was broken
    - Handling of ObjectMessages is broken 
    - WebSphere MQ: Fixed a connection problem when securityExit is specified
    - WebSphere MQ: Fixed a NPE when securityExit is not null and securityExitData is null
    - ActiveMQ: Fixed many bugs in message handling (browse, remove etc.)
    - Various minor bug fixes and UI improvements
- Other:
    - Major refactoring to isolate JMS Connection per client type (GUI, REST, Script Engine)
    - Upgraded ActiveMQ jars to v5.13.2

2016-03-23 v2.0.0
------------------
- New features:
    - JMSToolBox now exposes some operation as REST services:
        ! This feature is considered experimental in this version 
       - List destinations defined in a Session
       - Browse messages from a Queue
       - Post/publish a message to a Queue/Topic
       - Post/publish a message from a Message Template to a Queue/Topic
       - Get/consume messages from a Queue
       - Empty a Queue
     - Object Message: Display the string representation of the Object held in Object Messages in the "Payload (Raw)" tab
- Bugs fixed:
    - Fixed bug related to focus lost when multiple Q in multiple Q Managers are open and "refresh" is used
    - Script: Using UTF-8 characters in script names or data file names causes a crash on JTB startup
    - Preferences: Pressing 'Restore Defaults' does not restore default value for some preferences 
    - Various minor bug fixes and UI improvements
- Other:
    - Upgraded eclipse RCP to v4.5.2

2016-02-03 v1.9.0
-----------------
- New features:
    - Possibility to drag and drop multiple messages at once
    - It is now possible to dynamically change the message refresh delay by letting the cursor on the "Auto refresh" buttom  
    - The maximum number of messages displayed is now easily changeable in the message browser view
    - New confirmation dialog before executing/simulating a script with the possibility to limit the number of messages posted/simulated  
- Bugs fixed:
    - Various minor bug fixes and UI improvements

2016-01-28 v1.8.0
-----------------
- New features:
    - Possibility to move and copy Scripts and Script folders with Drag & Drop 
    - Possibility to move and copy Template folders with Drag & Drop 
- Bugs fixed:
    - Variables: The "Kind" drop down is now read only
    - Various minor bug fixes
- Other:
    - Moved from a "PDE-ant build" to an "eclipse tycho" build 
    - Internally, code related to Drag & Drop has been rewritten
    
2015-12-23 v1.7.0
-----------------
- New features: 
    - It is now possible to filter destinations displayed, based on name patterns
    - "Text Search" now also search into "values" of MapMessage properties
    - Display the "JMSType" JMS property into the message table viewer
- Bugs fixed:
    - Inline edit of MapMessages properties values was broken
    - Various minor bug fixes

2015-12-09 v1.6.0
-----------------
- New features: 
    - Added "Select All" (CTRL-A) and "Copy" (CTRL-C) on MapMessages content
    - Added "Select All" (CTRL-A) and "Copy" (CTRL-C) on Message JMS Properties and Properties
    - Added a contextual menu to "open" a datafile from the script center
    - Added a preference parameter to automatically clear scripts logs berfore simulation/execution
- Bugs fixed:
    - Various minor bug fixes

2015-10-08 v1.5.0
-----------------
- New features: 
    - Scripts: A step can reference a directory of template, each one will be posted at runtime
    - Added the name of the template that generated the messaged posted during a script execution  
    - Added some key shortcuts:
       - CTRL+A to select all the messages displayed for a Q
       - F5 to refresh the Template tree browser
- Updgraded eclipse RCP to v4.5.1
- Bugs fixed:
    - Various minor bug fixes
  
2015-10-01 v1.4.0
-----------------
- New Feature: Script: Execute a step based on data specified in CSV files  
- Added the "Template Browser" part in the Scripts Center
- Various minor UI fixes and adjustments 

2015-09-16 v1.3.0
-----------------
- Preferences : Auto refresh delay can now be set in 1 second increment
- Added a option in preference to bypass all server certificates validation
- Various minor UI fixes and adjustments 

2015-08-18 v1.2.0
-----------------
- Renamed Q Manager "IBM MQ Series" to "IBM WebSphere MQ"
- New Feature: Display Map Messages payload in its own tab  
- Bug Fix: Dropping a Message on the Template browser was no more allowed 
- Bug Fix: While editing a Step, it was impossible to select the destination by pressing "OK"
- Bug Fix: Adding a new Step in a script was erasing the currently selected Step
- Various minor UI fixes and adjustments 

2015-08-17 v1.1.0
-----------------
- New Feature: Script Center
- New kind of Date Variable: Offset. Allows to generate a field with current date with a time offset 
- Various minor UI fixes and adjustments 

2015-07-01 v1.0.0
-----------------
- first non beta version
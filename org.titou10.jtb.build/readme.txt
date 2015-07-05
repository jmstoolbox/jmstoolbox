JMSToolBox v@jtb.version@ (built @date.build@)

Thank you for downloading JMSToolBox!

Feedback is welcome, please help us offer a better product..


Q Managers currently supported:
===============================
- IBM MQ Series (Tested with 7.x, 8.0, may work with other versions also)
- IBM WebSphere SIB (Tested with v8.5, may work with other versions also)
- IBM WebSphere, ie acces the Destination via a JMS Connection Factory (Tested with v8.5, may work with other versions also)
- IBM WebSphere Liberty Profile (v9.0 beta and v8.5, may work with other versions also)
- Apache Active MQ v5.11
- Oracle OpenMQ v5.1
- JBoss HornetQ v2.4.6


Q Managers that have not been tested in all configuration:
==========================================================
Some Q Managers have had less testing that other , I would like some feedback of the usage of JMSToolBox with the following Q Providers: 
- Apache Active MQ v5.11
- Oracle OpenMQ v5.1
- JBoss HornetQ v2.4.6
Thanks!


Quick start:
============

Installing JMSToolBox
---------------------
Unzip the dowloaded package somewhere
If you are not using the windows (32 or 64 bits versions), a JRE (1.7+) must be present on the running path 

Configuring a Queue Provider:
-----------------------------
In order to access a specific Queue Provider, it is necessary to have the jars specific to this Queue manager

Usually, if those jars can be freely distributed, they are included with JMSToolBox
  
For the others, jars are not bundled with JMSToolBox for legal reasons

To configure a Queue provider:
- start JMSToolBox
- open the "Q Managers" tab
- double click on the Queue Provider you want to configure
- add the jars to the list of jars
  The "help" icon will give you an idea of what jars to add and how to configure the Q Manager 
- Save -> JMSToolBox : will restart

Configuring a Session:
----------------------
- Menu File/New Session..., choose the Q Provider and specify the various parameters. Use the help icon for this
- Double click on the session, the list of queues and topics should appear...


Home Page
==========
http://sourceforge.net/projects/jmstoolbox/


Licensing
=========
 GPLv3.0 : https://www.gnu.org/copyleft/gpl.html


Authors
=======
  Architect, main programmer, team leader : Denis Forveille (titou10.titou10 at gmail dot com)
  Contributors:
  - Design assistant : Yannick Beaudoin (yannickb at gmail dot com)


Other Licenses:
===============
JMSToolBox is an eclise application : https://eclipse.org/ (EPL licensing)
JMSToolBox embeds some other OSS, namely slf4j: www.slf4j.org (MIT Licensing)
JMSToolBox embeds "famfamfam silk" icons from Mark James (Birmingham, UK) : http://www.famfamfam.com/

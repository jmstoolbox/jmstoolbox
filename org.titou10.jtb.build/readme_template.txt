JMSToolBox v@jtb.version@ (built @jtb.date.build@)

Thank you for downloading JMSToolBox!

Feedback is welcome, please help us offer a better product..

User manual: https://github.com/jmstoolbox/jmstoolbox/wiki

Q Managers currently supported:
===============================
- IBM MQ                  : Tested with v9.0, v8.0. v7.x, v6.x. May work with other versions also
- IBM WebSphere SIB       : Direct connection to the internal SIB
                            Tested with v8.5, v9.0. May work with other versions also
- IBM WebSphere AS        : Destinations are read from a JMS Connection Factory. 
                            Tested with v8.5, v9.0. May work with other versions also
- IBM WebSphere Liberty   : v18.x. v17.x, v16.x, v8.5. May work with other versions also
- Apache ActiveMQ         : Tested v5.x (5.4, 5.7, 5.13...)
- Apache ActiveMQ Artemis : v2.x, v1.5.x
- Azure Service Bus
- JBoss HornetQ           : v2.4.6
- OpenMQ                  : v5.1
- Oracle WebLogic Server  : v12.2. May work with other versions also
- Sofware AG Universal Messaging : v9.+. May work with other versions also
- Solace PubSub+          : v9.0+. May work with other versions also
- SonicMQ                 : 2015 (v10.0)
- TIBCO ems               : v8.3

Q Managers that have not been tested in all possible configurations:
====================================================================
Some Q Managers have had less testing that others. Feedback on how JMSToolBox behave in various configuration (SSL, HTTP..) for those QM is welcome: 
- Apache ActiveMQ
- Apache ActiveMQ Artemis
- Azure Service Bus
- JBoss HornetQ
- OpenMQ
- SonicMQ 2015 (v10.0)
- TIBCO ems v8.3

Thanks!


Quick start:
============

Installing JMSToolBox
---------------------
Unpack the dowloaded package somewhere. JMSToolBox is bundled with Java JRE 21
The executable is in the JMSToolBox folder 

For versions prior to v6.2.0 only:
   For linux:
   - set a 64 bits JRE (17+) on the execution path
   - in the JMSToolBox folder: make the JMSToolBox executable (ie chmod u+x JMSToolBox)
   - start JMSToolBox: ./JMSToolBox

   For Mac OS X users:
   - have a 64 bits JRE (17+) present on the execution path
   - in the JMSToolBox.app/Contents/MacOS folder, set executable permission on JMSToolBox  
   - start JMSToolBox

Updating JMSToolBox
---------------------
- remove the folder where JMSToolBox have been previously unpacked
- the settings (with the product logs) will be preserved as they are stored in the "<user_home>/.jtb" folder 
- redo the installation procedure

Uninstall JMSToolBox
---------------------
- remove the folder where JMSToolBox have been previously unpacked
- remove the "<user_home>/.jtb" folder

Configuring a Queue Provider:
-----------------------------
In order to access a specific Queue Provider, it is necessary to have the jars specific to this Queue manager

Usually, if those jars can be freely distributed, they are included with JMSToolBox
  
For the others, jars are not bundled with JMSToolBox for legal reasons.

To configure a Queue provider:
- start JMSToolBox
- open the "Q Managers" tab
- double click on the Queue Provider you want to configure
- add the jars to the list of jars
  The "help" icon will give you an idea of what jars to add and how to configure the Q Manager. The WiKi list them also https://github.com/jmstoolbox/jmstoolbox/wiki)
- Save -> JMSToolBox : will restart

Configuring a Session:
----------------------
- Menu File/New Session..., choose the Q Provider and specify the various parameters. Use the help icon for this
- Double click on the session, the list of queues and topics should appear...


Home Page
==========
https://github.com/jmstoolbox/jmstoolbox


Licensing
=========
 GPLv3.0 : https://www.gnu.org/copyleft/gpl.html


Authors
=======
  Architect, main programmer, team leader : Denis Forveille (titou10.titou10 at gmail dot com)
  Contributors:
  - Design assistant: Yannick Beaudoin (yannickb at gmail dot com)
  - Help for the SonicMQ plugin: Raymond Meester (raymondmeester at gmail dot com)
  - Help for the Solace plugin: Monica Zhang (Solace)
  - Columns Sets and many other ideas/improvements: Ralf Lehmann
  - Author of the Azure Service Bus plugin: Anqi Yan.
  

Other Licenses:
===============
JMSToolBox is an eclipse based application : https://eclipse.org/ (EPL licensing)
JMSToolBox embeds "famfamfam silk" icons from Mark James (Birmingham, UK) : http://www.famfamfam.com/
JMSToolBox uses some classes from "Eclipse Hex Viewer": https://sourceforge.net/projects/jexview/
JMSToolBox uses the CDatetIme Widget from Nebula: https://projects.eclipse.org/projects/technology.nebula (EPL licensing)
JMSToolBox embeds some parts of other OSS, namely:
- slf4j    : http://www.slf4j.org (MIT licensing)
- jetty    : http://eclipse.org/jetty (Apache License v2.0 + Eclipse Public License v1.0)
- RESTEasy : http://resteasy.jboss.org  (Apache License v2.0 license)
- jackson  : http://wiki.fasterxml.com/JacksonHome (Apache License v2.0)
- ActiveMQ : http://activemq.apache.org (Apache License v2.0)
- ActiveMQ Artemis : https://activemq.apache.org/artemis  (Apache License v2.0)
- HornetQ  : http://hornetq.jboss.org (Apache License v2.0)
- OpenMQ   : https://mq.java.net (GPL v2.0)
- OpenJDK  : https://adoptopenjdk.net (GPL v2.0 with Classpath Exception)
- AzulZulu : https://www.azul.com/downloads/?package=jdkt 
- GraalVM  : https://www.graalvm.org (GPL v2.0 with Classpath Exception)

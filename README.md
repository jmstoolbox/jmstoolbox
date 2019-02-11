[![GitHub release](https://img.shields.io/github/release/jmstoolbox/jmstoolbox.svg)](https://github.com/jmstoolbox/jmstoolbox/releases/latest)

## JMSToolBox
**JMSToolBox is an "Universal" JMS Client** able to interact with the greatest number of Queue Managers/Queue providers on the market in a consistent manner.   

JMSToolBox is a **JMS client** based on the **Eclipse RCP** platform, and interactions with Q Managers are implementend via "Eclipse plugins"    

JMSToolBox exposes some of its features as **REST services** so it can be easily used in a continuous integration pipe, or for unit testing or test automation.    

### Features
- Browse, Post, Remove, Move messages to/from Queues and Topics
- "Message templates": Save message as templates, create new templates from scratch
- "Dynamic variables" of various kind (String, integers, dates, list of values etc.) to be used in templates
- "Scripts": define a sequence of message template post to a destination, can be used for load tests
- Some features are exposed as REST services (get destination, browse/get/post/etc messages,...)
- Fast and easy configuration compared to other similar tools
- Share setup/configuration with co-workers via import/export fonctions (config, variables, templates,...)
- Display messages published to a JMS Topic
- Perfect for testing JMS based applications (Create templates with random values, replay them...)
- Internally architected as "eclipse plugins". 1 Q Manager = 1 Plugin

Feedback is welcome, please help us offer a better product..

### Q Managers currently supported:
- IBM MQ                  : Tested with v9.0, v8.0. v7.x, v6.x. May work with other versions also
- IBM WebSphere SIB       : Direct connection to the internal SIB
                            Tested with v8.5, v9.0. May work with other versions also
- IBM WebSphere AS        : Destinations are read from a JMS Connection Factory. 
                            Tested with v8.5, v9.0. May work with other versions also
- IBM WebSphere Liberty   : v16.x+, v8.5. May work with other versions also
- Apache ActiveMQ         : Tested v5.x (5.4, 5.7, 5.13+)
- Apache ActiveMQ Artemis : v2.x, v1.5.x
- JBoss HornetQ           : v2.4.6
- OpenMQ                  : v5.1
- Oracle WebLogic Server  : v12.2. May work with other versions also
- Sofware AG Universal Messaging : v9.+. May work with other versions also
- SonicMQ                 : 2015 (v10.0)
- TIBCO ems               : v8.3


##### Q Managers that have not been tested in all possible configurations:
Some Q Managers have had less testing than others. Feedback on how JMSToolBox behaves in various configuration (SSL, HTTP..) for those QM is welcome: 
- Apache ActiveMQ
- Apache ActiveMQ Artemis
- JBoss HornetQ
- OpenMQ
- SonicMQ 2015 (v10.0)
- TIBCO ems v8.3    
Thanks!

### Full User manual
https://github.com/jmstoolbox/jmstoolbox/wiki

<a href="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_general.png"><img src="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_general.png" width="400"/></a>
<a href="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_message.png"><img src="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_message.png" width="400"/></a>
<a href="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_q_depth.png"><img src="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_q_depth.png" width="400"/></a>
<a href="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_scripts.png"><img src="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_scripts.png" width="400"/></a>

### Quick start:

#### Installing JMSToolBox
Unpack the dowloaded package somewhere. From v5.0.0 on Java 11 64Bit is required. If you are stuck with older Java or 32 bits for some reasons please use an older release. 

For Windows users:
- JMSToolBox is packaged with Java JRE 11 (64 bits)

For linux:
- have a 64 bits JRE (11+) version, depending of the JMSToolBox version you are using, present on the execution path
- in the JMSToolBox folder: make the JMSToolBox executable (eg `chmod u+x JMSToolBox`)
- start JMSToolBox: `./JMSToolBox`

For Mac OS X users:
- have a 64 bits JRE (11+) present on the execution path
- in the JMSToolBox.app/Contents/MacOS folder, set executable permission on JMSToolBox  
- start JMSToolBox

#### Updating JMSToolBox
- remove the folder where JMSToolBox have been previously unpacked
- the settings (with the product logs) will be preserved as they are stored in the `"<user_home>/.jtb"` folder 
- redo the installation procedure

#### Uninstalling JMSToolBox
- remove the folder where JMSToolBox have been previously unpacked
- remove the "`<user_home>/.jtb`" folder

#### Configuring a Queue Provider:
In order to access a specific Queue Provider, it is necessary to have the jars specific to this Queue manager

Usually, if those jars can be freely distributed, they are included with JMSToolBox
  
For the others, jars are not bundled with JMSToolBox for legal reasons.

To configure a Queue provider:
- start JMSToolBox
- open the "Q Managers" tab
- double click on the Queue Provider you want to configure
- add the jars to the list of jars
  The "help" icon will give you an idea of what jars to add and how to configure the Q Manager. The WiKi list them also (https://github.com/jmstoolbox/jmstoolbox/wiki)
- Save -> JMSToolBox : will restart

#### Configuring a Session:
- Menu File/New Session..., choose the Q Provider and specify the various parameters. Use the help icon for this
- Double click on the session, the list of queues and topics should appear...

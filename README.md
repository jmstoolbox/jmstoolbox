[![GitHub release](https://img.shields.io/github/release/jmstoolbox/jmstoolbox.svg)](https://github.com/jmstoolbox/jmstoolbox/releases/latest)
[![Build JMSToolBox](https://github.com/jmstoolbox/jmstoolbox/actions/workflows/build.yml/badge.svg)](https://github.com/jmstoolbox/jmstoolbox/actions/workflows/build.yml)

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/I2I7BYYKC)

## JMSToolBox
**JMSToolBox is an "Universal" JMS Client** able to interact with the greatest number of Queue Managers/Queue providers on the market in a consistent manner.   

JMSToolBox is a **JMS client** based on the **Eclipse RCP** platform, and interactions with Q Managers are implementend via "Eclipse plugins"    

JMSToolBox exposes some of its features as **REST services** so it can be easily used in a continuous integration pipe, or for unit testing or test automation.    

### Features
- Browse, Post, Remove, Move messages to/from Queues and Topics
- "Message templates": Save message as templates, create new templates from scratch
- "Dynamic variables" of various kind (String, integers, dates, list of values etc.) to be used in templates
- "Scripts": define a sequence of message template post to a destination, can be used for load tests
- Some features are exposed as REST services (get destination, browse/get/post messages etc.)
- Fast and easy configuration compared to other similar tools
- Share setup/configuration with co-workers via import/export fonctions (config, variables, templates,...)
- Display messages published to a JMS Topic
- Perfect for testing JMS based applications (Create templates with random values, replay them...)
- Internally architected as "eclipse plugins". 1 Q Manager = 1 Plugin

Feedback is welcome, please help us offer a better product..

### Q Managers currently supported:
- IBM MQ                  : Tested with v9.x, v8.x. v7.x, v6.x. May work with other versions also
- IBM WebSphere SIB       : Direct connection to the internal SIB
                            Tested with v8.5, v9.0. May work with other versions also
- IBM WebSphere AS        : Destinations are read from a JMS Connection Factory. 
                            Tested with v8.5, v9.0. May work with other versions also
- IBM WebSphere Liberty   : v16.x+, v8.5. May work with other versions also
- Apache ActiveMQ         : Tested v5.x (5.4, 5.7, 5.13+)
- Apache ActiveMQ Artemis : v2.x, v1.5.x
- Azure Service Bus
- JBoss HornetQ           : v2.4.6
- Open Liberty            : v18.x+
- OpenMQ                  : v5.1+
- Oracle AQ               : v21c+ May work with other versions also
- Oracle WebLogic Server  : v12.2. May work with other versions also
- Solace PubSub+          : v9.0+. May work with other versions also
- Sofware AG Universal Messaging : v9.+. May work with other versions also
- SonicMQ                 : 2015 (v10.0)
- TIBCO ems               : v8.3


### Full User manual
https://github.com/jmstoolbox/jmstoolbox/wiki

<a href="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_general.png"><img src="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_general.png" width="400"/></a>
<a href="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_message.png"><img src="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_message.png" width="400"/></a>
<a href="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_q_depth.png"><img src="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_q_depth.png" width="400"/></a>
<a href="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_scripts.png"><img src="https://github.com/jmstoolbox/jmstoolbox/blob/dev/.github/printscreen/jmstoolbox_scripts.png" width="400"/></a>

### Quick start:

#### Installing JMSToolBox

Unpack the dowloaded package somewhere.    
The executable is in the JMSToolBox folder

For Mac OS X users:, if JMSToolBox fails to start with a message similar to  "JMSToolBox is damaged and can't be opened...":
-  open a terminal and remove the restriction:`xattr -d -r com.apple.quarantine /<JMSToolBox unpack dir>/`
-  Ref: https://osxdaily.com/2010/09/12/disable-application-downloaded-from-the-internet-message-in-mac-os-x/

JMSToolBox latest versions are bundled with Java JRE 21.
From v7, JMSToolBox requires Java 21. JMSToolBox v6.x requires Java 17 and JMSToolBox v5.x needed Java 11 64Bit and for linux, a machine with GTk3+ is required    
If you are stuck with an older version of Java, or with a 32 bits OS, or with a linux machine with GTK2 only, please use an older release (v5.x or v4.x) 

##### ! For versions prior to v6.2.0 only !

- For linux:
   - set a 64 bits JRE (11+) on the execution path
   - in the JMSToolBox folder: make the JMSToolBox executable (ie chmod u+x JMSToolBox)
   - start JMSToolBox: ./JMSToolBox

- For Mac OS X users:
   - have a 64 bits JRE (11+) present on the execution path
   - in the JMSToolBox.app/Contents/MacOS folder, set executable permission on JMSToolBox  
   - if JMSToolBox fails to start with a message like "JMSToolBox is damaged and can't be opened...":
     -  remove the restriction:`xattr -d -r com.apple.quarantine /Path/to/application/`
     -  Ref: https://osxdaily.com/2010/09/12/disable-application-downloaded-from-the-internet-message-in-mac-os-x/
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

### Building and testing JMSToolBox

#### Building
Requires Java 25 or newer. Maven doesn't need to be installed: the Maven Wrapper downloads the right version (Maven 3.9.x)
```
./mvnw -f org.titou10.jtb.build/pom.xml clean verify
```
The packages for all platforms end up in `org.titou10.jtb.product/target/products`    
Maven 3.10 can't build JMSToolBox yet (Tycho fails with "Invalid Collect Request"), so please use `./mvnw` instead of `mvn`

#### Smoke test
The folder `org.titou10.jtb.build/smoketest` contains a manual smoke test against real brokers running in Docker.
It requires Docker with about 5 GB of memory and works on macOS and Linux
```
org.titou10.jtb.build/smoketest/smoketest.sh
```
The script
- builds JMSToolBox
- starts ActiveMQ Classic, Artemis 1.x and 2.x, Solace and Oracle (for Oracle AQ) in Docker and creates a test queue in each of them
- starts JMSToolBox with a separate workspace (your `<user_home>/.jtb` folder is not touched) that already contains a session for each broker
- stops and removes the brokers when JMSToolBox is closed

In JMSToolBox, open each session, send a message to the queue `JTB.TEST` (`JTB_TEST` for Oracle AQ) and check that it shows up when browsing the queue.
Also run a script (Scripts tab) to check the JavaScript engine.
In case of problems, the log is in `org.titou10.jtb.build/smoketest/target/workspace/JMSToolBox/jmstoolbox.log`

Options:
- `--skip-build`: use the existing build
- `--reset`: start with a fresh workspace. Needed after changing `smoketest/config.xml`, as the workspace is kept between runs
- `--keep`: leave the brokers running. Stop them later with `docker compose -f org.titou10.jtb.build/smoketest/docker-compose.yml down -v`

The steps can also be run on their own: `setup.sh` starts the brokers, `run.sh` starts JMSToolBox

#### Automated smoke test
The same brokers and sessions, tested automatically through the REST services of JMSToolBox. It runs on every push and
pull request to `dev` and `maven-managed-libs` in GitHub Actions (`.github/workflows/smoketest.yml`), and locally with
```
org.titou10.jtb.build/smoketest/headless.sh
```
(options `--skip-build` and `--keep` as above). On macOS, the JMSToolBox window shows up while the test runs; on Linux
without a display, it runs in a virtual one (`xvfb-run`). The script
- tests every session of `smoketest/config.xml` (`headless_test.py`): list the destinations, send a message to the test
  queue, browse it, empty the queue. Each request has a timeout of 60 s, so a hanging Q Manager fails the test
- checks that ActiveMQ Classic without JMX lists all of 600 queues and 600 topics on the first listing after connecting
  (sessions "Discovery" and, through Toxiproxy with 50 ms network latency, "Discovery slow")
- checks that JMSToolBox found its JavaScript engine (GraalJS, used by the visualizers) at startup, and runs a
  visualizer-like script with the GraalJS jars and the Java runtime shipped with JMSToolBox (`GraalCheck.java`)
- checks the log for unexpected errors

It ends with exit code 0 only if all checks passed. In GitHub Actions, the log of JMSToolBox is available as artifact
`smoketest-logs`

#### UI tests
`org.titou10.jtb.uitest` contains UI tests written with [SWTBot](https://eclipse.dev/swtbot/): Tycho starts
JMSToolBox and SWTBot clicks through it like a user. They connect a session, browse a queue and open a message with a
JavaScript visualizer (`org.titou10.jtb.uitest/workspace/visualizers.xml`). They are only built with the Maven profile
`uitest` and run with
```
org.titou10.jtb.build/smoketest/uitest.sh
```
(option `--keep` as above, `--slow` to watch them). Don't use mouse and keyboard while they run: the JMSToolBox window
shows up and SWTBot needs the focus. On Linux without a display, they run in a virtual one (`xvfb-run`).
The report `org.titou10.jtb.uitest/target/uitest-report/index.html` shows every step of each test with pictures of
the JMSToolBox windows. In GitHub Actions, the tests run in their own job; the report is available as artifact
`uitest-report`, a summary is shown on the page of the run

| Broker           | Session in JMSToolBox | Host ports                                   | User / password |
|------------------|-----------------------|----------------------------------------------|-----------------|
| ActiveMQ Classic | ActiveMQ Classic      | 61617 (OpenWire), 1099 (JMX), 8162 (console) | admin / admin   |
| Artemis 2.x      | Artemis 2.x           | 61616, 8161 (console)                        | admin / admin   |
| Artemis 1.5      | Artemis 1.x           | 61618                                        | admin / admin   |
| Solace           | Solace                | 55556 (SMF), 8088 (SEMP)                     | admin / admin (management) |
| Oracle Free      | Oracle AQ             | 1521 (service FREEPDB1)                      | jtb / jtb       |

The other Q Managers are not part of the smoke test (yet). Most of them need proprietary jars or brokers that can't easily be run in Docker

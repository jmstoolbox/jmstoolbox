## How to setup the developement environnement for JMSToolBox

1. Install a jdk v8 64 bits to run Eclipse
2. Download Eclipse Oxygen RCP v4.7.2+ 64 bits and unzip it somewhere
3. Start Eclipse on a new workspace
4. Install `"E4 Tools"` and `"WindowBuilder"` (http://www.eclipse.org/windowbuilder/download.php)    
   Update Site : Oxygen : http://download.eclipse.org/releases/Oxygen
    - General Purpose Tools. Check:
      - `Eclipse e4 Tools Developper Resources`
      - `SWT Designer` (will pull WindowsBuilder plugins as dependencies)


5. Install the `"Nebula/CDateTime"` Widget    
   Update Site : http://download.eclipse.org/nebula/releases/latest
    - `Nebula Release Individual Widgets`: Check:	
      - `Nebula CDateTime Widget`


6. (optionnal) Install extra spy tools     
   Update Site: http://download.eclipse.org/e4/snapshots/org.eclipse.e4.tools/latest/	
   - select `"Eclipse 4 - All spies"`


7. Clone the GitHub repository :    
    - `"File > Import..."`, `"Git > Projects from Git"`, `"Clone URI"`
      URI: `https://github.com/jmstoolbox/jmstoolbox.git`
    - `"Branch selection"`: Select all or the one you want to hack
    - `"Local Destination"`: Whatever fits
    - Check `"Import existing Eclipse projects"`    
      Select all projects except `org.titou10.jtb.hook` and `org.titou10.jtb.qm.rabbitmq`


8. !!! Import the JMSToolBox "java code formatter" file into eclipse preferences:    
    - `Window > Preferences > Java > Code Style > Formatter`, `Import...`: 
      `org.titou10.jtb.core/doc/eclipse_java_formatter.xml`
    - `Window > Preferences > Java > Editor > Save Actions`:
      Check `"Format source code" and "Organize imports"`


9. Run/test. Either:    
  - open `org.titou10.jtb.product/org.titou10.jtb.product`, tab `"overview"`, `"Launch an eclipse application"`
  - create a runtime configuration and run it

## For buildind the artefacts
- Install a jre v8 32bits and 64 bits in some place

- Change the following properties in `org.titou10.jtb.build/pom.xml` to point to the correct JDK locations:
   - `jtb.jre8.32`
   - `jtb.jre8.64`  
- right click on `pom.xml`, `"Run As/Maven build..."`
  - choose `"clean verify"` as goal
- distibutables will be in `org.titou10.jtb.build/dist`

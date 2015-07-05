setlocal

set JAVA_HOME="C:\Program Files (x86)\Java\jdk1.8.0_45"
set PATH=%JAVA_HOME%\bin;%PATH%

rem RC=D:\dev\ws\jmstoolbox\org.titou10.jtb.contributions\liberty\restConnector.jar
rem set KEY=D:/dev/wlp/usr/servers/defaultServer/resources/security/key.jks
set RC=D:\dev\java\jmstoolbox\org.titou10.jtb.contributions\liberty\restConnector.jar
set KEY=D:/dev_tools/wlp/usr/servers/testServer/resources/security/key.jks

rem service:jmx:rest://localhost:9443/IBMJMXConnectorREST

jconsole -J-Djavax.net.ssl.trustStore=%KEY% -J-Djavax.net.ssl.trustStorePassword=Liberty -J-Dcom.ibm.ws.jmx.connector.client.disableURLHostnameVerification=true -J-Djava.class.path=%JAVA_HOME%\lib\jconsole.jar;%JAVA_HOME%\lib\tools.jar;%RC% 

endlocal
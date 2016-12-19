setlocal

rem
rem ne fonctionne que si le CSIv2 transport est a "ssl supported"
rem global securitty/ RMI IIOP security
rem CSIv2 inbound communications + CSIv2 outbound communications 
rem transport = ssl supported
rem
 
set LIBEXT=D:/aae/libext/websphere_v90
set JAVA_HOME=K:/jdk8_64

set CLASSPATH=
set CLASSPATH=%JAVA_HOME%/lib/jconsole.jar
set CLASSPATH=%CLASSPATH%;%JAVA_HOME%/lib/tools.jar
set CLASSPATH=%CLASSPATH%;%LIBEXT%/com.ibm.ws.admin.client_9.0.jar
set CLASSPATH=%CLASSPATH%;%LIBEXT%/tools/com.ibm.ws.ejb.thinclient_9.0.jar
set CLASSPATH=%CLASSPATH%;%LIBEXT%/tools/com.ibm.ws.orb_9.0.jar

set OPTS=
rem set OPTS=-J-Dcom.ibm.CORBA.authenticationTarget=BasicAuth
rem set OPTS=%OPTS% -J-Dcom.ibm.CORBA.authenticationRetryEnabled=true
rem set OPTS=%OPTS% -J-Dcom.ibm.CORBA.authenticationRetryCount=3
rem set OPTS=%OPTS% -J-Dcom.ibm.CORBA.validateBasicAuth=true
rem set OPTS=%OPTS% -J-Dcom.ibm.CORBA.securityServerHost=sldadm0192.saq.qc.ca
rem set OPTS=%OPTS% -J-Dcom.ibm.CORBA.securityServerPort=20164
rem set OPTS=%OPTS% -J-Dcom.ibm.CORBA.loginTimeout=300
rem set OPTS=%OPTS% -J-Dcom.ibm.CORBA.loginSource=properties
rem set OPTS=%OPTS% -J-Dcom.ibm.CORBA.loginUserid=???
rem set OPTS=%OPTS% -J-Dcom.ibm.CORBA.loginPassword=???

rem set OPTS=%OPTS% -J-Djava.naming.provider.url=corbaname:iiop:sldadm0192.saq.qc.ca:20164 

"%JAVA_HOME%/bin/jconsole" -debug -J-Djava.class.path="%CLASSPATH%" %OPTS%

endlocal
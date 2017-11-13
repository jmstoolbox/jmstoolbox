
# http://nsis.sourceforge.net/Docs/Modern%20UI%202/Readme.html
# http://nsis.sourceforge.net/NSIS_2
# https://github.com/vogella/com.voclipse.packaging

SetCompressor lzma

#
# GENERAL SYMBOL DEFINITIONS
#
Name jmstoolbox
!define REGKEY "SOFTWARE\$(^Name)"
!define VERSION "4.6.0"
!define URL "https://sourceforge.net/projects/jmstoolbox"

#
# SOURCE CODE...
#
!define BASE_DIR   "..\..\.."
!define README_DIR "${BASE_DIR}\org.titou10.jtb.build"
!define SOURCE_DIR "${BASE_DIR}\org.titou10.jtb.product\target\products\org.titou10.jtb.product\win32\win32\x86_64\JMSToolBox"

!define JRE        "C:\Program Files\Java\jre1.8.0_152" 

#
# MUI SYMBOL DEFINITIONS
#
!define MUI_ICON "jms-icon.ico"
!define MUI_UNICON "jms-icon.ico"
!define MUI_WELCOMEPAGE_TITLE title

#
# MUI SETTINGS / HEADER
#
!define MUI_TEXT_WELCOME_INFO_TEXT Bienvenue

#
# MODERN INTERFACE
#
!include MultiUser.nsh
!include Sections.nsh
!include MUI2.nsh

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE copyright.txt
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
Var StartMenuGroup
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuGroup
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

#
# INSTALLER LANGUAGES
#
!insertmacro MUI_LANGUAGE English

#
# INSTALLER VALUES
#
Outfile "jmstoolbox.exe"
# OutFile voclipse-${VERSION}-${ARCHITECTURE}-setup.exe
InstallDir $INSTDIR
CRCCheck on
XPStyle on
ShowInstDetails show

VIProductVersion 4.6.0.0
VIAddVersionKey /LANG=${LANG_ENGLISH} ProductName $(^Name)
VIAddVersionKey /LANG=${LANG_ENGLISH} ProductVersion "${VERSION}"
VIAddVersionKey /LANG=${LANG_ENGLISH} CompanyWebsite "${URL}"
VIAddVersionKey /LANG=${LANG_ENGLISH} FileVersion "${VERSION}"
VIAddVersionKey /LANG=${LANG_ENGLISH} FileDescription "JMSToolBox 'Universal' JMS Client"
VIAddVersionKey /LANG=${LANG_ENGLISH} LegalCopyright "Copyright (C) 2015 Denis Forveille titou10.titou10@gmail.com"
#VIAddVersionKey /LANG=${LANG_ENGLISH} CompanyName "${COMPANY}"
InstallDirRegKey HKLM "${REGKEY}" Path
ShowUninstDetails show

!define SOURCE_DIR="D:\dev\java\jmstoolbox\org.titou10.jtb.product\target\products\org.titou10.jtb.product\win32\win32\x86_64\JMSToolBox"

#
# Core
#
Section "!JMSToolBox core (Required)" SEC01
   SectionIn RO
  
   SetOutPath $INSTDIR\configurations
   File /r "${SOURCE_DIR}\configuration\*"

   SetOutPath $INSTDIR
   File "${SOURCE_DIR}\*.xml"
   File "${SOURCE_DIR}\*.exe"
   File "${SOURCE_DIR}\*.ini"

# DF: faux: il faut changer les dates et versions a l'interieur...
   File "${README_DIR}\readme.txt"
   File "${README_DIR}\changelog.txt"
   
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.core*"
   
SectionEnd

#
# JRE
#
Section "Java Runtime Environment (JRE)" SEC02
   SetOutPath $INSTDIR\jre
   File /r "${JRE}\*" 
SectionEnd

#
# REST
#
Section "REST Server" SEC03
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.rest*"
SectionEnd

#
# Plugins QM
#
SectionGroup /e "Queue Managers"

Section "Apache ActiveMQ (TomEE, Geronimo)" 
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.qm.activemq*"
SectionEnd

Section "Apache ActiveMQ Artemis v1.x (JBoss EAP v7.x+, Wildfly v10.x+) " 
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.qm.artemis*"
SectionEnd

Section "Apache ActiveMQ Artemis v2.x" 
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.qm.artemis2*"
SectionEnd

Section "HornetQ ((JBoss AS v6.x, Wildfly v8.x + v9.x)" 
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.qm.hornetq*"
SectionEnd

Section "IBM MQ" 
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.qm.ibmmq*"
SectionEnd

Section "IBM WebSphere AS (JMS in JNDI)" 
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.qm.websphere*"
SectionEnd

Section "IBM WebSphere AS (SIB)" 
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.qm.wassib*"
SectionEnd

Section "IBM WebSphere Liberty Profile" 
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.qm.liberty*"
SectionEnd

Section "Open MQ (GlassFish)" 
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.qm.openmq*"
SectionEnd

Section "Oracle WebLogic Server" 
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.qm.weblogic*"
SectionEnd

Section "Sonic MQ" 
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.qm.sonicmq*"
SectionEnd

Section "TIBCO ems" 
   SetOutPath $INSTDIR\plugins
   File "${SOURCE_DIR}\plugins\org.titou10.jtb.qm.tibco*"
SectionEnd

SectionGroupEnd

Section "Uninstall"
  Delete $INSTDIR\Uninst.exe ; delete self (see explanation below why this works)
  Delete $INSTDIR\myApp.exe
  RMDir $INSTDIR
  DeleteRegKey HKLM SOFTWARE\myApp
SectionEnd



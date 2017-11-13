
SetCompressor lzma
Outfile "jmstoolbox.exe"

!define MUI_ICON "jms-icon.ico"
!define MUI_UNICON "jms-icon.ico"
!define MUI_HEADERIMAGE jms-icon-256x256-32bits.bmp
!define MUI_WELCOMEFINISHPAGE_BITMAP jms-icon-256x256-32bits.bmp
!define MUI_WELCOMEPAGE_TITLE title
!define MUI_TEXT_WELCOME_INFO_TEXT Bienvenue
;!define MUI_COMPONENTSPAGE_TEXT_COMPLIST changelog.txt

!include "MUI2.nsh"

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE copyright.txt
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
;!insertmacro MUI_PAGE_STARTMENU pageid variable
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_COMPONENTS
!insertmacro MUI_UNPAGE_DIRECTORY
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

Section "!Core JMSToolBox" SEC01
  SectionIn RO
SectionEnd

Section "REST Server" SEC02
SectionEnd


SectionGroup /e "Queue Managers"

Section "IBM MQ" 
SectionEnd

Section "WebSphere SIB" 
SectionEnd

Section "Apache Active MQ" 
SectionEnd

SectionGroupEnd

Section "Uninstall"
  Delete $INSTDIR\Uninst.exe ; delete self (see explanation below why this works)
  Delete $INSTDIR\myApp.exe
  RMDir $INSTDIR
  DeleteRegKey HKLM SOFTWARE\myApp
SectionEnd



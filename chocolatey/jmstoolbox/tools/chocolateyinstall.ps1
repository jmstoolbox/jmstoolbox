$ErrorActionPreference = 'Stop';

$packageName= 'JMS Toolbox'
$toolsDir   = "$(Split-Path -parent $MyInvocation.MyCommand.Definition)"
$fileLocation = Join-Path $toolsDir 'jmstoolbox.zip'

$packageArgs = @{
  packageName   = $packageName
  unzipLocation = $toolsDir
  file         = $fileLocation
  destination   = $toolsDir

  checksum      = 'F32FCAA6B09B9FB8846ECC46965EB1C8BD3A0D8E60A607E335C224B69FDC67E4'
  checksumType  = 'sha256'
}

Get-ChocolateyUnzip @packageArgs
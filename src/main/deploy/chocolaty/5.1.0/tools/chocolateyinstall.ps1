$ErrorActionPreference = 'Stop'; # stop on all errors
$toolsDir   = "$(Split-Path -parent $MyInvocation.MyCommand.Definition)"
$url        = 'https://github.com/yermak/AudioBookConverter/releases/download/version_5.1.0/AudioBookConverter-5.1.0.msi' # download url, HTTPS preferred

$packageArgs = @{
  packageName   = $env:ChocolateyPackageName
  unzipLocation = $toolsDir
  fileType      = 'msi' #only one of these: exe, msi, msu
  url           = $url
  softwareName  = 'AudioBookConverter*' #part or all of the Display Name as you see it in Programs and Features. It should be enough to be unique
  checksum      = '56109C70A78E3290BE76E76B0CC1BB65E06DAF82E57F1A12484E0DA86131155B'
  checksumType  = 'sha256' #default is md5, can also be sha1, sha256 or sha512
  silentArgs    = "/qn /norestart /l*v `"$($env:TEMP)\$($packageName).$($env:chocolateyPackageVersion).MsiInstall.log`"" # ALLUSERS=1 DISABLEDESKTOPSHORTCUT=1 ADDDESKTOPICON=0 ADDSTARTMENU=0
  validExitCodes= @(0, 3010, 1641)
}

Install-ChocolateyPackage @packageArgs # https://chocolatey.org/docs/helpers-install-chocolatey-package

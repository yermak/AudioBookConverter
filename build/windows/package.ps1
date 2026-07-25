<#
.SYNOPSIS
    Builds the AudioBookConverter distribution.
.DESCRIPTION
    Requires 'env.TEAMCITY_7ZIP_PATH' in buildAgent.properties.
    Extracts local JMODs zip to target/jmods automatically.
    Builds MSIs from the modified App Image to include injected FFmpeg binaries.
.EXAMPLE
    .\package.ps1 -JavaHome "C:\Path\To\Jdk" -AppVersion "1.0.0"
#>

param(
    [Parameter(Mandatory=$true)]
    [string]$JavaHome,

    [Parameter(Mandatory=$true)]
    [string]$AppVersion
)

$ErrorActionPreference = "Stop"

# --- Configuration ---
$TargetRelease = "target\release"
$TargetImageRoot = "target\image"
$TargetJmods    = "target\jmods"
$OutputJre      = "target\fx-jre"

$AppImageName   = "AudioBookConverter"
$AppImageDir    = Join-Path $TargetImageRoot $AppImageName
$JLinkExe       = Join-Path $JavaHome "bin\jlink.exe"
$JPackageExe    = Join-Path $JavaHome "bin\jpackage.exe"

# --- 1. Cleanup and Setup ---
Write-Host "--- Initializing ---" -ForegroundColor Cyan
New-Item -ItemType Directory -Path $TargetRelease -Force | Out-Null

if (Test-Path $AppImageDir) {
    Write-Host "Removing old image: $AppImageDir"
    Remove-Item -Path $AppImageDir -Recurse -Force
}

# --- 2. Prepare JavaFX JMODs ---
Write-Host "--- Preparing JavaFX JMODs ---" -ForegroundColor Cyan

$jmodsZip = Get-ChildItem -Path . -Filter "openjfx-*_windows-x64_bin-jmods.zip" -Recurse | Select-Object -First 1

if (-not $jmodsZip) {
    if (Test-Path "javafx/jmods") {
        $jmodsZip = Get-ChildItem -Path "javafx/jmods" -Filter "openjfx-*_windows-x64_bin-jmods.zip" | Select-Object -First 1
    }
}

if (-not $jmodsZip) {
    throw "Could not find openjfx-*_windows-x64_bin-jmods.zip in current directory or subfolders."
}

Write-Host "Found JMODs archive: $($jmodsZip.FullName)"

if (Test-Path $TargetJmods) { Remove-Item $TargetJmods -Recurse -Force }
New-Item -ItemType Directory -Path $TargetJmods -Force | Out-Null

# --- FIX: Use 7-Zip from Env Var if available ---
if ($env:TEAMCITY_7ZIP_PATH) {
    $7zExe = $env:TEAMCITY_7ZIP_PATH
} else {
    $7zExe = "7z.exe"
}

Write-Host "Extracting JMODs to $TargetJmods using $7zExe..."
& $7zExe x $jmodsZip.FullName "-o$TargetJmods" -y | Out-Null

$actualJmodsDir = Get-ChildItem -Path $TargetJmods -Recurse -Directory |
        Where-Object { (Get-ChildItem -Path $_.FullName -Filter "*.jmod").Count -gt 0 } |
        Select-Object -First 1

if (-not $actualJmodsDir) {
    throw "Could not find any directory containing .jmod files inside the extracted archive."
}

$JavaFxJModsPath = $actualJmodsDir.FullName
Write-Host "JMODs located at: $JavaFxJModsPath"

# --- 3. Build Custom JRE (jlink) ---
Write-Host "--- Building Custom JRE ---" -ForegroundColor Cyan
$JLinkArgs = @(
    "--module-path", "`"$($JavaHome)\jmods`";`"$JavaFxJModsPath`"",
    "--add-modules", "java.base,java.sql,java.management,javafx.controls,javafx.fxml,javafx.media,javafx.base,javafx.swing,javafx.graphics",
    "--strip-native-commands",
    "--strip-debug",
    "--no-man-pages",
    "--no-header-files",
    "--exclude-files=**.md",
    "--output", $OutputJre
)

if (Test-Path $OutputJre) { Remove-Item $OutputJre -Recurse -Force }
& $JLinkExe $JLinkArgs
if ($LASTEXITCODE -ne 0) { throw "jlink failed" }

# --- 4. Create App Image (jpackage) ---
Write-Host "--- Creating Application Image ---" -ForegroundColor Cyan
$InputPath = "target\package\audiobookconverter-$AppVersion-windows-installer\audiobookconverter-$AppVersion\app"
$MainJar = "lib\audiobookconverter-$AppVersion.jar"

$BaseJPackageArgs = @(
    "--app-version", $AppVersion,
    "--icon", "AudioBookConverter.ico",
    "--name", "AudioBookConverter",
    "--vendor", "Recoupler",
    "--main-jar", $MainJar,
    "--runtime-image", $OutputJre
)

$ImageArgs = $BaseJPackageArgs + @(
    "--type", "app-image",
    "--input", $InputPath,
    "--dest", $TargetImageRoot,
    "--java-options", "'-XX:+UseAllWindowsProcessorGroups'"
)

& $JPackageExe $ImageArgs
if ($LASTEXITCODE -ne 0) { throw "jpackage (app-image) failed" }

# --- 5. FFmpeg Integration ---
Write-Host "--- Injecting FFmpeg binaries ---" -ForegroundColor Cyan
$ffmpegArchive = Get-ChildItem -Path . -Filter "external\x64\windows\ffmpeg-*.7z" | Select-Object -First 1

if ($ffmpegArchive) {
    Write-Host "Found FFmpeg archive: $($ffmpegArchive.Name)"
    $ffmpegExtractPath = Join-Path $env:TEMP "ffmpeg_extract_$(Get-Random)"
    New-Item -ItemType Directory -Force -Path $ffmpegExtractPath | Out-Null

    & $7zExe x $ffmpegArchive.FullName "-o$ffmpegExtractPath" -y | Out-Null

    $binDir = Get-ChildItem -Path $ffmpegExtractPath -Recurse -Directory |
            Where-Object { $_.Name -eq "bin" -and (Test-Path (Join-Path $_.FullName "ffmpeg.exe")) } |
            Select-Object -First 1

    if ($binDir) {
        $FFmpegDestDir = Join-Path $AppImageDir "app\external"

        if (-not (Test-Path $FFmpegDestDir)) {
            New-Item -ItemType Directory -Force -Path $FFmpegDestDir | Out-Null
        }

        Write-Host "Copying binaries from $($binDir.FullName) to $FFmpegDestDir"
        Get-ChildItem -Path $binDir.FullName -Include *.exe, *.dll -Recurse | ForEach-Object {
            Copy-Item -Path $_.FullName -Destination $FFmpegDestDir -Force
        }
    } else {
        Write-Warning "FFmpeg bin directory not found in archive."
    }
    Remove-Item -Path $ffmpegExtractPath -Recurse -Force
} else {
    Write-Warning "No FFmpeg archive found. Skipping FFmpeg injection."
}

# --- 6. Compressing Portable Versions (7z) ---
Write-Host "--- Compressing Portable Versions ---" -ForegroundColor Cyan
Push-Location $TargetImageRoot

$SfxName = "..\release\AudioBookConverter-Portable-$AppVersion.exe"
$ZipName = "..\release\AudioBookConverter-Portable-$AppVersion.zip"

if (Test-Path $SfxName) { Remove-Item $SfxName }
if (Test-Path $ZipName) { Remove-Item $ZipName }

# --- Retrieve 7z path from TeamCity Agent Config ---
if (-not $env:TEAMCITY_7ZIP_PATH) {
    Write-Warning "TEAMCITY_7ZIP_PATH not found. Attempting to resolve 7z.exe from PATH."
    $7zCommand = Get-Command "7z.exe" -ErrorAction SilentlyContinue
    if ($7zCommand) {
        $7zExe = $7zCommand.Source
    } else {
        throw "TEAMCITY_7ZIP_PATH is not set and 7z.exe is not in PATH."
    }
} else {
    $7zExe = $env:TEAMCITY_7ZIP_PATH
}

$7zDir = Split-Path -Parent $7zExe
$SfxModule = Join-Path $7zDir "7z.sfx"

if (-not (Test-Path $SfxModule)) {
    throw "7z.sfx module not found at: $SfxModule. Ensure 7-Zip is installed fully."
}

Write-Host "Using 7-Zip: $7zExe"
Write-Host "Using SFX:   $SfxModule"

& $7zExe a -t7z -mx5 -mmt "-sfx$SfxModule" $SfxName $AppImageName
& $7zExe a -tzip -mx5 -mmt $ZipName $AppImageName

Pop-Location

# --- 7. Build MSIs (jpackage) ---
Write-Host "--- Building MSI Installers ---" -ForegroundColor Cyan

$MsiBaseArgs = @(
    "--name", "AudioBookConverter",
    "--vendor", "Recoupler",
    "--app-version", $AppVersion,
    "--license-file", "LICENSE",
    "--type", "msi",
    "--win-shortcut",
    "--win-menu",
    "--win-menu-group", "AudioBookConverter",
    "--app-image", $AppImageDir
)

# A. All Users MSI
Write-Host "Building: All Users MSI"
$MsiAllUsersArgs = $MsiBaseArgs + "--win-dir-chooser"
& $JPackageExe $MsiAllUsersArgs
if ($LASTEXITCODE -ne 0) { throw "jpackage (msi-all-users) failed" }
Move-Item -Path "AudioBookConverter-$AppVersion.msi" -Destination "$TargetRelease\AudioBookConverter-All-Users-$AppVersion.msi" -Force

# B. Single User MSI
Write-Host "Building: Single User MSI"
$MsiSingleUserArgs = $MsiBaseArgs + "--win-per-user-install"
& $JPackageExe $MsiSingleUserArgs
if ($LASTEXITCODE -ne 0) { throw "jpackage (msi-single-user) failed" }
Move-Item -Path "AudioBookConverter-$AppVersion.msi" -Destination "$TargetRelease\AudioBookConverter-Single-User-$AppVersion.msi" -Force

Write-Host "--- Build Complete ---" -ForegroundColor Green
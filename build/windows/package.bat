ECHO ON
SET JAVA_HOME=%~1
ECHO %JAVA_HOME%
SET JAVAFX_JMODS=%~2
ECHO %JAVAFX_JMODS%
SET APP_VERSION=%~3
ECHO %APP_VERSION%
mkdir target\release
rmdir /s /q target\image\AudioBookConverter

%JAVA_HOME%\bin\jlink.exe --module-path %JAVA_HOME%\jmods;%JAVAFX_JMODS% --add-modules java.base,java.sql,javafx.controls,javafx.fxml,javafx.media,javafx.base,javafx.swing,javafx.graphics --strip-native-commands --strip-debug --no-man-pages --no-header-files --exclude-files=**.md --output target\fx-jre

%JAVA_HOME%\bin\jpackage.exe --app-version %APP_VERSION%  --icon AudioBookConverter.ico -t app-image --name AudioBookConverter --vendor Recoupler --input target\package\audiobookconverter-%APP_VERSION%-windows-installer\audiobookconverter-%APP_VERSION%\app --main-jar lib\audiobookconverter-%APP_VERSION%.jar --runtime-image target\fx-jre --dest target\image --java-options '--enable-preview'
cd target\image
7z.exe a -t7z -mx9 -mmt4 -sfx7z.sfx ..\release\AudioBookConverter-Portable-%APP_VERSION%.exe AudioBookConverter
7z.exe a -tzip -mx9 -mmt4 ..\release\AudioBookConverter-Portable-%APP_VERSION%.zip AudioBookConverter
cd ..\..

%JAVA_HOME%\bin\jpackage.exe --app-version %APP_VERSION%  --license-file LICENSE --icon AudioBookConverter.ico -t msi --win-dir-chooser --win-shortcut --win-menu --win-menu-group AudioBookConverter --name AudioBookConverter --vendor Recoupler --input target\package\audiobookconverter-%APP_VERSION%-windows-installer\audiobookconverter-%APP_VERSION%\app --main-jar lib\audiobookconverter-%APP_VERSION%.jar --runtime-image target\fx-jre --java-options '--enable-preview'
move AudioBookConverter-%APP_VERSION%.msi target\release\AudioBookConverter-All-Users-%APP_VERSION%.msi
%JAVA_HOME%\bin\jpackage.exe --app-version %APP_VERSION%  --license-file LICENSE --icon AudioBookConverter.ico --win-per-user-install -t msi --win-shortcut --win-menu --win-menu-group AudioBookConverter --name AudioBookConverter --vendor Recoupler --input target\package\audiobookconverter-%APP_VERSION%-windows-installer\audiobookconverter-%APP_VERSION%\app --main-jar lib\audiobookconverter-%APP_VERSION%.jar --runtime-image target\fx-jre --java-options '--enable-preview'
move AudioBookConverter-%APP_VERSION%.msi target\release\AudioBookConverter-Single-User-%APP_VERSION%.msi

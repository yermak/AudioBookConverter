@ECHO ON
SET JAVA_HOME=%1
SET JAVAFX_MODS=%2
SET APP_VERSION=%3
rmdir /s /q target\fx-jre
%JAVA_HOME%\bin\jlink.exe --module-path %JAVA_HOME%\jmods;%JAVAFX_MODS% --add-modules java.base,java.sql,javafx.controls,javafx.fxml,javafx.media,javafx.base,javafx.swing,javafx.graphics --output target\fx-jre

%JAVA_HOME%\bin\jpackage.exe --app-version %APP_VERSION%  --icon AudioBookConverter.ico -t app-image --name AudioBookConverter --vendor Recoupler --input target/package/audiobookconverter-%APP_VERSION%/audiobookconverter-%APP_VERSION%/app --main-jar lib/audiobookconverter-%APP_VERSION%.jar --runtime-image target/fx-jre --dest target/image --java-options '--enable-preview'
cd target\image\
7z a -t7z -mx9 -mmt8 -sfx7z.sfx AudioBookConverter-Portable-%APP_VERSION%.exe AudioBookConverter
move AudioBookConverter-Portable-%APP_VERSION%.exe ..\
cd ..\..

%JAVA_HOME%\bin\jpackage.exe --app-version %APP_VERSION%  --license-file README.md --icon AudioBookConverter.ico -t msi --win-dir-chooser --win-shortcut --win-menu --win-menu-group AudioBookConverter --name AudioBookConverter --vendor Recoupler --input target/package/audiobookconverter-%APP_VERSION%/audiobookconverter-%APP_VERSION%/app --main-jar lib/audiobookconverter-%APP_VERSION%.jar --runtime-image target/fx-jre --java-options '--enable-preview'
move AudioBookConverter-%APP_VERSION%.msi target/AudioBookConverter-All-Users-%APP_VERSION%.msi

%JAVA_HOME%\bin\jpackage.exe --app-version %APP_VERSION%  --license-file README.md --icon AudioBookConverter.ico --win-per-user-install -t msi --win-shortcut --win-menu --win-menu-group AudioBookConverter --name AudioBookConverter --vendor Recoupler --input target/package/audiobookconverter-%APP_VERSION%/audiobookconverter-%APP_VERSION%/app --main-jar lib/audiobookconverter-%APP_VERSION%.jar --runtime-image target/fx-jre --java-options '--enable-preview'
move AudioBookConverter-%APP_VERSION%.msi target/AudioBookConverter-Single-User-%APP_VERSION%.msi



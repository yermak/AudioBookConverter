@ECHO ON
SET JAVA_HOME=%1
SET JAVAFX_MODS=%2
SET APP_VERSION=%3
rmdir /s /q target\fx-jre
%JAVA_HOME%\bin\jlink.exe --module-path %JAVA_HOME%\jmods;%JAVAFX_MODS% --add-modules java.base,java.sql,javafx.controls,javafx.fxml,javafx.media,javafx.base,javafx.swing,javafx.graphics --output target\fx-jre
%JAVA_HOME%\bin\jpackage.exe --app-version %APP_VERSION%  --license-file README.md --icon AudioBookConverter.ico --win-per-user-install -t msi --win-dir-chooser --win-shortcut --win-menu --win-menu-group AudioBookConverter --name AudioBookConverter --vendor Recoupler --input target/package/audiobookconverter-%APP_VERSION%/audiobookconverter-%APP_VERSION%/app --main-jar lib/audiobookconverter-%APP_VERSION%.jar --runtime-image target/fx-jre
move AudioBookConverter-%APP_VERSION%.msi target/

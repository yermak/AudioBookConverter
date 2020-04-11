@ECHO ON
SET APP_VERSION=%1
rmdir /s /q target\fx-jre
C:\Users\Yermak\Programs\Java\jdk-14\bin\jlink.exe --module-path C:\Users\Yermak\Programs\Java\jdk-14\jmods;C:\Users\Yermak\Programs\Java\javafx-jmods-14 --add-modules java.base,java.sql,javafx.controls,javafx.fxml,javafx.media,javafx.base,javafx.swing,javafx.graphics --output target\fx-jre
C:\Users\Yermak\Programs\Java\jdk-14\bin\jpackage.exe --app-version %APP_VERSION%  --license-file README.md --icon AudioBookConverter.ico --win-per-user-install -t msi --win-dir-chooser --win-shortcut --win-menu --win-menu-group AudioBookConverter --name AudioBookConverter --vendor Recoupler --input target/package/audiobookconverter-%APP_VERSION%/audiobookconverter-%APP_VERSION%/app --main-jar lib/audiobookconverter-%APP_VERSION%.jar --runtime-image target/fx-jre
move AudioBookConverter-%APP_VERSION%.msi target/

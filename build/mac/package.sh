JAVA_HOME=/Users/Yarick_Yermak/Library/Java/JavaVirtualMachines/temurin-17/Contents/Home
#JAVA_HOME=$1
JAVAFX_JMODS=jmods/mac
APP_VERSION=6.1.2

rm -rf target/release
mkdir target/release

rm -rf target/fx-jre
$JAVA_HOME/bin/jlink --module-path $JAVA_HOME/jmods:$JAVAFX_JMODS \
--add-modules java.base,java.sql,java.management,javafx.controls,javafx.fxml,javafx.media,javafx.base,javafx.swing,javafx.graphics \
--strip-native-commands --strip-debug --no-man-pages --no-header-files --exclude-files=**.md \
--output target/fx-jre

rm -rf target/image
$JAVA_HOME/bin/jpackage --app-version $APP_VERSION  \
--icon build/mac/AudioBookConverter.icns \
--type app-image \
--input target/package/audiobookconverter-$APP_VERSION-mac-installer/audiobookconverter-$APP_VERSION/app \
--main-jar lib/audiobookconverter-$APP_VERSION.jar \
--runtime-image target/fx-jre \
--java-options "--add-exports java.desktop/com.apple.eio=ALL-UNNAMED" \
--dest target/release \
--vendor "Recoupler Limited" \
--app-version $APP_VERSION \
--mac-entitlements build/mac/entitlements.plist \
--mac-package-identifier com.recoupler.abc \
--mac-package-name AudioBookConverter \
--mac-signing-key-user-name "Developer ID Application: Recoupler Limited" \
--mac-app-store \
--mac-sign

#/Users/Yarick_Yermak/Library/Keychains/login.keychain-db


#$JAVA_HOME/bin/jpackage --app-version $(APP_VERSION)  --icon AudioBookConverter.icns \
#--type dmg \
#--input target/package/audiobookconverter-$(APP_VERSION)-mac-installer/audiobookconverter-$(APP_VERSION)/app \
#--main-jar lib/audiobookconverter-$(APP_VERSION).jar --runtime-image target/fx-jre --java-options '--enable-preview'
#mkdir target/release



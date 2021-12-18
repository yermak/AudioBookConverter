JAVA_HOME=$1
JAVAFX_JMODS=$2
APP_VERSION=$3

rm -rf target/release
mkdir target/release

rm -rf target/fx-jre
$JAVA_HOME/bin/jlink --module-path $JAVA_HOME/jmods:$JAVAFX_JMODS \
--add-modules java.base,java.sql,javafx.controls,javafx.fxml,javafx.media,javafx.base,javafx.swing,javafx.graphics \
--strip-native-commands --strip-debug --no-man-pages --no-header-files --exclude-files=**.md \
--output target/fx-jre

rm -rf target/image
$JAVA_HOME/bin/jpackage --app-version $APP_VERSION  -t app-image --icon AudioBookConverter.png --name AudioBookConverter --vendor Recoupler \
--input target/package/audiobookconverter-$APP_VERSION-linux-installer/audiobookconverter-$APP_VERSION/app \
--main-jar lib/audiobookconverter-$APP_VERSION.jar --runtime-image target/fx-jre \
--dest target/image --java-options '--enable-preview'
strip target/image/AudioBookConverter/bin/AudioBookConverter

cd target/image
tar -czf ../release/AudioBookConverter-static-binaries-$APP_VERSION.tar.gz AudioBookConverter
cd ../..

#$JAVA_HOME/bin/jpackage --app-version $APP_VERSION  --license-file LICENSE --icon AudioBookConverter.png \
#-t deb --name AudioBookConverter --vendor Recoupler \
#--linux-menu-group AudioBookConverter --linux-shortcut \
#--input target/package/audiobookconverter-$APP_VERSION-linux-nodeps-installer/audiobookconverter-$APP_VERSION/app \
#--main-jar lib/audiobookconverter-$APP_VERSION.jar --runtime-image target/fx-jre --java-options '--enable-preview'

#mv audiobookconverter_$APP_VERSION-1_amd64.deb target/release/
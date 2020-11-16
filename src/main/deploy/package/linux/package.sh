export JAVA_HOME=$1
export JAVAFX_MODS=$2
export APP_VERSION=$3
rm -rf target/fx-jre
$JAVA_HOME/bin/jlink --module-path $JAVA_HOME/jmods:$JAVAFX_MODS \
--add-modules java.base,java.sql,javafx.controls,javafx.fxml,javafx.media,javafx.base,javafx.swing,javafx.graphics --output target/fx-jre

$JAVA_HOME/bin/jpackage --app-version $APP_VERSION  -t app-image --name AudioBookConverter --vendor Recoupler \
--input target/package/audiobookconverter-$APP_VERSION-linux-installer/audiobookconverter-$APP_VERSION/app \
--main-jar lib/audiobookconverter-$APP_VERSION.jar --runtime-image target/fx-jre --dest target/image --java-options '--enable-preview'


$JAVA_HOME/bin/jpackage --app-version $APP_VERSION  --license-file README.md --icon AudioBookConverter.png \
-t deb --name AudioBookConverter --vendor Recoupler \
--linux-menu-group AudioBookConverter --linux-shortcut \
--linux-package-deps ffmpeg \
--linux-package-deps mp4v2-utils \
--input target/package/audiobookconverter-$APP_VERSION-linux-installer/audiobookconverter-$APP_VERSION/app \
--main-jar lib/audiobookconverter-$APP_VERSION.jar --runtime-image target/fx-jre --java-options '--enable-preview'
ls
mv audiobookconverter_$APP_VERSION-1_amd64.deb target/

#$JAVA_HOME/bin/jpackage --app-version $APP_VERSION  --license-file README.md --icon AudioBookConverter.png \
#-t rpm --name AudioBookConverter --vendor Recoupler \
#--linux-menu-group AudioBookConverter --linux-shortcut \
#--linux-package-deps ffmpeg mp4v2-utils \
#--input target/package/audiobookconverter-$APP_VERSION-linux-installer/audiobookconverter-$APP_VERSION/app \
#--main-jar lib/audiobookconverter-$APP_VERSION.jar --runtime-image target/fx-jre --java-options '--enable-preview'
#mv audiobookconverter-$APP_VERSION-1_amd64.rpm target/
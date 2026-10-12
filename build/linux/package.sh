#!/bin/bash
# usage: package.sh JAVA_HOME APP_VERSION  -- run after 'mvn package', builds target/release/AudioBookConverter-static-binaries-APP_VERSION.tar.gz
set -euo pipefail
JAVA_HOME=$1
APP_VERSION=$2

rm -rf target/release target/jmods target/fx-jre target/image
mkdir -p target/release target/jmods

"$JAVA_HOME/bin/jar" --extract --file javafx/jmods/openjfx-*_linux-x64_bin-jmods.zip --dir target/jmods
JAVAFX_JMODS=$(dirname "$(find target/jmods -name 'javafx.base.jmod' | head -1)")

# a JDK without jmods links from its own runtime image (JEP 493)
MODULE_PATH=$JAVAFX_JMODS
if [ -d "$JAVA_HOME/jmods" ]; then MODULE_PATH=$JAVA_HOME/jmods:$MODULE_PATH; fi

# --strip-debug would also strip native libraries and needs objcopy, which the build agent lacks
"$JAVA_HOME/bin/jlink" --module-path "$MODULE_PATH" \
--add-modules java.base,java.sql,java.management,javafx.controls,javafx.fxml,javafx.media,javafx.base,javafx.swing,javafx.graphics \
--strip-native-commands --strip-java-debug-attributes --no-man-pages --no-header-files '--exclude-files=**.md' \
--output target/fx-jre

"$JAVA_HOME/bin/jpackage" --app-version "$APP_VERSION" -t app-image --icon AudioBookConverter.png --name AudioBookConverter --vendor Recoupler \
--input "target/package/audiobookconverter-$APP_VERSION-linux-installer/audiobookconverter-$APP_VERSION/app" \
--main-jar "lib/audiobookconverter-$APP_VERSION.jar" --runtime-image target/fx-jre \
--dest target/image

tar -C target/image -czf "target/release/AudioBookConverter-static-binaries-$APP_VERSION.tar.gz" AudioBookConverter

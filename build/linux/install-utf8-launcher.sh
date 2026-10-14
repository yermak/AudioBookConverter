#!/bin/sh
# Puts a script in front of the jpackage launcher of the app image in $1 (e.g. target/image/AudioBookConverter).
#
# Java reads file names in the charset of the locale it starts with, and -Dsun.jnu.encoding cannot change that: under
# a C/POSIX locale every non-ASCII name became '?', so such files could not be opened (issue #627). The script switches
# the character type to UTF-8 when the locale is not UTF-8. The jpackage launcher reads <its name>.cfg, so both move.
set -e
IMAGE=$1
NAME=$(basename "$IMAGE")
mv "$IMAGE/bin/$NAME" "$IMAGE/bin/$NAME-launcher"
mv "$IMAGE/lib/app/$NAME.cfg" "$IMAGE/lib/app/$NAME-launcher.cfg"
cat > "$IMAGE/bin/$NAME" <<EOF
#!/bin/sh
# Java reads file names in the charset of the locale: under C/POSIX non-ASCII names cannot be opened
case "\$(locale charmap 2>/dev/null)" in
  UTF-8) ;;
  *) if [ -n "\$LC_ALL" ]; then export LC_ALL=C.UTF-8; else export LC_CTYPE=C.UTF-8; fi ;;
esac
exec "\$(dirname "\$(readlink -f "\$0")")/$NAME-launcher" "\$@"
EOF
chmod +x "$IMAGE/bin/$NAME"

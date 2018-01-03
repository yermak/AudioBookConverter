package com.freeipodsoftware.abc;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static String nullToEmptyString(String string) {
        return string == null ? "" : string;
    }

    public static boolean hasText(String text) {
        return text != null && text.trim().length() > 0;
    }

    public static void centerDialog(Shell parent, Shell shell) {
        Rectangle parentSize = parent.getBounds();
        Rectangle mySize = shell.getBounds();
        int locationX = (parentSize.width - mySize.width) / 2 + parentSize.x;
        int locationY = (parentSize.height - mySize.height) / 2 + parentSize.y;
        shell.setLocation(new Point(locationX, locationY));
    }

    public static String makeFilenameUnique(String filename) {
        Pattern extPattern = Pattern.compile("\\.(\\w+)$");
        Matcher extMatcher = extPattern.matcher(filename);
        if (extMatcher.find()) {
            try {
                String extension = extMatcher.group(1);

                for (File outputFile = new File(filename); outputFile.exists(); outputFile = new File(filename)) {
                    Pattern pattern = Pattern.compile("(?i)(.*)\\((\\d+)\\)\\." + extension + "$");
                    Matcher matcher = pattern.matcher(filename);
                    if (matcher.find()) {
                        filename = matcher.group(1) + "(" + (Integer.parseInt(matcher.group(2)) + 1) + ")." + extension;
                    } else {
                        filename = filename.replaceAll("." + extension + "$", "(1)." + extension);
                    }
                }

                return filename;
            } catch (Exception var7) {
                throw new RuntimeException(Messages.getString("Util.connotUseFilename") + " " + filename);
            }
        } else {
            throw new RuntimeException(Messages.getString("Util.connotUseFilename") + " " + filename + " (2)");
        }
    }

}

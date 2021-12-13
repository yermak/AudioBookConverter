package uk.yermak.audiobookconverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

public enum Environment {
    DEV, MAC, LINUX, WINDOWS;

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Properties PATH = new Properties();

    private static Environment target;

    static {
        if (isDebug()) target = DEV;
        if (isLinux()) target = LINUX;
        if (isMac()) target = MAC;
        if (isWindows()) target = WINDOWS;

    }


    private static boolean isDebug() {
        String debug = System.getenv("DEBUG");
        return (StringUtils.isNotEmpty(debug)) && Boolean.parseBoolean(debug);
    }
    public static boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }

    public static boolean isLinux() {
        return System.getProperty("os.name").contains("Linux");
    }

    public static boolean isMac() {
        return System.getProperty("os.name").contains("Mac OS X");
    }



    static String getPath(String command) {
        String property = loadAppProperties().getProperty(command);
        if (property != null && !isDebug()) {
            return getAppPath() + property;
        }
        return command + (isWindows() ? ".exe" : "");

    }

    private static String getAppPath() {
        if (isMac()) return com.apple.eio.FileManager.getPathToApplicationBundle() + "/";
        else return "";
    }

    private static synchronized Properties loadAppProperties() {
        if (PATH.isEmpty()) {
            File file = null;
            if (isDebug()){
                file = new File( "app/path.properties");
            }else if (isMac()) {
                file = new File(getAppPath(), "Contents/app/path.properties");
            } else if (isLinux()) {
                file = new File("../lib/app/path.properties");
            } else if (isWindows()){
                file = new File( "app/path.properties");
            } else {
                logger.error("Unknown OS: can't find path name");
            }

            if (file.exists()) {
                try (FileInputStream in = new FileInputStream(file)) {
                    PATH.load(in);
                } catch (IOException e) {
                    logger.error("Error during loading properties", e);
                }
            } else {
                logger.error("Path properties is not found at: ", file.getPath());
            }
        }
        return PATH;
    }


    public static File getInitialDirecotory(String sourceFolder) {
        if (sourceFolder == null) {
            return new File(System.getProperty("user.home"));
        }
        File file = new File(sourceFolder);
        return file.exists() ? file : getInitialDirecotory(file.getParent());
    }
}

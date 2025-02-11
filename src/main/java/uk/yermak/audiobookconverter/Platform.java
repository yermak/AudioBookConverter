package uk.yermak.audiobookconverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Properties;

public enum Platform {
    DEV {
    },

    MAC {
        protected String getAppPath() {
            return /*com.apple.eio.FileManager.getPathToApplicationBundle() +*/ "/";
        }

        @Override
        protected File getConfigFilePath() {
            return new File(getAppPath(), "Contents/app/path.properties");
        }
    },

    LINUX {
        @Override
        protected File getConfigFilePath() {
            return new File("../lib/app/path.properties");

        }
    },

    WINDOWS {
        @Override
        public Process createProcess(List<String> arguments) throws IOException {
            return Runtime.getRuntime().exec( String.join(" ", arguments));

        }
    };
    static Platform current;
    private static Properties properties = new Properties();

    static {
        if (LINUX.isLinux()) current = LINUX;
        if (MAC.isMac()) current = MAC;
        if (WINDOWS.isWindows()) current = WINDOWS;
        if (DEV.isDebug()) current = DEV;
        properties = current.loadAppProperties();
    }

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public static final String FFPROBE = current.getPath("ffprobe");
    public static final String MP4INFO = current.getPath("mp4info").replaceAll(" ", "\\ ");
    public static final String MP4ART = current.getPath("mp4art").replaceAll(" ", "\\ ");
    public final static String FFMPEG = current.getPath("ffmpeg").replaceAll(" ", "\\ ");


    private boolean isDebug() {
        String debug = System.getenv("DEBUG");
        return (StringUtils.isNotEmpty(debug)) && Boolean.parseBoolean(debug);
    }

    public boolean isWindows() {
        return System.getProperty("os.name").contains("Windows");
    }

    public boolean isLinux() {
        return System.getProperty("os.name").contains("Linux");
    }

    public boolean isMac() {
        return System.getProperty("os.name").contains("Mac OS X");
    }


    String getPath(String command) {
        return getAppPath() + properties.getProperty(command);
    }

    protected String getAppPath() {
        return "";
    }

    protected File getConfigFilePath() {
        return new File("app/path.properties");
    }

    synchronized Properties loadAppProperties() {
        if (properties.isEmpty()) {
            File file = getConfigFilePath();

            if (file.exists()) {
                try (FileInputStream in = new FileInputStream(file)) {
                    properties.load(in);
                } catch (IOException e) {
                    logger.error("Error during loading properties", e);
                }
            } else {
                logger.error("Path properties is not found at: ", file.getPath());
            }
        }
        return properties;
    }


/*
    public static File getInitialDirectory(String sourceFolder) {
        if (sourceFolder == null) {
            return new File(System.getProperty("user.home"));
        }
        File file = new File(sourceFolder);
        return file.exists() ? file : getInitialDirectory(file.getParent());
    }
*/

    //IMPORTANT !!!
    //using custom processes for Windows here -  Runtime.exec() due to JDK specific way of interpreting quoted arguments in ProcessBuilder https://bugs.openjdk.java.net/browse/JDK-8131908
    public Process createProcess(List<String> arguments) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(arguments);
        return pb.start();
    }
}

package uk.yermak.audiobookconverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public enum Platform {
    DEV {
    },

    MAC {
        // path.properties holds absolute binary paths, so only the config file is resolved against the bundle
        @Override
        protected File getConfigFilePath() {
            File launcherDir = launcherDir(); // <bundle>.app/Contents/MacOS
            return launcherDir == null ? new File("/Contents/app/path.properties") : new File(launcherDir.getParentFile(), "app/path.properties");
        }
    },

    LINUX {
        // path.properties and the binary paths in it are relative to the launcher's bin directory
        @Override
        protected String getAppPath() {
            File launcherDir = launcherDir();
            return launcherDir == null ? "" : launcherDir.getPath() + File.separator;
        }

        @Override
        protected File getConfigFilePath() {
            return new File(getAppPath() + "../lib/app/path.properties");
        }
    },

    WINDOWS {
        @Override
        public Process createProcess(List<String> arguments) throws IOException {
            List<String> quoted = new ArrayList<>();
            quoted.add(arguments.get(0)); // the executable path has no spaces on Windows (relative to the install dir)
            arguments.stream().skip(1).map(Platform::quoteWindowsArgument).forEach(quoted::add);
            return Runtime.getRuntime().exec(String.join(" ", quoted));
        }
    };
    // declared before the static block, which logs
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static Platform current;
    private static Properties properties = new Properties();

    static {
        if (LINUX.isLinux()) current = LINUX;
        if (MAC.isMac()) current = MAC;
        if (WINDOWS.isWindows()) current = WINDOWS;
        if (DEV.isDebug()) current = DEV;
        if (current == null) {
            logger.warn("Unknown os.name {}, using Linux paths", System.getProperty("os.name"));
            current = LINUX;
        }
        properties = current.loadAppProperties();
    }


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

    /**
     * Directory of the native launcher, from the property jpackage launchers set.
     * Null when not started by a launcher (IDE, mvn): paths then stay relative to the working directory.
     */
    static File launcherDir() {
        String appPath = System.getProperty("jpackage.app-path");
        return appPath == null ? null : new File(appPath).getParentFile();
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
                logger.error("Path properties is not found at: {}", file.getAbsolutePath());
            }
        }
        return properties;
    }

    /**
     * Quotes an argument for the Windows command line (CommandLineToArgvW rules): whitespace needs quotes, a backslash run
     * before a quote is doubled, and an embedded quote is escaped. Arguments already in quotes are left as they are.
     */
    // ponytail: Runtime.exec(String) splits at whitespace runs and rejoins with one space, so inside a quoted argument
    // several spaces collapse to one and tabs/newlines become spaces; exec(String[]) would avoid it but hits JDK-8131908
    static String quoteWindowsArgument(String arg) {
        boolean needsQuotes = arg.isEmpty() || arg.chars().anyMatch(c -> Character.isWhitespace(c) || c == '"');
        if (!needsQuotes || (arg.length() > 1 && arg.startsWith("\"") && arg.endsWith("\""))) return arg;
        StringBuilder quoted = new StringBuilder("\"");
        int backslashes = 0;
        for (char c : arg.toCharArray()) {
            if (c == '\\') {
                backslashes++;
                continue;
            }
            quoted.append("\\".repeat(c == '"' ? backslashes * 2 + 1 : backslashes)).append(c);
            backslashes = 0;
        }
        return quoted.append("\\".repeat(backslashes * 2)).append('"').toString();
    }

    //IMPORTANT !!!
    //using custom processes for Windows here -  Runtime.exec() due to JDK specific way of interpreting quoted arguments in ProcessBuilder https://bugs.openjdk.java.net/browse/JDK-8131908
    public Process createProcess(List<String> arguments) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(arguments);
        return pb.start();
    }
}

package uk.yermak.audiobookconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Properties;

public class AppProperties {
    public static final String WEBSITE_URL = "https://github.com/yermak/AudioBookConverter";
    public static final String HELP_URL = "https://github.com/yermak/AudioBookConverter/";
    private static final String APP_PROPERTIES = "AudioBookConverter-V2.properties";
    private static final String APPDATA = "APPDATA";
    private static final String MP3TOI_POD_AUDIO_BOOK_CONVERTER = "AudioBookConverter-V2";
    public static final String STAY_UPDATED = "stayUpdated";
    public static final String NO_UPDATECHECK_UNTIL = "noUpdateCheckUntil";

    public AppProperties() {
    }

    private static Properties getAppProperties() {
        Properties defaultProperties = new Properties();
        defaultProperties.put("stayUpdated", Boolean.TRUE.toString());
        defaultProperties.put("optionPanel.singleOutputFileMode", Boolean.TRUE.toString());
        Properties applicationProps = new Properties(defaultProperties);

        try {
            FileInputStream in = new FileInputStream(new File(new File(System.getenv("APPDATA"), "AudioBookConverter-V2"), "AudioBookConverter-V2.properties"));
            applicationProps.load(in);
            in.close();
        } catch (Exception e) {
        }

        return applicationProps;
    }

    public static String getProperty(String key) {
        Properties applicationProps = getAppProperties();
        return applicationProps.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        Properties applicationProps = getAppProperties();
        return applicationProps.getProperty(key, defaultValue);
    }

    public static void setProperty(String key, String value) {
        Properties applicationProps = getAppProperties();
        applicationProps.put(key, value);

        try {
            File appDir = new File(new File(System.getenv("APPDATA")), "AudioBookConverter-V2");
            if (!appDir.exists()) {
                boolean succ = appDir.mkdir();
                System.out.println(succ);
            }
            FileOutputStream out = new FileOutputStream(new File(appDir, "AudioBookConverter-V2.properties"));
            applicationProps.store(out, "");
            out.close();
        } catch (Exception var5) {
        }

    }

    public static boolean getBooleanProperty(String key) {
        return Boolean.valueOf(getProperty(key));
    }

    public static void setBooleanProperty(String key, boolean value) {
        setProperty(key, (Boolean.valueOf(value)).toString());
    }

    public static Date getDateProperty(String key) {
        return new Date(Long.parseLong(getProperty(key)));
    }

    public static void setDateProperty(String key, Date date) {
        setProperty(key, String.valueOf(date.getTime()));
    }
}

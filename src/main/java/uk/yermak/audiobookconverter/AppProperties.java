package uk.yermak.audiobookconverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class AppProperties {

    public AppProperties() {
    }

    private static Properties getAppProperties() {
        Properties defaultProperties = new Properties();
        Properties applicationProps = new Properties(defaultProperties);

        try {
            FileInputStream in = new FileInputStream(new File(new File(System.getenv("APPDATA"), "AudioBookConverter-V3"), "AudioBookConverter-V3.properties"));
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

    public static void setProperty(String key, String value) {
        Properties applicationProps = getAppProperties();
        applicationProps.put(key, value);

        try {
            File appDir = new File(new File(System.getenv("APPDATA")), "AudioBookConverter-V3");
            if (!appDir.exists()) {
                boolean succ = appDir.mkdir();
                System.out.println(succ);
            }
            FileOutputStream out = new FileOutputStream(new File(appDir, "AudioBookConverter-V3.properties"));
            applicationProps.store(out, "");
            out.close();
        } catch (Exception var5) {
        }

    }

}

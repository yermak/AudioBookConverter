package uk.yermak.audiobookconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

public class AppProperties {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
            logger.error("Error during loading properties", e);
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
            File appDir = new File(new File(System.getenv("APPDATA")), "AudioBookConverter-V4");
            if (!appDir.exists()) {
                boolean succ = appDir.mkdir();
                System.out.println(succ);
            }
            FileOutputStream out = new FileOutputStream(new File(appDir, "AudioBookConverter-V4.properties"));
            applicationProps.store(out, "");
            out.close();
        } catch (Exception e) {
            logger.error("Error during saving propertie", e);
        }

    }

}

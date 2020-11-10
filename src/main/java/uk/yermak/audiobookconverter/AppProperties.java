package uk.yermak.audiobookconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Enumeration;
import java.util.Properties;

public class AppProperties {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final File APP_DIR = new File(System.getenv("APPDATA"), Version.getVersionString());
    public static final File PROP_FILE = new File(APP_DIR, Version.getVersionString() + ".properties");

    private static Properties getAppProperties() {
        //TODO:add default props here
        Properties defaultProperties = new Properties();
        Properties applicationProps = new Properties(defaultProperties);
        if (PROP_FILE.exists()) {
            try (FileInputStream in = new FileInputStream(PROP_FILE)) {
                applicationProps.load(in);
            } catch (IOException e) {
                logger.error("Error during loading properties", e);
            }
        }
        return applicationProps;
    }

    public static String getProperty(String key) {
        Properties applicationProps = getAppProperties();
        return applicationProps.getProperty(key);
    }

    public static Properties getProperties(String group) {
        Properties properties = new Properties();
        Properties applicationProps = getAppProperties();
        Enumeration<Object> keys = applicationProps.keys();
        while (keys.hasMoreElements()) {
            String propName = (String) keys.nextElement();
            if (propName.startsWith(group + ".")) {
                String nameWithoutGroup = propName.substring(group.length() + 1);
                properties.setProperty(nameWithoutGroup, applicationProps.getProperty(propName));
            }
        }
        return properties;
    }

    public static void setProperty(String key, String value) {
        Properties applicationProps = getAppProperties();
        applicationProps.put(key, value);
        File appDir = APP_DIR;
        if (appDir.exists() || appDir.mkdir()) {
            try (FileOutputStream out = new FileOutputStream(PROP_FILE)) {
                applicationProps.store(out, "");
            } catch (Exception e) {
                logger.error("Error during saving properties", e);
            }
        }
    }
}

package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jetbrains.exodus.bindings.StringBinding;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.env.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Enumeration;
import java.util.Properties;
import java.util.stream.StreamSupport;

public class AppProperties {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final File APP_DIR = new File(System.getProperty("APP_HOME"));
    public static final File PROP_FILE = new File(APP_DIR, Version.getVersionString() + ".properties");
    private static final Properties applicationProps = new Properties();

    static {
//        loadAppProperties();
    }

    static synchronized Properties loadAppProperties() {
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
        try (jetbrains.exodus.env.Environment env = Environments.newInstance(APP_DIR.getPath())) {
            env.computeInReadonlyTransaction(txn -> {
                final Store store = env.openStore("Settings", StoreConfig.WITHOUT_DUPLICATES, txn);
                return store.get(txn, StringBinding.stringToEntry(key));
            });
        }
        return null;
    }

    public static Properties getProperties(String group) {
        Properties properties = new Properties();
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

    public static synchronized void setProperty(String key, String value) {
        try (jetbrains.exodus.env.Environment env = Environments.newInstance(APP_DIR.getPath())) {
            env.executeInTransaction(txn -> {
                final Store store = env.openStore("Settings", StoreConfig.WITHOUT_DUPLICATES, txn);
                store.put(txn, StringBinding.stringToEntry(key), StringBinding.stringToEntry(value));
            });
        }
    }

    public static void saveGenres(String genre) {
        if (StringUtils.isNotEmpty(genre) & loadGenres().stream().noneMatch(s -> s.equals(genre))) {
            try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance(APP_DIR.getPath())) {
                entityStore.executeInTransaction(txn -> {
                    final Entity genreEntity = txn.newEntity("Genre");
                    genreEntity.setProperty("title", genre);
                    genreEntity.setProperty("created", System.currentTimeMillis());
                });
            }
        }
    }

    public static ObservableList<String> loadGenres() {
        ObservableList<String> genres = FXCollections.observableArrayList();
        try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance(APP_DIR.getPath())) {
            entityStore.executeInReadonlyTransaction(txn -> {
                StreamSupport.stream(txn.sort("Genre", "title", true).spliterator(), false).forEach(g -> genres.add((String) g.getProperty("title")));
            });
        }
        return genres;
    }

    public static void removeGenre(String genre) {
        try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance(APP_DIR.getPath())) {
            entityStore.executeInTransaction(txn -> {
                EntityIterable entities = txn.find("Genre", "title", genre);
                for (Entity entity : entities) {
                    entity.delete();
                }
            });
        }
    }
}

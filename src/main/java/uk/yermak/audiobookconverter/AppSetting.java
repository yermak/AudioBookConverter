package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.bindings.StringBinding;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.env.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

public class AppSetting {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final File APP_DIR = new File(System.getProperty("APP_HOME"));
    public static final String PRESET_ENTITY = "Preset";
    public static final String PRESET_NAME = "name";
    public static final String PRESET_FORMAT = "format";
    public static final String PRESET_BITRATE = "bitrate";
    public static final String PRESET_FREQUENCY = "frequency";
    public static final String PRESET_CHANNELS = "channels";
    public static final String PRESET_CUTOFF = "cutoff";
    public static final String PRESET_CBR = "cbr";
    public static final String PRESET_QUALITY = "quality";
    public static final String GENRE_ENTITY = "Genre";
    public static final String GENRE_TITLE = "title";
    public static final String GENRE_CREATED = "created";
    public static final String SETTINGS = "Settings";
    public static final String PRESET_SPEED = "speed";
    public static final String PRESET_FORCE = "force";
    public static final String PRESET_SPLIT_CHAPTERS = "split_chapters";
    private static Map<String, String> cache = new ConcurrentHashMap<>();


    public static synchronized String getProperty(String key) {
        String result = cache.get(key);
        if (result != null) return result;
        logger.debug("Settings cache is missed for property: " + key);
        try (jetbrains.exodus.env.Environment env = Environments.newInstance(APP_DIR.getPath())) {
            result = env.computeInReadonlyTransaction(txn -> {
                final Store store = env.openStore(SETTINGS, StoreConfig.WITHOUT_DUPLICATES, txn);
                ByteIterable entry = store.get(txn, StringBinding.stringToEntry(key));
                if (entry != null) {
                    return StringBinding.entryToString(entry);
                } else {
                    return null;
                }
            });
            cache.put(key, result);
        }
        return result;
    }

    public static synchronized void setProperty(String key, String value) {
        cache.put(key, value);
        logger.debug("Updating settings cache and database with key: [" + key + "] and value: [" + value + "]");
        try (jetbrains.exodus.env.Environment env = Environments.newInstance(APP_DIR.getPath())) {
            env.executeInTransaction(txn -> {
                final Store store = env.openStore(SETTINGS, StoreConfig.WITHOUT_DUPLICATES, txn);
                store.put(txn, StringBinding.stringToEntry(key), StringBinding.stringToEntry(value));
            });
        }
    }

    public static synchronized void saveGenres(String genre) {
        if (StringUtils.isNotEmpty(genre) & loadGenres().stream().noneMatch(s -> s.equals(genre))) {
            try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance(APP_DIR.getPath())) {
                entityStore.executeInTransaction(txn -> {
                    final Entity genreEntity = txn.newEntity(GENRE_ENTITY);
                    genreEntity.setProperty(GENRE_TITLE, genre);
                    genreEntity.setProperty(GENRE_CREATED, System.currentTimeMillis());
                });
            }
        }
    }

    public static synchronized ObservableList<String> loadGenres() {
        ObservableList<String> genres;
        try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance(APP_DIR.getPath())) {
            genres = entityStore.computeInReadonlyTransaction(txn -> {
                ObservableList<String> list = FXCollections.observableArrayList();
                StreamSupport.stream(txn.sort(GENRE_ENTITY, GENRE_TITLE, true).spliterator(), false).forEach(g -> list.add((String) g.getProperty(GENRE_TITLE)));
                return list;
            });
        }
        return genres;
    }

    public static synchronized void removeGenre(String genre) {
        try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance(APP_DIR.getPath())) {
            entityStore.executeInTransaction(txn -> {
                EntityIterable entities = txn.find(GENRE_ENTITY, GENRE_TITLE, genre);
                for (Entity entity : entities) {
                    entity.delete();
                }
            });
        }
    }

    public static synchronized List<Preset> loadPresets() {
        List<Preset> presets;
        try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance(APP_DIR.getPath())) {
            presets = entityStore.computeInReadonlyTransaction(txn -> {
                List<Preset> list = new ArrayList<>();
                EntityIterable all = txn.getAll(PRESET_ENTITY);
                for (Entity entity : all) {
                    Preset preset = bindPreset(entity);
                    list.add(preset);
                }
                return list;
            });
        }
        return presets;
    }

    @NotNull
    private static Preset bindPreset(Entity entity) {
        String name = (String) entity.getProperty(PRESET_NAME);
        String format = (String) entity.getProperty(PRESET_FORMAT);
        Integer bitrate = (Integer) entity.getProperty(PRESET_BITRATE);
        Integer frequency = (Integer) entity.getProperty(PRESET_FREQUENCY);
        Integer channels = (Integer) entity.getProperty(PRESET_CHANNELS);
        Integer cutoff = (Integer) entity.getProperty(PRESET_CUTOFF);
        Boolean cbr = (Boolean) entity.getProperty(PRESET_CBR);
        Integer quality = (Integer) entity.getProperty(PRESET_QUALITY);
        Double speed = (Double) entity.getProperty(PRESET_SPEED);
        OutputParameters.Force force = OutputParameters.Force.valueOf((String) entity.getProperty(PRESET_FORCE));
        Boolean splitChapters = (Boolean) entity.getProperty(PRESET_SPLIT_CHAPTERS);
        Preset preset = new Preset(name, new OutputParameters(Format.instance(format), bitrate, frequency, channels, cutoff, cbr, quality, speed, force, splitChapters));
        return preset;
    }

    public static synchronized void savePreset(Preset preset) {
        try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance(APP_DIR.getPath())) {
            entityStore.executeInTransaction(txn -> {
                Entity entity;
                EntityIterable entities = txn.find(PRESET_ENTITY, PRESET_NAME, preset.getName());
                if (entities.isEmpty()) {
                    entity = txn.newEntity(PRESET_ENTITY);
                } else {
                    entity = entities.getFirst();
                }
                entity.setProperty(PRESET_NAME, preset.getName());
                entity.setProperty(PRESET_FORMAT, preset.getFormat().extension);
                entity.setProperty(PRESET_BITRATE, preset.getBitRate());
                entity.setProperty(PRESET_FREQUENCY, preset.getFrequency());
                entity.setProperty(PRESET_CHANNELS, preset.getChannels());
                entity.setProperty(PRESET_CUTOFF, preset.getCutoff());
                entity.setProperty(PRESET_CBR, preset.isCbr());
                entity.setProperty(PRESET_QUALITY, preset.getVbrQuality());
                entity.setProperty(PRESET_SPEED, preset.getSpeed());
                entity.setProperty(PRESET_FORCE, preset.getForce().toString());
                entity.setProperty(PRESET_SPLIT_CHAPTERS, preset.isSplitChapters());
            });
        }
    }

    public static Preset loadPreset(String presetName) {
        Preset preset;
        try (PersistentEntityStore entityStore = PersistentEntityStores.newInstance(APP_DIR.getPath())) {
            preset = entityStore.computeInReadonlyTransaction(txn -> {
                EntityIterable entities = txn.find(PRESET_ENTITY, PRESET_NAME, presetName);
                if (entities.isEmpty()) return null;
                return bindPreset(entities.getFirst());
            });
        }
        return preset;
    }
}

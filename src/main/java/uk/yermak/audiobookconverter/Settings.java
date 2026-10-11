package uk.yermak.audiobookconverter;

import com.google.gson.*;
import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.formats.Format;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

public class Settings {

    private static final Preferences preferences = Preferences.userNodeForPackage(AudiobookConverter.class);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Format.class, (JsonDeserializer<Format>) (jsonElement, type, context) -> {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonElement extension = jsonObject.get("extension");
                String formatType = extension.getAsString();
                return new FormatInstanceCreator(formatType).createInstance(type);
            }).create();

    private boolean darkMode = false;
    private boolean showHints = true;
    private int lastUsedPreset = 0;
    private List<Preset> presets = new ArrayList<>();
    private Set<String> genres = new TreeSet<>();
    private String chapterFormat = "<if(BOOK_NUMBER)><BOOK_NUMBER>. <endif>" +
            "<if(BOOK_TITLE)><BOOK_TITLE>. <endif>" +
            "<if(CHAPTER_TEXT)><CHAPTER_TEXT> <endif>" +
            "<if(CHAPTER_NUMBER)><CHAPTER_NUMBER; format=\"%,03d\"> <endif>" +
            "<if(TAG)><TAG> <endif>" +
            "<if(CUSTOM_TITLE)><CUSTOM_TITLE> <endif>" +
            "<if(DURATION)>- <DURATION; format=\"%02d:%02d:%02d\"><endif>";
    private String filenameFormat = "<WRITER><if(SERIES)> - [<SERIES><if(BOOK_NUMBER)> - <BOOK_NUMBER; format=\"%,02d\"><endif>]<endif> - <TITLE><if(NARRATOR)> (<NARRATOR>)<endif>";
    private String partFormat = "<if(WRITER)><WRITER> <endif>" +
            "<if(SERIES)>- [<SERIES><if(BOOK_NUMBER)> -<BOOK_NUMBER><endif>] - <endif>" +
            "<if(TITLE)><TITLE><endif>" +
            "<if(NARRATOR)> (<NARRATOR>)<endif>" +
            "<if(YEAR)>-<YEAR><endif>" +
            "<if(PART)>, Part <PART; format=\"%,03d\"><endif>";
    private String chapterContext = "CHAPTER_NUMBER:CHAPTER_TEXT:DURATION";
    private String chapterCustomTitle = "";
    private String outputFolder = System.getProperty("user.home");
    private String sourceFolder = System.getProperty("user.home");
    private String language = "";

    // A Preferences value is limited to MAX_VALUE_LENGTH (8 KB), and presets and genres grow past it: a longer value
    // is split over key, key.part1, key.part2, ... A value saved by an older version (one key) still reads the same.
    static void write(Preferences node, String key, String value) {
        int max = Preferences.MAX_VALUE_LENGTH;
        int parts = Math.max(1, (value.length() + max - 1) / max);
        for (int i = 0; i < parts; i++) {
            node.put(partKey(key, i), value.substring(i * max, Math.min(value.length(), (i + 1) * max)));
        }
        removeParts(node, key, parts); // left over from a longer earlier value
    }

    static String read(Preferences node, String key) {
        String first = node.get(key, null);
        if (first == null) return null;
        StringBuilder value = new StringBuilder(first);
        for (int i = 1; node.get(partKey(key, i), null) != null; i++) {
            value.append(node.get(partKey(key, i), null));
        }
        return value.toString();
    }

    static void remove(Preferences node, String key) {
        removeParts(node, key, 0);
    }

    private static void removeParts(Preferences node, String key, int from) {
        for (int i = from; i == 0 || node.get(partKey(key, i), null) != null; i++) {
            node.remove(partKey(key, i));
        }
    }

    private static String partKey(String key, int part) {
        return part == 0 ? key : key + ".part" + part;
    }

    public static void saveSetting(Settings settings) {
        write(preferences, Version.getSettingsVersion(), gson.toJson(settings));
        current = null;
    }

    public static void clear() {
        remove(preferences, Version.getSettingsVersion());
        current = null;
    }

    private static volatile Settings current;

    /**
     * Settings for reading only, cached until the next save or clear: title and file name rendering asks for them for
     * every chapter, and loadSetting() parses the stored JSON each time. Change settings on a loadSetting() copy.
     */
    public static Settings current() {
        Settings settings = current;
        if (settings == null) {
            settings = loadSetting();
            current = settings;
        }
        return settings;
    }

    public void save() {
        saveSetting(this);
    }

    public static Settings loadSetting() {
        String settingsJson = read(preferences, Version.getSettingsVersion());
        if (settingsJson == null) {
            Settings settings = new Settings();
            settings.setPresets(Preset.defaultValues);
            return settings;
        }
        Settings settings = gson.fromJson(settingsJson, Settings.class);
        return settings;
    }

    public static String getRawData() {
        String settingsJson = read(preferences, Version.getSettingsVersion());
        return settingsJson;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public Settings setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        return this;
    }

    public boolean isShowHints() {
        return showHints;
    }

    public Settings setShowHints(boolean showHints) {
        this.showHints = showHints;
        return this;
    }

    public List<Preset> getPresets() {
        return presets;
    }

    public void setPresets(List<Preset> presets) {
        this.presets = presets;
    }

    public Set<String> getGenres() {
        return genres;
    }

    public void setGenres(Set<String> genres) {
        this.genres = genres;
    }

    public String getChapterFormat() {
        return chapterFormat;
    }

    public void setChapterFormat(String chapterFormat) {
        this.chapterFormat = chapterFormat;
    }

    public String getFilenameFormat() {
        return filenameFormat;
    }

    public void setFilenameFormat(String filenameFormat) {
        this.filenameFormat = filenameFormat;
    }

    public String getPartFormat() {

        return partFormat;
    }

    public void setPartFormat(String partFormat) {
        this.partFormat = partFormat;
    }

    public String getChapterContext() {
        return chapterContext;
    }

    public Settings setChapterContext(String chapterContext) {
        this.chapterContext = chapterContext;
        return this;
    }

    public String getChapterCustomTitle() {
        return chapterCustomTitle;
    }

    public Settings setChapterCustomTitle(String chapterCustomTitle) {
        this.chapterCustomTitle = chapterCustomTitle;
        return this;
    }

    public File getOutputFolder() {
        return existingFolder(outputFolder);
    }

    /** The folder, else its parent, else the home folder: a drive root (unplugged USB drive) has no parent. */
    private static File existingFolder(String path) {
        if (StringUtils.isNotBlank(path)) {
            File folder = new File(path);
            if (folder.exists()) return folder;
            File parent = folder.getParentFile();
            if (parent != null && parent.exists()) return parent;
        }
        return new File(System.getProperty("user.home"));
    }

    public Settings setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
        return this;
    }

    public File getSourceFolder() {
        return existingFolder(sourceFolder);
    }

    public Settings setSourceFolder(String sourceFolder) {
        this.sourceFolder = sourceFolder;
        return this;
    }

    public String getLanguage() {
        // older versions saved country codes instead of language codes for these
        return switch (language) {
            case "ua" -> "uk";
            case "by" -> "be";
            case "kz" -> "kk";
            case "gr" -> "el";
            case null, default -> language;
        };
    }

    public Settings setLanguage(String language) {
        this.language = language;
        return this;
    }

    public Preset findPreset(String name) {
        for (Preset preset : presets) {
            if (preset.getName().equals(name)) return preset;
        }
        return null;
    }

    public int getLastUsedPreset() {
        return lastUsedPreset;
    }

    public void setLastUsedPreset(int lastUsedPreset) {
        this.lastUsedPreset = lastUsedPreset;
    }

    public static class FormatInstanceCreator implements InstanceCreator<Format> {
        private final String formatType;

        public FormatInstanceCreator(String formatType) {
            this.formatType = formatType;
        }

        @Override
        public Format createInstance(Type type) {
            return Format.instance(formatType);
        }
    }
}


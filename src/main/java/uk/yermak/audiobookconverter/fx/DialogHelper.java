package uk.yermak.audiobookconverter.fx;

import com.google.common.collect.ImmutableSet;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.book.AudioBookInfo;
import uk.yermak.audiobookconverter.fx.util.Comparators;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.*;

public class DialogHelper {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String M4B = "m4b";
    private static final String M4A = "m4a";
    public static final String MP3 = "mp3";
    public static final String WMA = "wma";
    public static final String FLAC = "flac";
    public static final String AAC = "aac";
    public static final String OGG = "ogg";
    public static final String WAV = "wav";
    public static final String AAX = "aax";
    public static final String AA = "aa";

    private final static String[] FILE_EXTENSIONS = {MP3, M4A, M4B, WMA, FLAC, OGG, AAC, WAV, AAX, AA};


    static String selectOutputFile(AudioBookInfo audioBookInfo) {
        JfxEnv env = AudiobookConverter.getEnv();

        final FileChooser fileChooser = new FileChooser();
        File outputFolder = Settings.loadSetting().getOutputFolder();
        fileChooser.setInitialDirectory(outputFolder);
        fileChooser.setInitialFileName(Utils.getOuputFilenameSuggestion(audioBookInfo));
        ResourceBundle bundle = AudiobookConverter.getBundle();
        String title = bundle != null ? bundle.getString("chooser.save_audiobook") : "Save AudioBook";
        fileChooser.setTitle(title);
        String formatAsString = AudiobookConverter.getContext().getFormat().toString();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(formatAsString, "*." + formatAsString)
        );
        File file = fileChooser.showSaveDialog(env.getWindow());
        if (file == null) return null;
        File parentFolder = file.getParentFile();
        Settings.loadSetting().setOutputFolder(parentFolder.getAbsolutePath()).save();
        return file.getPath();
    }

    public static List<String> selectFilesDialog() {
        Window window = AudiobookConverter.getEnv().getWindow();
        final FileChooser fileChooser = new FileChooser();
        try {
            File sourceFolder = Settings.loadSetting().getSourceFolder();
            fileChooser.setInitialDirectory(sourceFolder);
        } catch (Exception e) {
            logger.error("Failed to load Source Folder and set Initial Directory", e);
        }
        StringJoiner filetypes = new StringJoiner("/");

        Arrays.stream(FILE_EXTENSIONS).map(String::toUpperCase).forEach(filetypes::add);

        ResourceBundle bundle = AudiobookConverter.getBundle();
        String title = bundle != null
                ? MessageFormat.format(bundle.getString("chooser.select_files"), filetypes)
                : MessageFormat.format("Select {0} files for conversion", filetypes);
        fileChooser.setTitle(title);

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio", Arrays.asList(toSuffixes("*.", FILE_EXTENSIONS))));

        List<File> files = fileChooser.showOpenMultipleDialog(window);
        if (files == null) return null;

        if (!files.isEmpty()) {
            File firstFile = files.get(0);
            File parentFile = firstFile.getParentFile();
            Settings.loadSetting().setSourceFolder(parentFile.getAbsolutePath()).save();
        }
        return collectFiles(files);
    }

    public static List<String> selectFolderDialog() {
        Window window = AudiobookConverter.getEnv().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        try {
            File sourceFolder = Settings.loadSetting().getSourceFolder();
            directoryChooser.setInitialDirectory(sourceFolder);
        } catch (Exception e) {
            logger.error("Failed to load Source Folder and set Initial Directory", e);
        }

        StringJoiner filetypes = new StringJoiner("/");

        Arrays.stream(FILE_EXTENSIONS).map(String::toUpperCase).forEach(filetypes::add);

        ResourceBundle bundle = AudiobookConverter.getBundle();
        String title = bundle != null
                ? MessageFormat.format(bundle.getString("chooser.select_folder"), filetypes)
                : MessageFormat.format("Select folder with {0} files for conversion", filetypes);
        directoryChooser.setTitle(title);
        File selectedDirectory = directoryChooser.showDialog(window);

        if (selectedDirectory == null) return null;
        Settings.loadSetting().setSourceFolder(selectedDirectory.getAbsolutePath()).save();

        return collectFiles(Collections.singleton(selectedDirectory));
    }

    static List<String> collectFiles(Collection<File> files) {
        List<String> fileNames = new ArrayList<>();
        ImmutableSet<String> extensions = ImmutableSet.copyOf(FILE_EXTENSIONS);

        for (File file : files) {
            if (file.isDirectory()) {
                SuffixFileFilter suffixFileFilter = new SuffixFileFilter(toSuffixes(".", FILE_EXTENSIONS), IOCase.INSENSITIVE);
                Collection<File> nestedFiles = FileUtils.listFiles(file, suffixFileFilter, TrueFileFilter.INSTANCE);
                nestedFiles.stream().map(File::getPath).forEach(fileNames::add);
            } else {
                boolean allowedFileExtension = extensions.contains(FilenameUtils.getExtension(file.getName()).toLowerCase());
                if (allowedFileExtension) {
                    fileNames.add(file.getPath());
                }
            }
        }

        Comparator<String> cmp = Comparators.comparingAlphaDecimal(Comparator.comparing(CharSequence::toString, String::compareToIgnoreCase));
        fileNames.sort(cmp);
        return fileNames;
    }

    private static String[] toSuffixes(String prefix, final String[] extensions) {
        final String[] suffixes = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            suffixes[i] = prefix + extensions[i];
        }
        return suffixes;
    }


}

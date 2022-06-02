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
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.fx.util.Comparators;

import java.io.File;
import java.util.*;

public class DialogHelper {

    private static final String M4B = "m4b";
    private static final String M4A = "m4a";
    public static final String MP3 = "mp3";
    public static final String WMA = "wma";
    public static final String FLAC = "flac";
    public static final String AAC = "aac";
    public static final String OGG = "ogg";
    public static final String WAV = "wav";

    private final static String[] FILE_EXTENSIONS = {MP3, M4A, M4B, WMA, FLAC, OGG, AAC, WAV};


    static String selectOutputFile(AudioBookInfo audioBookInfo) {
        JfxEnv env = AudiobookConverter.getEnv();

        final FileChooser fileChooser = new FileChooser();
        String outputFolder = AppSetting.getProperty("output.folder");
        fileChooser.setInitialDirectory(Platform.getInitialDirecotory(outputFolder));
        fileChooser.setInitialFileName(Utils.getOuputFilenameSuggestion(audioBookInfo));
        fileChooser.setTitle("Save AudioBook");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(AudiobookConverter.getContext().getOutputParameters().getFormat().toString(), "*." + AudiobookConverter.getContext().getOutputParameters().getFormat().toString())
        );
        File file = fileChooser.showSaveDialog(env.getWindow());
        if (file == null) return null;
        File parentFolder = file.getParentFile();
        AppSetting.setProperty("output.folder", parentFolder.getAbsolutePath());
        return file.getPath();
    }

    public static List<String> selectFilesDialog() {
        Window window = AudiobookConverter.getEnv().getWindow();
        final FileChooser fileChooser = new FileChooser();
        String sourceFolder = AppSetting.getProperty("source.folder");
        fileChooser.setInitialDirectory(Platform.getInitialDirecotory(sourceFolder));
        StringJoiner filetypes = new StringJoiner("/");

        Arrays.stream(FILE_EXTENSIONS).map(String::toUpperCase).forEach(filetypes::add);

        fileChooser.setTitle("Select " + filetypes + " files for conversion");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio", Arrays.asList(toSuffixes("*.", FILE_EXTENSIONS))));

        List<File> files = fileChooser.showOpenMultipleDialog(window);
        if (files == null) return null;

        if (!files.isEmpty()) {
            File firstFile = files.get(0);
            File parentFile = firstFile.getParentFile();
            AppSetting.setProperty("source.folder", parentFile.getAbsolutePath());
        }
        return collectFiles(files);
    }

    public static List<String> selectFolderDialog() {
        Window window = AudiobookConverter.getEnv().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        String sourceFolder = AppSetting.getProperty("source.folder");
        directoryChooser.setInitialDirectory(Platform.getInitialDirecotory(sourceFolder));

        StringJoiner filetypes = new StringJoiner("/");

        Arrays.stream(FILE_EXTENSIONS).map(String::toUpperCase).forEach(filetypes::add);

        directoryChooser.setTitle("Select folder with " + filetypes + " files for conversion");
        File selectedDirectory = directoryChooser.showDialog(window);

        if (selectedDirectory == null) return null;
        AppSetting.setProperty("source.folder", selectedDirectory.getAbsolutePath());

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

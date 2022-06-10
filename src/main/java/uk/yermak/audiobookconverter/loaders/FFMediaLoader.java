package uk.yermak.audiobookconverter.loaders;

import javafx.scene.image.Image;
import net.bramp.ffmpeg.FFprobe;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.book.*;
import uk.yermak.audiobookconverter.fx.ConversionContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * Background media info loader. Media files are processed in the same order as passed to the constructor.
 *
 * @author yermak
 */
public class FFMediaLoader {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<String> fileNames;
    private final ConversionGroup conversionGroup;
    private static final ExecutorService mediaExecutor = Executors.newSingleThreadExecutor();

    public FFMediaLoader(List<String> files, ConversionGroup conversionGroup) {
        this.fileNames = files;
        this.conversionGroup = conversionGroup;
    }

    public List<MediaInfo> loadMediaInfo() {
        logger.info("Loading media info");
        try {
            FFprobe ffprobe = new FFprobe(Platform.FFPROBE);
            List<MediaInfo> media = new ArrayList<>();
            for (String fileName : fileNames) {
                Future<MediaInfo> futureLoad = mediaExecutor.submit(new MediaInfoLoader(ffprobe, fileName, conversionGroup));
                MediaInfo mediaInfo = new MediaInfoProxy(fileName, futureLoad);
                media.add(mediaInfo);
            }

            searchForPosters(media);

            return media;
        } catch (Exception e) {
            logger.error("Error during loading media info", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void detach() {

    }

    static void parseCueChapters(MediaInfoBean mediaInfo) throws IOException {
        String filename = mediaInfo.getFileName();
        File file = new File(FilenameUtils.getFullPath(filename) + FilenameUtils.getBaseName(filename) + ".cue");
        if (file.exists()) {
            String cue = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            parseCue(mediaInfo, cue);
        }
    }

    static void parseCue(MediaInfoBean mediaInfo, String cue) {
        AudioBookInfo bookInfo = mediaInfo.getBookInfo();
        String[] split = StringUtils.split(cue, "\n");
        for (String line : split) {
            int i = -1;
            if (bookInfo.tracks().isEmpty()) {
                if ((i = line.indexOf("GENRE")) != -1) bookInfo.genre().set(cleanText(line.substring(i + 5)));
                if ((i = line.indexOf("TITLE")) != -1) bookInfo.title().set(cleanText(line.substring(i + 5)));
                if ((i = line.indexOf("DATE")) != -1) bookInfo.year().set(cleanText(line.substring(i + 4)));
                if ((i = line.indexOf("PERFORMER")) != -1) bookInfo.narrator().set(cleanText(line.substring(i + 9)));
            } else {
                Track track = bookInfo.tracks().get(bookInfo.tracks().size() - 1);
                if ((i = line.indexOf("TITLE")) != -1) track.setTitle(cleanText(line.substring(i + 5)));
                if ((i = line.indexOf("PERFORMER")) != -1) track.setWriter(cleanText(line.substring(i + 9)));
            }
            if ((i = line.indexOf("TRACK")) != -1) {
                bookInfo.tracks().add(new Track(cleanText(line.substring(i + 5))));
            } else {
                if ((i = line.indexOf("INDEX 01")) != -1) {
                    long time = parseCueTime(line.substring(i + 8));
                    if (bookInfo.tracks().size() > 1) {
                        Track track = bookInfo.tracks().get(bookInfo.tracks().size() - 2);
                        track.setEnd(time);
                    }
                    Track track = bookInfo.tracks().get(bookInfo.tracks().size() - 1);
                    track.setStart(time);
                }
            }
        }
        if (!bookInfo.tracks().isEmpty()) {
            bookInfo.tracks().get(bookInfo.tracks().size() - 1).setEnd(mediaInfo.getDuration());
        }
    }

    private static long parseCueTime(String substring) {
        String cleanText = cleanText(substring);
        String[] split = cleanText.split(":");
        return 1000 * (Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1])) + Integer.parseInt(split[2]) * 1000 / 75;
    }

    private static String cleanText(String text) {
        return StringUtils.remove(StringUtils.trim(text), '"');
    }


    static Collection<File> findPictures(File dir) {
        try {
            return FileUtils.listFiles(dir, ArtWork.IMAGE_EXTENSIONS, true);
        } catch (Exception e) {
            logger.error("Failed to search for images in dir:" + dir, e);
            return Collections.emptyList();
        }
    }

    static void searchForPosters(List<MediaInfo> media) throws FileNotFoundException {
        Set<File> searchDirs = new HashSet<>();
        media.forEach(mi -> searchDirs.add(new File(mi.getFileName()).getParentFile()));

        List<File> pictures = new ArrayList<>();

        ConversionContext context = AudiobookConverter.getContext();
        for (File d : searchDirs) {
            pictures.addAll(findPictures(d));
        }

        //adding artificial limit of image count to address issue #153.
        if (!pictures.isEmpty()) {
            for (int i = 0; i < 10 && i < pictures.size(); i++) {
//                context.addPosterIfMissingWithDelay(new ArtWorkBean(Utils.tempCopy(pictures.get(i).getPath())));
                context.addPosterIfMissingWithDelay(new ArtWorkImage(new Image(new FileInputStream(pictures.get(i).getPath()))));
            }
        }
    }


}

package uk.yermak.audiobookconverter.loaders;

import javafx.scene.image.Image;
import net.bramp.ffmpeg.FFprobe;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.ConversionGroup;
import uk.yermak.audiobookconverter.Platform;
import uk.yermak.audiobookconverter.book.ArtWork;
import uk.yermak.audiobookconverter.book.MediaInfo;
import uk.yermak.audiobookconverter.fx.ConversionContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    private static final ExecutorService posterExecutor = Executors.newSingleThreadExecutor();

    public FFMediaLoader(List<String> files, ConversionGroup conversionGroup) {
        this.fileNames = files;
        this.conversionGroup = conversionGroup;
    }

    public List<MediaInfo> loadMediaInfo() {
        logger.info("Loading media info");
        try {
            FFprobe ffprobe = new FFprobe(Platform.FFPROBE);
            logger.info("FFprobe created: " + ffprobe.getPath());
            List<MediaInfo> media = new ArrayList<>();
            ResourceBundle bundle = AudiobookConverter.getBundle();
            for (String fileName : fileNames) {
                Future<MediaInfo> futureLoad = mediaExecutor.submit(new MediaInfoLoader(ffprobe, fileName, conversionGroup, bundle));
                logger.info("MediaLoader submitted for file:" + fileName);
                MediaInfo mediaInfo = new MediaInfoProxy(fileName, futureLoad);
                media.add(mediaInfo);
            }
            // listing folders and decoding images is slow: keep it off the caller (the JavaFX thread)
            posterExecutor.submit(() -> {
                try {
                    searchForPosters(media, conversionGroup);
                } catch (Exception e) {
                    logger.error("Failed to search for posters", e);
                }
            });
            return media;
        } catch (Exception e) {
            logger.error("Error during loading media info", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    /**
     * Images in the folder and its direct subfolders (e.g. "Scans"), the folder's own first, extensions in any case (COVER.JPG).
     * Not deeper, so a file in a library root does not walk the whole library.
     */
    // ponytail: from a library root, the top-level covers of each book folder are still candidates; depth 1 if that bites
    static List<File> findPictures(File dir) {
        try (Stream<Path> paths = Files.walk(dir.toPath(), 2)) {
            return paths.filter(Files::isRegularFile)
                    .filter(p -> ArrayUtils.contains(ArtWork.IMAGE_EXTENSIONS, FilenameUtils.getExtension(p.toString()).toLowerCase()))
                    .sorted(Comparator.comparingInt(Path::getNameCount).thenComparing(Comparator.naturalOrder()))
                    .map(Path::toFile)
                    .toList();
        } catch (Exception e) {
            logger.error("Failed to search for images in dir:" + dir, e);
            return Collections.emptyList();
        }
    }

    static void searchForPosters(List<MediaInfo> media, ConversionGroup conversionGroup) {
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
                try (var imageStream = new FileInputStream(pictures.get(i).getPath())) {
                    ArtWork poster = new ArtWorkImage(new Image(imageStream));
                    // the book may have been started or cleared meanwhile: its posters must not go to the next book
                    javafx.application.Platform.runLater(() -> {
                        if (!conversionGroup.isDetached()) context.addPosterIfMissing(poster);
                    });
                } catch (IOException | IllegalArgumentException e) {
                    logger.error("Failed to add poster:", e);
//                    throw new RuntimeException(e);
                }
            }
        }
    }


}

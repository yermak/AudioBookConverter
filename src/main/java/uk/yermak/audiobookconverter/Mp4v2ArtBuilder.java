package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Yermak on 04-Jan-18.
 */
public class Mp4v2ArtBuilder {

    private List<MediaInfo> media;
    private final String outputFileName;
    private static final String MP4ART = new File("external/x64/mp4art.exe").getAbsolutePath();

    public Mp4v2ArtBuilder(List<MediaInfo> media, String outputFileName, long jobId) {
        this.media = media;
        this.outputFileName = outputFileName;
    }

    private Collection<File> findPictures(File dir) {
        return FileUtils.listFiles(dir, new String[]{"jpg", "jpeg", "png", "bmp"}, true);
    }


    public void coverArt() throws IOException, ExecutionException, InterruptedException {
        Process artProcess = null;
        Map<Long, String> posters = new HashMap<>();
        Set<String> tempPosters = new HashSet<>();

        Set<File> searchDirs = new HashSet<>();
        media.forEach(mi -> searchDirs.add(new File(mi.getFileName()).getParentFile()));

        searchDirs.forEach(d -> findPictures(d).forEach(f -> {
            try {
                long crc32 = FileUtils.checksumCRC32(f);
                posters.putIfAbsent(crc32, f.getPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        media.forEach(m -> {
            if (m.getArtWork() != null) {
                posters.putIfAbsent(m.getArtWork().getCrc32(), m.getArtWork().getFileName());
                tempPosters.add(m.getArtWork().getFileName());
            }
        });

        try {
            int i = 0;
            for (String poster : posters.values()) {
                ProcessBuilder artProcessBuilder = new ProcessBuilder(MP4ART,
                        "--art-index", String.valueOf(i++),
                        "--add", poster,
                        outputFileName);

                artProcessBuilder.redirectErrorStream();
                artProcess = artProcessBuilder.start();

                StreamCopier artToOut = new StreamCopier(artProcess.getInputStream(), System.out);
                Future<Long> artFuture = Executors.newWorkStealingPool().submit(artToOut);
                artFuture.get();
            }
        } finally {
            for (String tempPoster : tempPosters) {
                FileUtils.deleteQuietly(new File(tempPoster));
            }
            if (artProcess != null) artProcess.destroy();
        }
    }
}

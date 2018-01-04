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
    private long jobId;

    public Mp4v2ArtBuilder(List<MediaInfo> media, String outputFileName, long jobId) {
        this.media = media;
        this.outputFileName = outputFileName;
        this.jobId = jobId;
    }

    private Collection<File> findPictures(File dir) {
        Collection<File> files = FileUtils.listFiles(dir, new String[]{"jpg", "jpeg", "png", "bmp"}, true);
        return files;
    }


    public void coverArt() throws IOException, ExecutionException, InterruptedException {
        Process pictureProcess = null;
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

        try {
            for (MediaInfo mediaInfo : media) {
                String pictureFormat = mediaInfo.getPictureFormat();
                if (pictureFormat != null) {
                    String poster = Utils.getTmp(jobId, mediaInfo.hashCode(), "." + pictureFormat);
                    ProcessBuilder pictureProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe",
                            "-i", mediaInfo.getFileName(),
                            poster);
                    pictureProcessBuilder.redirectErrorStream();
                    pictureProcess = pictureProcessBuilder.start();

                    StreamCopier pictureToOut = new StreamCopier(pictureProcess.getInputStream(), System.out);
                    Future<Long> pictureFuture = Executors.newWorkStealingPool().submit(pictureToOut);
                    pictureFuture.get();
                    File posterFile = new File(poster);
                    long crc32 = FileUtils.checksumCRC32(posterFile);
                    posters.putIfAbsent(crc32, poster);
                    tempPosters.add(poster);
                }
            }

            int i = 0;
            for (String poster : posters.values()) {
                ProcessBuilder artProcessBuilder = new ProcessBuilder("external/x64/mp4art.exe",
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
            if (pictureProcess != null) pictureProcess.destroy();
            if (artProcess != null) artProcess.destroy();
        }
    }
}

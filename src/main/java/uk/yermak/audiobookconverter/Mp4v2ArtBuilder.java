package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Yermak on 04-Jan-18.
 */
public class Mp4v2ArtBuilder implements StateListener {

    private final ExecutorService executorService = Executors.newWorkStealingPool();
    private static final String MP4ART = new File("external/x64/mp4art.exe").getAbsolutePath();
    private boolean cancelled;

    public Mp4v2ArtBuilder() {
        StateDispatcher.getInstance().addListener(this);
    }

    private Collection<File> findPictures(File dir) {
        return FileUtils.listFiles(dir, new String[]{"jpg", "jpeg", "png", "bmp"}, true);
    }


    public void coverArt(List<MediaInfo> media, String outputFileName) throws IOException, ExecutionException, InterruptedException {
        Map<Long, String> posters = new HashMap<>();
        Set<String> tempPosters = new HashSet<>();

        searchForPosters(media, posters, tempPosters);

        try {
            int i = 0;
            for (String poster : posters.values()) {
                if (cancelled) break;
                updateSinglePoster(poster, i++, outputFileName);
            }
        } finally {
            for (String tempPoster : tempPosters) {
                FileUtils.deleteQuietly(new File(tempPoster));
            }
        }
    }

    private void searchForPosters(List<MediaInfo> media, Map<Long, String> posters, Set<String> tempPosters) {
        Set<File> searchDirs = new HashSet<>();
        media.forEach(mi -> searchDirs.add(new File(mi.getFileName()).getParentFile()));

        searchDirs.forEach(d -> findPictures(d).forEach(f -> posters.putIfAbsent(Utils.checksumCRC32(f), f.getPath())));

        media.forEach(m -> {
            if (m.getArtWork() != null) {
                posters.putIfAbsent(m.getArtWork().getCrc32(), m.getArtWork().getFileName());
                tempPosters.add(m.getArtWork().getFileName());
            }
        });
    }

    public void updateSinglePoster(String poster, int index, String outputFileName) throws IOException, ExecutionException, InterruptedException {
        Process artProcess = null;
        try {
            ProcessBuilder artProcessBuilder = new ProcessBuilder(MP4ART,
                    "--art-index", String.valueOf(index),
                    "--add", poster,
                    outputFileName);

            artProcessBuilder.redirectErrorStream();
            artProcess = artProcessBuilder.start();

            StreamCopier artToOut = new StreamCopier(artProcess.getInputStream(), System.out);
            Future<Long> artFuture = executorService.submit(artToOut);
            artFuture.get();
        } finally {
            Utils.closeSilently(artProcess);
        }
    }

    @Override
    public void finishedWithError(String error) {

    }

    @Override
    public void finished() {

    }

    @Override
    public void canceled() {
        cancelled = true;
        Utils.closeSilently(executorService);
    }

    @Override
    public void paused() {
    }

    @Override
    public void resumed() {
    }

    @Override
    public void fileListChanged() {

    }

    @Override
    public void modeChanged(ConversionMode mode) {

    }
}

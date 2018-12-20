package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ParallelConversionStrategy implements ConversionStrategy {

    private final StatusChangeListener listener;
    private static ExecutorService executorService = Executors.newWorkStealingPool();
    private Conversion conversion;
    private Map<String, ProgressCallback> progressCallbacks;


    public ParallelConversionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks) {
        this.conversion = conversion;
        this.progressCallbacks = progressCallbacks;
        listener = new StatusChangeListener();
        conversion.addStatusChangeListener(listener);
    }

    public void run() {
        List<Future<ConverterOutput>> futures = new ArrayList<>();
        long jobId = System.currentTimeMillis();

        String tempFile = Utils.getTmp(jobId, 999999, ".m4b");

        File fileListFile = null;
        File metaFile = null;
        try {
//            MediaInfo maxMedia = maximiseEncodingParameters();
            conversion.getOutputParameters().updateAuto(conversion.getMedia());


            fileListFile = prepareFiles(jobId);
            metaFile = MetadataBuilder.prepareMeta(jobId, conversion.getBookInfo(), conversion.getMedia());

            List<MediaInfo> prioritizedMedia = prioritiseMedia();
            for (MediaInfo mediaInfo : prioritizedMedia) {
                String tempOutput = getTempFileName(jobId, mediaInfo.hashCode(), ".m4b");
                ProgressCallback callback = progressCallbacks.get(mediaInfo.getFileName());
                Future<ConverterOutput> converterFuture = executorService.submit(new FFMpegConverter(conversion, conversion.getOutputParameters(), mediaInfo, tempOutput, callback));
                futures.add(converterFuture);
            }

            for (Future<ConverterOutput> future : futures) {
                if (listener.isCancelled()) return;
                future.get();
            }
            if (listener.isCancelled()) return;
            Concatenator concatenator = new FFMpegConcatenator(conversion, tempFile, metaFile.getAbsolutePath(), fileListFile.getAbsolutePath(), progressCallbacks.get("output"));
            concatenator.concat();

            if (listener.isCancelled()) return;
            Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder(conversion);
            artBuilder.coverArt(conversion.getMedia(), tempFile);

            if (listener.isCancelled()) return;
            FileUtils.moveFile(new File(tempFile), new File(conversion.getOutputDestination()));
            conversion.finished();
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            conversion.error(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            for (MediaInfo mediaInfo : conversion.getMedia()) {
                FileUtils.deleteQuietly(new File(getTempFileName(jobId, mediaInfo.hashCode(), ".m4b")));
            }
            FileUtils.deleteQuietly(metaFile);
            FileUtils.deleteQuietly(fileListFile);
            conversion.removeStatusChangeListener(listener);
        }
    }

    private List<MediaInfo> prioritiseMedia() {
        List<MediaInfo> sortedMedia = new ArrayList<>(conversion.getMedia().size());

        for (MediaInfo mediaInfo : conversion.getMedia()) {
            sortedMedia.add(mediaInfo);
//            mediaInfo.setFrequency(maxMedia.getFrequency());
//            mediaInfo.setChannels(maxMedia.getChannels());
//            mediaInfo.setBitrate(maxMedia.getBitrate());
        }
        Collections.sort(sortedMedia, (o1, o2) -> (int) (o2.getDuration() - o1.getDuration()));
        return sortedMedia;
    }


    protected String getTempFileName(long jobId, int index, String extension) {
        return Utils.getTmp(jobId, index, extension);
    }

    protected File prepareFiles(long jobId) throws IOException {
        File fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt");
        List<String> outFiles = conversion.getMedia().stream().map(mediaInfo -> "file '" + getTempFileName(jobId, mediaInfo.hashCode(), ".m4b") + "'").collect(Collectors.toList());
        FileUtils.writeLines(fileListFile, "UTF-8", outFiles);
        return fileListFile;
    }
}

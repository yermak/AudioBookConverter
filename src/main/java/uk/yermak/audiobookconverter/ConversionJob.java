package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ConversionJob implements Runnable {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static ExecutorService executorService = Executors.newWorkStealingPool();
    private ConversionGroup conversionGroup;
    private Map<String, ProgressCallback> progressCallbacks;
    private String outputDestination;


    public ConversionJob(ConversionGroup conversionGroup, Map<String, ProgressCallback> progressCallbacks, String outputDestination) {
        this.conversionGroup = conversionGroup;
        this.progressCallbacks = progressCallbacks;
        this.outputDestination = outputDestination;
    }

    public void run() {
        List<Future<String>> futures = new ArrayList<>();
        long jobId = System.currentTimeMillis();

        String tempFile = Utils.getTmp(jobId, outputDestination.hashCode(), conversionGroup.getWorkfileExtension());

        File fileListFile = null;
        File metaFile = null;
        try {
//            conversion.getOutputParameters().updateAuto(conversion.getMedia());

            fileListFile = prepareFiles(jobId);


            List<MediaInfo> prioritizedMedia = prioritiseMedia();

            for (MediaInfo mediaInfo : prioritizedMedia) {
                String tempOutput = Utils.getTmp(jobId, mediaInfo.hashCode() + mediaInfo.getDuration(), conversionGroup.getWorkfileExtension());
                ProgressCallback callback = progressCallbacks.get(mediaInfo.getFileName() + "-" + mediaInfo.getDuration());
                Future<String> converterFuture = executorService.submit(new FFMpegNativeConverter(conversionGroup, mediaInfo, tempOutput, callback));
                futures.add(converterFuture);
            }

            for (Future<String> future : futures) {
                if (conversionGroup.getStatus().isOver()) return;
                String outputFileName = future.get();
                logger.debug("Waited for completion of {}", outputFileName);
            }
            if (conversionGroup.getStatus().isOver()) return;
            metaFile = new MetadataBuilder().prepareMeta(jobId, conversionGroup.getBookInfo(), conversionGroup.getConverable());
            FFMpegConcatenator concatenator = new FFMpegConcatenator(conversionGroup, tempFile, metaFile.getAbsolutePath(), fileListFile.getAbsolutePath(), progressCallbacks.get("output"));
            concatenator.concat();

            if (conversionGroup.getStatus().isOver()) return;
            Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder(conversionGroup);
            artBuilder.coverArt(tempFile);

            if (conversionGroup.getStatus().isOver()) return;
            File destFile = new File(outputDestination);
            if (destFile.exists()) FileUtils.deleteQuietly(destFile);
            FileUtils.moveFile(new File(tempFile), destFile);
            conversionGroup.finished();
        } catch (Exception e) {
            logger.error("Error during parallel conversion", e);
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            conversionGroup.error(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            for (MediaInfo mediaInfo : conversionGroup.getMedia()) {
                FileUtils.deleteQuietly(new File(Utils.getTmp(jobId, mediaInfo.hashCode()+mediaInfo.getDuration(), conversionGroup.getWorkfileExtension())));
            }
            FileUtils.deleteQuietly(metaFile);
            FileUtils.deleteQuietly(fileListFile);
        }
    }

    private List<MediaInfo> prioritiseMedia() {
        return conversionGroup.getMedia().stream().sorted((o1, o2) -> (int) (o2.getDuration() - o1.getDuration())).collect(Collectors.toList());
    }


    protected File prepareFiles(long jobId) throws IOException {
        File fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt");
        List<String> outFiles = conversionGroup.getMedia().stream().map(mediaInfo -> "file '" + Utils.getTmp(jobId, mediaInfo.hashCode() + mediaInfo.getDuration(), conversionGroup.getWorkfileExtension()) + "'").collect(Collectors.toList());
        FileUtils.writeLines(fileListFile, "UTF-8", outFiles);
        return fileListFile;
    }
}

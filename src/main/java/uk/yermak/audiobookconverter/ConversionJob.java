package uk.yermak.audiobookconverter;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.book.Convertable;
import uk.yermak.audiobookconverter.book.MediaInfo;

import java.io.File;
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

import static uk.yermak.audiobookconverter.ProgressStatus.*;

public class ConversionJob implements Runnable {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final ExecutorService executorService = Executors.newWorkStealingPool();
    private final ConversionGroup conversionGroup;
    private final Convertable convertable;
    private final Map<String, ProgressCallback> progressCallbacks;
    private final String outputDestination;
    private final SimpleObjectProperty<ProgressStatus> status = new SimpleObjectProperty<>(this, "status", READY);


    public ConversionJob(ConversionGroup conversionGroup, Convertable convertable, Map<String, ProgressCallback> progressCallbacks, String outputDestination) {
        this.conversionGroup = conversionGroup;
        this.convertable = convertable;
        this.progressCallbacks = progressCallbacks;
        this.outputDestination = outputDestination;

        addStatusChangeListener((observable, oldValue, newValue) -> {
            if (FINISHED.equals(newValue)) {
                Platform.runLater(() -> AudiobookConverter.showNotification(outputDestination));
            }
        });

    }

    public void run() {
        status.set(IN_PROGRESS);

        List<Future<String>> futures = new ArrayList<>();

        String tempFile = Utils.getTmp(conversionGroup.getGroupId(), outputDestination.hashCode(), conversionGroup.getWorkfileExtension());

        try {
            List<MediaInfo> prioritizedMedia = prioritiseMedia();

            for (MediaInfo mediaInfo : prioritizedMedia) {
                String tempOutput = Utils.getTmp(conversionGroup.getGroupId(), mediaInfo.getUID(), conversionGroup.getWorkfileExtension());
                ProgressCallback callback = progressCallbacks.get(mediaInfo.getFileName() + "-" + mediaInfo.getDuration());
                Future<String> converterFuture = executorService.submit(new FFMpegNativeConverter(this, mediaInfo, tempOutput, callback));
                futures.add(converterFuture);
            }

            for (Future<String> future : futures) {
                if (status.get().isOver()) return;
                String outputFileName = future.get();
                logger.debug("Waited for completion of {}", outputFileName);
            }
            if (status.get().isOver()) return;

            FFMpegConcatenator concatenator = new FFMpegConcatenator(this, tempFile, convertable.getMedia(), progressCallbacks.get("output"));
            concatenator.concat();

            if (status.get().isOver()) return;

            if (conversionGroup.getOutputParameters().getFormat().mp4Compatible()) {
                Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder(this, progressCallbacks.get("output"));
                artBuilder.coverArt(tempFile);
            }

            if (status.get().isOver()) return;

            File destFile = new File(outputDestination);

            if (getConversionGroup().getOutputParameters().getFormat().mp4Compatible()) {
                try {
                    String optimisedOutput = new FFMpegOptimizer(this, tempFile, outputDestination, progressCallbacks.get("output")).optimize();
                    if (destFile.exists()) FileUtils.deleteQuietly(destFile);
                    FileUtils.moveFile(new File(optimisedOutput), destFile);
                } finally {
                    FileUtils.deleteQuietly(new File(tempFile));
                }
            } else {
                if (destFile.exists()) FileUtils.deleteQuietly(destFile);
                FileUtils.moveFile(new File(tempFile), destFile);
            }
            finished();
        } catch (Exception e) {
            logger.error("Error during parallel conversion", e);
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            error(e.getMessage() + "; " + sw.getBuffer().toString());
        }
    }

    private List<MediaInfo> prioritiseMedia() {
        return convertable.getMedia().stream().sorted((o1, o2) -> (int) (o2.getDuration() - o1.getDuration())).collect(Collectors.toList());
    }


    public void addStatusChangeListener(ChangeListener<ProgressStatus> listener) {
        status.addListener(listener);
    }

    public void pause() {
        if (status.get().equals(IN_PROGRESS)) {
            status.set(PAUSED);
        }
    }

    public void stop() {
        if (!status.get().equals(FINISHED)) {
            status.set(CANCELLED);
        }
    }

    public ProgressStatus getStatus() {
        return status.get();
    }


    public void finished() {
        status.set(FINISHED);
    }

    public void error(String message) {
        status.set(ERROR);
    }

    public void resume() {
        if (status.get().equals(PAUSED)) {
            status.set(IN_PROGRESS);
        }
    }

    public ConversionGroup getConversionGroup() {
        return conversionGroup;
    }

    public String getOutputDestination() {
        return outputDestination;
    }

    public Convertable getConvertable() {
        return convertable;
    }
}

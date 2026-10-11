package uk.yermak.audiobookconverter;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.book.Convertable;
import uk.yermak.audiobookconverter.book.MediaInfo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static uk.yermak.audiobookconverter.ProgressStatus.*;

public class ConversionJob implements Runnable {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int PARALLELISM = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService executorService = Executors.newWorkStealingPool(PARALLELISM);
    // one permit per pool thread, shared by all jobs: a file is submitted only when it can start at once,
    // so a paused job waits on its own thread and never holds a pool thread
    static final Semaphore POOL_SLOTS = new Semaphore(PARALLELISM);
    private static final AtomicLong TEMP_IDS = new AtomicLong();
    private final ConversionGroup conversionGroup;
    private final Convertable convertable;
    private final Map<String, ProgressCallback> progressCallbacks;
    private final String outputDestination;
    private final SimpleObjectProperty<ProgressStatus> status = new SimpleObjectProperty<>(this, "status", READY);
    private volatile String errorMessage;


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
            List<MediaInfo> media = convertable.getMedia();
            // a temp file per entry, not per file name: the same file can be added twice
            List<String> tempOutputs = media.stream()
                    .map(m -> Utils.getTmp(conversionGroup.getGroupId(), TEMP_IDS.incrementAndGet(), conversionGroup.getWorkfileExtension()))
                    .toList();
            boolean reencode = conversionGroup.getOutputParameters().needReencode(media);

            for (int i : prioritiseMedia(media)) {
                MediaInfo mediaInfo = media.get(i);
                ProgressCallback callback = progressCallbacks.get(mediaInfo.getFileName() + "-" + mediaInfo.getDuration());
                FFMpegNativeConverter converter = new FFMpegNativeConverter(this, mediaInfo, tempOutputs.get(i), callback, reencode);
                if (!acquireSlot(status::get)) return;
                try {
                    futures.add(executorService.submit(() -> {
                        try {
                            return converter.call();
                        } finally {
                            POOL_SLOTS.release();
                        }
                    }));
                } catch (RuntimeException e) {
                    POOL_SLOTS.release();
                    throw e;
                }
            }

            for (Future<String> future : futures) {
                if (status.get().isOver()) return;
                String outputFileName = future.get();
                logger.debug("Waited for completion of {}", outputFileName);
            }
            if (status.get().isOver()) return;

            FFMpegConcatenator concatenator = new FFMpegConcatenator(this, tempFile, tempOutputs, progressCallbacks.get("output"));
            concatenator.concat();

            if (status.get().isOver()) return;

            if (conversionGroup.getOutputParameters().getFormat().mp4Compatible()) {
                Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder(this, progressCallbacks.get("output"));
                artBuilder.coverArt(tempFile);
            }

            if (status.get().isOver()) return;

            String result = tempFile;
            try {
                if (getConversionGroup().getOutputParameters().getFormat().mp4Compatible()) {
                    result = new FFMpegOptimizer(this, tempFile, outputDestination, progressCallbacks.get("output"), AudiobookConverter.getBundle()).optimize();
                    if (result == null) return;
                }
                // throws, with the reason, if the destination is locked (e.g. open in a player)
                Files.move(Path.of(result), Path.of(outputDestination), StandardCopyOption.REPLACE_EXISTING);
            } finally {
                // no-ops after a successful move; otherwise the audiobook-sized temp files stay in %TEMP%
                FileUtils.deleteQuietly(new File(tempFile));
                if (result != null) FileUtils.deleteQuietly(new File(result));
            }
            finished();
        } catch (Exception e) {
            logger.error("Error during parallel conversion", e);
            error(ExceptionUtils.getRootCauseMessage(e)); // full stack trace is in the log
        }
    }

    /**
     * Waits, on the job's own thread, until the job is not paused and a pool thread is free.
     * @return true with a permit taken (release it when the task ends), false without one if the job is over
     */
    static boolean acquireSlot(Supplier<ProgressStatus> status) throws InterruptedException {
        while (true) {
            while (PAUSED.equals(status.get())) Thread.sleep(500);
            if (status.get().isOver()) return false;
            if (POOL_SLOTS.tryAcquire(500, TimeUnit.MILLISECONDS)) {
                if (!PAUSED.equals(status.get()) && !status.get().isOver()) return true;
                POOL_SLOTS.release(); // paused or stopped while waiting
            }
        }
    }

    /** Indexes into media, longest first. */
    private static List<Integer> prioritiseMedia(List<MediaInfo> media) {
        return IntStream.range(0, media.size()).boxed()
                .sorted(Comparator.comparingLong((Integer i) -> media.get(i).getDuration()).reversed())
                .toList();
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
        errorMessage = message; // before the status change, so its listeners can show it
        status.set(ERROR);
    }

    public String getErrorMessage() {
        return errorMessage;
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

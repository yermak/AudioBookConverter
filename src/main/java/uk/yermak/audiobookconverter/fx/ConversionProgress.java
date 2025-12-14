package uk.yermak.audiobookconverter.fx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.ConversionJob;
import uk.yermak.audiobookconverter.ProgressStatus;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by yermak on 08-Feb-18.
 */
public class ConversionProgress implements Runnable {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    SimpleLongProperty elapsed = new SimpleLongProperty();
    SimpleLongProperty remaining = new SimpleLongProperty();
    SimpleLongProperty size = new SimpleLongProperty();
    SimpleStringProperty filesCount = new SimpleStringProperty();
    SimpleDoubleProperty progress = new SimpleDoubleProperty();
    SimpleStringProperty state = new SimpleStringProperty("");

    private long startTime;
    private boolean finished;
    private final ConversionJob conversionJob;
    private final int totalFiles;
    private int completedFiles;
    private final long totalDuration;
    private final Map<String, Long> durations = new HashMap<>();
    private final Map<String, Long> sizes = new HashMap<>();
    private boolean paused;
    private boolean cancelled;
    private long pausePeriod;
    private long pauseTime;

    public ConversionProgress(ConversionJob conversionJob) {
        this.conversionJob = conversionJob;
        this.totalFiles = conversionJob.getConvertable().getMedia().size();
        this.totalDuration = conversionJob.getConvertable().getDuration();
        ResourceBundle resources = AudiobookConverter.getBundle();
        conversionJob.addStatusChangeListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case CANCELLED:
                    cancelled();
                    break;
                case PAUSED:
                    paused();
                    break;
                case IN_PROGRESS:
                    if (oldValue.equals(ProgressStatus.PAUSED)) {
                        resumed();
                    }
                    break;
                case FINISHED:
                    finished();
                    break;
                case ERROR:
                    error();
                    break;
            }
        });
        setState(resources.getString("progress.state.converting"));
    }


    public void run() {
        startTime = System.currentTimeMillis();

        filesCount.set(completedFiles + "/" + totalFiles);
        progress.set(0);
        remaining.set(10 * 60 * 1000);

        while (!finished && !cancelled) {
            if (!paused) {
                elapsed.set(System.currentTimeMillis() - startTime - pausePeriod);
            }
            silentSleep();
        }
    }

    private void silentSleep() {
        try {
            Thread.sleep(1000);
//            Thread.yield();
        } catch (InterruptedException e) {
        }
    }

    public synchronized void converted(String fileName, long timeInMillis, long size) {
        if (paused || cancelled) return;
        long currentDuration;
        durations.put(fileName, timeInMillis);

        currentDuration = durations.values().stream().mapToLong(d -> d).sum();

        sizes.put(fileName, size);
        long estimatedSize = sizes.values().stream().mapToLong(l -> l).sum();

        if (currentDuration > 0 && totalDuration > 0) {
            double progress = (double) currentDuration / totalDuration;
            long delta = System.currentTimeMillis() - pausePeriod - startTime;
            long remainingTime = ((long) (delta / progress)) - delta + 1000;
            this.progress.set(progress);
            this.remaining.set(remainingTime);
            this.size.set((long) (estimatedSize / progress));
        }
    }

    public synchronized void incCompleted(String fileName) {
        logger.debug("Completed conversion of file: {}", fileName);
        completedFiles++;
        if (paused || cancelled) return;
        if (completedFiles == totalFiles) {
            ResourceBundle resources = AudiobookConverter.getBundle();
            setState(resources.getString("progress.state.merging"));
            progress.set(1.0);
        }
        filesCount.set(completedFiles + "/" + totalFiles);
    }

    private void finished() {
        finished = true;
        ResourceBundle resources = AudiobookConverter.getBundle();
        setState(resources.getString("progress.state.completed"));
    }
    private void error() {
        finished = true;
        ResourceBundle resources = AudiobookConverter.getBundle();
        setState(resources.getString("progress.state.error"));
    }

    private void cancelled() {
        cancelled = true;
        durations.clear();
        sizes.clear();
        progress.set(0);
        remaining.set(0);
        elapsed.set(0);
        size.set(-1);
        ResourceBundle resources = AudiobookConverter.getBundle();
        setState(resources.getString("progress.state.cancelled"));
    }

    private void paused() {
        paused = true;
        pauseTime = System.currentTimeMillis();
        ResourceBundle resources = AudiobookConverter.getBundle();
        setState(resources.getString("progress.state.paused"));
    }

    private void resumed() {
        paused = false;
        pausePeriod += System.currentTimeMillis() - pauseTime;
        ResourceBundle resources = AudiobookConverter.getBundle();
        setState(resources.getString("progress.state.converting"));
    }

    public void reset() {
        durations.clear();
        sizes.clear();
        progress.set(0);
        remaining.set(60 * 1000);
    }

    public ConversionJob getConversionJob() {
        return conversionJob;
    }

    public void setState(String message) {
        state.set(message);
    }
}


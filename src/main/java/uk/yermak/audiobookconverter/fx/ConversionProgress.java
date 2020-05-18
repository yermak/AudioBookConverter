package uk.yermak.audiobookconverter.fx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.ConversionGroup;
import uk.yermak.audiobookconverter.ProgressStatus;
import uk.yermak.audiobookconverter.Refreshable;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by yermak on 08-Feb-18.
 */
public class ConversionProgress implements Runnable, Refreshable {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    //TODO move to Conversion class
    String fileName;

    SimpleLongProperty elapsed = new SimpleLongProperty();
    SimpleLongProperty remaining = new SimpleLongProperty();
    SimpleLongProperty size = new SimpleLongProperty();
    SimpleStringProperty filesCount = new SimpleStringProperty();
    SimpleDoubleProperty progress = new SimpleDoubleProperty();
    SimpleStringProperty state = new SimpleStringProperty("");

    private long startTime;
    private boolean finished;
    private ConversionGroup conversionGroup;
    private int totalFiles;
    private int completedFiles;
    private long totalDuration;
    private Map<String, Long> durations = new HashMap<>();
    private Map<String, Long> sizes = new HashMap<>();
    private boolean paused;
    private boolean cancelled;
    private long pausePeriod;
    private long pauseTime;

    public ConversionProgress(ConversionGroup conversionGroup, int totalFiles, long totalDuration, String fileName) {
        this.conversionGroup = conversionGroup;
        this.totalFiles = totalFiles;
        this.totalDuration = totalDuration;
        this.fileName = fileName;
        conversionGroup.addStatusChangeListener((observable, oldValue, newValue) -> {
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
    }


    public void run() {
        state.set("Converting...");
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
            this.size.set((int) (estimatedSize / progress));
        }
    }

    public synchronized void incCompleted(String fileName) {
        logger.debug("Completed conversion of file: {}", fileName);
        completedFiles++;
        if (paused || cancelled) return;
        if (completedFiles == totalFiles) {
            state.set("Merging chapters...");
            progress.set(1.0);
        }
        filesCount.set(completedFiles + "/" + totalFiles);
    }

    private void finished() {
        finished = true;
        state.set("Completed!");
    }
    private void error() {
        finished = true;
        state.set("Error!");
    }

    private void cancelled() {
        cancelled = true;
        durations.clear();
        sizes.clear();
        progress.set(0);
        remaining.set(0);
        elapsed.set(0);
        size.set(-1);
        state.set("Cancelled");
    }

    private void paused() {
        paused = true;
        pauseTime = System.currentTimeMillis();
        state.set("Paused");
    }

    private void resumed() {
        paused = false;
        pausePeriod += System.currentTimeMillis() - pauseTime;
        state.set("Converting...");
    }

    public void reset() {
        durations.clear();
        sizes.clear();
        progress.set(0);
        remaining.set(60 * 1000);
    }

    public ConversionGroup getConversionGroup() {
        return conversionGroup;
    }

}

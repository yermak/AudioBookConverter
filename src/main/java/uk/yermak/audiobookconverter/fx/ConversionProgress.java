package uk.yermak.audiobookconverter.fx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import uk.yermak.audiobookconverter.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yermak on 08-Feb-18.
 */
public class ConversionProgress implements Runnable, StateListener, Refreshable {

    SimpleLongProperty elapsed = new SimpleLongProperty();
    SimpleLongProperty remaining = new SimpleLongProperty();
    SimpleLongProperty size = new SimpleLongProperty();
    SimpleObjectProperty<ProgressStatus> state = new SimpleObjectProperty<>(ProgressStatus.STARTED);
    SimpleStringProperty filesCount = new SimpleStringProperty();
    SimpleDoubleProperty progress = new SimpleDoubleProperty();

    private long startTime;
    private boolean finished;
    private int totalFiles;
    private int completedFiles;
    private long totalDuration;
    private Map<String, Long> durations = new HashMap<>();
    private Map<String, Long> sizes = new HashMap<>();
    private boolean paused;
    private boolean cancelled;
    private long pausePeriod;
    private long pauseTime;

    public ConversionProgress(int totalFiles, long totalDuration) {
        this.totalFiles = totalFiles;
        this.totalDuration = totalDuration;
    }


    public void run() {
        startTime = System.currentTimeMillis();
        StateDispatcher.getInstance().addListener(this);
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
            long finalSize = estimatedSize;
            this.progress.set(progress);
            this.remaining.set(remainingTime);
            this.size.set((int) (finalSize / progress));
        }
    }

    public synchronized void incCompleted(String fileName) {
        completedFiles++;
        if (paused || cancelled) return;
        if (completedFiles == totalFiles) {
            state.set(ProgressStatus.COMPLETED);
//            infoText = "Updating media information...";
        }
        filesCount.set(completedFiles + "/" + totalFiles);
    }

    @Override
    public void finishedWithError(String error) {
        finished = true;
    }

    @Override
    public void finished() {
        finished = true;
        resetStats();
    }

    @Override
    public void canceled() {
        cancelled = true;
        durations.clear();
        sizes.clear();
        progress.set(0);
        remaining.set(0);
        elapsed.set(0);
        size.set(-1);
        state.set(ProgressStatus.CANCELLED);
//        infoText = "Conversion was cancelled";
    }

    @Override
    public void paused() {
        paused = true;
        pauseTime = System.currentTimeMillis();
    }

    @Override
    public void resumed() {
        paused = false;
        pausePeriod += System.currentTimeMillis() - pauseTime;
    }

    @Override
    public void fileListChanged() {
        resetStats();
    }

    private void resetStats() {
//        progress.set(0);
//        elapsed.set(0);
//        size.set(-1);
    }

    @Override
    public void modeChanged(ConversionMode mode) {
        resetStats();
    }

    public void reset() {
        durations.clear();
        sizes.clear();
        progress.set(0);
        remaining.set(60 * 1000);
    }
}

package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.Messages;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yermak on 08-Feb-18.
 */
public class ConversionProgress implements Runnable, StateListener, Refreshable {
    SimpleStringProperty info = new SimpleStringProperty();
    SimpleIntegerProperty progress = new SimpleIntegerProperty();
    SimpleStringProperty message = new SimpleStringProperty();


//    private int progress;

    private ProgressStatus status;

    private Refreshable refreshable;

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
    private long elapsedTime;
    private String infoText;
    private long remainingTime;
    private long estimatedFinalOutputSize;

    public ConversionProgress(int totalFiles, long totalDuration) {
        this.totalFiles = totalFiles;
        this.totalDuration = totalDuration;
    }


    public void run() {
        startTime = System.currentTimeMillis();
        StateDispatcher.getInstance().addListener(this);

        infoText = Messages.getString("BatchConversionStrategy.file") + " " + completedFiles + "/" + totalFiles;
        progress.set(0);
        remainingTime = 10 * 60 * 1000;

        while (!finished && !cancelled) {
            if (!paused) {
                elapsedTime = System.currentTimeMillis() - startTime - pausePeriod;
            }
            silentSleep();
        }
    }

    private void silentSleep() {
        try {
            Thread.sleep(1000);
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
            this.progress.set((int) (progress * 100));
            this.remainingTime = remainingTime;
            this.estimatedFinalOutputSize = (long) (finalSize / progress);
        }
    }

    public synchronized void incCompleted(String fileName) {
        completedFiles++;
        if (paused || cancelled) return;
        if (completedFiles != totalFiles) {
            infoText = Messages.getString("BatchConversionStrategy.file") + " " + completedFiles + "/" + totalFiles;
        } else {
            infoText = "Updating media information...";
        }

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
        remainingTime = 0;
        elapsedTime = 0;
        estimatedFinalOutputSize = -1L;
        infoText = "Conversion was cancelled";
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
        progress.set(0);
        elapsedTime = 0;
        estimatedFinalOutputSize = -1L;
    }

    @Override
    public void modeChanged(ConversionMode mode) {
        resetStats();
    }

    public void reset() {
        durations.clear();
        sizes.clear();
        progress.set(0);
        remainingTime = 60 * 1000;
    }
}

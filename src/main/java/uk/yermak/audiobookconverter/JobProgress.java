package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.Messages;
import com.freeipodsoftware.abc.ProgressView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yermak on 03-Jan-18.
 */
public class JobProgress implements Runnable, StateListener, Refreshable {
    private final ConversionStrategy conversionStrategy;
    private ProgressView progressView;
    private final List<MediaInfo> media;
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

    public JobProgress(ConversionStrategy conversionStrategy, ProgressView progressView, List<MediaInfo> media) {
        this.conversionStrategy = conversionStrategy;
        this.progressView = progressView;
        this.media = media;
    }


    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        Map<String, ProgressCallback> progressCallbacks = new HashMap<>();
        for (MediaInfo mediaInfo : media) {
            progressCallbacks.put(mediaInfo.getFileName(), new ProgressCallback(mediaInfo.getFileName(), this));
            totalFiles++;
            totalDuration += mediaInfo.getDuration();
        }
        progressCallbacks.put("output", new ProgressCallback("output", this));
        conversionStrategy.setCallbacks(progressCallbacks);
        StateDispatcher.getInstance().addListener(this);

        progressView.getDisplay().syncExec(() -> {
            progressView.setInfoText(Messages.getString("BatchConversionStrategy.file") + " " + completedFiles + "/" + totalFiles);
            progressView.setProgress(0);
            progressView.setRemainingTime(10 * 60 * 1000);
        });

        while (!finished && !cancelled) {
            if (!paused) {
                progressView.getDisplay().syncExec(() -> progressView.setElapsedTime(System.currentTimeMillis() - startTime - pausePeriod));
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
        int currentDuration = 0;
        durations.put(fileName, timeInMillis);

        for (Long l : durations.values()) {
            currentDuration += l;
        }

        long estimatedSize = 0;
        sizes.put(fileName, size);
        for (Long l : sizes.values()) {
            estimatedSize += l;
        }

        if (currentDuration > 0 && totalDuration > 0) {
            double progress = (double) currentDuration / totalDuration;
            long delta = System.currentTimeMillis() - pausePeriod - startTime;
            long remainingTime = ((long) (delta / progress)) - delta + 1000;
            long finalSize = estimatedSize;
            progressView.getDisplay().syncExec(() -> {
                progressView.setProgress((int) (progress * 100));
                progressView.setRemainingTime(remainingTime);
                progressView.setEstimatedFinalOutputSize((long) (finalSize / progress));
            });
        }

    }

    public synchronized void incCompleted(String fileName) {
        completedFiles++;
        if (paused || cancelled) return;
        if (completedFiles != totalFiles) {
            progressView.getDisplay().syncExec(() -> progressView.setInfoText(Messages.getString("BatchConversionStrategy.file") + " " + completedFiles + "/" + totalFiles));
        } else {
            progressView.getDisplay().syncExec(() -> progressView.setInfoText("Updating media information..."));
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
        progressView.getDisplay().syncExec(() -> {
            progressView.setProgress(0);
            progressView.setRemainingTime(0);
            progressView.setElapsedTime(0);
            progressView.setEstimatedFinalOutputSize(-1L);
            progressView.getDisplay().syncExec(() -> progressView.setInfoText("Conversion was cancelled"));
        });
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
        progressView.getDisplay().syncExec(() -> {
            progressView.setProgress(0);
            progressView.setElapsedTime(0);
            progressView.setEstimatedFinalOutputSize(0);
        });
    }

    @Override
    public void modeChanged(ConversionMode mode) {
        resetStats();
    }

    public void reset() {
        durations.clear();
        sizes.clear();
        progressView.getDisplay().syncExec(() -> {
            progressView.setProgress(0);
            progressView.setRemainingTime(60 * 1000);
        });
    }
}
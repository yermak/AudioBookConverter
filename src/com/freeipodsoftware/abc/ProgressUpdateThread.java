package com.freeipodsoftware.abc;

import com.freeipodsoftware.abc.conversionstrategy.ConversionStrategy;

public class ProgressUpdateThread extends Thread {
    private ConversionStrategy converter;
    private ProgressView progressView;

    public ProgressUpdateThread(ConversionStrategy conversionStrategy, ProgressView progressView) {
        this.converter = conversionStrategy;
        this.progressView = progressView;
    }

    public void run() {
        for (; !this.converter.isFinished(); this.progressView.getDisplay().syncExec(new Runnable() {
            public void run() {
                ProgressUpdateThread.this.progressView.setProgress(ProgressUpdateThread.this.converter.getProgress());
                ProgressUpdateThread.this.progressView.setElapsedTime(ProgressUpdateThread.this.converter.getElapsedTime());
                ProgressUpdateThread.this.progressView.setRemainingTime(ProgressUpdateThread.this.converter.getRemainingTime());
                ProgressUpdateThread.this.progressView.setEstimatedFinalOutputSize(this.calculateEstimatedFinalOutputSize());
                ProgressUpdateThread.this.progressView.setInfoText(ProgressUpdateThread.this.converter.getInfoText());
                ProgressUpdateThread.this.converter.setPaused(ProgressUpdateThread.this.progressView.isPaused());
                if (ProgressUpdateThread.this.progressView.isCanceled()) {
                    ProgressUpdateThread.this.converter.cancel();
                }

            }

            private long calculateEstimatedFinalOutputSize() {
                if (ProgressUpdateThread.this.converter.getProgressForCurrentOutputFile() < 2) {
                    return -1L;
                } else {
                    try {
                        return (long) ((float) ProgressUpdateThread.this.converter.getOutputSize() / (float) ProgressUpdateThread.this.converter.getProgressForCurrentOutputFile() * 100.0F);
                    } catch (RuntimeException var2) {
                        return 0L;
                    }
                }
            }
        })) {
            try {
                Thread.sleep(500L);
            } catch (Exception var2) {
            }
        }

    }
}

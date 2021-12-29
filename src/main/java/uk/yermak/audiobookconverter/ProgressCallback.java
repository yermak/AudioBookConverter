package uk.yermak.audiobookconverter;

import uk.yermak.audiobookconverter.fx.ConversionProgress;

public class ProgressCallback {
    private final String fileName;
    private final ConversionProgress conversionProgress;

    public String fileName() {
        return this.fileName;
    }

    public void converted(final long timeInMillis, final long size) {
        this.conversionProgress.converted(this.fileName(), timeInMillis, size);
    }

    public void completedConversion() {
        this.conversionProgress.incCompleted(this.fileName());
    }

    public void reset() {
        this.conversionProgress.reset();
    }

    public ProgressCallback(final String fileName, final ConversionProgress refreshable) {
        this.fileName = fileName;
        this.conversionProgress = refreshable;
    }

    public void setState(String message) {
        conversionProgress.setState(message);
    }
}

        
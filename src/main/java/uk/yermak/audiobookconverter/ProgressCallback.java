package uk.yermak.audiobookconverter;

public class ProgressCallback {
    private String fileName;
    private Refreshable refreshable;

    public String fileName() {
        return this.fileName;
    }

    public Refreshable refreshable() {
        return this.refreshable;
    }

    public void converted(final long timeInMillis, final long size) {
        this.refreshable().converted(this.fileName(), timeInMillis, size);
    }

    public void completedConversion() {
        this.refreshable().incCompleted(this.fileName());
    }

    public void reset() {
        this.refreshable().reset();
    }

    public ProgressCallback(final String fileName, final Refreshable refreshable) {
        this.fileName = fileName;
        this.refreshable = refreshable;
    }
}

        
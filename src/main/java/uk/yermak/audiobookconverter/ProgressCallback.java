package uk.yermak.audiobookconverter;

/**
 * Created by Yermak on 03-Jan-18.
 */
public class ProgressCallback {

    protected String fileName;
    private Refreshable refreshable;

    public ProgressCallback(String fileName, Refreshable refreshable) {
        this.fileName = fileName;
        this.refreshable = refreshable;
    }

    public void converted(long timeInMillis, long size) {
        refreshable.converted(fileName, timeInMillis, size);
    }


    public void completedConversion() {
        refreshable.incCompleted(fileName);
    }

    public void reset() {
        refreshable.reset();
    }
}

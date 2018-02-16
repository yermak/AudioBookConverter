package uk.yermak.audiobookconverter;

/**
 * Created by yermak on 08-Feb-18.
 */
public class ProgressCallback2 extends ProgressCallback {

    private final ConversionProgress progress;

    public ProgressCallback2(String fileName, ConversionProgress progress) {
        super(fileName, null);
        this.progress = progress;
    }

    @Override
    public void converted(long timeInMillis, long size) {
        progress.converted(fileName, timeInMillis, size);
    }

    @Override
    public void completedConversion() {
        progress.incCompleted(fileName);
    }

    @Override
    public void reset() {
        progress.reset();
    }
}

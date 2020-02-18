package uk.yermak.audiobookconverter;

public interface Refreshable extends Runnable {
    
    void converted(final String fileName, final long timeInMillis, final long size);

    void incCompleted(final String fileName);

    void reset();
}

        
package uk.yermak.audiobookconverter;

/**
 * Created by yermak on 16-Feb-18.
 */
public interface Refreshable extends Runnable{

    void converted(String fileName, long timeInMillis, long size);
    void incCompleted(String fileName);
    void reset();
}

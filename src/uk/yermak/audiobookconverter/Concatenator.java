package uk.yermak.audiobookconverter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Yermak on 29-Dec-17.
 */
public interface Concatenator {
    void concat() throws IOException, ExecutionException, InterruptedException;
}

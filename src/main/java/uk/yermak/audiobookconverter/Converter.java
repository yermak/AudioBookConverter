package uk.yermak.audiobookconverter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Yermak on 27-Dec-17.
 */
public interface Converter {

    @SuppressWarnings("unused")
    ConverterOutput convertMp3toM4a() throws IOException, InterruptedException, ExecutionException;

}

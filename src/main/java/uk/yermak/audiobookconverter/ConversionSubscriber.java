package uk.yermak.audiobookconverter;

/**
 * Created by yermak on 06-Dec-18.
 */
public interface ConversionSubscriber {
    void resetForNewConversion(Conversion conversion);
}

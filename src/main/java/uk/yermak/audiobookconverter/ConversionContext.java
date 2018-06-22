package uk.yermak.audiobookconverter;

import uk.yermak.audiobookconverter.fx.ConversionProgress;

import java.util.LinkedList;

/**
 * Created by yermak on 06-Feb-18.
 */
public class ConversionContext {

    private LinkedList<Conversion> conversionQueue = new LinkedList<>();
    private Conversion conversion = new Conversion();
    private Subscriber subscriber;

    public ConversionContext() {
    }

    public void setMode(ConversionMode mode) {
        conversion.setMode(mode);
    }

    public void setBookInfo(AudioBookInfo bookInfo) {
        conversion.setBookInfo(bookInfo);
    }

    public AudioBookInfo getBookInfo() {
        return conversion.getBookInfo();
    }


    public ConversionMode getMode() {
        return conversion.getMode();
    }

    public void startConversion(String outputDestination, ConversionProgress conversionProgress) {
        subscriber.addConversionProgress(conversionProgress);
        conversion.start(outputDestination, conversionProgress);
    }

    public Conversion getConversion() {
        return conversion;
    }

    public void pauseConversion() {
        conversion.pause();
    }

    public void stopConversion() {
        conversion.stop();
    }

    public void subscribeForStart(Subscriber subscriber) {
        this.subscriber = subscriber;
    }
}

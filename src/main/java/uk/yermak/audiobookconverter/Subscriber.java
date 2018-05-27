package uk.yermak.audiobookconverter;

import uk.yermak.audiobookconverter.fx.ConversionProgress;

public interface Subscriber {
    void addConversionProgress(ConversionProgress conversionProgress);
}

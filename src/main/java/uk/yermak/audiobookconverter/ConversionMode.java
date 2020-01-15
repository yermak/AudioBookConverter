package uk.yermak.audiobookconverter;

import java.util.Map;

/**
 * Created by Yermak on 28-Dec-17.
 */
public enum ConversionMode {

    PARALLEL {
        @Override
        public ConversionStrategy createConvertionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks) {
            return new ParallelConversionStrategy(conversion, progressCallbacks);
        }
    };

    public abstract ConversionStrategy createConvertionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks);
}

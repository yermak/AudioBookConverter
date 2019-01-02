package uk.yermak.audiobookconverter;

import java.util.Map;

/**
 * Created by Yermak on 28-Dec-17.
 */
public enum ConversionMode {
    SINGLE {
        @Override
        public JoiningConversionStrategy createConvertionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks) {
            return new JoiningConversionStrategy(conversion, progressCallbacks);
        }
    }, BATCH {
        @Override
        public ConversionStrategy createConvertionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks) {
            return new BatchConversionStrategy(conversion, progressCallbacks);
        }
    }, PARALLEL {
        @Override
        public ConversionStrategy createConvertionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks) {
            return new ParallelConversionStrategy(conversion, progressCallbacks);
        }
    };

    public abstract ConversionStrategy createConvertionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks);
}

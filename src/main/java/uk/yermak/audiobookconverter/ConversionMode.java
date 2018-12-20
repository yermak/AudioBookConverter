package uk.yermak.audiobookconverter;

import java.util.Map;

/**
 * Created by Yermak on 28-Dec-17.
 */
public enum ConversionMode {
    SINGLE {
        @Override
        public boolean supportTags() {
            return true;
        }

        @Override
        public JoiningConversionStrategy createConvertionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks) {
            return new JoiningConversionStrategy(conversion, progressCallbacks);
        }
    }, BATCH {
        @Override
        public boolean supportTags() {
            return false;
        }

        @Override
        public ConversionStrategy createConvertionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks) {
            return new BatchConversionStrategy(conversion, progressCallbacks);
        }
    }, PARALLEL {
        @Override
        public boolean supportTags() {
            return true;
        }

        @Override
        public ConversionStrategy createConvertionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks) {
            return new ParallelConversionStrategy(conversion, progressCallbacks);
        }
    };

    public abstract boolean supportTags();

    public abstract ConversionStrategy createConvertionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks);
}

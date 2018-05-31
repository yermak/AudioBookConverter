package uk.yermak.audiobookconverter;

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
        public JoiningConversionStrategy createConvertionStrategy() {
            return new JoiningConversionStrategy();
        }
    }, BATCH {
        @Override
        public boolean supportTags() {
            return false;
        }

        @Override
        public ConversionStrategy createConvertionStrategy() {
            return new BatchConversionStrategy();
        }
    }, PARALLEL {
        @Override
        public boolean supportTags() {
            return true;
        }

        @Override
        public ConversionStrategy createConvertionStrategy() {
            return new ParallelConversionStrategy();
        }
    };

    public abstract boolean supportTags();

    public abstract ConversionStrategy createConvertionStrategy();
}

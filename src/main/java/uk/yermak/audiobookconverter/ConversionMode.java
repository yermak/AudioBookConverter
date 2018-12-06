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
        public JoiningConversionStrategy createConvertionStrategy(Conversion conversion) {
            return new JoiningConversionStrategy(conversion);
        }
    }, BATCH {
        @Override
        public boolean supportTags() {
            return false;
        }

        @Override
        public ConversionStrategy createConvertionStrategy(Conversion conversion) {
            return new BatchConversionStrategy(conversion);
        }
    }, PARALLEL {
        @Override
        public boolean supportTags() {
            return true;
        }

        @Override
        public ConversionStrategy createConvertionStrategy(Conversion conversion) {
            return new ParallelConversionStrategy(conversion);
        }
    };

    public abstract boolean supportTags();

    public abstract ConversionStrategy createConvertionStrategy(Conversion conversion);
}

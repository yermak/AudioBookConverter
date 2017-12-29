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
    }, BATCH {
        @Override
        public boolean supportTags() {
            return false;
        }
    }, PARALLEL {
        @Override
        public boolean supportTags() {
            return true;
        }
    };

    public abstract boolean supportTags();
}

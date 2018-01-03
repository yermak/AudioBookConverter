package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.conversionstrategy.BatchConversionStrategy;
import com.freeipodsoftware.abc.conversionstrategy.ConversionStrategy;
import com.freeipodsoftware.abc.conversionstrategy.JoiningConversionStrategy;

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

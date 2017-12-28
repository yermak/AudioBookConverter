package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.conversionstrategy.BatchConversionStrategy;
import com.freeipodsoftware.abc.conversionstrategy.ConversionStrategy;
import com.freeipodsoftware.abc.conversionstrategy.JoiningConversionStrategy;

/**
 * Created by Yermak on 28-Dec-17.
 */
public enum ConversionMode {
    SINGLE, BATCH, PARALLEL;
}

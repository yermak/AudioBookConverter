package com.freeipodsoftware.abc;

import uk.yermak.audiobookconverter.ConversionMode;

public interface StateListener {
    void finishedWithError(String error);

    void finished();

    void canceled();

    void paused();

    void resumed();

    void fileListChanged();

    void modeChanged(ConversionMode mode);

}

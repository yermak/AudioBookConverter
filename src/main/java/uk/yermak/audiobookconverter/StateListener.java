package uk.yermak.audiobookconverter;

public interface StateListener {
    void finishedWithError(String error);

    void finished();

    void canceled();

    void paused();

    void resumed();

    void fileListChanged();

    void modeChanged(ConversionMode mode);

}

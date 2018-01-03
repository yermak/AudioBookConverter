package com.freeipodsoftware.abc;

public interface StateListener {
    void finishedWithError(String error);

    void finished();

    void canceled();

    void paused();

    void resumed();
}

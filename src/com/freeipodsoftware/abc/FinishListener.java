package com.freeipodsoftware.abc;

public interface FinishListener {
    void finishedWithError(String var1);

    void finished();

    void canceled();
}

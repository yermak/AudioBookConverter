package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.FinishListener;
import com.freeipodsoftware.abc.Mp4Tags;
import org.eclipse.swt.widgets.Shell;

public interface ConversionStrategy {
    void setInputFileList(String[] var1);

    void setFinishListener(FinishListener var1);

    boolean makeUserInterview(Shell var1);

    void start(Shell var1);

    void cancel();

    boolean isFinished();

    int getProgress();

    int getProgressForCurrentOutputFile();

    long getElapsedTime();

    long getRemainingTime();

    long getOutputSize();

    String getInfoText();

    boolean supportsTagEditor();

    void setMp4Tags(Mp4Tags var1);

    String getAdditionalFinishedMessage();

    void setPaused(boolean var1);
}

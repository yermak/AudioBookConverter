package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.FinishListener;
import com.freeipodsoftware.abc.Mp4Tags;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.MediaInfo;

import java.util.List;

public interface ConversionStrategy {

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

    void setMp4Tags(Mp4Tags var1);

    String getAdditionalFinishedMessage();

    void setPaused(boolean var1);

    void setMedia(List<MediaInfo> media);
}

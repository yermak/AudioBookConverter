package com.freeipodsoftware.abc.conversionstrategy;

import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.AudioBookInfo;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.ProgressCallback;

import java.util.List;
import java.util.Map;

public interface ConversionStrategy {

    boolean makeUserInterview(Shell shell, String fileName);

    void start(Shell shell);

    void setBookInfo(AudioBookInfo audioBookInfo);

    String getAdditionalFinishedMessage();

    void setMedia(List<MediaInfo> media);

    void setCallbacks(Map<String, ProgressCallback> progressCallbacks);
}

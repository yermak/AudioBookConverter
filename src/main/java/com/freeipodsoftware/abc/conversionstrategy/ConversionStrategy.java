package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.Mp4Tags;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.ProgressCallback;

import java.util.List;
import java.util.Map;

public interface ConversionStrategy {

    boolean makeUserInterview(Shell shell, String fileName);

    void start(Shell var1);

    void setMp4Tags(Mp4Tags var1);

    String getAdditionalFinishedMessage();

    void setMedia(List<MediaInfo> media);

    void setCallbacks(Map<String, ProgressCallback> progressCallbacks);
}

package com.freeipodsoftware.abc.conversionstrategy;

import uk.yermak.audiobookconverter.AudioBookInfo;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.ProgressCallback;

import java.util.List;
import java.util.Map;

public interface ConversionStrategy {

    void start();

    void setBookInfo(AudioBookInfo audioBookInfo);

    String getAdditionalFinishedMessage();

    void setMedia(List<MediaInfo> media);

    void setCallbacks(Map<String, ProgressCallback> progressCallbacks);

    void setOutputDestination(String outputDestination);
}
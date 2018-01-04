package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.Mp4Tags;

/**
 * Created by Yermak on 03-Jan-18.
 */
public interface MediaInfo {
    void setChannels(int channels);

    void setFrequency(int frequency);

    void setBitrate(int bitrate);

    void setDuration(long duration);

    int getChannels();

    int getFrequency();

    int getBitrate();

    long getDuration();

    String getFileName();

    void setMp4Tags(Mp4Tags mp4Tags);

    Mp4Tags getMp4Tags();

    String getPictureFormat();
}

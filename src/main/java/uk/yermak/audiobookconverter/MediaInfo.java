package uk.yermak.audiobookconverter;

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

    void setMp4Tags(AudioBookInfo mp4Tags);

    AudioBookInfo getMp4Tags();

    ArtWork getArtWork();

    void setArtWork(ArtWork artWork);
}

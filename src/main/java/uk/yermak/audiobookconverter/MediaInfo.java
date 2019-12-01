package uk.yermak.audiobookconverter;

public interface MediaInfo {
    void setChannels(final int channels);

    void setFrequency(final int frequency);

    void setBitrate(final int bitrate);

    void setDuration(final long duration);

    int getChannels();

    int getFrequency();

    int getBitrate();

    long getDuration();

    String getFileName();

    void setBookInfo(final AudioBookInfo bookInfo);

    AudioBookInfo getBookInfo();

    ArtWork getArtWork();

    void setArtWork(final ArtWork artWork);

    String getCodec();

    void setCodec(final String codec);
}

        
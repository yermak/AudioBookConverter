package uk.yermak.audiobookconverter;

public interface MediaInfo extends Organisable {

    int getChannels();

    int getFrequency();

    int getBitrate();

    String getFileName();

    AudioBookInfo getBookInfo();

    ArtWork getArtWork();

    String getCodec();

    long getOffset();

    void setDuration(final long duration);

    void setChapter(Chapter chapter);

    public Chapter getChapter();

    public int getUID();
}

        
package uk.yermak.audiobookconverter.book;

public interface MediaInfo extends Organisable {

    int getChannels();

    int getFrequency();

    int getBitrate();

    String getFileName();

    AudioBookInfo getBookInfo();

    String getCodec();

    long getOffset();

    void setDuration(final long duration);

    void setChapter(Chapter chapter);

    Chapter getChapter();

    int getUID();

    String getReference();
}

        
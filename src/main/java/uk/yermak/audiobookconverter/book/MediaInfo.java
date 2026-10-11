package uk.yermak.audiobookconverter.book;

import uk.yermak.audiobookconverter.AudiobookConverter;

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

    /** Speed the output plays at: the UI's while the book is prepared, the job's once frozen at detach. */
    default double getSpeed() {
        return AudiobookConverter.getContext().getSpeed();
    }

    /** A detached job keeps its own speed when the UI speed changes for the next book. */
    default void freezeSpeed(double speed) {
    }
}

        
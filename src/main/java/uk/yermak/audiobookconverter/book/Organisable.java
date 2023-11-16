package uk.yermak.audiobookconverter.book;

import java.util.List;

public interface Organisable extends Countable {

    String getTitle();

    String getDetails();

    long getDuration();

    boolean split();

    void remove();

    void moveUp();

    void moveDown();

    /**
     * @return full list of all inner MediaInfo objects
     */
    List<MediaInfo> getMedia();
}

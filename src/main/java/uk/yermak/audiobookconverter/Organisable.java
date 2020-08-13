package uk.yermak.audiobookconverter;

public interface Organisable {
    String getTitle();

    String getDetails();

    long getDuration();

    boolean split();

    void remove();

    void moveUp();

    void moveDown();
}

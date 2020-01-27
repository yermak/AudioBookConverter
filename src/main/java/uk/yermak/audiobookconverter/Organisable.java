package uk.yermak.audiobookconverter;

public interface Organisable {
    String getTitle();

    String getDetails();

    long getDuration();

    void split();

    void remove();

    void moveUp();

    void moveDown();
}

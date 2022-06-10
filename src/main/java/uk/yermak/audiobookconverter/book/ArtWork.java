package uk.yermak.audiobookconverter.book;

import javafx.scene.image.Image;


public interface ArtWork {
    String[] IMAGE_EXTENSIONS = new String[]{"jpg", "jpeg", "jfif", "png", "bmp"};

    long getCrc32();

    String getFileName();

    boolean matchCrc32(long crc32);

    Image image();
}

        
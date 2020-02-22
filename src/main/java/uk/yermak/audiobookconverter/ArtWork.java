package uk.yermak.audiobookconverter;

public interface ArtWork {
    String[] IMAGE_EXTENSIONS = new String[]{"jpg", "jpeg", "jfif", "png", "bmp"};

    long getCrc32();

    String getFileName();

    boolean matchCrc32(long crc32);
}

        
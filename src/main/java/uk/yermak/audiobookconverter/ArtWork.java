package uk.yermak.audiobookconverter;

public interface ArtWork {
    String[] IMAGE_EXTENSIONS = new String[]{"jpg", "jpeg", "jfif", "png", "bmp"};

    long getCrc32();

    void setCrc32(final long crc32);

    String getFileName();

    void setFileName(final String fileName);

    String getFormat();

    void setFormat(final String format);

    boolean matchCrc32(long crc32);
}

        
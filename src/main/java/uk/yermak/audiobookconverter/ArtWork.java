package uk.yermak.audiobookconverter;

public interface ArtWork {
    long getCrc32();

    void setCrc32(final long crc32);

    String getFileName();

    void setFileName(final String fileName);

    String getFormat();

    void setFormat(final String format);
}

        
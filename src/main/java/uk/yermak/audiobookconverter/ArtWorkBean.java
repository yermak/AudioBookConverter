package uk.yermak.audiobookconverter;

public class ArtWorkBean implements ArtWork {
    private String fileName;
    private String format;
    private long crc32;

    public String fileName() {
        return this.fileName;
    }

    public String format() {
        return this.format;
    }

    public long crc32() {
        return this.crc32;
    }

    public long getCrc32() {
        return this.crc32();
    }

    public void setCrc32(final long crc32) {
        this.crc32 = crc32;
    }

    public String getFileName() {
        return this.fileName();
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getFormat() {
        return this.format();
    }

    public void setFormat(final String format) {
        this.format = format;
    }

    public ArtWorkBean(final String fileName, final String format, final long crc32) {
        this.fileName = fileName;
        this.format = format;
        this.crc32 = crc32;
    }
}

        
package uk.yermak.audiobookconverter;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

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

    @Override
    public boolean matchCrc32(long crc32) {
        return this.crc32 == crc32;
    }

    public ArtWorkBean(final String fileName, final String format, final long crc32) {
        this.fileName = fileName;
        this.format = format;
        this.crc32 = crc32;
    }

    public ArtWorkBean(String fileName) {
        this(fileName, FilenameUtils.getExtension(fileName), Utils.checksumCRC32(new File(fileName)));
    }

}

        
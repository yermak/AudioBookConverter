package uk.yermak.audiobookconverter;

import java.io.File;

public class ArtWorkBean implements ArtWork {
    private String fileName;
    private long crc32;

    public String fileName() {
        return this.fileName;
    }

    public long crc32() {
        return this.crc32;
    }

    public long getCrc32() {
        return this.crc32();
    }

    public String getFileName() {
        return this.fileName();
    }

    @Override
    public boolean matchCrc32(long crc32) {
        return this.crc32 == crc32;
    }

    public ArtWorkBean(final String fileName, final long crc32) {
        this.fileName = fileName;
        this.crc32 = crc32;
    }

    public ArtWorkBean(String fileName) {
        this(fileName, Utils.checksumCRC32(new File(fileName)));
    }

}

        
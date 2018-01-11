package uk.yermak.audiobookconverter;

/**
 * Created by yermak on 1/11/2018.
 */
public class ArtWorkBean implements ArtWork {

    private String fileName;
    private String format;
    private long crc32;

    public ArtWorkBean(String fileName, String format, long crc32) {
        this.fileName = fileName;
        this.format = format;
        this.crc32 = crc32;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public Long getCrc32() {
        return crc32;
    }

    @Override
    public void setCrc32(Long crc32) {
        this.crc32 = crc32;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

package uk.yermak.audiobookconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Future;

public class ArtWorkProxy implements ArtWork {
    private final Future futureLoad;
    private String format;
    private final Logger logger;

    public Future futureLoad() {
        return this.futureLoad;
    }

    public String format() {
        return this.format;
    }

    private Logger logger() {
        return this.logger;
    }

    private ArtWork getArtWork() {
        try {
            return (ArtWork) this.futureLoad().get();
        } catch (Exception e) {
            this.logger().error("Failed to load ArtWork Proxy:", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getFormat() {
        return this.format();
    }

    public void setFormat(final String format) {
        this.getArtWork().setFormat(format);
    }

    @Override
    public boolean matchCrc32(long crc32) {
        return this.getArtWork().matchCrc32(crc32);
    }

    public long getCrc32() {
        return this.getArtWork().getCrc32();
    }

    public void setCrc32(final long crc32) {
        this.getArtWork().setCrc32(crc32);
    }

    public String getFileName() {
        return this.getArtWork().getFileName();
    }

    public void setFileName(final String fileName) {
        this.getArtWork().setFileName(fileName);
    }

    public ArtWorkProxy(final Future futureLoad, final String format) {
        this.futureLoad = futureLoad;
        this.format = format;
        this.logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    }
}

        
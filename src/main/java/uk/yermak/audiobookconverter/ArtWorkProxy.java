package uk.yermak.audiobookconverter;

import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Future;

public class ArtWorkProxy implements ArtWork {
    private final Future<ArtWork> futureLoad;
    private final Logger logger;

    private Logger logger() {
        return this.logger;
    }

    private ArtWork getArtWork() {
        try {
            return this.futureLoad.get();
        } catch (Exception e) {
            this.logger().error("Failed to load ArtWork Proxy:", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean matchCrc32(long crc32) {
        return this.getArtWork().matchCrc32(crc32);
    }

    @Override
    public Image image() {
        return getArtWork().image();
    }

    public long getCrc32() {
        return this.getArtWork().getCrc32();
    }

    public String getFileName() {
        return this.getArtWork().getFileName();
    }

    public ArtWorkProxy(final Future<ArtWork> futureLoad) {
        this.futureLoad = futureLoad;
        this.logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    }
}

        
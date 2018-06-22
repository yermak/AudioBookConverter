package uk.yermak.audiobookconverter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by yermak on 1/11/2018.
 */
public class ArtWorkProxy implements ArtWork {

    private final Future<ArtWork> futureLoad;
    private String format;

    public ArtWorkProxy(Future<ArtWork> futureLoad, String format) {
        this.futureLoad = futureLoad;
        this.format = format;
    }

    private ArtWork getArtWork() {
        try {
            return futureLoad.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public void setFormat(String format) {
        getArtWork().setFormat(format);
    }

    @Override
    public Long getCrc32() {
        return getArtWork().getCrc32();
    }

    @Override
    public void setCrc32(Long crc32) {
        getArtWork().setCrc32(crc32);
    }

    @Override
    public String getFileName() {
        return getArtWork().getFileName();
    }

    @Override
    public void setFileName(String fileName) {
        getArtWork().setFileName(fileName);
    }
}

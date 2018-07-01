package uk.yermak.audiobookconverter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by Yermak on 03-Jan-18.
 */
public class MediaInfoProxy implements MediaInfo {
    private final String filename;
    private final Future<MediaInfo> futureLoad;

    public MediaInfoProxy(String filename, Future futureLoad) {
        this.filename = filename;
        this.futureLoad = futureLoad;
    }

    private MediaInfo getMediaInfo() {
        try {
            return futureLoad.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setChannels(int channels) {
        getMediaInfo().setChannels(channels);
    }

    @Override
    public void setFrequency(int frequency) {
        getMediaInfo().setFrequency(frequency);
    }

    @Override
    public void setBitrate(int bitrate) {
        getMediaInfo().setBitrate(bitrate);
    }

    @Override
    public void setDuration(long duration) {
        getMediaInfo().setDuration(duration);
    }

    @Override
    public int getChannels() {
        return getMediaInfo().getChannels();
    }

    @Override
    public int getFrequency() {
        return getMediaInfo().getFrequency();
    }

    @Override
    public int getBitrate() {
        return getMediaInfo().getBitrate();
    }

    @Override
    public long getDuration() {
        return getMediaInfo().getDuration();
    }

    @Override
    public String getFileName() {
        return filename;
    }

    @Override
    public void setBookInfo(AudioBookInfo bookInfo) {
        getMediaInfo().setBookInfo(bookInfo);
    }

    @Override
    public AudioBookInfo getBookInfo() {
        return getMediaInfo().getBookInfo();
    }

    @Override
    public ArtWork getArtWork() {
        return getMediaInfo().getArtWork();
    }

    @Override
    public void setArtWork(ArtWork artWork) {
        getMediaInfo().setArtWork(artWork);
    }


    @Override
    public String toString() {
        return filename;
    }
}

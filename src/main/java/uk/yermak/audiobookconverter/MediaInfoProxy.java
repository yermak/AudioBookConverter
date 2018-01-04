package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.Mp4Tags;

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
        return getMediaInfo().getFileName();
    }

    @Override
    public void setMp4Tags(Mp4Tags mp4Tags) {
        getMediaInfo().setMp4Tags(mp4Tags);
    }

    @Override
    public Mp4Tags getMp4Tags() {
        return getMediaInfo().getMp4Tags();
    }

    @Override
    public String getPictureFormat() {
        return getMediaInfo().getPictureFormat();
    }
}

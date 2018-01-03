package uk.yermak.audiobookconverter;

/**
 * Created by Yermak on 02-Jan-18.
 */
public class MediaInfo {
    private int channels;
    private int frequency;
    private int bitrate;
    private long duration;
    private String fileName;

    public MediaInfo(String fileName) {
        this.fileName = fileName;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getChannels() {
        return channels;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getBitrate() {
        return bitrate;
    }

    public long getDuration() {
        return duration;
    }

    public String getFileName() {
        return fileName;
    }
}

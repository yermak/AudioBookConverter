package uk.yermak.audiobookconverter;

/**
 * Created by Yermak on 02-Jan-18.
 */
public class MediaInfoBean implements MediaInfo {
    private int channels = 2;
    private int frequency = 44100;
    private int bitrate = 128000;
    private long duration;
    private String fileName;
    private AudioBookInfo bookInfo;
    private ArtWork artWork;

    public MediaInfoBean(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void setChannels(int channels) {
        this.channels = channels;
    }

    @Override
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    @Override
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public int getChannels() {
        return channels;
    }

    @Override
    public int getFrequency() {
        return frequency;
    }

    @Override
    public int getBitrate() {
        return bitrate;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setBookInfo(AudioBookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    @Override
    public AudioBookInfo getBookInfo() {
        return bookInfo;
    }

    public ArtWork getArtWork() {
        return artWork;
    }

    public void setArtWork(ArtWork artWork) {
        this.artWork = artWork;
    }
}

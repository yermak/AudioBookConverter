package uk.yermak.audiobookconverter;

import java.util.Objects;

public class MediaTrackAdaptor extends MediaInfoOrganiser implements MediaInfo {

    private final MediaInfo mediaInfo;
    private final Track track;

    public MediaTrackAdaptor(MediaInfo mediaInfo, Track track) {
        this.mediaInfo = mediaInfo;
        this.track = track;
    }

    @Override
    public String getTitle() {
        return track.getTrackNo();
    }

    @Override
    public String getDetails() {
        return track.getTitle();
    }

    @Override
    public void setChannels(int channels) {

    }

    @Override
    public void setFrequency(int frequency) {

    }

    @Override
    public void setBitrate(int bitrate) {

    }

    @Override
    public void setDuration(long duration) {

    }

    @Override
    public int getChannels() {
        return mediaInfo.getChannels();
    }

    @Override
    public int getFrequency() {
        return mediaInfo.getFrequency();
    }

    @Override
    public int getBitrate() {
        return mediaInfo.getBitrate();
    }

    @Override
    public long getDuration() {
        return track.getDuration();
    }

    @Override
    public String getFileName() {
        return mediaInfo.getFileName();
    }

    @Override
    public void setBookInfo(AudioBookInfo bookInfo) {

    }

    @Override
    public AudioBookInfo getBookInfo() {
        return mediaInfo.getBookInfo();
    }

    @Override
    public ArtWork getArtWork() {
        return mediaInfo.getArtWork();
    }

    @Override
    public void setArtWork(ArtWork artWork) {

    }

    @Override
    public String getCodec() {
        return mediaInfo.getCodec();
    }

    @Override
    public void setCodec(String codec) {

    }

    @Override
    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    @Override
    public long getOffset() {
        return track.getStart();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileName()+getDuration()) ;
    }

}

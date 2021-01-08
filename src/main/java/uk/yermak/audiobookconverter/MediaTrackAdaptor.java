package uk.yermak.audiobookconverter;

import java.util.List;
import java.util.Objects;

public class MediaTrackAdaptor extends MediaInfoOrganiser implements MediaInfo {

    private final MediaInfo mediaInfo;
    private final Track track;
    private long duration;

    public MediaTrackAdaptor(MediaInfo mediaInfo, Track track) {
        this.mediaInfo = mediaInfo;
        this.chapter = mediaInfo.getChapter();
        this.track = track;
        this.duration = track.getDuration();
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
    public void setDuration(long duration) {
        this.duration = duration;
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
        return duration;
    }

    @Override
    public List<MediaInfo> getMedia() {
        return List.of(mediaInfo);
    }

    @Override
    public String getFileName() {
        return mediaInfo.getFileName();
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
    public String getCodec() {
        return mediaInfo.getCodec();
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
        return Objects.hash(getFileName(), track.getTrackNo(), getDuration());
    }

    @Override
    public int getTotalNumbers() {
        return 0;
    }
}

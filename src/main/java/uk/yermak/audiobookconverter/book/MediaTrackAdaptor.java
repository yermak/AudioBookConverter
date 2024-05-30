package uk.yermak.audiobookconverter.book;

import uk.yermak.audiobookconverter.AudiobookConverter;

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
        return (long) (duration / AudiobookConverter.getContext().getSpeed());
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

//    @Override
//    public ArtWork getArtWork() {
//        return mediaInfo.getArtWork();
//    }

    @Override
    public String getCodec() {
        return mediaInfo.getCodec();
    }

    @Override
    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    @Override
    public int getUID() {
        return Objects.hash(mediaInfo.getFileName(), track.getTrackNo());
    }

    @Override
    public String getReference() {
        return getFileName() + "-" + track.getTrackNo();
    }

    @Override
    public long getOffset() {
        return track.getStart();
    }

    @Override
    public int getTotalNumbers() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaInfo)) return false;
        MediaInfo that = (MediaInfo) o;
        return getReference().equals(that.getReference());
    }
}

package uk.yermak.audiobookconverter;

import com.google.gson.Gson;

import java.util.Map;

public class AudioBookInfo {
    private String writer = "";
    private String narrator = "";
    private String title = "";
    private String series = "";
    private String genre = "";
    private String year = "";
    private int bookNumber = 0;
    private int totalTracks = 0;
    private String comment = "";
    private String longDescription = "";

    public AudioBookInfo() {
    }

    public AudioBookInfo(Map<String, String> tags) {
        if (tags != null) {
            this.setTitle(tags.get("title"));
            this.setWriter(tags.get("artist"));
            this.setNarrator(tags.get("album_artist"));
            this.setSeries(tags.get("album"));
            this.setYear(tags.get("year"));
            this.setComment(tags.get("comment-0"));
            this.setGenre(tags.get("genre"));
            String trackNumber = tags.get("track number");
            if (trackNumber != null) {
                this.setBookNumber(Integer.parseInt(trackNumber));
            }
            String trackCount = tags.get("track count");
            if (trackCount != null) {
                this.setTotalTracks(Integer.parseInt(trackCount));
            }
        }
    }

    private String writer() {
        return this.writer;
    }

    private String narrator() {
        return this.narrator;
    }

    private String title() {
        return this.title;
    }

    private String series() {
        return this.series;
    }

    private String genre() {
        return this.genre;
    }

    private String year() {
        return this.year;
    }

    private int bookNumber() {
        return this.bookNumber;
    }

    private int totalTracks() {
        return this.totalTracks;
    }

    private String comment() {
        return this.comment;
    }

    private String longDescription() {
        return this.longDescription;
    }

    public String getSeries() {
        return this.series() == null ? this.title() : this.series();
    }

    public void setSeries(final String series) {
        this.series = series;
    }

    public String getWriter() {
        return this.writer();
    }

    public void setWriter(final String writer) {
        this.writer = writer;
    }

    public String getComment() {
        return this.comment();
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public String getGenre() {
        return this.genre();
    }

    public void setGenre(final String genre) {
        this.genre = genre;
    }

    public String getTitle() {
        return this.title();
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getBookNumber() {
        return this.bookNumber();
    }

    public void setBookNumber(final int bookNumber) {
        this.bookNumber = bookNumber;
    }

    public String getNarrator() {
        return this.narrator();
    }

    public void setNarrator(final String narrator) {
        this.narrator = narrator;
    }

    public String getYear() {
        return this.year();
    }

    public void setYear(final String year) {
        this.year = year;
    }

    public int getTotalTracks() {
        return this.totalTracks();
    }

    public void setTotalTracks(final int totalTracks) {
        this.totalTracks = totalTracks;
    }

    public String getLongDescription() {
        return this.longDescription();
    }

    public void setLongDescription(final String longDescription) {
        this.longDescription = longDescription;
    }

    public String toString() {
        return (new Gson()).toJson(this);
    }
}

        
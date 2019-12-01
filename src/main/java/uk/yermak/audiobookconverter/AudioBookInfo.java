//decompiled from AudioBookInfo$.class
package uk.yermak.audiobookconverter;

import java.util.Map;

import com.google.gson.Gson;

import javafx.collections.ObservableList;

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
    private ObservableList posters;

    public static AudioBookInfo instance(final Map tags) {
        AudioBookInfo audioBookInfo = new AudioBookInfo();
        if (tags != null) {
            audioBookInfo.setTitle((String) tags.get("title"));
            audioBookInfo.setWriter((String) tags.get("artist"));
            audioBookInfo.setNarrator((String) tags.get("album_artist"));
            audioBookInfo.setSeries((String) tags.get("album"));
            audioBookInfo.setYear((String) tags.get("year"));
            audioBookInfo.setComment((String) tags.get("comment-0"));
            audioBookInfo.setGenre((String) tags.get("genre"));
            String trackNumber = (String) tags.get("track number");
            if (trackNumber != null) {
                audioBookInfo.setBookNumber(Integer.parseInt(trackNumber));
            }
            String trackCount = (String) tags.get("track count");
            if (trackCount != null) {
                audioBookInfo.setTotalTracks(Integer.parseInt(trackCount));
            }
        }
        return audioBookInfo;
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

    private ObservableList posters() {
        return this.posters;
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

    public ObservableList getPosters() {
        return this.posters();
    }

    public void setPosters(final ObservableList posters) {
        this.posters = posters;
    }

    public String toString() {
        return (new Gson()).toJson(this);
    }
}

        
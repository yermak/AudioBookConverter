package com.freeipodsoftware.abc;

public class Mp4Tags {
    private String writer = "";
    private String narrator = "";
    private String title = "";
    private String series = "";
    private String genre = "";
    private String year = "";
    private String track = "";
    private String disc = "";
    private String comment = "";
    private String totalTracks = "";
    private String longDescription = "";

    public Mp4Tags() {
    }

    public String getSeries() {
        return this.series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getWriter() {
        return this.writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDisc() {
        return this.disc;
    }

    public void setDisc(String disc) {
        this.disc = disc;
    }

    public String getGenre() {
        return this.genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrack() {
        return this.track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getNarrator() {
        return this.narrator;
    }

    public void setNarrator(String narrator) {
        this.narrator = narrator;
    }

    public String getYear() {
        return this.year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTotalTracks() {
        return totalTracks;
    }

    public void setTotalTracks(String totalTracks) {
        this.totalTracks = totalTracks;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }
}

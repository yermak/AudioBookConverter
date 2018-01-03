package com.freeipodsoftware.abc;

import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Map;

public class Mp4Tags {
    private String writer = "";
    private String narrator = "";
    private String title = "";
    private String series = "";
    private String genre = "";
    private String year = "";
    private int track;
    private int totalTracks;
    private String comment = "";
    private String longDescription = "";

    public Mp4Tags() {
    }

    public Mp4Tags(Map<String, String> tags) {
        if (tags != null) {
            setTitle(tags.get("title"));
            setWriter(tags.get("artist"));
            setNarrator(tags.get("album_artist"));
            setSeries(tags.get("album"));
            setYear(tags.get("year"));
            setComment(tags.get("comment"));
            setGenre(tags.get("genre"));

            String track = tags.get("track");

            if (StringUtils.isNotBlank(track)) {
                String[] split = track.split("/");

                if (split.length > 0 && StringUtils.isNumeric(split[0])) {
                    setTrack(Integer.parseInt(split[0]));
                }
                if (split.length > 1 && StringUtils.isNumeric(split[1])) {
                    setTotalTracks(Integer.parseInt(split[1]));
                }
            }
        }
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

    public int getTrack() {
        return this.track;
    }

    public void setTrack(int track) {
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

    public int getTotalTracks() {
        return totalTracks;
    }

    public void setTotalTracks(int totalTracks) {
        this.totalTracks = totalTracks;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }
}

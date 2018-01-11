package uk.yermak.audiobookconverter;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class AudioBookInfo {
    private String writer = "";
    private String narrator = "";
    private String title = "";
    private String series = "";
    private String genre = "";
    private String year = "";
    private int bookNumber;
    private int totalTracks;
    private String comment = "";
    private String longDescription = "";

    public AudioBookInfo() {
    }

    public AudioBookInfo(Map<String, String> tags) {
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
                    setBookNumber(Integer.parseInt(split[0]));
                }
                if (split.length > 1 && StringUtils.isNumeric(split[1])) {
                    setTotalTracks(Integer.parseInt(split[1]));
                }
            }
        }
    }

    public String getSeries() {
        if (series == null) return title;
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

    public int getBookNumber() {
        return this.bookNumber;
    }

    public void setBookNumber(int bookNumber) {
        this.bookNumber = bookNumber;
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

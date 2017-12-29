package com.freeipodsoftware.abc;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class TagEditor extends TagEditorGui {
    public TagEditor(Composite parent) {
        super(parent);
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        return this.isVisible() ? super.computeSize(wHint, hHint, changed) : new Point(0, 0);
    }

    public void setArtist(String artist) {
        this.writerText.setText(Util.nullToEmptyString(artist));
    }

    public void setWriter(String writer) {
        this.narratorText.setText(Util.nullToEmptyString(writer));
    }

    public void setTitle(String title) {
        this.titleText.setText(Util.nullToEmptyString(title));
    }

    public void setAlbum(String album) {
        this.series.setText(Util.nullToEmptyString(album));
    }

    public void setGenre(String genre) {
        this.genreCombo.setText(Util.nullToEmptyString(genre));
    }

    public void setYear(String year) {
        this.yearText.setText(Util.nullToEmptyString(year));
    }

    public void setTrack(String track) {
        this.bookNumberText.setText(Util.nullToEmptyString(track));
    }

    public void setTotalTracks(String totalTracks) {
        this.totalBooksText.setText(totalTracks);
    }

    public void setComment(String comment) {
        this.commentText.setText(Util.nullToEmptyString(comment));
    }

    public void clear() {
        this.setArtist("");
        this.setWriter("");
        this.setTitle("");
        this.setAlbum("");
        this.setGenre("");
        this.setYear("");
        this.setTrack("");
        this.setTotalTracks("");
        this.setComment("");
    }

    public Mp4Tags getMp4Tags() {
        Mp4Tags tags = new Mp4Tags();
        tags.setWriter(this.writerText.getText());
        tags.setNarrator(this.narratorText.getText());
        tags.setTitle(this.titleText.getText());
        tags.setSeries(this.series.getText());
        tags.setGenre(this.genreCombo.getText());
        tags.setYear(this.yearText.getText());
        tags.setTrack(this.bookNumberText.getText());
        tags.setTotalTracks(this.totalBooksText.getText());
        tags.setComment(this.commentText.getText());
        return tags;
    }
}

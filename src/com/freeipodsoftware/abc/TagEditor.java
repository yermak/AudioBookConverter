package com.freeipodsoftware.abc;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class TagEditor extends TagEditorGui {
    public TagEditor(Composite parent, int style) {
        super(parent, style);
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        return this.isVisible() ? super.computeSize(wHint, hHint, changed) : new Point(0, 0);
    }

    public void setArtist(String artist) {
        this.artistText.setText(Util.nullToEmptyString(artist));
    }

    public void setWriter(String writer) {
        this.writerText.setText(Util.nullToEmptyString(writer));
    }

    public void setTitle(String title) {
        this.titleText.setText(Util.nullToEmptyString(title));
    }

    public void setAlbum(String album) {
        this.albumText.setText(Util.nullToEmptyString(album));
    }

    public void setGenre(String genre) {
        this.genreCombo.setText(Util.nullToEmptyString(genre));
    }

    public void setYear(String year) {
        this.yearText.setText(Util.nullToEmptyString(year));
    }

    public void setTrack(String track) {
        this.trackText.setText(Util.nullToEmptyString(track));
    }

    public void setDisc(String disc) {
        this.discText.setText(disc);
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
        this.setDisc("");
        this.setComment("");
    }

    public Mp4Tags getMp4Tags() {
        Mp4Tags tags = new Mp4Tags();
        tags.setArtist(this.artistText.getText());
        tags.setWriter(this.writerText.getText());
        tags.setTitle(this.titleText.getText());
        tags.setAlbum(this.albumText.getText());
        tags.setGenre(this.genreCombo.getText());
        tags.setYear(this.yearText.getText());
        tags.setTrack(this.trackText.getText());
        tags.setDisc(this.discText.getText());
        tags.setComment(this.commentText.getText());
        return tags;
    }
}

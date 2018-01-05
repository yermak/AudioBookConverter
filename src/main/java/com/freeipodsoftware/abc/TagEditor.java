package com.freeipodsoftware.abc;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class TagEditor extends TagEditorGui {
    public TagEditor(Composite parent) {
        super(parent);
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        return this.isVisible() ? super.computeSize(wHint, hHint, changed) : new Point(0, 0);
    }

    public void setWriter(String writer) {
        this.writerText.setText(StringUtils.stripToEmpty(writer));
    }

    public void setNarrator(String narrator) {
        this.narratorText.setText(StringUtils.stripToEmpty(narrator));
    }

    public void setTitle(String title) {
        this.titleText.setText(StringUtils.stripToEmpty(title));
    }

    public void setAlbum(String album) {
        this.series.setText(StringUtils.stripToEmpty(album));
    }

    public void setGenre(String genre) {
        this.genreCombo.setText(StringUtils.stripToEmpty(genre));
    }

    public void setYear(String year) {
        this.yearText.setText(StringUtils.stripToEmpty(year));
    }

    public void setTrack(String track) {
        this.bookNumberText.setText(StringUtils.stripToEmpty(track));
    }

    public void setTotalTracks(String totalTracks) {
        this.totalBooksText.setText(totalTracks);
    }

    public void setComment(String comment) {
        this.commentText.setText(StringUtils.stripToEmpty(comment));
    }

    public void clear() {
        this.setWriter("");
        this.setNarrator("");
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
        if (StringUtils.isNotBlank(this.bookNumberText.getText()) && StringUtils.isNumeric(this.bookNumberText.getText())){
            tags.setTrack(Integer.valueOf(this.bookNumberText.getText()));
        }
        if (StringUtils.isNotBlank(this.totalBooksText.getText()) && StringUtils.isNumeric(this.totalBooksText.getText())){
            tags.setTotalTracks(Integer.valueOf(this.totalBooksText.getText()));
        }

        tags.setComment(this.commentText.getText());
        return tags;
    }
}

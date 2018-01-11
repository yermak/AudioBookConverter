package com.freeipodsoftware.abc;

import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.AudioBookInfo;
import uk.yermak.audiobookconverter.ConversionMode;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.StateDispatcher;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class TagSuggester implements StateListener {
    private TagEditor tagEditor;
    private InputFileSelection inputFileSelection;
    private StateDispatcher stateDispatcher = StateDispatcher.getInstance();
    private ConversionMode mode;

    public TagSuggester() {
        stateDispatcher.addListener(this);
    }

    public void setTagEditor(TagEditor tagEditor) {
        this.tagEditor = tagEditor;
    }

    public void setInputFileSelection(InputFileSelection inputFileSelection) {
        this.inputFileSelection = inputFileSelection;
    }

    private void suggestTags() {
        if (mode == ConversionMode.BATCH) return;

        List<MediaInfo> media = inputFileSelection.getMedia();
        this.tagEditor.clear();
        if (media.size() > 0) {
            AudioBookInfo tags = media.get(0).getMp4Tags();
            this.tagEditor.setWriter(tags.getWriter());
//            this.tagEditor.setWriter(fixEncoding(tags.getWriter()));
            this.tagEditor.setNarrator(tags.getNarrator());
            this.tagEditor.setTitle(tags.getTitle());
            this.tagEditor.setAlbum(tags.getSeries());
            this.tagEditor.setGenre(tags.getGenre());
            this.tagEditor.setYear(tags.getYear());
            if (tags.getBookNumber() > 0) {
                this.tagEditor.setTrack(String.valueOf(tags.getBookNumber()));
            }
            if (tags.getTotalTracks() > 0) {
                this.tagEditor.setTotalTracks(String.valueOf(tags.getTotalTracks()));
            }
            this.tagEditor.setComment(tags.getComment());
        }
    }

    private String fixEncoding(String text) {
        String t = StringUtils.stripToEmpty(text);
        t = StringUtils.lowerCase(t);
        if (StringUtils.containsOnly(t, "абвгдеёжзийклмнопрстуфхцчшщъыьэюяabcdefghijklmnopqrstuvwxyz1234567890!@#$%^&*()_-=+")) {
            return t;
        }
        try {
            return new String(text.getBytes("CP1252"), "CP1251");
        } catch (UnsupportedEncodingException e) {
            return text;
        }

    }

    @Override
    public void finishedWithError(String error) {

    }

    @Override
    public void finished() {

    }

    @Override
    public void canceled() {

    }

    @Override
    public void paused() {

    }

    @Override
    public void resumed() {

    }

    @Override
    public void fileListChanged() {
        suggestTags();
    }

    @Override
    public void modeChanged(ConversionMode mode) {
        this.mode = mode;
    }
}

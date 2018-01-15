package com.freeipodsoftware.abc;

import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.*;

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
            AudioBookInfo bookInfo = media.get(0).getBookInfo();
            this.tagEditor.setWriter(bookInfo.getWriter());
//            this.tagEditor.setWriter(fixEncoding(bookInfo.getWriter()));
            this.tagEditor.setNarrator(bookInfo.getNarrator());
            this.tagEditor.setTitle(bookInfo.getTitle());
            this.tagEditor.setAlbum(bookInfo.getSeries());
            this.tagEditor.setGenre(bookInfo.getGenre());
            this.tagEditor.setYear(bookInfo.getYear());
            if (bookInfo.getBookNumber() > 0) {
                this.tagEditor.setTrack(String.valueOf(bookInfo.getBookNumber()));
            }
            if (bookInfo.getTotalTracks() > 0) {
                this.tagEditor.setTotalTracks(String.valueOf(bookInfo.getTotalTracks()));
            }
            this.tagEditor.setComment(bookInfo.getComment());
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

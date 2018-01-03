package com.freeipodsoftware.abc;

import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.MediaInfo;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class TagSuggestionStrategy implements EventListener {
    private TagEditor tagEditor;
    private InputFileSelection inputFileSelection;

    public TagSuggestionStrategy() {
    }

    public void setEventDispatcher(EventDispatcher eventDispatcher) {
        eventDispatcher.addListener(this);
    }

    public void setTagEditor(TagEditor tagEditor) {
        this.tagEditor = tagEditor;
    }

    public void setInputFileSelection(InputFileSelection inputFileSelection) {
        this.inputFileSelection = inputFileSelection;
    }

    public void onEvent(String eventId) {
        if (eventId.equals("fileListChangedEvent")) {
            this.suggestTags();
        }
    }


    private void suggestTags() {
        List<MediaInfo> media = inputFileSelection.getMedia();
        this.tagEditor.clear();
        if (media.size() > 0) {
            Mp4Tags tags = media.get(0).getMp4Tags();
            this.tagEditor.setWriter(tags.getWriter());
//            this.tagEditor.setWriter(fixEncoding(tags.getWriter()));
            this.tagEditor.setNarrator(tags.getNarrator());
            this.tagEditor.setTitle(tags.getTitle());
            this.tagEditor.setAlbum(tags.getSeries());
            this.tagEditor.setGenre(tags.getGenre());
            this.tagEditor.setYear(tags.getYear());
            if (tags.getTrack() > 0) {
                this.tagEditor.setTrack(String.valueOf(tags.getTrack()));
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
}

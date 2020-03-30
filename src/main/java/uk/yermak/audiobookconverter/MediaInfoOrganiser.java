package uk.yermak.audiobookconverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MediaInfoOrganiser implements Organisable{
    protected Chapter chapter;

    public void split() {
        List<MediaInfo> currentMedia = new ArrayList<>(chapter.getMedia().subList(0, getNumber() - 1));
        List<MediaInfo> nextMedia = new ArrayList<>(chapter.getMedia().subList(getNumber() - 1, chapter.getMedia().size()));
        chapter.getMedia().clear();
        chapter.getMedia().addAll(currentMedia);
        chapter.createNextChapter(nextMedia);
    }

    public void remove() {
        chapter.getMedia().remove(this);
        if (chapter.getMedia().isEmpty()) {
            chapter.remove();
        }
    }

    public void moveUp() {
        if (getNumber() < 2) return;
        Collections.swap(chapter.getMedia(), getNumber() - 1, getNumber() - 2);
    }

    public void moveDown() {
        if (getNumber() > chapter.getMedia().size()) return;
        Collections.swap(chapter.getMedia(), getNumber() - 1, getNumber());
    }

    public int getNumber() {
        return chapter.getMedia().indexOf(this) + 1;
    }

}

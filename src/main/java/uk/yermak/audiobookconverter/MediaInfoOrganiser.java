package uk.yermak.audiobookconverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


//TODO MediaInfoBean can't interite this class as this it could be in invalid state, when chapters are not created.
// It's better to use methods of this class as wrapper around MediaInfoBean, when chapters are initalised and this become valid state.
public abstract class MediaInfoOrganiser implements Organisable {
    protected Chapter chapter;

    public boolean split() {
        if (chapter.getMedia().size() == 1) {
            return false;
        }
        if (getNumber() == 1) {
            return false;
        }
        List<MediaInfo> currentMedia = new ArrayList<>(chapter.getMedia().subList(0, getNumber() - 1));
        List<MediaInfo> nextMedia = new ArrayList<>(chapter.getMedia().subList(getNumber() - 1, chapter.getMedia().size()));
        chapter.getMedia().clear();
        chapter.getMedia().addAll(currentMedia);
        chapter.createNextChapter(nextMedia);
        return true;
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
        if (getNumber() >= chapter.getMedia().size()) return;
        Collections.swap(chapter.getMedia(), getNumber() - 1, getNumber());
    }

    @Override
    public int getNumber() {
        return chapter.getMedia().indexOf(this) + 1;
    }

    @Override
    public int getTotalNumbers() {
        return getChapter().getMedia().size();
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public Chapter getChapter() {
        return chapter;
    }
}

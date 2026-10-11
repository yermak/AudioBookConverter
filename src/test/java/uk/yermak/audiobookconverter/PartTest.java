package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.book.*;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class PartTest {

    @BeforeClass
    public void initLog() {
        AudiobookConverter.initAppHome();
    }

    private static Track track(String no, long start, long end) {
        Track track = new Track(no);
        track.setStart(start);
        track.setEnd(end);
        return track;
    }

    @Test
    public void subTrackChaptersKeepAudioOrder() {
        List<MediaInfo> files = List.of(new MediaInfoBean("A.mp3"), new MediaInfoBean("B.mp3"), new MediaInfoBean("C.mp3"));
        files.forEach(m -> ((MediaInfoBean) m).setBookInfo(AudioBookInfo.instance()));
        Book book = new Book(AudioBookInfo.instance());
        book.construct(FXCollections.observableArrayList(files));
        Part part = book.getParts().get(0);
        List<Chapter> chapters = part.getChapters();
        chapters.get(0).combine(List.of(chapters.get(1), chapters.get(2)));

        part.replaceMediaChapterByTracksChapters(files.get(1), List.of(track("1", 0, 1000), track("2", 1000, 2000)));

        assertEquals(part.getChapters().stream().map(c -> c.getMedia().stream().map(MediaInfo::getReference).toList()).toList(),
                List.of(List.of("A.mp3"), List.of("B.mp3-1"), List.of("B.mp3-2"), List.of("C.mp3")));
    }
}

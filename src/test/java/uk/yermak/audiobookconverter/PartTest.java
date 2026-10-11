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

    private static Book book(String... files) {
        List<MediaInfo> media = java.util.Arrays.stream(files).map(f -> {
            MediaInfoBean m = new MediaInfoBean(f);
            m.setBookInfo(AudioBookInfo.instance());
            return (MediaInfo) m;
        }).toList();
        Book book = new Book(AudioBookInfo.instance());
        book.construct(FXCollections.observableArrayList(media));
        return book;
    }

    @Test
    public void partsMoveUpAndDown() {
        Book book = book("A.mp3", "B.mp3");
        book.getParts().get(0).getChapters().get(1).split(); // B into a second part
        Part a = book.getParts().get(0);
        Part b = book.getParts().get(1);

        b.moveUp();
        assertEquals(book.getParts(), List.of(b, a));
        b.moveDown();
        assertEquals(book.getParts(), List.of(a, b));
    }

    @Test
    public void tracksCountTheirSiblings() {
        Book book = book("A.mp3");
        Chapter chapter = book.getParts().get(0).getChapters().get(0);
        chapter.replaceMediaWithTracks(chapter.getMedia().get(0), List.of(track("1", 0, 1000), track("2", 1000, 2000)));
        chapter.getMedia().forEach(m -> m.setChapter(chapter));

        assertEquals(((Organisable) chapter.getMedia().get(1)).getTotalNumbers(), 2);
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

    @Test
    public void unreadableFileIsLeftOutNotTheBook() {
        MediaInfoBean good = new MediaInfoBean("A.mp3");
        good.setBookInfo(AudioBookInfo.instance());
        MediaInfo bad = new MediaInfoBean("B.mp3") {
            @Override
            public AudioBookInfo getBookInfo() {
                throw new RuntimeException("ffprobe failed"); // as MediaInfoProxy does for a failed probe
            }
        };
        Book book = new Book(AudioBookInfo.instance());

        assertEquals(book.construct(FXCollections.observableArrayList(List.of(bad, good))), List.of("B.mp3"));
        assertEquals(book.getParts().size(), 1);
        assertEquals(book.getParts().get(0).getChapters().size(), 1);

        // nothing readable: no empty part for a job to choke on
        Book none = new Book(AudioBookInfo.instance());
        assertEquals(none.construct(FXCollections.observableArrayList(List.of(bad))), List.of("B.mp3"));
        assertEquals(none.getParts().size(), 0);
    }
}

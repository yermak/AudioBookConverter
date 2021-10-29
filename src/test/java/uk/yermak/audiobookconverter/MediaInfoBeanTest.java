package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MediaInfoBeanTest {

    @BeforeClass
    public void initLog(){
        AudiobookConverter.initAppHome();
    }

    @Test
    public void testRemove() {
        MediaInfoBean media = new MediaInfoBean("file.mp3");
        media.setBookInfo(AudioBookInfo.instance());
        Book book = new Book(AudioBookInfo.instance());
        book.construct(FXCollections.observableArrayList(media));
        Part part = book.getParts().get(0);
        Chapter chapter = part.getChapters().get(0);
        media.remove();
        assertTrue(chapter.getMedia().isEmpty());
        assertTrue(part.getChapters().isEmpty());
        assertTrue(book.getParts().isEmpty());
    }

    @Test
    public void testCombine_Move() {
        MediaInfoBean media1 = new MediaInfoBean("file1.mp3");
        media1.setBookInfo(AudioBookInfo.instance());

        MediaInfoBean media2 = new MediaInfoBean("file2.mp3");
        media2.setBookInfo(AudioBookInfo.instance());

        Book book = new Book(AudioBookInfo.instance());
        book.construct(FXCollections.observableArrayList(media1, media2));
        Chapter chapter = book.getParts().get(0).getChapters().get(0);
        chapter.combine(book.getParts().get(0).getChapters().subList(1, 2));
        assertEquals(chapter.getMedia().size(),2 );

        assertEquals(chapter.getMedia().get(0).getFileName(), "file1.mp3");
        assertEquals(chapter.getMedia().get(1).getFileName(), "file2.mp3");

        media2.moveUp();

        assertEquals(chapter.getMedia().get(1).getFileName(), "file1.mp3");
        assertEquals(chapter.getMedia().get(0).getFileName(), "file2.mp3");

        media2.moveDown();

        assertEquals(chapter.getMedia().get(0).getFileName(), "file1.mp3");
        assertEquals(chapter.getMedia().get(1).getFileName(), "file2.mp3");
    }

}
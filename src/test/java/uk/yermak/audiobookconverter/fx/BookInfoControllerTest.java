package uk.yermak.audiobookconverter.fx;

import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.book.AudioBookInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class BookInfoControllerTest {

    private static List<AudioBookInfo> files(String... tracks) {
        return Arrays.stream(tracks).map(t -> AudioBookInfo.instance(t == null ? Map.of() : Map.of("track", t))).toList();
    }

    @Test
    public void tracksOfChapterFilesAreNotTheBookNumber() {
        assertEquals(BookInfoController.bookNumber(files("1/32", "2/32", "3/32")), "");
        assertEquals(BookInfoController.bookNumber(files("1", "2", "3")), "");
    }

    @Test
    public void trackOfAWholeBookIsTheBookNumber() {
        assertEquals(BookInfoController.bookNumber(files("3")), "3");
    }

    @Test
    public void partsOfOneBookShareTheBookNumber() {
        assertEquals(BookInfoController.bookNumber(files("3", "3")), "3");
    }

    @Test
    public void chapterFileAddedAloneIsNotTheBookNumber() {
        assertEquals(BookInfoController.bookNumber(files("1/32")), "");
    }

    @Test
    public void filesWithoutTracksHaveNoBookNumber() {
        assertEquals(BookInfoController.bookNumber(files(null, null)), "");
    }
}

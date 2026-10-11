package uk.yermak.audiobookconverter.fx;

import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.book.AudioBookInfo;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public class BookInfoControllerTest {

    @Test
    public void trackOfOneOfManyFilesIsNotTheBookNumber() {
        assertEquals(BookInfoController.bookNumber(AudioBookInfo.instance(Map.of("track", "1/32")), 32), "");
    }

    @Test
    public void trackOfAWholeBookIsTheBookNumber() {
        assertEquals(BookInfoController.bookNumber(AudioBookInfo.instance(Map.of("track", "3")), 1), "3");
    }
}

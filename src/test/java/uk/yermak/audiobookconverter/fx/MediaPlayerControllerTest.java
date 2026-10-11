package uk.yermak.audiobookconverter.fx;

import javafx.util.Duration;
import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.book.MediaInfoBean;
import uk.yermak.audiobookconverter.book.MediaTrackAdaptor;
import uk.yermak.audiobookconverter.book.Track;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class MediaPlayerControllerTest {

    @Test
    public void chapterOfAFilePlaysOnlyItsPart() {
        Track chapter3 = new Track("3");
        chapter3.setStart(600_000);
        chapter3.setEnd(900_000);

        Duration[] window = MediaPlayerController.window(new MediaTrackAdaptor(new MediaInfoBean("book.m4b"), chapter3));

        assertEquals(window[0], Duration.seconds(600));
        assertEquals(window[1], Duration.seconds(900));
    }

    @Test
    public void wholeFilePlaysFromStartToEnd() {
        assertNull(MediaPlayerController.window(new MediaInfoBean("chapter.mp3")));
    }
}

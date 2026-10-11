package uk.yermak.audiobookconverter;

import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.book.MediaInfoBean;
import uk.yermak.audiobookconverter.book.MediaTrackAdaptor;
import uk.yermak.audiobookconverter.book.Track;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

public class MediaTrackAdaptorTest {

    private static Track track(String no, long start, long end) {
        Track track = new Track(no);
        track.setStart(start);
        track.setEnd(end);
        return track;
    }

    @Test
    public void subTrackOfTrackSeeksFromParentStart() {
        MediaInfoBean file = new MediaInfoBean("book.m4b");
        MediaTrackAdaptor chapter3 = new MediaTrackAdaptor(file, track("3", 600_000, 900_000));
        MediaTrackAdaptor chapter4 = new MediaTrackAdaptor(file, track("4", 900_000, 1_200_000));

        assertEquals(chapter3.getOffset(), 600_000);
        assertEquals(new MediaTrackAdaptor(chapter3, track("1", 60_000, 120_000)).getOffset(), 660_000);

        // sub-track 1 of two different chapters
        assertNotEquals(new MediaTrackAdaptor(chapter3, track("1", 0, 60_000)), new MediaTrackAdaptor(chapter4, track("1", 0, 60_000)));
    }
}

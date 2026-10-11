package uk.yermak.audiobookconverter;

import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.book.AudioBookInfo;

import java.util.Map;

import static org.testng.Assert.*;

public class AudioBookInfoTest {

    @Test
    public void testInstance_Year_from_iTunes_tag_release_date() {
        AudioBookInfo bookInfo = AudioBookInfo.instance(Map.of("date", "2022-06-05T19:09:25Z"));
        assertEquals(bookInfo.year().toString(), "2022");
    }

    @Test
    public void testInstance_TrackNumber_from_iTunes_tag_track() {
        AudioBookInfo bookInfo = AudioBookInfo.instance(Map.of("track", "1"));
        assertEquals(bookInfo.bookNumber().toString(), "1");
    }

    @Test
    public void testInstance_TrackNumberAndTrackCount_from_iTunes_tag_track() {
        AudioBookInfo bookInfo = AudioBookInfo.instance(Map.of("track", "1/436"));
        assertEquals(bookInfo.bookNumber().toString(), "1");
        assertEquals(bookInfo.totalTracks().toString(), "436");
    }

    @Test
    public void testInstance_uppercase_Vorbis_comment_keys() {
        AudioBookInfo bookInfo = AudioBookInfo.instance(Map.of("TITLE", "Dune", "ARTIST", "Herbert", "DATE", "1965", "TRACK", "2/6"));
        assertEquals(bookInfo.title().toString(), "Dune");
        assertEquals(bookInfo.writer().toString(), "Herbert");
        assertEquals(bookInfo.year().toString(), "1965");
        assertEquals(bookInfo.bookNumber().toString(), "2");
    }
}
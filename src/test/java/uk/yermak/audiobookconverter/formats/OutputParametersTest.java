package uk.yermak.audiobookconverter.formats;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static uk.yermak.audiobookconverter.formats.Format.toFFMpegTime;

public class OutputParametersTest {
    @Test
    public void testToFFMpegTime() {
        assertEquals(toFFMpegTime(5432), "5.432");
        assertEquals(toFFMpegTime(123), "0.123");
    }

}
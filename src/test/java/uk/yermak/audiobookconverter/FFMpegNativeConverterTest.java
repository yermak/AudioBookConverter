package uk.yermak.audiobookconverter;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class FFMpegNativeConverterTest {

    @Test
    public void testToFFMpegTime() {
        assertEquals(FFMpegNativeConverter.toFFMpegTime(5432), "5.432");
        assertEquals(FFMpegNativeConverter.toFFMpegTime(123), "0.123");
    }
}
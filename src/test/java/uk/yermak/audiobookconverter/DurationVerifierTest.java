package uk.yermak.audiobookconverter;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class DurationVerifierTest {

    @Test
    public void testParseInfo() {
        String info = """
                Track   Type    Info
                1       audio   MPEG-4 AAC LC, 7137.041 secs, 119 kbps, 44100 Hz
                2       text
                 Comments: null
                 Media Type: Audio Book""";

        long l = DurationVerifier.parseDuration(info);
        assertEquals(l, 7137041);
    }
}
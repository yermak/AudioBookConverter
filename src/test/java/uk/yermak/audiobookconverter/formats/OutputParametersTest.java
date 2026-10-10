package uk.yermak.audiobookconverter.formats;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static uk.yermak.audiobookconverter.formats.Format.toFFMpegTime;

public class OutputParametersTest {
    @Test
    public void testToFFMpegTime() {
        assertEquals(toFFMpegTime(5432), "5.432");
        assertEquals(toFFMpegTime(123), "0.123");
        assertEquals(toFFMpegTime(1321093), "1321.093");
        assertEquals(toFFMpegTime(5), "0.005");
        assertEquals(toFFMpegTime(60000), "60.000");
    }

    @Test
    public void speedForcesReencodeOfMatchingCodec() {
        OutputParameters parameters = new OutputParameters(Format.M4B, 128, 44100, 2, 0, true, 4, 1.0, OutputParameters.Force.Auto, false);
        assertFalse(parameters.needReencode("aac"));
        parameters.setSpeed(1.5);
        assertTrue(parameters.needReencode("aac"));
        parameters.setForce(OutputParameters.Force.Avoid);
        assertTrue(parameters.needReencode("aac"));
    }

}
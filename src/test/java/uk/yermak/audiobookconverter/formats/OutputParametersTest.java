package uk.yermak.audiobookconverter.formats;

import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.book.MediaInfo;
import uk.yermak.audiobookconverter.book.MediaInfoBean;

import java.util.List;

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
    public void metaDataValuesAreEscaped() {
        assertEquals(Format.escapeMetaData(List.of(";FFMETADATA1", "[CHAPTER]", "TIMEBASE=1/1000",
                        "comment=Line one\r\nLine = two; #3", "title=C:\\dir\\", "track=2")),
                List.of(";FFMETADATA1", "[CHAPTER]", "TIMEBASE=1/1000",
                        "comment=Line one\\\nLine \\= two\\; \\#3", "title=C:\\\\dir\\\\", "track=2"));
    }

    private static MediaInfo media(String codec, int frequency, int channels) {
        MediaInfoBean media = new MediaInfoBean("file." + codec);
        media.setCodec(codec);
        media.setFrequency(frequency);
        media.setChannels(channels);
        return media;
    }

    @Test
    public void reencodeDecidedForWholeJob() {
        OutputParameters parameters = new OutputParameters(Format.M4B, 128, 44100, 2, 0, true, 4, 1.0, OutputParameters.Force.Auto, false);
        assertFalse(parameters.needReencode(List.of(media("aac", 22050, 1), media("aac", 22050, 1))));
        // one piece needs re-encoding, so all do: concat stream-copies the pieces
        assertTrue(parameters.needReencode(List.of(media("aac", 44100, 2), media("mp3", 44100, 2))));
        // all copyable, but the pieces differ
        assertTrue(parameters.needReencode(List.of(media("aac", 44100, 2), media("aac", 22050, 2))));
        assertTrue(parameters.needReencode(List.of(media("aac", 44100, 2), media("aac", 44100, 1))));
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
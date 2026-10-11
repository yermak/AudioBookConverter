package uk.yermak.audiobookconverter.fx;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.ConversionJob;
import uk.yermak.audiobookconverter.book.Convertable;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.testng.Assert.assertEquals;

public class ConversionProgressTest {

    @BeforeClass
    public void englishBundle() throws Exception {
        Field bundle = AudiobookConverter.class.getDeclaredField("bundle");
        bundle.setAccessible(true);
        bundle.set(null, ResourceBundle.getBundle("locales/messages", Locale.ENGLISH));
    }

    @Test
    public void finishedJobShowsTheActualOutputSize() throws Exception {
        File output = File.createTempFile("abc-size", ".m4b");
        output.deleteOnExit();
        Files.write(output.toPath(), new byte[519_404]);

        // ffprobe guessed 76s for a VBR mp3 without a Xing header that really plays 120s
        Convertable book = new Convertable.EmptyConvertable() {
            @Override
            public long getDuration() {
                return 75_966;
            }
        };
        ConversionJob job = new ConversionJob(null, book, Map.of(), output.getPath());
        ConversionProgress progress = new ConversionProgress(job);

        progress.reset(false); // merge: the estimate is scaled by the probed duration
        progress.converted("output", 120_056, 519_404);
        progress.reset(true); // artwork and optimise keep the merge estimate
        job.finished();

        assertEquals(progress.size.get(), 519_404);
    }
}

package uk.yermak.audiobookconverter;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.book.AudioBookInfo;
import uk.yermak.audiobookconverter.book.Convertable;
import uk.yermak.audiobookconverter.formats.Format;
import uk.yermak.audiobookconverter.formats.OutputParameters;
import uk.yermak.audiobookconverter.fx.ConversionProgress;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.testng.Assert.assertEquals;

public class ConversionJobErrorTest {

    @BeforeClass
    public void englishBundle() throws Exception {
        Field bundle = AudiobookConverter.class.getDeclaredField("bundle");
        bundle.setAccessible(true);
        bundle.set(null, ResourceBundle.getBundle("locales/messages", Locale.ENGLISH));
    }

    // issue #737: an Error escaped run(), so the job stayed IN_PROGRESS and its row on "Merging chapters..." for good
    @Test(timeOut = 30_000)
    public void errorWhileMergingFailsTheJob() {
        // no media: run() goes straight to the merge, where building the chapter metadata throws
        Convertable book = new Convertable.EmptyConvertable() {
            @Override
            public List<String> getMetaData(AudioBookInfo bookInfo) {
                throw new StackOverflowError("chapter metadata");
            }
        };
        ConversionGroup group = new ConversionGroup();
        group.setOutputParameters(new OutputParameters(Format.M4B, 128, 44100, 2, 0, true, 4, 1.0, OutputParameters.Force.Auto, false));
        group.setBookInfo(AudioBookInfo.instance());
        Map<String, ProgressCallback> callbacks = new HashMap<>();
        ConversionJob job = new ConversionJob(group, book, callbacks, new File(System.getProperty("java.io.tmpdir"), "abc-737.m4b").getPath());
        callbacks.put("output", new ProgressCallback("output", new ConversionProgress(job)));

        job.run();

        assertEquals(job.getStatus(), ProgressStatus.ERROR);
        assertEquals(job.getErrorMessage(), "StackOverflowError: chapter metadata");
    }
}

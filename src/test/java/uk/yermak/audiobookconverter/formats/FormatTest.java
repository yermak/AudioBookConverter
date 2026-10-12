package uk.yermak.audiobookconverter.formats;

import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.ConversionGroup;
import uk.yermak.audiobookconverter.ConversionJob;

import java.util.Map;

import static org.testng.Assert.assertNotEquals;

public class FormatTest {

    @Test
    public void parallelJobsOfOneBookWriteTheirOwnMetadata() {
        // split by chapters runs every chapter as its own job at the same time (issue #430)
        ConversionGroup book = new ConversionGroup();
        assertNotEquals(Format.metaFile(new ConversionJob(book, null, Map.of(), "Book, Chapter 001.m4b")),
                Format.metaFile(new ConversionJob(book, null, Map.of(), "Book, Chapter 002.m4b")));
    }
}

package uk.yermak.audiobookconverter;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.book.AudioBookInfo;

import java.util.Map;

import static org.testng.Assert.assertEquals;

public class UtilsFilenameTest {

    @BeforeClass
    public void initLog() {
        AudiobookConverter.initAppHome();
    }

    @Test
    public void bookNumberFormatApplies() {
        String format = "<SERIES> - <BOOK_NUMBER; format=\"%,02d\">";
        assertEquals(Utils.getOuputFilenameSuggestion(format, AudioBookInfo.instance(Map.of("album", "Dune", "track", "3"))), "Dune - 03");
    }
}

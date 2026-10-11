package uk.yermak.audiobookconverter;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.book.Chapter;
import uk.yermak.audiobookconverter.fx.util.SmartStringProperty;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static org.testng.Assert.assertEquals;

public class UtilsRenderChapterTest {

    @BeforeClass
    public void initLog() {
        AudiobookConverter.initAppHome();
    }

    @Test
    public void blankValuesDoNotRenderTheirBlocks() {
        String format = "<if(BOOK_NUMBER)><BOOK_NUMBER>. <endif><if(BOOK_TITLE)><BOOK_TITLE>. <endif><if(TAG)><TAG> <endif><if(CHAPTER_TEXT)><CHAPTER_TEXT><endif>";
        Map<String, Function<Chapter, Object>> context = new LinkedHashMap<>();
        context.put("BOOK_NUMBER", c -> "");
        context.put("BOOK_TITLE", c -> new SmartStringProperty(" "));
        context.put("TAG.1", c -> new SmartStringProperty(""));
        context.put("CHAPTER_TEXT", c -> "Chapter");

        assertEquals(Utils.renderChapter(format, null, context), "Chapter");
    }

    @Test
    public void durationsPast24HoursDoNotWrap() {
        Map<String, Function<Chapter, Object>> context = Map.of("DURATION", c -> java.time.Duration.ofHours(25).plusMinutes(3).plusSeconds(4));
        assertEquals(Utils.renderChapter("<DURATION>", null, context), "25:03:04");
    }
}

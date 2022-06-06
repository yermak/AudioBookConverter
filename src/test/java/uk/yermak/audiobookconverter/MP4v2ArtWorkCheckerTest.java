package uk.yermak.audiobookconverter;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

public class MP4v2ArtWorkCheckerTest {

    @Test
    public void testParseMP4v2ArtList() {
        String out= "IDX     BYTES  CRC32     TYPE       FILE\n" +
                "----------------------------------------------------------------------\n" +
                "  0    622631  13f74ccc  implicit   ./File, Part 1.m4b\n" +
                "  1    379847  ea048b8d  jpeg\n" +
                "  2     98468  b5ffd5c8  implicit\n" +
                "  3   1836956  0ecf599e  png";

        List<String> formats = MP4v2ArtWorkChecker.parseMP4v2ArtList(out);
        assertEquals(formats.size(), 4);
        assertEquals(formats.get(0), "dat");
        assertEquals(formats.get(1), "jpg");
        assertEquals(formats.get(2), "dat");
        assertEquals(formats.get(3), "png");
    }
}
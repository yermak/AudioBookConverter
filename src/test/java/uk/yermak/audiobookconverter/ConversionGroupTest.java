package uk.yermak.audiobookconverter;

import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.formats.Format;

import static org.testng.Assert.assertEquals;

public class ConversionGroupTest {

    @Test
    public void destinationNamesIgnoreExtensionCase() {
        assertEquals(ConversionGroup.destination("C:/books/Book.m4b", Format.M4B, ", Part 2"), "C:/books/Book, Part 2.m4b");
        assertEquals(ConversionGroup.destination("C:/books/Book.M4B", Format.M4B, ", Part 2"), "C:/books/Book, Part 2.m4b");
        assertEquals(ConversionGroup.destination("C:/books/Book", Format.M4B, ", Part 2"), "C:/books/Book, Part 2.m4b");
        assertEquals(ConversionGroup.destination("C:/books/Dr. Who", Format.M4B, ""), "C:/books/Dr. Who.m4b");
        assertEquals(ConversionGroup.destination("C:/x.m4b/Book.m4b", Format.M4B, ", Part 1"), "C:/x.m4b/Book, Part 1.m4b");
    }
}

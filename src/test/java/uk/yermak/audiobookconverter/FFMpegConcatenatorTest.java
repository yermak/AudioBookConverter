package uk.yermak.audiobookconverter;

import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class FFMpegConcatenatorTest {

    @Test
    public void apostropheInPathIsEscaped() throws IOException {
        File list = FFMpegConcatenator.prepareFiles(1, List.of("C:\\Users\\O'Brien\\Temp\\a.m4b"), "out.m4b");
        try {
            assertEquals(Files.readAllLines(list.toPath()), List.of("file 'C:\\Users\\O'\\''Brien\\Temp\\a.m4b'"));
        } finally {
            Files.deleteIfExists(list.toPath());
        }
    }
}

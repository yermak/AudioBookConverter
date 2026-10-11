package uk.yermak.audiobookconverter.formats;

import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

import static org.testng.Assert.assertEquals;

public class FlacPictureTest {

    @Test
    public void loadReleasesTheFile() throws Exception {
        File poster = File.createTempFile("poster", ".png");
        ImageIO.write(new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB), "png", poster);

        FlacPicture picture = FlacPicture.load(poster.getPath());

        assertEquals(picture.width(), 3);
        assertEquals(picture.mime(), "image/png");
        Files.delete(poster.toPath()); // fails on Windows while a stream is still open
    }
}

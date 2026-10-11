package uk.yermak.audiobookconverter.loaders;

import javafx.scene.image.Image;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;

public class ArtWorkImageTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void rejectsUndecodableImage() {
        new ArtWorkImage(new Image(new ByteArrayInputStream("not an image".getBytes())));
    }
}

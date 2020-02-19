package uk.yermak.audiobookconverter;

import javafx.collections.ObservableList;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import static org.testng.Assert.*;

/**
 * Created by yermak on 03-Dec-18.
 */
public class FFMediaLoaderTest {

    @org.testng.annotations.Test
    public void testAddPosterIfMissing() {
        ConversionContext context = ConverterApplication.getContext();
        ObservableList<ArtWork> posters = context.getPosters();

        ArtWorkBean art1 = new ArtWorkBean("", "", 1);
        context.addPosterIfMissing(art1);
        assertTrue(posters.contains(art1));
        assertEquals(posters.size(), 1);

        ArtWorkBean art2 = new ArtWorkBean("", "", 2);

        context.addPosterIfMissing(art2);
        assertTrue(posters.contains(art2));
        assertEquals(posters.size(), 2);

        ArtWorkBean art22 = new ArtWorkBean("", "", 2);

        context.addPosterIfMissing(art22);
        assertFalse(posters.contains(art22));
        assertEquals(posters.size(), 2);

    }
}
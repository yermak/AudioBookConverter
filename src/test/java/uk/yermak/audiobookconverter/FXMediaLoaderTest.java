package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static org.testng.Assert.*;

/**
 * Created by yermak on 03-Dec-18.
 */
public class FXMediaLoaderTest {

    @org.testng.annotations.Test
    public void testAddPosterIfMissing() {
        ObservableList<ArtWork> posters = FXCollections.observableArrayList();

        ArtWorkBean art1 = new ArtWorkBean("", 1);
        FXMediaLoader.addPosterIfMissing(art1, posters);
        assertTrue(posters.contains(art1));
        assertEquals(posters.size(), 1);

        ArtWorkBean art2 = new ArtWorkBean("", 2);

        FXMediaLoader.addPosterIfMissing(art2, posters);
        assertTrue(posters.contains(art2));
        assertEquals(posters.size(), 2);

        ArtWorkBean art22 = new ArtWorkBean("", 2);

        FXMediaLoader.addPosterIfMissing(art22, posters);
        assertFalse(posters.contains(art22));
        assertEquals(posters.size(), 2);

    }
}
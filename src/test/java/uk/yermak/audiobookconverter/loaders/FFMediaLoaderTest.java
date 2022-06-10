package uk.yermak.audiobookconverter.loaders;

import javafx.collections.ObservableList;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.book.ArtWork;
import uk.yermak.audiobookconverter.book.AudioBookInfo;
import uk.yermak.audiobookconverter.book.MediaInfoBean;
import uk.yermak.audiobookconverter.book.Track;
import uk.yermak.audiobookconverter.fx.ConversionContext;
import uk.yermak.audiobookconverter.loaders.ArtWorkBean;
import uk.yermak.audiobookconverter.loaders.FFMediaLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by yermak on 03-Dec-18.
 */
public class FFMediaLoaderTest {

    @org.testng.annotations.Test
    public void testAddPosterIfMissing() {
        ConversionContext context = AudiobookConverter.getContext();
        ObservableList<ArtWork> posters = context.getPosters();

        ArtWorkBean art1 = new ArtWorkBean("", 1);
        context.addPosterIfMissing(art1);
        assertTrue(posters.contains(art1));
        assertEquals(posters.size(), 1);

        ArtWorkBean art2 = new ArtWorkBean("", 2);

        context.addPosterIfMissing(art2);
        assertTrue(posters.contains(art2));
        assertEquals(posters.size(), 2);

        ArtWorkBean art22 = new ArtWorkBean("", 2);

        context.addPosterIfMissing(art22);
        assertFalse(posters.contains(art22));
        assertEquals(posters.size(), 2);

    }


    @Test
    public void testParseCueChapters() throws IOException {
        InputStream stream = this.getClass().getResourceAsStream("/Брамс.cue");
        String cue = IOUtils.toString(stream, "cp1251");
        AudioBookInfo bookInfo = AudioBookInfo.instance();
        MediaInfoBean mediaInfo = new MediaInfoBean("test");
        mediaInfo.setBookInfo(bookInfo);
        mediaInfo.setDuration(2305071);
        FFMediaLoader.parseCue(mediaInfo, cue);
        assertEquals(bookInfo.genre().get(), "Classical");
        assertEquals(bookInfo.title().get(), "Брамс");
        assertEquals(bookInfo.year().get(), "2007");
        assertEquals(bookInfo.narrator().get(), "DeAgostini Classica");
        List<Track> tracks = bookInfo.tracks();
        assertEquals(tracks.size(), 3);

        Track track1 = tracks.get(0);
        assertEquals(track1.getStart(), 426);
        assertEquals(track1.getEnd(), 1321093);
        assertEquals(track1.getTrackNo(),"01 AUDIO");
        assertEquals(track1.getTitle(), "Фортепианный концерт ре минор соч. 15 - Maestoso");
        assertEquals(track1.getWriter(), "DeAgostini Classica 1");

        Track track2 = tracks.get(1);
        assertEquals(track2.getStart(), 1321093);
        assertEquals(track2.getEnd(), 2205093);
        assertEquals(track2.getTitle(), "Фортепианный концерт ре минор соч. 15 - Adagio");
        assertEquals(track2.getWriter(), "DeAgostini Classica 2");

        Track track3 = tracks.get(2);
        assertEquals(track3.getStart(), 2205093);
        assertEquals(track3.getEnd(), 2305071);
        assertEquals(track3.getTitle(), "Фортепианный концерт ре минор соч. 15 - Rondo. Allegro ma non troppo");
        assertEquals(track3.getWriter(), "DeAgostini Classica 3");
    }
}
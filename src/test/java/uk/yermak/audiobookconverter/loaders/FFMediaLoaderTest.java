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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
        FlacLoader.parseCue(mediaInfo, cue);
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

    @Test
    public void sidecarCueReplacesEmbeddedChapters() throws IOException {
        Path dir = Files.createTempDirectory("abc-cue");
        Files.writeString(dir.resolve("album.cue"), """
                TITLE "Album"
                FILE "album.flac" WAVE
                  TRACK 01 AUDIO
                    TITLE "First"
                    INDEX 01 00:00:00
                  TRACK 02 AUDIO
                    TITLE "Second"
                    INDEX 01 01:00:00
                """);
        MediaInfoBean mediaInfo = new MediaInfoBean(dir.resolve("album.flac").toString());
        AudioBookInfo bookInfo = AudioBookInfo.instance();
        mediaInfo.setBookInfo(bookInfo);
        mediaInfo.setDuration(120_000);
        bookInfo.tracks().add(new Track("001")); // as added from the embedded CUESHEET chapters
        bookInfo.tracks().add(new Track("002"));

        FlacLoader.parseCueChapters(mediaInfo);

        assertEquals(bookInfo.tracks().stream().map(Track::getTitle).toList(), List.of("First", "Second"));
        assertEquals(bookInfo.title().get(), "Album");
    }

    @Test
    public void keywordInsideATitleIsNoKeyword() {
        MediaInfoBean mediaInfo = new MediaInfoBean("test");
        AudioBookInfo bookInfo = AudioBookInfo.instance();
        mediaInfo.setBookInfo(bookInfo);
        mediaInfo.setDuration(120_000);

        FlacLoader.parseCue(mediaInfo, """
                TITLE "Back on TRACK"
                  TRACK 01 AUDIO
                    TITLE "The DATE of PERFORMER"
                    INDEX 01 00:00:00
                """);

        assertEquals(bookInfo.title().get(), "Back on TRACK");
        assertEquals(bookInfo.tracks().size(), 1);
        assertEquals(bookInfo.tracks().get(0).getTitle(), "The DATE of PERFORMER");
        assertNull(bookInfo.tracks().get(0).getWriter());
    }

    @Test
    public void cueReadAsUtf8OrElseAnsi() throws IOException {
        Path dir = Files.createTempDirectory("abc-cue");
        Path utf8 = dir.resolve("utf8.cue");
        Files.write(utf8, ("﻿TITLE \"Брамс\"").getBytes(StandardCharsets.UTF_8));
        Path ansi = dir.resolve("ansi.cue");
        byte[] ansiBytes = "TITLE \"Брамс\"".getBytes(java.nio.charset.Charset.forName("cp1251")); // not valid UTF-8
        Files.write(ansi, ansiBytes);

        assertEquals(FlacLoader.readCue(utf8.toFile()), "TITLE \"Брамс\"");
        assertEquals(FlacLoader.readCue(ansi.toFile()), new String(ansiBytes, java.nio.charset.Charset.forName(System.getProperty("native.encoding"))));
    }

    @Test
    public void extractedCoverFoundWhateverItsExtension() throws IOException {
        Path dir = Files.createTempDirectory("abc-mp4art");
        touch(dir.resolve("book.art[0].jpg")); // listed as "implicit", written with the detected type
        touch(dir.resolve("book.art[1].png"));

        assertEquals(MP4v2ArtWorkExtractor.extractedFile(dir.resolve("book.m4b").toString(), 0), dir.resolve("book.art[0].jpg").toFile());
    }

    private static void touch(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        Files.createFile(file);
    }

    @Test
    public void findsCoversCaseInsensitivelyInFolderAndOneLevelBelow() throws IOException {
        Path dir = Files.createTempDirectory("abc-covers");
        touch(dir.resolve("Scans/back.png"));
        touch(dir.resolve("COVER.JPG"));
        touch(dir.resolve("Scans/Other Book/cover.jpg"));
        touch(dir.resolve("notes.txt"));

        List<String> found = FFMediaLoader.findPictures(dir.toFile()).stream()
                .map(f -> dir.relativize(f.toPath()).toString().replace(File.separatorChar, '/'))
                .toList();

        assertEquals(found, List.of("COVER.JPG", "Scans/back.png"));
    }
}
package uk.yermak.audiobookconverter.loaders;

import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.book.MediaInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.testng.Assert.*;

public class MediaInfoProxyTest {

    private static MediaInfoProxy failed(String fileName) {
        return new MediaInfoProxy(fileName, CompletableFuture.failedFuture(new RuntimeException("ffprobe failed")));
    }

    @Test
    public void failedProbeCanBeComparedAndRemoved() {
        MediaInfoProxy a = failed("a.mp3");
        MediaInfoProxy sameFile = failed("a.mp3");
        MediaInfoProxy b = failed("b.mp3");

        assertEquals(a, sameFile);
        assertEquals(a.hashCode(), sameFile.hashCode());
        assertNotEquals(a, b);

        List<MediaInfo> list = new ArrayList<>(List.of(a, b));
        list.removeAll(List.of(b));
        assertEquals(list, List.of(a));
    }
}

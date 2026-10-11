package uk.yermak.audiobookconverter.loaders;

import org.testng.annotations.Test;
import uk.yermak.audiobookconverter.book.MediaInfo;
import uk.yermak.audiobookconverter.book.MediaInfoBean;
import uk.yermak.audiobookconverter.book.MediaTrackAdaptor;
import uk.yermak.audiobookconverter.book.Track;

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

    @Test
    public void frozenSpeedReachesTracksOfTheFile() {
        MediaInfoBean bean = new MediaInfoBean("book.m4b");
        bean.setDuration(3_000_000);
        MediaInfoProxy file = new MediaInfoProxy("book.m4b", CompletableFuture.completedFuture(bean));
        Track track = new Track("1");
        track.setStart(0);
        track.setEnd(1_500_000);
        MediaTrackAdaptor chapter = new MediaTrackAdaptor(file, track);

        // detach freezes the file, the speed reaches the chapter through it; no UI context is needed after that
        file.freezeSpeed(1.5);

        assertEquals(file.getDuration(), 2_000_000);
        assertEquals(chapter.getDuration(), 1_000_000);
    }
}

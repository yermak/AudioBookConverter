package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by yermak on 1/11/2018.
 */
public class FFMpegArtWorkLoader {

    private int fileId;
    private String format;
    private long jobId;
    private MediaInfo mediaInfo;
    private static final String FFMPEG = new File("external/x64/ffmpeg.exe").getAbsolutePath();

    public FFMpegArtWorkLoader(MediaInfo mediaInfo) {
        this.mediaInfo = mediaInfo;
    }


    public void loadArtWork() {
        Process pictureProcess = null;


        try {
            fileId = mediaInfo.hashCode();
            String poster = Utils.getTmp(jobId, fileId, "." + format);

            ProcessBuilder pictureProcessBuilder = new ProcessBuilder(FFMPEG,
                    "-i", mediaInfo.getFileName(),
                    poster);
            pictureProcess = pictureProcessBuilder.start();

            StreamCopier pictureToOut = new StreamCopier(pictureProcess.getInputStream(), System.out);
            Future<Long> pictureFuture = Executors.newWorkStealingPool().submit(pictureToOut);
            // not using redirectErrorStream() as sometimes error stream is not closed by process which cause feature to hang indefinitely
            StreamCopier pictureToErr = new StreamCopier(pictureProcess.getErrorStream(), System.err);
            Future<Long> errFuture = Executors.newWorkStealingPool().submit(pictureToErr);
            pictureFuture.get();
            File posterFile = new File(poster);
            long crc32 = FileUtils.checksumCRC32(posterFile);
        } catch (IOException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            if (pictureProcess != null) pictureProcess.destroy();
        }
    }
}

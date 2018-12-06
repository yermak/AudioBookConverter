package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegConcatenator implements Concatenator {

    private Conversion conversion;
    private final String outputFileName;
    private final StatusChangeListener listener;
    private String metaDataFileName;
    private String fileListFileName;
    private ProgressCallback callback;
    private ProgressParser progressParser;
    private static final String FFMPEG = new File("external/x64/ffmpeg.exe").getAbsolutePath();


    public FFMpegConcatenator(Conversion conversion, String outputFileName, String metaDataFileName, String fileListFileName, ProgressCallback callback) {
        this.conversion = conversion;
        this.outputFileName = outputFileName;
        this.metaDataFileName = metaDataFileName;
        this.fileListFileName = fileListFileName;
        this.callback = callback;
        listener = new StatusChangeListener();
        conversion.addStatusChangeListener(listener);
    }

    public void concat() throws IOException, InterruptedException {
        if (listener.isCancelled()) return;
        while (listener.isPaused()) Thread.sleep(1000);
        callback.reset();
        try {
            progressParser = new TcpProgressParser(progress -> {
                callback.converted(progress.out_time_ns / 1000000, progress.total_size);
            });
            progressParser.start();
        } catch (URISyntaxException e) {
        }

        Process process = null;
        try {

            ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder(FFMPEG,
                    "-protocol_whitelist", "file,pipe,concat",
                    "-vn",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", fileListFileName,
                    "-i", metaDataFileName,
                    "-map_metadata", "1",
                    "-f", "ipod",
                    "-c:a", "copy",
                    "-movflags", "+faststart",
                    "-progress", progressParser.getUri().toString(),
                    outputFileName);

            process = ffmpegProcessBuilder.start();

            StreamCopier.copy(process.getInputStream(), System.out);
            StreamCopier.copy(process.getErrorStream(), System.err);

            boolean finished = false;
            while (!listener.isCancelled() && !finished) {
                finished = process.waitFor(500, TimeUnit.MILLISECONDS);
            }

        } finally {
            Utils.closeSilently(process);
            Utils.closeSilently(progressParser);
        }
    }
}

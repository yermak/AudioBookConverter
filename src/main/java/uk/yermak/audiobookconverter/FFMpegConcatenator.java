package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.StateListener;
import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.*;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegConcatenator implements Concatenator, StateListener {

    private final String outputFileName;
    private String metaDataFileName;
    private String fileListFileName;
    private ProgressCallback callback;
    private boolean cancelled;
    private boolean paused;
    private ProgressParser progressParser;
    private static final String FFMPEG = new File("external/x64/ffmpeg.exe").getAbsolutePath();



    public FFMpegConcatenator(String outputFileName, String metaDataFileName, String fileListFileName, ProgressCallback callback) {
        this.outputFileName = outputFileName;
        this.metaDataFileName = metaDataFileName;
        this.fileListFileName = fileListFileName;
        this.callback = callback;
        StateDispatcher.getInstance().addListener(this);

    }

    public void concat() throws IOException, ExecutionException, InterruptedException {
        if (cancelled) return;
        while (paused) Thread.sleep(1000);
        callback.reset();
        try {
            progressParser = new TcpProgressParser(progress -> {
                callback.converted(progress.out_time_ns / 1000000, progress.total_size);
            });
            progressParser.start();
        } catch (URISyntaxException e) {
        }

        Process ffmpegProcess = null;
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
                    "-progress", progressParser.getUri().toString(),
                    outputFileName);

            ffmpegProcess = ffmpegProcessBuilder.start();

            StreamCopier ffmpegToOut = new StreamCopier(ffmpegProcess.getInputStream(), System.out);
            Future<Long> ffmpegFuture = Executors.newWorkStealingPool().submit(ffmpegToOut);
            StreamCopier ffmpegToErr = new StreamCopier(ffmpegProcess.getErrorStream(), System.err);
            Future<Long> ffmpegErrFuture = Executors.newWorkStealingPool().submit(ffmpegToErr);

            while (!cancelled) {
                try {
                    ffmpegFuture.get(500, TimeUnit.MILLISECONDS);
                    break;
                } catch (TimeoutException ignored) {
                }
            }

        } finally {
            if (ffmpegProcess != null) ffmpegProcess.destroy();
            if (progressParser != null) progressParser.stop();
        }

    }

    @Override
    public void finishedWithError(String error) {

    }

    @Override
    public void finished() {

    }

    @Override
    public void canceled() {
        cancelled = true;
    }

    @Override
    public void paused() {
        paused = true;
    }

    @Override
    public void resumed() {
        paused = false;
    }

    @Override
    public void fileListChanged() {

    }

    @Override
    public void modeChanged(ConversionMode mode) {

    }
}

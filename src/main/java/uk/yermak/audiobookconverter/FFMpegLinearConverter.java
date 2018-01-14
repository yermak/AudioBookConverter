package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.StateListener;
import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.*;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegLinearConverter implements Concatenator, StateListener {

    private final String outputFileName;
    private String metaDataFileName;
    private String fileListFileName;
    private MediaInfo mediaInfo;
    private boolean cancelled = false;
    private boolean paused = false;
    private Process ffmpegProcess;
    private ProgressParser progressParser;
    private ProgressCallback callback;


    public FFMpegLinearConverter(String outputFileName, String metaDataFileName, String fileListFileName, MediaInfo mediaInfo, ProgressCallback callback) {
        this.outputFileName = outputFileName;
        this.metaDataFileName = metaDataFileName;
        this.fileListFileName = fileListFileName;
        this.mediaInfo = mediaInfo;
        this.callback = callback;
        StateDispatcher.getInstance().addListener(this);
    }

    public void concat() throws IOException, ExecutionException, InterruptedException {
        if (cancelled) return;
        while (paused) Thread.sleep(1000);

        try {
            progressParser = new TcpProgressParser(progress -> {
                callback.converted(progress.out_time_ns / 1000000, progress.total_size);
                if (progress.isEnd()) {
                    callback.completedConversion();
                }
            });
            progressParser.start();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        try {
            ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe",
                    "-protocol_whitelist", "file,pipe,concat",
                    "-vn",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", fileListFileName,
                    "-i", metaDataFileName,
                    "-map_metadata", "1",
                    "-ar", String.valueOf(mediaInfo.getFrequency()),
                    "-ac", String.valueOf(mediaInfo.getChannels()),
                    "-b:a", String.valueOf(mediaInfo.getBitrate()),
                    "-f", "ipod",
                    "-codec:a", "libfdk_aac",
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
        if (ffmpegProcess != null) {
            ffmpegProcess.destroy();
        }
    }

    @Override
    public void paused() {

    }

    @Override
    public void resumed() {

    }

    @Override
    public void fileListChanged() {

    }

    @Override
    public void modeChanged(ConversionMode mode) {

    }
}

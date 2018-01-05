package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.StateListener;
import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.*;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegConverter implements Callable<ConverterOutput>, Converter, StateListener {
    private MediaInfo mediaInfo;
    private final String outputFileName;
    private ProgressCallback callback;
    private final static Semaphore mutex = new Semaphore(Runtime.getRuntime().availableProcessors() + 1);
    private boolean cancelled;
    private boolean paused;
    private Process process;

    public FFMpegConverter(MediaInfo mediaInfo, String outputFileName, ProgressCallback callback) {
        this.mediaInfo = mediaInfo;
        this.outputFileName = outputFileName;
        this.callback = callback;
        StateDispatcher.getInstance().addListener(this);
    }

    public ConverterOutput convertMp3toM4a() throws IOException, InterruptedException, ExecutionException {
        ProgressParser progressParser = null;
        try {
            mutex.acquire();
            if (cancelled) return null;
            while (paused) Thread.sleep(1000);


            progressParser = new TcpProgressParser(progress -> {
                callback.converted(progress.out_time_ns / 1000000, progress.total_size);
                if (progress.isEnd()) {
                    callback.completedConversion();
                }
            });
            progressParser.start();

            ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe",
                    "-i", mediaInfo.getFileName(),
                    "-vn",
                    "-codec:a", "libfdk_aac",
                    "-f", "ipod",
                    "-b:a", String.valueOf(mediaInfo.getBitrate()),
                    "-ar", String.valueOf(mediaInfo.getFrequency()),
                    "-ac", String.valueOf(mediaInfo.getChannels()),
                    "-progress", progressParser.getUri().toString(),
                    outputFileName
            );

            process = ffmpegProcessBuilder.start();

            InputStream ffmpegIn = process.getInputStream();
            InputStream ffmpegErr = process.getErrorStream();

            StreamCopier ffmpegToConsole = new StreamCopier(ffmpegIn, System.out);
            Future<Long> ffmpegFuture = Executors.newWorkStealingPool().submit(ffmpegToConsole);
            StreamCopier ffmpegToErr = new StreamCopier(ffmpegErr, System.err);
            Future<Long> ffmpegErrFuture = Executors.newWorkStealingPool().submit(ffmpegToErr);

            ffmpegFuture.get();

            return new ConverterOutput(mediaInfo, outputFileName);

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            mutex.release();
            if (process != null) {
                process.destroy();
            }
            if (progressParser != null) {
                progressParser.stop();
            }
        }
    }

    @Override
    public ConverterOutput call() throws Exception {
        return convertMp3toM4a();
    }

    @Override
    public void finishedWithError(String error) {

    }

    @Override
    public void finished() {

    }

    @Override
    public void canceled() {
        this.cancelled = true;
        if (process != null) {
            process.destroy();
        }
    }

    @Override
    public void paused() {
        this.paused = true;
    }

    @Override
    public void resumed() {
        this.paused = false;
    }

    @Override
    public void fileListChanged() {

    }

    @Override
    public void modeChanged(ConversionMode mode) {

    }
}

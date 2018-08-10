package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.concurrent.*;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegConverter implements Callable<ConverterOutput>, Converter, StateListener {
    private final ExecutorService executorService = Executors.newWorkStealingPool();
    private MediaInfo mediaInfo;
    private final String outputFileName;
    private ProgressCallback callback;
    private final static Semaphore mutex = new Semaphore(Runtime.getRuntime().availableProcessors() + 1);
    private boolean cancelled;
    private boolean paused;
    private Process process;
    private final static String FFMPEG = new File("external/x64/ffmpeg.exe").getAbsolutePath();
    private ProgressParser progressParser = null;


    public FFMpegConverter(MediaInfo mediaInfo, String outputFileName, ProgressCallback callback) {
        this.mediaInfo = mediaInfo;
        this.outputFileName = outputFileName;
        this.callback = callback;
        StateDispatcher.getInstance().addListener(this);
    }

    public ConverterOutput convertMp3toM4a() throws IOException, InterruptedException, ExecutionException {
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

            ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder(FFMPEG,
                    "-i", mediaInfo.getFileName(),
                    "-vn",
                    "-codec:a", "libfdk_aac",
                    "-f", "ipod",
//                    "-vbr","3 ",
//                    "-b:a", String.valueOf(mediaInfo.getBitrate()),
                    "-ar", String.valueOf(mediaInfo.getFrequency()),
                    "-ac", String.valueOf(mediaInfo.getChannels()),
                    "-progress", progressParser.getUri().toString(),
                    outputFileName
            );

            process = ffmpegProcessBuilder.start();

            InputStream ffmpegIn = process.getInputStream();
            InputStream ffmpegErr = process.getErrorStream();

            StreamCopier ffmpegToConsole = new StreamCopier(ffmpegIn, System.out);
            Future<Long> ffmpegFuture = executorService.submit(ffmpegToConsole);
            StreamCopier ffmpegToErr = new StreamCopier(ffmpegErr, System.err);
            Future<Long> ffmpegErrFuture = executorService.submit(ffmpegToErr);

            ffmpegFuture.get();

            return new ConverterOutput(mediaInfo, outputFileName);
        } catch (CancellationException ce) {
            return null;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            mutex.release();
            Utils.closeSilently(process);
            Utils.closeSilently(progressParser);
        }
    }

    @Override
    public ConverterOutput call() throws Exception {
        return convertMp3toM4a();
    }

    @Override
    public void finishedWithError(String error) {
        Utils.closeSilently(executorService);
//        Utils.closeSilently(progressParser);
    }

    @Override
    public void finished() {
        Utils.closeSilently(executorService);
//        Utils.closeSilently(progressParser);
    }

    @Override
    public void canceled() {
        this.cancelled = true;
        Utils.closeSilently(executorService);
//        Utils.closeSilently(progressParser);
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

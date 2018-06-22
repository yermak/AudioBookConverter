package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegConcatenator implements Concatenator {

    private final String outputFileName;
    private final ExecutorService executorService = Executors.newWorkStealingPool();
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
        ConverterApplication.getContext().getConversion().addStatusChangeListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case CANCELLED:
                    cancelled = true;
                    break;
                case PAUSED:
                    paused = true;
                    break;
            }
        });
    }

    public void concat() throws IOException, InterruptedException {
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

            StreamCopier.copy(ffmpegProcess.getInputStream(), System.out);
            StreamCopier.copy(ffmpegProcess.getErrorStream(), System.err);

            for (boolean finished = false; !cancelled && !finished; finished = ffmpegProcess.waitFor(500, TimeUnit.MILLISECONDS))
                ;

        } finally {
            Utils.closeSilently(ffmpegProcess);
            Utils.closeSilently(progressParser);
        }
    }
}

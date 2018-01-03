package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.StateListener;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegConcatenator implements Concatenator, StateListener {

    private final List<ConverterOutput> outputs;
    private final String outputFileName;
    private String metaDataFileName;
    private String fileListFileName;
    private boolean cancelled;
    private boolean paused;


    public FFMpegConcatenator(List<ConverterOutput> outputs, String outputFileName, String metaDataFileName, String fileListFileName) {
        this.outputs = outputs;
        this.outputFileName = outputFileName;
        this.metaDataFileName = metaDataFileName;
        this.fileListFileName = fileListFileName;
    }

    public void concat() throws IOException, ExecutionException, InterruptedException {
        if (cancelled) return;
        while (paused) Thread.sleep(1000);

        Process ffmpegProcess = null;
        try {
            ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe",
                    "-protocol_whitelist", "file,pipe,concat",
                    "-vn",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", fileListFileName,
                    "-i", metaDataFileName,
                    "-map_metadata", "1",
                    "-f", "ipod",
                    "-c:a", "copy",
                    outputFileName);


            ffmpegProcess = ffmpegProcessBuilder.start();

            StreamCopier ffmpegToOut = new StreamCopier(ffmpegProcess.getInputStream(), System.out);
            Future<Long> ffmpegFuture = Executors.newWorkStealingPool().submit(ffmpegToOut);
            StreamCopier ffmpegToErr = new StreamCopier(ffmpegProcess.getErrorStream(), System.err);
            Future<Long> ffmpegErrFuture = Executors.newWorkStealingPool().submit(ffmpegToErr);

            while (!cancelled) {
                try {
                    ffmpegFuture.get(100, TimeUnit.MILLISECONDS);
                    break;
                } catch (TimeoutException ignored) {
                }
            }

        } finally {
            if (ffmpegProcess != null) ffmpegProcess.destroy();
            for (ConverterOutput output : outputs) {
                FileUtils.deleteQuietly(new File(output.getOutputFileName()));
            }

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

    }

    @Override
    public void paused() {

    }

    @Override
    public void resumed() {

    }
}

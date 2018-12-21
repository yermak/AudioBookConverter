package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegLinearConverter implements Concatenator {

    private Conversion conversion;
    private final String outputFileName;
    private final StatusChangeListener listener;
    private String metaDataFileName;
    private String fileListFileName;
    private Process ffmpegProcess;
    private ProgressParser progressParser;
    private OutputParameters outputParameters;
    private ProgressCallback callback;


    public FFMpegLinearConverter(Conversion conversion, String outputFileName, String metaDataFileName, String fileListFileName, OutputParameters outputParameters, ProgressCallback callback) {
        this.conversion = conversion;
        this.outputFileName = outputFileName;
        this.metaDataFileName = metaDataFileName;
        this.fileListFileName = fileListFileName;
        this.outputParameters = outputParameters;
        this.callback = callback;
        listener = new StatusChangeListener();
        conversion.addStatusChangeListener(listener);
    }

    public void concat() throws IOException, InterruptedException {
        if (listener.isCancelled()) return;
        while (listener.isPaused()) Thread.sleep(1000);

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
                    "-f", "concat",
                    "-safe", "0",
                    "-i", fileListFileName,
                    "-i", metaDataFileName,
                    "-map_metadata", "1",
                    "-vn",
                    outputParameters.getFFMpegQualityParameter(), outputParameters.getFFMpegQualityValue(),
                    "-ar", outputParameters.getFFMpegFrequencyValue(),
                    "-ac", outputParameters.getFFMpegChannelsValue(),
                    "-cutoff", outputParameters.getCutoffValue(),
                    "-f", "ipod",
                    "-codec:a", "libfdk_aac",
                    "-codec:v", "copy",
                    "-progress", progressParser.getUri().toString(),
                    outputFileName);

            ffmpegProcess = ffmpegProcessBuilder.start();

            StreamCopier.copy(ffmpegProcess.getInputStream(), System.out);
            StreamCopier.copy(ffmpegProcess.getErrorStream(), System.err);
            boolean finished = false;
            while (!listener.isCancelled() && !finished) {
                finished = ffmpegProcess.waitFor(500, TimeUnit.MILLISECONDS);
            }
        } finally {
            conversion.removeStatusChangeListener(listener);
            Utils.closeSilently(ffmpegProcess);
            Utils.closeSilently(progressParser);
        }

    }

}

package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegConcatenator {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Conversion conversion;
    private final String outputFileName;

    private String metaDataFileName;
    private String fileListFileName;
    private ProgressCallback callback;
    private ProgressParser progressParser;


    public FFMpegConcatenator(Conversion conversion, String outputFileName, String metaDataFileName, String fileListFileName, ProgressCallback callback) {
        this.conversion = conversion;
        this.outputFileName = outputFileName;
        this.metaDataFileName = metaDataFileName;
        this.fileListFileName = fileListFileName;
        this.callback = callback;
    }

    public void concat() throws IOException, InterruptedException {
        if (conversion.getStatus().isOver()) return;
        while (ProgressStatus.PAUSED.equals(conversion.getStatus())) Thread.sleep(1000);
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
            OutputParameters outputParameters = conversion.getOutputParameters();
            ProcessBuilder ffmpegProcessBuilder;
            List<String> concatOptions = outputParameters.getConcatOptions(fileListFileName, metaDataFileName, progressParser.getUri().toString(), outputFileName);
            ffmpegProcessBuilder = new ProcessBuilder(concatOptions);
            logger.debug("Starting concat with options {}", String.join("\n", concatOptions));
            process = ffmpegProcessBuilder.start();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamCopier.copy(process.getInputStream(), out);
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            StreamCopier.copy(process.getErrorStream(), err);

            boolean finished = false;
            while (!conversion.getStatus().isOver() && !finished) {
                finished = process.waitFor(500, TimeUnit.MILLISECONDS);
            }
            logger.debug("Concat Out: {}", out.toString());
            logger.error("Concat Error: {}", err.toString());

        } catch (Exception e) {
            logger.error("Error during concatination of files:", e);
        } finally {
            Utils.closeSilently(process);
            Utils.closeSilently(progressParser);
        }
    }
}

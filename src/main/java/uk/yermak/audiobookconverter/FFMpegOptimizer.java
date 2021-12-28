package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class FFMpegOptimizer {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ConversionJob conversionJob;

    private String tempFile;
    private final String outputFileName;
    private ProgressCallback callback;

    private ProgressParser progressParser;


    public FFMpegOptimizer(ConversionJob conversionJob, String tempFile, String outputFileName, ProgressCallback callback) {
        this.conversionJob = conversionJob;
        this.tempFile = tempFile;
        this.outputFileName = outputFileName;
        this.callback = callback;
    }


    String optimize() throws InterruptedException, IOException {
        while (ProgressStatus.PAUSED.equals(conversionJob.getStatus())) Thread.sleep(1000);
        callback.reset();
        callback.setState("Optimising...");
        try {
            progressParser = new TcpProgressParser(progress -> {
                callback.converted(progress.out_time_ns / 1000000, progress.total_size);
            });
            progressParser.start();
        } catch (URISyntaxException e) {
        }

        Process process = null;
        try {

            String tmp = Utils.getTmp(conversionJob.getConversionGroup().getJobId(), outputFileName.hashCode() + 1, conversionJob.getConversionGroup().getWorkfileExtension());
            String[] optimize;
            if (conversionJob.getConversionGroup().getPosters().isEmpty()) {
                optimize = new String[]{
                        Platform.FFMPEG,
                        "-i", tempFile,
                        "-c", "copy",
                        "-progress", progressParser.getUri().toString(),
                        "-movflags", "+faststart",
                        tmp
                };
            } else {
                optimize = new String[]{
                        Platform.FFMPEG,
                        "-i", tempFile,
                        "-map", "0:v",
                        "-map", "0:a",
                        "-c", "copy",
                        "-progress", progressParser.getUri().toString(),
                        "-movflags", "+faststart",
                        tmp
                };
            }

            logger.debug("Starting optimisation with options {}", String.join(" ", optimize));

            ProcessBuilder pb = new ProcessBuilder(optimize);
            process = pb.start();

//            process = Runtime.getRuntime().exec( String.join(" ", optimize));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamCopier.copy(process.getInputStream(), out);
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            StreamCopier.copy(process.getErrorStream(), err);

            boolean finished = false;
            while (!conversionJob.getStatus().isOver() && !finished) {
                finished = process.waitFor(500, TimeUnit.MILLISECONDS);
            }
            logger.debug("Optimize Out: {}", out);
            logger.error("Optimize Error: {}", err);

            if (process.exitValue() != 0) {
                throw new ConversionException("Optimisation exit code " + process.exitValue() + "!=0", new Error(err.toString()));
            }

            if (!new File(tmp).exists()) {
                throw new ConversionException("Optimisation failed, no output file:" + out, new Error(err.toString()));
            }
            return tmp;
        } catch (Exception e) {
            logger.error("Error during optimisation of resulting file:", e);
            throw new RuntimeException(e);
        } finally {
            Utils.closeSilently(process);
            Utils.closeSilently(progressParser);
        }
    }
}

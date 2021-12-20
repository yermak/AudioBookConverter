package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

public class FFMpegOptimizer {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ConversionJob conversionJob;

    private String tempFile;
    private final String outputFileName;

    private final ProgressCallback callback;
    private ProgressParser progressParser;


    public FFMpegOptimizer(ConversionJob conversionJob, String tempFile, String outputFileName, ProgressCallback callback) {
        this.conversionJob = conversionJob;
        this.tempFile = tempFile;
        this.outputFileName = outputFileName;
        this.callback = callback;
    }



    public void moveResultingFile() {
        try {
            File destFile = new File(outputFileName);
            optimize();
            if (destFile.exists()) FileUtils.deleteQuietly(destFile);
            FileUtils.moveFile(new File(Utils.getTmp(conversionJob.jobId, outputFileName.hashCode()+1, conversionJob.getConversionGroup().getWorkfileExtension())), destFile);
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to optimize resulting file", e);
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(new File(tempFile));
        }
    }

    private  void optimize() throws IOException, InterruptedException {
        if (conversionJob.getStatus().isOver()) return;
        while (ProgressStatus.PAUSED.equals(conversionJob.getStatus())) Thread.sleep(1000);
//        callback.reset();
//        try {
//            progressParser = new TcpProgressParser(progress -> {
//                callback.converted(progress.out_time_ns / 1000000, progress.total_size);
//            });
//            progressParser.start();
//        } catch (URISyntaxException e) {
//        }

        Process process = null;
        try {

            String[] optimize = {
                    Environment.FFMPEG,
                    "-i", tempFile,
                    "-map", "0:v",
                    "-map", "0:a",
                    "-c", "copy",
                    "-movflags", "+faststart",
                    Utils.getTmp(conversionJob.jobId, outputFileName.hashCode()+1, conversionJob.getConversionGroup().getWorkfileExtension())
            } ;

            logger.debug("Starting optimisation with options {}", String.join(" ", optimize));

            //falling back to Runtime.exec() due to JDK specific way of interpreting quoted arguments in ProcessBuilder https://bugs.openjdk.java.net/browse/JDK-8131908
            process = Runtime.getRuntime().exec( String.join(" ", optimize));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamCopier.copy(process.getInputStream(), out);
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            StreamCopier.copy(process.getErrorStream(), err);

            boolean finished = false;
            while (!conversionJob.getStatus().isOver() && !finished) {
                finished = process.waitFor(500, TimeUnit.MILLISECONDS);
            }
            logger.debug("Optimize Out: {}", out.toString());
            logger.error("Optimize Error: {}", err.toString());

            if (process.exitValue() != 0) {
                throw new ConversionException("Optimisation exit code " + process.exitValue() + "!=0", new Error(err.toString()));
            }

            if (!new File(Utils.getTmp(conversionJob.jobId, outputFileName.hashCode()+1, conversionJob.getConversionGroup().getWorkfileExtension())).exists()) {
                throw new ConversionException("Optimisation failed, no output file:" + out.toString(), new Error(err.toString()));
            }
        } catch (Exception e) {
            logger.error("Error during optimisation of resulting file:", e);
            throw new RuntimeException(e);
        } finally {
            Utils.closeSilently(process);
            Utils.closeSilently(progressParser);
        }



    }




}

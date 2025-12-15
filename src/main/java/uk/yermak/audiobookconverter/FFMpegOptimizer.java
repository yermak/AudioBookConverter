package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.formats.MP4Format;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class FFMpegOptimizer {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ConversionJob conversionJob;
    private final ResourceBundle resources;

    private String tempFile;
    private final String outputFileName;
    private ProgressCallback callback;


    public FFMpegOptimizer(ConversionJob conversionJob, String tempFile, String outputFileName, ProgressCallback callback, ResourceBundle resources) {
        this.conversionJob = conversionJob;
        this.tempFile = tempFile;
        this.outputFileName = outputFileName;
        this.callback = callback;
        this.resources = resources;
    }


    String optimize() throws InterruptedException {
        while (ProgressStatus.PAUSED.equals(conversionJob.getStatus())) Thread.sleep(1000);

        if (!conversionJob.getConversionGroup().getOutputParameters().getFormat().mp4Compatible()) {
            return outputFileName;
        }

        callback.reset(true);
        callback.setState(resources.getString("progress.state.optimizing"));
        Process process = null;
        try (ProgressParser progressParser = new TcpProgressParser(this::progress)) {

            progressParser.start();

            String optimizedFile = Utils.getTmp(conversionJob.getConversionGroup().getGroupId(), outputFileName.hashCode() + 1, conversionJob.getConversionGroup().getWorkfileExtension());

            MP4Format format = (MP4Format) conversionJob.getConversionGroup().getOutputParameters().getFormat();

            boolean hasPosters = !conversionJob.getConversionGroup().getPosters().isEmpty();
            String[] optimize = format.getOptimizeOptions(tempFile, progressParser, optimizedFile, hasPosters);
            logger.debug("Starting optimisation with options {}", String.join(" ", optimize));

            ProcessBuilder pb = new ProcessBuilder(optimize);
            process = pb.start();

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

            if (!new File(optimizedFile).exists()) {
                throw new ConversionException("Optimisation failed, no output file:" + out, new Error(err.toString()));
            }
            return optimizedFile;
        } catch (URISyntaxException e) {
            logger.error("Error during optimisation of resulting file:", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error("Error during optimisation of resulting file:", e);
            throw new RuntimeException(e);
        } finally {
            Utils.closeSilently(process);
        }
    }

    private void progress(Progress progress) {
        callback.converted(progress.out_time_ns / 1000000, progress.total_size);
    }
}

package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.book.MediaInfo;
import uk.yermak.audiobookconverter.formats.OutputParameters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegConcatenator {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ConversionJob conversionJob;
    private final String outputFileName;

    private List<MediaInfo> media;
    private final ProgressCallback callback;

    public FFMpegConcatenator(ConversionJob conversionJob, String outputFileName, List<MediaInfo> media, ProgressCallback callback) {
        this.conversionJob = conversionJob;
        this.outputFileName = outputFileName;
        this.media = media;
        this.callback = callback;
    }

    protected static File prepareFiles(long jobId, List<MediaInfo> media, String workfileExtension, String outputFileName) throws IOException {
        File fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId +"_"+ Objects.hash(outputFileName)+ ".txt");
        List<String> outFiles = media.stream().map(mediaInfo -> "file '" + Utils.getTmp(jobId, mediaInfo.getUID(), workfileExtension) + "'").collect(Collectors.toList());
        FileUtils.writeLines(fileListFile, "UTF-8", outFiles);
        return fileListFile;
    }

    public void concat() throws IOException, InterruptedException {
        if (conversionJob.getStatus().isOver()) return;
        String fileListFileName = prepareFiles(conversionJob.getConversionGroup().getGroupId(), media, conversionJob.getConversionGroup().getWorkfileExtension(), outputFileName).getAbsolutePath();

        while (ProgressStatus.PAUSED.equals(conversionJob.getStatus())) Thread.sleep(1000);
        callback.reset(false);

        Process process = null;

        try(ProgressParser progressParser = new TcpProgressParser(this::progress)){
            progressParser.start();

            OutputParameters outputParameters = conversionJob.getConversionGroup().getOutputParameters();
            List<String> concatOptions = outputParameters.getFormat().getConcatOptions(fileListFileName, outputFileName, progressParser.getUri().toString(), conversionJob);

            logger.info("Starting concat with options {}", String.join(" ", concatOptions));
            //using custom processes for Windows here -  Runtime.exec() due to JDK specific way of interpreting quoted arguments in ProcessBuilder https://bugs.openjdk.java.net/browse/JDK-8131908
            process = Platform.current.createProcess(concatOptions);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamCopier.copy(process.getInputStream(), out);
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            StreamCopier.copy(process.getErrorStream(), err);

            boolean finished = false;
            while (!conversionJob.getStatus().isOver() && !finished) {
                finished = process.waitFor(500, TimeUnit.MILLISECONDS);
            }
            logger.debug("Concat Out: {}", out);
            logger.error("Concat Error: {}", err);

            if (process.exitValue() != 0) {
                throw new ConversionException("Concatenation exit code " + process.exitValue() + "!=0", new Error(err.toString()));
            }

            if (!new File(outputFileName).exists()) {
                throw new ConversionException("Concatenation failed, no output file:" + out, new Error(err.toString()));
            }
        } catch (URISyntaxException e) {
            logger.error("Error during concatination of files:", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error("Error during concatination of files:", e);
            throw new RuntimeException(e);
        } finally {
            Utils.closeSilently(process);
            media.forEach(mediaInfo -> FileUtils.deleteQuietly(new File(Utils.getTmp(conversionJob.getConversionGroup().getGroupId(), mediaInfo.getUID(), conversionJob.getConversionGroup().getWorkfileExtension()))));
            FileUtils.deleteQuietly(new File(fileListFileName));
        }
    }

    private void progress(Progress progress) {
        callback.converted(progress.out_time_ns / 1000000, progress.total_size);
    }
}

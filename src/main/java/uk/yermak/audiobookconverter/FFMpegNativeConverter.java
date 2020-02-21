package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegNativeConverter implements Callable<ConverterOutput> {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Conversion conversion;
    private MediaInfo mediaInfo;
    private final String outputFileName;
    private ProgressCallback callback;
    private Process process;
    private final static String FFMPEG = new File("external/x64/ffmpeg.exe").getAbsolutePath();
    private ProgressParser progressParser = null;


    public FFMpegNativeConverter(Conversion conversion, MediaInfo mediaInfo, String outputFileName, ProgressCallback callback) {
        this.conversion = conversion;
        this.mediaInfo = mediaInfo;
        this.outputFileName = outputFileName;
        this.callback = callback;
    }

    public ConverterOutput convertMp3toM4a() throws IOException, InterruptedException {
        try {
            if (conversion.getStatus().isOver()) return null;
            while (ProgressStatus.PAUSED.equals(conversion.getStatus())) Thread.sleep(1000);


            progressParser = new TcpProgressParser(progress -> {
                callback.converted(progress.out_time_ns / 1000000, progress.total_size);
                if (progress.isEnd()) {
                    callback.completedConversion();
                }
            });
            progressParser.start();

            ProcessBuilder ffmpegProcessBuilder;
            OutputParameters outputParameters = conversion.getOutputParameters();
            if (outputParameters.isAuto()) {
                if (mediaInfo.getCodec().equals("aac")) {
                    logger.debug("Transcoding aac stream for {}", outputFileName);
                    ffmpegProcessBuilder = new ProcessBuilder(FFMPEG,
                            "-i", mediaInfo.getFileName(),
                            "-vn",
                            "-codec:a", "copy",
                            "-f", "ipod",
                            "-progress", progressParser.getUri().toString(),
                            outputFileName
                    );
                } else {
                    logger.debug("Re-encoding in auto mode to aac for {}", outputFileName);
                    ffmpegProcessBuilder = new ProcessBuilder(FFMPEG,
                            "-i", mediaInfo.getFileName(),
                            "-vn",
                            "-codec:a", "aac",
                            "-filter:a", outputParameters.getFiltersValue(),
                            "-f", "ipod",
                            "-progress", progressParser.getUri().toString(),
                            outputFileName
                    );
                }
            } else {
                logger.debug("Re-encoding in custom mode to aac for {}", outputFileName);
                ffmpegProcessBuilder = new ProcessBuilder(FFMPEG,
                        "-i", mediaInfo.getFileName(),
                        "-vn",
                        "-codec:a", "aac",
                        "-f", "ipod",
                        outputParameters.getFFMpegQualityParameter(), outputParameters.getFFMpegQualityValue(),
                        "-ar", String.valueOf(outputParameters.getFFMpegFrequencyValue()),
                        "-ac", String.valueOf(outputParameters.getFFMpegChannelsValue()),
                        "-filter:a", outputParameters.getFiltersValue(),
                        "-cutoff", outputParameters.getCutoffValue(),
                        "-progress", progressParser.getUri().toString(),
                        outputFileName
                );

            }

            process = ffmpegProcessBuilder.start();

            InputStream ffmpegIn = process.getInputStream();
            InputStream ffmpegErr = process.getErrorStream();

            StreamCopier.copy(ffmpegIn, System.out);
            StreamCopier.copy(ffmpegErr, System.err);

            boolean finished = false;
            while (!conversion.getStatus().isOver() && !finished) {
                finished = process.waitFor(500, TimeUnit.MILLISECONDS);
            }
            Mp4v2InfoLoader.updateDuration(mediaInfo, outputFileName);
            return new ConverterOutput(mediaInfo, outputFileName);
        } catch (CancellationException ce) {
            return null;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } finally {
            Utils.closeSilently(process);
            Utils.closeSilently(progressParser);
        }
    }

    @Override
    public ConverterOutput call() throws Exception {
        return convertMp3toM4a();
    }

}

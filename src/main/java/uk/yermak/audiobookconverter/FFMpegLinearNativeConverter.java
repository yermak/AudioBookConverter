package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegLinearNativeConverter {

    private Conversion conversion;
    private final String outputFileName;
    private String metaDataFileName;
    private String fileListFileName;
    private Process ffmpegProcess;
    private ProgressParser progressParser;
    private OutputParameters outputParameters;
    private ProgressCallback callback;


    public FFMpegLinearNativeConverter(Conversion conversion, String outputFileName, String metaDataFileName, String fileListFileName, OutputParameters outputParameters, ProgressCallback callback) {
        this.conversion = conversion;
        this.outputFileName = outputFileName;
        this.metaDataFileName = metaDataFileName;
        this.fileListFileName = fileListFileName;
        this.outputParameters = outputParameters;
        this.callback = callback;
    }

    public void concat() throws IOException, InterruptedException {
        if (conversion.getStatus().isOver()) return;
        while (ProgressStatus.PAUSED.equals(conversion.getStatus())) Thread.sleep(1000);


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
            ProcessBuilder ffmpegProcessBuilder;
            if (outputParameters.isAuto()) {
                if (conversion.getMedia().stream().allMatch(mediaInfo -> mediaInfo.getCodec().equals("aac"))) {
                    ffmpegProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe",
                            "-protocol_whitelist", "file,pipe,concat",
                            "-f", "concat",
                            "-safe", "0",
                            "-i", fileListFileName,
                            "-i", metaDataFileName,
                            "-map_metadata", "1",
                            "-vn",
                            "-f", "ipod",
                            "-codec:a", "copy",
                            "-progress", progressParser.getUri().toString(),
                            outputFileName);
                } else {
                    ffmpegProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe",
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
                            "-codec:a", "aac",
                            "-progress", progressParser.getUri().toString(),
                            outputFileName);
                }
            } else {
                ffmpegProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe",
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
                        "-codec:a", "aac",
                        "-progress", progressParser.getUri().toString(),
                        outputFileName);
            }

            ffmpegProcess = ffmpegProcessBuilder.start();

            StreamCopier.copy(ffmpegProcess.getInputStream(), System.out);
            StreamCopier.copy(ffmpegProcess.getErrorStream(), System.err);
            boolean finished = false;
            while (!conversion.getStatus().isOver() && !finished) {
                finished = ffmpegProcess.waitFor(500, TimeUnit.MILLISECONDS);
            }
        } finally {
            Utils.closeSilently(ffmpegProcess);
            Utils.closeSilently(progressParser);
        }

    }

}

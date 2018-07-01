package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import net.bramp.ffmpeg.progress.TcpProgressParser;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegLinearConverter implements Concatenator {

    private final String outputFileName;
    private final StatusChangeListener listener;
    private String metaDataFileName;
    private String fileListFileName;
    private MediaInfo mediaInfo;
    private Process ffmpegProcess;
    private ProgressParser progressParser;
    private ProgressCallback callback;


    public FFMpegLinearConverter(String outputFileName, String metaDataFileName, String fileListFileName, MediaInfo mediaInfo, ProgressCallback callback) {
        this.outputFileName = outputFileName;
        this.metaDataFileName = metaDataFileName;
        this.fileListFileName = fileListFileName;
        this.mediaInfo = mediaInfo;
        this.callback = callback;
        listener = new StatusChangeListener();
        ConverterApplication.getContext().getConversion().addStatusChangeListener(listener);
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
                    "-ar", String.valueOf(mediaInfo.getFrequency()),
                    "-ac", String.valueOf(mediaInfo.getChannels()),
//                    "-b:a", String.valueOf(mediaInfo.getBitrate()),
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
            Utils.closeSilently(ffmpegProcess);
            Utils.closeSilently(progressParser);
        }

    }

}

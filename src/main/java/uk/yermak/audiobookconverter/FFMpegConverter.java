package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.concurrent.*;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegConverter implements Callable<ConverterOutput>, Converter {
    private MediaInfo mediaInfo;
    private final String outputFileName;
    private final static Semaphore mutex = new Semaphore(Runtime.getRuntime().availableProcessors() + 1);

    public FFMpegConverter(MediaInfo mediaInfo, String outputFileName) {
        this.mediaInfo = mediaInfo;
        this.outputFileName = outputFileName;
    }

    public ConverterOutput convertMp3toM4a() throws IOException, InterruptedException, ExecutionException {
        try {
            mutex.acquire();


            ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe",
                    "-i", mediaInfo.getFileName(),
                    "-vn",
                    "-codec:a", "libfdk_aac",
                    "-f", "ipod",
                    "-b:a", String.valueOf(mediaInfo.getBitrate()),
                    "-ar", String.valueOf(mediaInfo.getFrequency()),
                    "-ac", String.valueOf(mediaInfo.getChannels()),
                    outputFileName);

            Process ffmpegProcess = ffmpegProcessBuilder.start();

            InputStream ffmpegIn = ffmpegProcess.getInputStream();
            InputStream ffmpegErr = ffmpegProcess.getErrorStream();

            StreamCopier ffmpegToConsole = new StreamCopier(ffmpegIn, System.out);
            Future<Long> ffmpegFuture = Executors.newWorkStealingPool().submit(ffmpegToConsole);
            StreamCopier ffmpegToErr = new StreamCopier(ffmpegErr, System.err);
            Future<Long> ffmpegErrFuture = Executors.newWorkStealingPool().submit(ffmpegToErr);


            Long totalBytes;
            try {
                totalBytes = ffmpegFuture.get();
                return new ConverterOutput(mediaInfo, outputFileName);
            } catch (InterruptedException | ExecutionException ignorable) {
                ffmpegProcess.destroy();
                ffmpegFuture.cancel(true);
                throw ignorable;
            }
        } finally {
            mutex.release();
        }
    }

    @Override
    public ConverterOutput call() throws Exception {
        return convertMp3toM4a();
    }

}

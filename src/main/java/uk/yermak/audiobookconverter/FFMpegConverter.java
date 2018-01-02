package uk.yermak.audiobookconverter;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.concurrent.*;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegConverter implements Callable<ConverterOutput>, Converter {
    private final int bitrate;
    private final int channels;
    private final int frequency;
    private final long duration;
    private final String outputFileName;
    private final String[] inputFileList;
    private final static Semaphore mutex = new Semaphore(Runtime.getRuntime().availableProcessors()+1);

    public FFMpegConverter(int bitrate, int channels, int frequency, long duration, String outputFileName, String... inputFileList) {
        this.bitrate = bitrate;
        this.channels = channels;
        this.frequency = frequency;
        this.duration = duration;
        this.outputFileName = outputFileName;
        this.inputFileList = inputFileList;
    }

    public ConverterOutput convertMp3toM4a() throws IOException, InterruptedException, ExecutionException {
        try {
            mutex.acquire();
            ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("external/x86/ffmpeg.exe",
                    "-i", inputFileList[0],
                    "-vn",
                    "-codec:a", "libfdk_aac",
                    "-f", "ipod",
                    "-b:a", String.valueOf(bitrate),
                    "-ar", String.valueOf(frequency),
                    "-ac", String.valueOf(channels),
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
                return new ConverterOutput(totalBytes, duration, outputFileName, inputFileList);
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

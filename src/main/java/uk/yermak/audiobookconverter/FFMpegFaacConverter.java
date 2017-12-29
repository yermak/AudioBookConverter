package uk.yermak.audiobookconverter;

import org.apache.commons.io.output.NullOutputStream;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegFaacConverter implements Callable<ConverterOutput>, Converter {
    private final int bitrate;
    private final int channels;
    private final int frequency;
    private final long duration;
    private final String outputFileName;
    private final String[] inputFileList;

    public FFMpegFaacConverter(int bitrate, int channels, int frequency, long duration, String outputFileName, String... inputFileList) {
        this.bitrate = bitrate;
        this.channels = channels;
        this.frequency = frequency;
        this.duration = duration;
        this.outputFileName = outputFileName;
        this.inputFileList = inputFileList;
    }

    public ConverterOutput convertMp3toM4a() throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("external/ffmpeg.exe",
                "-protocol_whitelist", "file,pipe,concat",
                "-f", "concat",
                "-safe", "0",
                "-i", "-",
                "-f", "s16le",
                "-acodec", "pcm_s16le",
                "-");


        Process ffmpegProcess = ffmpegProcessBuilder.start();
        InputStream ffmpegIn = ffmpegProcess.getInputStream();
        InputStream ffmpegErr = ffmpegProcess.getErrorStream();
        PrintWriter ffmpegOut = new PrintWriter(new OutputStreamWriter(ffmpegProcess.getOutputStream()));


        ProcessBuilder faacProcessBuilder = new ProcessBuilder("external/faac.exe",
                "-b", String.valueOf(bitrate / 1024),
                "-P",
                "-C", String.valueOf(channels),
                "-R", String.valueOf(frequency),
                "-X",
                "-o", outputFileName,
                "-");

        Process faacProcess = faacProcessBuilder.start();
        InputStream faacIn = faacProcess.getInputStream();
        OutputStream faacOut = faacProcess.getOutputStream();
        InputStream faacErr = faacProcess.getErrorStream();

        StreamCopier ffmpegToFaac = new StreamCopier(ffmpegIn, faacOut);
        Future<Long> ffmpegFuture = Executors.newWorkStealingPool().submit(ffmpegToFaac);
        StreamCopier ffmpegToErr = new StreamCopier(ffmpegErr, NullOutputStream.NULL_OUTPUT_STREAM);
        Future<Long> ffmpegErrFuture = Executors.newWorkStealingPool().submit(ffmpegToErr);

        StreamCopier faacToConsole = new StreamCopier(faacIn, NullOutputStream.NULL_OUTPUT_STREAM);
        Future<Long> faacFuture = Executors.newWorkStealingPool().submit(faacToConsole);
        StreamCopier faacToErr = new StreamCopier(faacErr, NullOutputStream.NULL_OUTPUT_STREAM);
        Future<Long> faacErrFuture = Executors.newWorkStealingPool().submit(faacToErr);


        for (String inputFile : inputFileList) {
            ffmpegOut.println("file '" + inputFile + "'");
        }
        ffmpegOut.close();

        Long totalBytes;
        try {
            totalBytes = ffmpegFuture.get();
            faacFuture.get();
            return new ConverterOutput(totalBytes, duration, outputFileName, inputFileList);
        } catch (InterruptedException | ExecutionException ignorable) {
            ffmpegProcess.destroy();
            faacProcess.destroy();
            ffmpegFuture.cancel(true);
            faacFuture.cancel(true);
            throw ignorable;
        }
    }

    @Override
    public ConverterOutput call() throws Exception {
        return convertMp3toM4a();
    }

}

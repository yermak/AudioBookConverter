package uk.yermak.audiobookconverter;

import java.io.*;
import java.util.concurrent.Callable;

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

    public ConverterOutput convertMp3toM4a() throws IOException, InterruptedException {
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

        StreamCopier.copy(ffmpegIn, faacOut);
        StreamCopier.copy(ffmpegErr, System.err);

        StreamCopier.copy(faacIn, System.out);
        StreamCopier.copy(faacErr, System.err);


        for (String inputFile : inputFileList) {
            ffmpegOut.println("file '" + inputFile + "'");
        }
        ffmpegOut.close();
        try {
            faacProcess.waitFor();
            ffmpegProcess.waitFor();
            return new ConverterOutput(new MediaInfoBean(inputFileList[0]), outputFileName);
        } catch (InterruptedException ignorable) {
            ignorable.printStackTrace();
            throw ignorable;
        } finally {
            Utils.closeSilently(ffmpegProcess);
            Utils.closeSilently(faacProcess);
        }
    }

    @Override
    public ConverterOutput call() throws Exception {
        return convertMp3toM4a();
    }

}

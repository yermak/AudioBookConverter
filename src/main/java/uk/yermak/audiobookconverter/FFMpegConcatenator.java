package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class FFMpegConcatenator implements Concatenator {

    private final List<Future<ConverterOutput>> futures;
    private final String outputFileName;

    public FFMpegConcatenator(List<Future<ConverterOutput>> futures, String outputFileName) {
        this.futures = futures;
        this.outputFileName = outputFileName;
    }

    public void concat() throws IOException, ExecutionException, InterruptedException {

        ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe",
                "-protocol_whitelist", "file,pipe,concat",
                "-vn",
                "-f", "concat",
                "-safe", "0",
                "-i", "-",
                "-f", "ipod",
                "-c:a", "copy",
                outputFileName);

        Process ffmpegProcess = ffmpegProcessBuilder.start();
        PrintWriter ffmpegOut = new PrintWriter(ffmpegProcess.getOutputStream());

        StreamCopier ffmpegToOut = new StreamCopier(ffmpegProcess.getInputStream(), System.out);
        Future<Long> ffmpegFuture = Executors.newWorkStealingPool().submit(ffmpegToOut);
        StreamCopier ffmpegToErr = new StreamCopier(ffmpegProcess.getErrorStream(), System.err);
        Future<Long> ffmpegErrFuture = Executors.newWorkStealingPool().submit(ffmpegToErr);


        for (Future<ConverterOutput> future : futures) {
            ConverterOutput output = future.get();
            ffmpegOut.println("file '" + output.getOutputFileName() + "'");
            ffmpegOut.flush();
        }
        ffmpegOut.close();
        ffmpegFuture.get();

        for (Future<ConverterOutput> future : futures) {
            ConverterOutput output = future.get();
            FileUtils.deleteQuietly(new File(output.getOutputFileName()));
        }

    }
}

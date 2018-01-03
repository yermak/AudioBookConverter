package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;

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

    private final List<ConverterOutput> outputs;
    private final String outputFileName;
    private String metaDataFileName;
    private String fileListFileName;

    public FFMpegConcatenator(List<ConverterOutput> outputs, String outputFileName, String metaDataFileName, String fileListFileName) {
        this.outputs = outputs;
        this.outputFileName = outputFileName;
        this.metaDataFileName = metaDataFileName;
        this.fileListFileName = fileListFileName;
    }

    public void concat() throws IOException, ExecutionException, InterruptedException {

        ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("external/x64/ffmpeg.exe",
                "-protocol_whitelist", "file,pipe,concat",
                "-vn",
                "-f", "concat",
                "-safe", "0",
                "-i", fileListFileName,
                "-i", metaDataFileName,
                "-map_metadata", "1",
                "-f", "ipod",
                "-c:a", "copy",
//                "-c:v", "copy",
                outputFileName);

        Process ffmpegProcess = ffmpegProcessBuilder.start();
//        PrintWriter ffmpegOut = new PrintWriter(ffmpegProcess.getOutputStream());

        StreamCopier ffmpegToOut = new StreamCopier(ffmpegProcess.getInputStream(), System.out);
        Future<Long> ffmpegFuture = Executors.newWorkStealingPool().submit(ffmpegToOut);
        StreamCopier ffmpegToErr = new StreamCopier(ffmpegProcess.getErrorStream(), System.err);
        Future<Long> ffmpegErrFuture = Executors.newWorkStealingPool().submit(ffmpegToErr);

        ffmpegFuture.get();

        for (ConverterOutput output : outputs) {
            FileUtils.deleteQuietly(new File(output.getOutputFileName()));
        }

    }
}

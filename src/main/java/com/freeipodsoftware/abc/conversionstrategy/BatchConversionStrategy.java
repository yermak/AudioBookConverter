package com.freeipodsoftware.abc.conversionstrategy;

import uk.yermak.audiobookconverter.FFMpegConverter;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.StateDispatcher;
import uk.yermak.audiobookconverter.Utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BatchConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private final ExecutorService executorService = Executors.newWorkStealingPool();

    public BatchConversionStrategy() {
    }

    protected void startConversion() {
        executorService.execute(this);
    }

    @Override
    protected String getTempFileName(long jobId, int index, String extension) {
        return "";
    }

    public void run() {
        List<Future> futures = new ArrayList<>();

        for (int i = 0; i < this.media.size(); ++i) {
            MediaInfo mediaInfo = this.media.get(i);
            String outputFileName = this.determineOutputFilename(mediaInfo.getFileName());
            Future converterFuture =
                    executorService
                            .submit(new FFMpegConverter(mediaInfo, outputFileName, progressCallbacks.get(mediaInfo.getFileName())));
            futures.add(converterFuture);
        }
        try {
            for (Future future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            StateDispatcher.getInstance().finishedWithError(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            finilize();
        }
    }

    private String determineOutputFilename(String inputFilename) {
        String outputFilename;
        if (outputDestination == null) {
            outputFilename = inputFilename.replaceAll("(?i)\\.mp3", ".m4b");
        } else {
            File file = new File(inputFilename);
            File outFile = new File(this.outputDestination, file.getName());
            outputFilename = outFile.getAbsolutePath().replaceAll("(?i)\\.mp3", ".m4b");
        }

        if (!outputFilename.endsWith(".m4b")) {
            outputFilename = outputFilename + ".m4b";
        }

        return Utils.makeFilenameUnique(outputFilename);
    }


    @Override
    public void canceled() {
        Utils.closeSilently(executorService);
    }

    @Override
    public String getAdditionalFinishedMessage() {
        return outputDestination;
    }

    @Override
    public void setOutputDestination(String outputDestination) {
        this.outputDestination = outputDestination;
    }
}

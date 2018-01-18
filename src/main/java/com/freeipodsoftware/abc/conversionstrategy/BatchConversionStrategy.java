package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.Messages;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BatchConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private final ExecutorService executorService = Executors.newWorkStealingPool();
    //    private boolean intoSameFolder;

    public BatchConversionStrategy() {
    }

    public static String makeFilenameUnique(String filename) {
        Pattern extPattern = Pattern.compile("\\.(\\w+)$");
        Matcher extMatcher = extPattern.matcher(filename);
        if (extMatcher.find()) {
            try {
                String extension = extMatcher.group(1);

                for (File outputFile = new File(filename); outputFile.exists(); outputFile = new File(filename)) {
                    Pattern pattern = Pattern.compile("(?i)(.*)\\((\\d+)\\)\\." + extension + "$");
                    Matcher matcher = pattern.matcher(filename);
                    if (matcher.find()) {
                        filename = matcher.group(1) + "(" + (Integer.parseInt(matcher.group(2)) + 1) + ")." + extension;
                    } else {
                        filename = filename.replaceAll("." + extension + "$", "(1)." + extension);
                    }
                }

                return filename;
            } catch (Exception var7) {
                throw new RuntimeException(Messages.getString("Util.connotUseFilename") + " " + filename);
            }
        } else {
            throw new RuntimeException(Messages.getString("Util.connotUseFilename") + " " + filename + " (2)");
        }
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

        return makeFilenameUnique(outputFilename);
    }


    @Override
    public void canceled() {
        Utils.closeSilently(executorService);
    }
}

package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.BatchModeOptionsDialog;
import com.freeipodsoftware.abc.Util;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.FFMpegConverter;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.StateDispatcher;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BatchConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private boolean intoSameFolder;
    private String folder;
    private int currentFileNumber;

    public BatchConversionStrategy() {
    }

    public boolean makeUserInterview(Shell shell, String fileName) {
        BatchModeOptionsDialog options = new BatchModeOptionsDialog(shell);
        options.setFolder(this.getSuggestedFolder(fileName));
        if (options.open()) {
            this.intoSameFolder = options.isIntoSameFolder();
            this.folder = options.getFolder();
            return true;
        } else {
            return false;
        }
    }


    protected void startConversion() {
        Executors.newWorkStealingPool().execute(this);
    }

    @Override
    protected String getTempFileName(long jobId, int currentFileNumber, String extension) {
        return "";
    }

    public void run() {
        List<Future> futures = new ArrayList<>();

        for (int i = 0; i < this.media.size(); ++i) {
            this.currentFileNumber = i + 1;
            MediaInfo mediaInfo = this.media.get(i);
            String outputFileName = this.determineOutputFilename(mediaInfo.getFileName());
            Future converterFuture =
                    Executors.newWorkStealingPool()
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
            this.finished = true;
            StateDispatcher.getInstance().finished();
        }
    }

    private String determineOutputFilename(String inputFilename) {
        String outputFilename;
        if (this.intoSameFolder) {
            outputFilename = inputFilename.replaceAll("(?i)\\.mp3", ".m4b");
        } else {
            File file = new File(inputFilename);
            File outFile = new File(this.folder, file.getName());
            outputFilename = outFile.getAbsolutePath().replaceAll("(?i)\\.mp3", ".m4b");
        }

        if (!outputFilename.endsWith(".m4b")) {
            outputFilename = outputFilename + ".m4b";
        }

        return Util.makeFilenameUnique(outputFilename);
    }

    protected String getSuggestedFolder(String fileName) {
        try {
            return new File(fileName).getParentFile().getAbsolutePath();
        } catch (Exception var2) {
            return "";
        }

    }
}

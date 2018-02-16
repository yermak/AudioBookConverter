package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.Messages;
import org.apache.commons.io.FileUtils;
import uk.yermak.audiobookconverter.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.Executors;

public class JoiningConversionStrategy extends AbstractConversionStrategy implements Runnable {

    public JoiningConversionStrategy() {
    }


    protected void startConversion() {
        Executors.newWorkStealingPool().execute(this);
    }

    @Override
    protected String getTempFileName(long jobId, int index, String extension) {
        return media.get(index).getFileName();
    }

    public void run() {
        long jobId = System.currentTimeMillis();

        String tempFile = Utils.getTmp(jobId, 999999, ".m4b");

        File metaFile = null;
        File fileListFile = null;

        try {
            MediaInfo maxMedia = maximiseEncodingParameters();

            metaFile = prepareMeta(jobId);
            fileListFile = prepareFiles(jobId);
            if (canceled) return;
            Concatenator concatenator = new FFMpegLinearConverter(tempFile, metaFile.getAbsolutePath(), fileListFile.getAbsolutePath(), maxMedia, progressCallbacks.get("output"));
            concatenator.concat();
            if (canceled) return;
            Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder();
            artBuilder.coverArt(media, tempFile);
            if (canceled) return;
            FileUtils.moveFile(new File(tempFile), new File(outputDestination));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            StateDispatcher.getInstance().finishedWithError(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            finilize();
            FileUtils.deleteQuietly(metaFile);
            FileUtils.deleteQuietly(fileListFile);
        }
    }


    public String getAdditionalFinishedMessage() {
        return Messages.getString("JoiningConversionStrategy.outputFilename") + ":\n" + this.outputDestination;
    }

    @Override
    public void setOutputDestination(String outputDestination) {
        if (new File(outputDestination).exists()) {
            this.outputDestination = Utils.makeFilenameUnique(outputDestination);
        } else {
            this.outputDestination = outputDestination;
        }
    }
}

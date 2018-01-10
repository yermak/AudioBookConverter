package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.Messages;
import org.apache.commons.io.FileUtils;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class JoiningConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private String outputFileName;

    public JoiningConversionStrategy() {
    }


    public boolean makeUserInterview(Shell shell, String fileName) {
        this.outputFileName = selectOutputFile(shell, this.getOuputFilenameSuggestion(fileName));
        return this.outputFileName != null;
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
        String tempFile = getTempFileName(jobId, 999999, ".m4b");

        try {

            File metaFile = new File(System.getProperty("java.io.tmpdir"), "FFMETADATAFILE" + jobId);
            File fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt");

            List<String> outFiles = new ArrayList<>();
            List<String> metaData = new ArrayList<>();

            prepareFilesAndFillMeta(jobId, outFiles, metaData, mp4Tags, media);

            FileUtils.writeLines(metaFile, "UTF-8", metaData);
            FileUtils.writeLines(fileListFile, "UTF-8", outFiles);

            MediaInfo maxMedia = maximiseEncodingParameters();

            Concatenator concatenator = new FFMpegLinearConverter(tempFile, metaFile.getAbsolutePath(), fileListFile.getAbsolutePath(), maxMedia, progressCallbacks.get("output"));
            concatenator.concat();

            FileUtils.deleteQuietly(metaFile);
            FileUtils.deleteQuietly(fileListFile);

            Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder(media, tempFile, jobId);
            artBuilder.coverArt();

            FileUtils.moveFile(new File(tempFile), new File(outputFileName));


        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            StateDispatcher.getInstance().finishedWithError(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            this.finished = true;
            StateDispatcher.getInstance().finished();
        }
    }



    public String getAdditionalFinishedMessage() {
        return Messages.getString("JoiningConversionStrategy.outputFilename") + ":\n" + this.outputFileName;
    }

}

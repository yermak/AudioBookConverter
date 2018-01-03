package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.Messages;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.*;

import java.io.*;
import java.util.concurrent.Executors;

public class JoiningConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private String outputFileName;

    public JoiningConversionStrategy() {
    }

    public long getOutputSize() {
        return this.canceled ? 0L : (new File(this.outputFileName)).length();
    }

    public int calcPercentFinishedForCurrentOutputFile() {
        return this.getProgress();
    }

    public String getInfoText() {
        return Messages.getString("JoiningConversionStrategy.file") + " " + "0" + "/" + this.media.size();
    }

    public boolean makeUserInterview(Shell shell) {
        this.outputFileName = selectOutputFile(shell, this.getOuputFilenameSuggestion(media.get(0).getFileName()));
        return this.outputFileName != null;
    }

    protected void startConversion() {
        Executors.newWorkStealingPool().execute(this);
    }

    public void run() {
        try {

            int maxChannels = 0;
            int maxFrequency = 0;
            int maxBitrate = 0;

            for (MediaInfo mediaInfo : media) {
                if (mediaInfo.getChannels() > maxChannels) maxChannels = mediaInfo.getChannels();
                if (mediaInfo.getFrequency() > maxFrequency) maxFrequency = mediaInfo.getFrequency();
                if (mediaInfo.getBitrate() > maxBitrate) maxBitrate = mediaInfo.getBitrate();
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.finishListener.finishedWithError(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            this.percentFinished = 100;
            this.finished = true;
            this.finishListener.finished();
        }
    }

    public String getAdditionalFinishedMessage() {
        return Messages.getString("JoiningConversionStrategy.outputFilename") + ":\n" + this.outputFileName;
    }

}

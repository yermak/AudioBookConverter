package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.Messages;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.StateDispatcher;

import java.io.PrintWriter;
import java.io.StringWriter;
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

package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.Messages;
import com.freeipodsoftware.abc.Util;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.*;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        return Messages.getString("JoiningConversionStrategy.file") + " " + "0" + "/" + this.inputFileList.length;
    }

    public boolean makeUserInterview(Shell shell) {
        this.outputFileName = selectOutputFile(shell, this.getOuputFilenameSuggestion(this.inputFileList));
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

            for (String fileName : inputFileList) {
                MediaInfo mediaInfo = Utils.determineChannelsAndFrequency(fileName);
                if (mediaInfo.getChannels() > maxChannels) maxChannels = mediaInfo.getChannels();
                if (mediaInfo.getFrequency() > maxFrequency) maxFrequency = mediaInfo.getFrequency();
                if (mediaInfo.getBitrate() > maxBitrate) maxBitrate = mediaInfo.getBitrate();
            }


//            Future converterFuture =
//                    Executors.newWorkStealingPool()
//                            .submit(new FFMpegConverter(new MediaInfo(inputFileList[]),outputFileName, inputFileList));
//
//            converterFuture.get();

//            Tagger tagger = new Mp4v2Tagger(mp4Tags, outputFileName);
//            tagger.tagIt();

//            ChapterBuilder chapterBuilder = new Mp4v2ChapterBuilder(futures, outputFileName);
//            chapterBuilder.chapters();

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

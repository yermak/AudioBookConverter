package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.FinishListener;
import com.freeipodsoftware.abc.Mp4Tags;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.MediaInfo;

import java.util.List;

public abstract class AbstractConversionStrategy implements ConversionStrategy {
    protected FinishListener finishListener;
    protected boolean finished;
    private long overallInputSize;
    private long inputBytesProcessed;
    int percentFinished;
    private int percentFinishedForCurrentOutputFile;
    private long elapsedTime;
    private long startTime;
    private long remainingTime;
    protected boolean canceled;
    private AbstractConversionStrategy.RemainingTimeCalculatorThread remainingTimeCalculatorThread;
    protected Mp4Tags mp4Tags;
    private boolean paused;
    protected long currentInputFileSize;
    protected long currentInputFileBytesProcessed;
    protected List<MediaInfo> media;

    protected AbstractConversionStrategy() {
    }

    protected abstract int calcPercentFinishedForCurrentOutputFile();

    public void setFinishListener(FinishListener finishListener) {
        this.finishListener = finishListener;
    }

    public void setMp4Tags(Mp4Tags mp4Tags) {
        this.mp4Tags = mp4Tags;
    }

    public long getElapsedTime() {
        return this.elapsedTime;
    }

    public String getInfoText() {
        return "";
    }

    public int getProgress() {
        return this.percentFinished;
    }

    public int getProgressForCurrentOutputFile() {
        return this.percentFinishedForCurrentOutputFile;
    }

    public long getRemainingTime() {
        return this.remainingTime;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void start(Shell shell) {
//        this.overallInputSize = this.determineInputSize();
        this.canceled = false;
        this.finished = false;
        this.inputBytesProcessed = 0L;
        this.startTime = System.currentTimeMillis();
        this.startConversion();
        this.remainingTimeCalculatorThread = new AbstractConversionStrategy.RemainingTimeCalculatorThread();
        this.remainingTimeCalculatorThread.start();
    }

    protected static String selectOutputFile(Shell shell, String filenameSuggestion) {
        FileDialog fileDialog = new FileDialog(shell, 8192);
        fileDialog.setFilterNames(new String[]{" (*.m4b)"});
        fileDialog.setFilterExtensions(new String[]{"*.m4b"});
        fileDialog.setFileName(filenameSuggestion);
        String fileName = fileDialog.open();
        if (fileName == null) return null;
        if (!fileName.toUpperCase().endsWith(".m4b".toUpperCase())) {
            fileName = fileName + ".m4b";
        }
        return fileName;
    }


    protected abstract void startConversion();

    public void cancel() {
        this.canceled = true;
    }

/*
    private long determineInputSize() {
        long size = 0L;

        for (String inputFile : this.inputFileList) {
            File file = new File(inputFile);
            if (!file.exists()) {
                throw new ConversionException(Messages.getString("AbstractConversionStrategy.fileNotFound") + ": " + inputFile);
            }

            size += file.length();
        }

        return size;
    }
*/

    protected String getOuputFilenameSuggestion(String... inputFileList) {
        if (media.size() > 0) {
            String mp3Filename = media.get(0).getFileName();
            return mp3Filename.replaceFirst("\\.\\w*$", ".m4b");
        } else {
            return "";
        }
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void setMedia(List<MediaInfo> media) {
        this.media = media;
    }

    public String getAdditionalFinishedMessage() {
        return "";
    }

    public class RemainingTimeCalculatorThread extends Thread {
        public RemainingTimeCalculatorThread() {
        }

        public void run() {
            long lastTimeStamp = AbstractConversionStrategy.this.startTime;

            while (!AbstractConversionStrategy.this.finished) {
                double percentFinishedDouble = (double) AbstractConversionStrategy.this.inputBytesProcessed / (double) AbstractConversionStrategy.this.overallInputSize * 100.0D;
                AbstractConversionStrategy.this.percentFinished = (int) percentFinishedDouble;
                AbstractConversionStrategy.this.percentFinishedForCurrentOutputFile = AbstractConversionStrategy.this.calcPercentFinishedForCurrentOutputFile();
                long currentTimeStamp = System.currentTimeMillis();
                if (!AbstractConversionStrategy.this.paused) {
                    AbstractConversionStrategy.this.elapsedTime += currentTimeStamp - lastTimeStamp;
                }

                lastTimeStamp = currentTimeStamp;

                try {
                    AbstractConversionStrategy.this.remainingTime = (new Double((double) AbstractConversionStrategy.this.elapsedTime / percentFinishedDouble * (100.0D - percentFinishedDouble))).longValue();
                } catch (Exception var9) {
                }

                try {
                    Thread.sleep(500L);
                } catch (Exception var8) {
                }
            }

        }
    }
}

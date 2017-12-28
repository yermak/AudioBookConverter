package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.*;
import javazoom.jl.converter.Converter.PrintWriterProgressListener;
import javazoom.jl.decoder.*;
import org.apache.commons.io.input.CountingInputStream;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public abstract class AbstractConversionStrategy implements ConversionStrategy {
    protected String[] inputFileList;
    protected FinishListener finishListener;
    protected boolean finished;
    protected long overallInputSize;
    protected long inputBytesProcessed;
    protected int percentFinished;
    protected int percentFinishedForCurrentOutputFile;
    protected long elapsedTime;
    protected long startTime;
    protected long remainingTime;
    protected boolean canceled;
    private AbstractConversionStrategy.RemainingTimeCalculatorThread remainingTimeCalculatorThread;
    protected Mp4Tags mp4Tags;
    private boolean paused;
    protected long currentInputFileSize;
    protected long currentInputFileBytesProcessed;

    public AbstractConversionStrategy() {
    }

    public void setInputFileList(String[] inputFileList) {
        this.inputFileList = inputFileList;
    }

    public abstract int calcPercentFinishedForCurrentOutputFile();

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
        this.overallInputSize = this.determineInputSize();
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
        if (!fileName.toUpperCase().endsWith(".m4b".toUpperCase())) {
            fileName = fileName + ".m4b";
        }
        return fileName;
    }

    protected String getSuggestedFolder() {
        if (this.inputFileList != null && this.inputFileList.length > 0) {
            try {
                return (new File(this.inputFileList[0])).getParentFile().getAbsolutePath();
            } catch (Exception var2) {
                return "";
            }
        } else {
            return "";
        }
    }

    protected String getOuputFilenameSuggestion(String... inputFileList) {
        if (inputFileList.length > 0) {
            String mp3Filename = this.inputFileList[0];
            String m4bFilename = mp3Filename.replaceFirst("\\.\\w*$", ".m4b");
            return m4bFilename;
        } else {
            return "";
        }
    }

    protected abstract void startConversion();

    public void cancel() {
        this.canceled = true;
    }

    protected long determineInputSize() {
        long size = 0L;

        for (int i = 0; i < this.inputFileList.length; ++i) {
            File file = new File(this.inputFileList[i]);
            if (!file.exists()) {
                throw new ConversionException(Messages.getString("AbstractConversionStrategy.fileNotFound") + ": " + this.inputFileList[i]);
            }

            size += file.length();
        }

        return size;
    }

    protected String getMp4TagsFaacOptions() {
        if (this.mp4Tags == null) {
            return "";
        } else {
            StringBuffer buffer = new StringBuffer();
            this.appendFaacOptionIfNotEmpty(buffer, "--artist", this.mp4Tags.getArtist());
            this.appendFaacOptionIfNotEmpty(buffer, "--writer", this.mp4Tags.getWriter());
            this.appendFaacOptionIfNotEmpty(buffer, "--title", this.mp4Tags.getTitle());
            this.appendFaacOptionIfNotEmpty(buffer, "--album", this.mp4Tags.getAlbum());
            this.appendFaacOptionIfNotEmpty(buffer, "--genre", this.mp4Tags.getGenre());
            this.appendFaacOptionIfNotEmpty(buffer, "--year", this.mp4Tags.getYear());
            this.appendFaacOptionIfNotEmpty(buffer, "--track", this.mp4Tags.getTrack());
            this.appendFaacOptionIfNotEmpty(buffer, "--disc", this.mp4Tags.getDisc());
            this.appendFaacOptionIfNotEmpty(buffer, "--comment", this.mp4Tags.getComment());
            return buffer.toString();
        }
    }

    private void appendFaacOptionIfNotEmpty(StringBuffer buffer, String option, String text) {
        if (Util.hasText(text)) {
            buffer.append(option);
            buffer.append(" \"");
            buffer.append(this.filterEscapeChars(text));
            buffer.append("\" ");
        }

    }

    private String filterEscapeChars(String text) {
        return text == null ? null : text.replace("\"", "\\\"");
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    protected void decodeInputFile(String filename, OutputStream destination, int channels, int frequency) throws Exception {
        long processedSoFar = this.inputBytesProcessed;
        this.currentInputFileSize = this.getFileSize(filename);
        PrintWriterProgressListener progressListener = PrintWriterProgressListener.newStdOut(0);
        CountingInputStream countingInputStream = new CountingInputStream(new FileInputStream(filename));
        BufferedInputStream sourceStream = new BufferedInputStream(countingInputStream, 1024 * 1024);
        progressListener.converterUpdate(1, -1, 0);
        Decoder decoder = new Decoder();
        Bitstream stream = new Bitstream(sourceStream);
        int frame = 0;
        int frameCount = 2147483647;

        StreamOBuffer output = null;
        try {
            for (; frame < frameCount && !this.canceled; ++frame) {
                while (this.paused) {
                    Thread.sleep(100L);
                    if (this.canceled) {
                        break;
                    }
                }
                this.currentInputFileBytesProcessed = (long) countingInputStream.getCount();
                this.inputBytesProcessed = processedSoFar + this.currentInputFileBytesProcessed;

                Header header = stream.readFrame();
                if (header == null) {
                    break;
                }
                if (output == null) {
                    int fileChannels = header.mode() == 3 ? 1 : 2;
                    int fileFrequency = header.frequency();
//                    resamplingOutputStream = new ResamplingOutputStream(destination, fileChannels, channels, fileFrequency, frequency);


                    output = new StreamOBuffer(destination, fileChannels);
                    decoder.setOutputBuffer(output);
                }
//                WaveFileObuffer output = new StreamOBuffer(channels, frequency, tempFileName);

                progressListener.readFrame(frame, header);
                Obuffer decoderOutput = decoder.decodeFrame(header, stream);
                if (decoderOutput != output) {
                    throw new InternalError("Output buffers are different.");
                }
                progressListener.decodedFrame(frame, header, output);
                stream.closeFrame();
            }
            output.close();
        } catch (Exception e) {
            boolean stop = !progressListener.converterException(e);
            if (stop) {
                throw new JavaLayerException(e.getLocalizedMessage(), e);
            }
        } finally {
            destination.flush();
        }
    }

    private long getFileSize(String filename) {
        File file = new File(filename);
        return file.exists() ? file.length() : 0L;
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
                    ;
                }

                try {
                    Thread.sleep(500L);
                } catch (Exception var8) {
                    ;
                }
            }

        }
    }
}

package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.Messages;
import com.freeipodsoftware.abc.StreamDumper;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import org.apache.commons.io.output.NullOutputStream;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.StreamCopier;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class JoiningConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private String outputFileName;
    private int currentFileNumber;
    private int channels = 2;
    private int frequency = 44100;
    private int bitrate = 128000;

    public JoiningConversionStrategy() {
    }

    public long getOutputSize() {
        return this.canceled ? 0L : (new File(this.outputFileName)).length();
    }

    public int calcPercentFinishedForCurrentOutputFile() {
        return this.getProgress();
    }

    public String getInfoText() {
        return Messages.getString("JoiningConversionStrategy.file") + " " + this.currentFileNumber + "/" + this.inputFileList.length;
    }

    public boolean supportsTagEditor() {
        return true;
    }

    public boolean makeUserInterview(Shell shell) {
        FileDialog fileDialog = new FileDialog(shell, 8192);
        fileDialog.setFilterNames(new String[]{" (*.m4b)"});
        fileDialog.setFilterExtensions(new String[]{"*.m4b"});
        fileDialog.setFileName(this.getOuputFilenameSuggestion());
        this.outputFileName = fileDialog.open();
        if (this.outputFileName != null) {
            if (!this.outputFileName.toUpperCase().endsWith(".m4b".toUpperCase())) {
                this.outputFileName = this.outputFileName + ".m4b";
            }

            return true;
        } else {
            return false;
        }
    }

    private String getOuputFilenameSuggestion() {
        if (this.inputFileList.length > 0) {
            String mp3Filename = this.inputFileList[0];
            String m4bFilename = mp3Filename.replaceFirst("\\.\\w*$", ".m4b");
            return m4bFilename;
        } else {
            return "";
        }
    }

    protected void startConversion() {
        (new Thread(this)).start();
    }

    public void run() {
        try {
            this.determineMaxChannelsAndFrequency();

            ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("external/ffmpeg.exe",
                    "-protocol_whitelist", "file,pipe",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", "-",
                    "-f", "s16le",
                    "-acodec", "pcm_s16le",
                    "-");


            Process ffmpegProcess = ffmpegProcessBuilder.start();
            InputStream ffmpegIn = ffmpegProcess.getInputStream();
            InputStream ffmpegErr = ffmpegProcess.getErrorStream();
            PrintWriter ffmpegOut = new PrintWriter(new OutputStreamWriter(ffmpegProcess.getOutputStream()));

            ProcessBuilder faacProcessBuilder = new ProcessBuilder("external/faac.exe",
                    "-b", String.valueOf(this.bitrate / 1024),
                    "-P",
                    "-C", String.valueOf(this.channels),
                    "-R", String.valueOf(this.frequency),
                    "-X",
                    this.getMp4TagsFaacOptions(),
                    "-o", this.outputFileName,
                    "-");

            Process faacProcess = faacProcessBuilder.start();
            InputStream faacIn = faacProcess.getInputStream();
            OutputStream faacOut = faacProcess.getOutputStream();
            InputStream faacErr = faacProcess.getErrorStream();

            StreamCopier ffmpegToFaac = new StreamCopier(ffmpegIn, faacOut);
            Future<Long> ffmpegFuture = Executors.newWorkStealingPool().submit(ffmpegToFaac);
            StreamCopier ffmpegToErr = new StreamCopier(ffmpegErr, NullOutputStream.NULL_OUTPUT_STREAM);
            Future<Long> ffmpegErrFuture = Executors.newWorkStealingPool().submit(ffmpegToErr);

            StreamCopier faacToConsole = new StreamCopier(faacIn, NullOutputStream.NULL_OUTPUT_STREAM);
            Future<Long> faacFuture = Executors.newWorkStealingPool().submit(faacToConsole);
            StreamCopier faacToErr = new StreamCopier(faacErr, NullOutputStream.NULL_OUTPUT_STREAM);
            Future<Long> faacErrFuture = Executors.newWorkStealingPool().submit(faacToErr);


            for (int i = 0; i < this.inputFileList.length; ++i) {
                ffmpegOut.println("file '" + this.inputFileList[i] + "'");
            }
            ffmpegOut.close();


            while (!ffmpegFuture.isDone() || !faacFuture.isDone()) {
                if (this.canceled) {
                    this.finishListener.canceled();
                    this.overallInputSize = 0L;
                    this.inputBytesProcessed = 0L;
                    this.percentFinished = 0;

                    ffmpegProcess.destroy();
                    faacProcess.destroy();
                    ffmpegFuture.cancel(true);
                    faacFuture.cancel(true);
                }
            }


            Long totalBytes = ffmpegFuture.get();
            this.overallInputSize = totalBytes;
            this.inputBytesProcessed = totalBytes;
            this.percentFinished = 100;
            this.finished = true;
            this.finishListener.finished();

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.finishListener.finishedWithError(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            this.finished = true;
        }
    }

    private void determineMaxChannelsAndFrequency() {
        this.channels = 0;
        this.frequency = 0;

        try {
            for (int i = 0; i < this.inputFileList.length; ++i) {
                BufferedInputStream sourceStream = new BufferedInputStream(new FileInputStream(this.inputFileList[i]));
                Bitstream stream = new Bitstream(sourceStream);
                Header header = stream.readFrame();
                int fileChannels = header.mode() == 3 ? 1 : 2;
                if (fileChannels > this.channels) {
                    this.channels = fileChannels;
                }

                int fileFrequency = header.frequency();
                if (fileFrequency > this.frequency) {
                    this.frequency = fileFrequency;
                }

                int fileBitrate = header.bitrate();
                if (fileBitrate > this.bitrate) {
                    this.bitrate = header.bitrate();
                }

                stream.close();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getAdditionalFinishedMessage() {
        return Messages.getString("JoiningConversionStrategy.outputFilename") + ":\n" + this.outputFileName;
    }
}

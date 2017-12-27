package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.Messages;
import com.freeipodsoftware.abc.StreamDumper;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class JoiningConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private String outputFileName;
    private int currentFileNumber;
    private int channels;
    private int frequency;

    public JoiningConversionStrategy() {
    }

    public long getOutputSize() {
        return this.canceled?0L:(new File(this.outputFileName)).length();
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
        if(this.outputFileName != null) {
            if(!this.outputFileName.toUpperCase().endsWith(".m4b".toUpperCase())) {
                this.outputFileName = this.outputFileName + ".m4b";
            }

            return true;
        } else {
            return false;
        }
    }

    private String getOuputFilenameSuggestion() {
        if(this.inputFileList.length > 0) {
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
            String commandLine = "external/faac.exe -P -C " + this.channels + " -R " + this.frequency + " " + this.getMp4TagsFaacOptions() + " -o \"" + this.outputFileName + "\" -";
            Process proc = Runtime.getRuntime().exec(commandLine);
            StreamDumper streamDumper = new StreamDumper(proc.getInputStream());
            OutputStream faacOutput = proc.getOutputStream();

            for(int i = 0; i < this.inputFileList.length; ++i) {
                if(!this.canceled) {
                    this.currentFileNumber = i + 1;
                    this.decodeInputFile(this.inputFileList[i], faacOutput, this.channels, this.frequency);
                }
            }

            streamDumper.stop();
            faacOutput.close();
            this.percentFinished = 100;
            this.finished = true;
            if(this.canceled) {
                this.finishListener.canceled();
                this.overallInputSize = 0L;
                this.inputBytesProcessed = 0L;
                this.percentFinished = 0;
            } else {
                this.finishListener.finished();
            }
        } catch (Exception var9) {
            StringWriter sw = new StringWriter();
            var9.printStackTrace(new PrintWriter(sw));
            this.finishListener.finishedWithError(var9.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            this.finished = true;
        }

    }

    private void determineMaxChannelsAndFrequency() {
        this.channels = 0;
        this.frequency = 0;

        try {
            for(int i = 0; i < this.inputFileList.length; ++i) {
                BufferedInputStream sourceStream = new BufferedInputStream(new FileInputStream(this.inputFileList[i]));
                Bitstream stream = new Bitstream(sourceStream);
                Header header = stream.readFrame();
                int fileChannels = header.mode() == 3?1:2;
                if(fileChannels > this.channels) {
                    this.channels = fileChannels;
                }

                int fileFrequency = header.frequency();
                if(fileFrequency > this.frequency) {
                    this.frequency = fileFrequency;
                }

                stream.close();
            }

        } catch (Exception var7) {
            throw new RuntimeException(var7);
        }
    }

    public String getAdditionalFinishedMessage() {
        return Messages.getString("JoiningConversionStrategy.outputFilename") + ":\n" + this.outputFileName;
    }
}

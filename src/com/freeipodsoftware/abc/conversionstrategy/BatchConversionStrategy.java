package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.BatchModeOptionsDialog;
import com.freeipodsoftware.abc.StreamDumper;
import com.freeipodsoftware.abc.Util;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import org.eclipse.swt.widgets.Shell;

public class BatchConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private boolean intoSameFolder;
    private String folder;
    private int currentFileNumber;
    private int channels;
    private int frequency;
    private String outputFileName;

    public BatchConversionStrategy() {
    }

    public long getOutputSize() {
        return this.canceled?0L:(new File(this.outputFileName)).length();
    }

    public int calcPercentFinishedForCurrentOutputFile() {
        return this.currentInputFileSize > 0L?(int)((double)this.currentInputFileBytesProcessed / (double)this.currentInputFileSize * 100.0D):0;
    }

    public boolean makeUserInterview(Shell shell) {
        BatchModeOptionsDialog options = new BatchModeOptionsDialog(shell);
        options.setFolder(this.getSuggestedFolder());
        if(options.open()) {
            this.intoSameFolder = options.isIntoSameFolder();
            this.folder = options.getFolder();
            return true;
        } else {
            return false;
        }
    }

    private String getSuggestedFolder() {
        if(this.inputFileList != null && this.inputFileList.length > 0) {
            try {
                return (new File(this.inputFileList[0])).getParentFile().getAbsolutePath();
            } catch (Exception var2) {
                return "";
            }
        } else {
            return "";
        }
    }

    protected void startConversion() {
        (new Thread(this)).start();
    }

    public boolean supportsTagEditor() {
        return false;
    }

    public void run() {
        for(int i = 0; i < this.inputFileList.length; ++i) {
            this.currentFileNumber = i + 1;
            this.outputFileName = this.determineOutputFilename(this.inputFileList[i]);
            this.determineChannelsAndFrequency(this.inputFileList[i]);
            this.mp4Tags = Util.readTagsFromInputFile(this.inputFileList[i]);
            String commandLine = "external/faac.exe -P -C " + this.channels + " -R " + this.frequency + " " + this.getMp4TagsFaacOptions() + " -o \"" + this.outputFileName + "\" -";

            try {
                Process proc = Runtime.getRuntime().exec(commandLine);
                StreamDumper streamDumper = new StreamDumper(proc.getInputStream());
                OutputStream faacOutput = proc.getOutputStream();
                this.decodeInputFile(this.inputFileList[i], faacOutput, this.channels, this.frequency);
                streamDumper.stop();
                faacOutput.close();
            } catch (Exception var6) {
                StringWriter sw = new StringWriter();
                var6.printStackTrace(new PrintWriter(sw));
                this.finishListener.finishedWithError(var6.getMessage() + "; " + sw.getBuffer().toString());
                break;
            }
        }

        this.finished = true;
        this.finishListener.finished();
    }

    private String determineOutputFilename(String inputFilename) {
        String outputFilename;
        if(this.intoSameFolder) {
            outputFilename = inputFilename.replaceAll("(?i)\\.mp3", ".m4b");
        } else {
            File file = new File(inputFilename);
            File outFile = new File(this.folder, file.getName());
            outputFilename = outFile.getAbsolutePath().replaceAll("(?i)\\.mp3", ".m4b");
        }

        if(!outputFilename.endsWith(".m4b")) {
            outputFilename = outputFilename + ".m4b";
        }

        return Util.makeFilenameUnique(outputFilename);
    }

    public String getInfoText() {
        return Messages.getString("BatchConversionStrategy.file") + " " + this.currentFileNumber + "/" + this.inputFileList.length;
    }

    private void determineChannelsAndFrequency(String filename) {
        this.channels = 0;
        this.frequency = 0;

        try {
            BufferedInputStream sourceStream = new BufferedInputStream(new FileInputStream(filename));
            Bitstream stream = new Bitstream(sourceStream);
            Header header = stream.readFrame();
            this.channels = header.mode() == 3?1:2;
            this.frequency = header.frequency();
            stream.close();
        } catch (Exception var5) {
            throw new RuntimeException(var5);
        }
    }
}

package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.BatchModeOptionsDialog;
import com.freeipodsoftware.abc.Util;
import com.freeipodsoftware.abc.conversionstrategy.AbstractConversionStrategy;
import com.freeipodsoftware.abc.conversionstrategy.Messages;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import org.apache.commons.io.output.NullOutputStream;
import org.eclipse.swt.widgets.Shell;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private boolean intoSameFolder;
    private String folder;
    private int currentFileNumber;
    private int channels;
    private int frequency;
    private int bitrate;
    private String outputFileName;

    public long getOutputSize() {
        return this.canceled ? 0L : (new File(this.outputFileName)).length();
    }

    public int calcPercentFinishedForCurrentOutputFile() {
        return this.currentInputFileSize > 0L ? (int) ((double) this.currentInputFileBytesProcessed / (double) this.currentInputFileSize * 100.0D) : 0;
    }

    public boolean makeUserInterview(Shell shell) {
        BatchModeOptionsDialog options = new BatchModeOptionsDialog(shell);
        options.setFolder(this.getSuggestedFolder());
        if (options.open()) {
            this.intoSameFolder = options.isIntoSameFolder();
            this.folder = options.getFolder();
            this.outputFileName = selectOutputFile(shell, getOuputFilenameSuggestion(inputFileList));
            return this.outputFileName != null;
        } else {
            return false;
        }

    }

    protected void startConversion() {
        Executors.newWorkStealingPool().execute(this);
    }

    public boolean supportsTagEditor() {
        return false;
    }

    public void run() {
        List<Future<Converter.Output>> futures = new ArrayList<>();

        for (int i = 0; i < this.inputFileList.length; ++i) {
            this.currentFileNumber = i + 1;
            String filename = this.determineOutputFilename(this.inputFileList[i]);
            this.determineChannelsAndFrequency(this.inputFileList[i]);
            this.mp4Tags = Util.readTagsFromInputFile(this.inputFileList[i]);

            Converter converter = new Converter(bitrate, channels, frequency, filename, inputFileList[i]);
            Future<Converter.Output> converterFuture = Executors.newWorkStealingPool().submit(converter);
            futures.add(converterFuture);
        }
        concat(futures);
    }

    private void concat(List<Future<Converter.Output>> futures) {

        try {
            ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("external/ffmpeg.exe",
                    "-protocol_whitelist", "file,pipe,concat",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", "-",
                    "-f", "ipod",
                    "-c", "copy",
                    this.outputFileName);


            Process ffmpegProcess = ffmpegProcessBuilder.start();
            InputStream ffmpegIn = ffmpegProcess.getInputStream();
            InputStream ffmpegErr = ffmpegProcess.getErrorStream();
            PrintWriter ffmpegOut = new PrintWriter(new OutputStreamWriter(ffmpegProcess.getOutputStream()));

            StreamCopier ffmpegToOut = new StreamCopier(ffmpegIn, NullOutputStream.NULL_OUTPUT_STREAM);
            Future<Long> ffmpegFuture = Executors.newWorkStealingPool().submit(ffmpegToOut);
            StreamCopier ffmpegToErr = new StreamCopier(ffmpegErr, NullOutputStream.NULL_OUTPUT_STREAM);
            Future<Long> ffmpegErrFuture = Executors.newWorkStealingPool().submit(ffmpegToErr);


            for (Future<Converter.Output> future : futures) {
                Converter.Output output = future.get();
                ffmpegOut.println("file '" + output.getOutputFileName() + "'");
                ffmpegOut.flush();
            }
            ffmpegOut.close();

        } catch (InterruptedException | ExecutionException | IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.finishListener.finishedWithError(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            this.finished = true;
            this.finishListener.finished();
        }


    }

    private String determineOutputFilename(String inputFilename) {
        String outputFilename;
        if (this.intoSameFolder) {
            outputFilename = inputFilename.replaceAll("(?i)\\.mp3", ".m4b");
        } else {
            File file = new File(inputFilename);
            File outFile = new File(this.folder, file.getName());
            outputFilename = outFile.getAbsolutePath().replaceAll("(?i)\\.mp3", ".m4b");
        }

        if (!outputFilename.endsWith(".m4b")) {
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
            this.channels = header.mode() == 3 ? 1 : 2;
            this.frequency = header.frequency();
            this.bitrate = header.bitrate();
            stream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

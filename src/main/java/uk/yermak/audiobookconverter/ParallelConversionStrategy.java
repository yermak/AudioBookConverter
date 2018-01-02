package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.conversionstrategy.AbstractConversionStrategy;
import com.freeipodsoftware.abc.conversionstrategy.Messages;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import org.apache.commons.io.IOUtils;
import org.eclipse.swt.widgets.Shell;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class ParallelConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private int currentFileNumber;
    private int channels;
    private int frequency;
    private int bitrate;
    private long duration;
    private String outputFileName;

    public long getOutputSize() {
        return this.canceled ? 0L : (new File(this.outputFileName)).length();
    }

    public int calcPercentFinishedForCurrentOutputFile() {
        return this.currentInputFileSize > 0L ? (int) ((double) this.currentInputFileBytesProcessed / (double) this.currentInputFileSize * 100.0D) : 0;
    }

    public boolean makeUserInterview(Shell shell) {
        this.outputFileName = selectOutputFile(shell, this.getOuputFilenameSuggestion(this.inputFileList));
        return this.outputFileName != null;
    }

    protected void startConversion() {
        Executors.newWorkStealingPool().execute(this);
    }

    public void run() {
        List<Future<ConverterOutput>> futures = new ArrayList<>();
        long time = System.currentTimeMillis();
        try {

            for (int i = 0; i < this.inputFileList.length; ++i) {
                this.currentFileNumber = i + 1;
                String filename = new File(System.getProperty("java.io.tmpdir"), "~ABC-v2-" + time + "-" + i + ".m4b").getAbsolutePath();
                this.determineChannelsAndFrequency(this.inputFileList[i]);

                Future<ConverterOutput> converterFuture =
                        Executors.newWorkStealingPool()
                                .submit(new FFMpegConverter(bitrate, channels, frequency, duration, filename, inputFileList[i]));
                futures.add(converterFuture);
            }


            Concatenator concatenator = new FFMpegConcatenator(futures, this.outputFileName);
            concatenator.concat();

            Tagger tagger = new Mp4v2Tagger(mp4Tags, outputFileName);
            tagger.tagIt();

            List<ConverterOutput> outputs = new ArrayList<>();
            for (Future<ConverterOutput> future : futures) {
                outputs.add(future.get());
            }

            ChapterBuilder chapterBuilder = new Mp4v2ChapterBuilder(outputs, outputFileName);
            chapterBuilder.chapters();

        } catch (InterruptedException | ExecutionException | IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.finishListener.finishedWithError(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            this.finished = true;
            this.finishListener.finished();
        }
    }

    public String getInfoText() {
        return Messages.getString("BatchConversionStrategy.file") + " " + this.currentFileNumber + "/" + this.inputFileList.length;
    }

    private void determineChannelsAndFrequency(String filename) {
        this.channels = 0;
        this.frequency = 0;
        FileInputStream in = null;
        try {
            in = new FileInputStream(filename);
            BufferedInputStream sourceStream = new BufferedInputStream(in);
            Bitstream stream = new Bitstream(sourceStream);
            Header header = stream.readFrame();
            this.channels = header.mode() == 3 ? 1 : 2;
            this.frequency = header.frequency();
            this.bitrate = header.bitrate();
            long tn = in.getChannel().size();
            this.duration = (long) header.total_ms((int) tn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
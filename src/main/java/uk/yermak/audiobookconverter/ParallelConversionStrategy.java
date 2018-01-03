package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.conversionstrategy.AbstractConversionStrategy;
import com.freeipodsoftware.abc.conversionstrategy.Messages;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Shell;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private int currentFileNumber;
    private String outputFileName;

    public long getOutputSize() {
        return this.canceled ? 0L : (new File(this.outputFileName)).length();
    }

    public int calcPercentFinishedForCurrentOutputFile() {
        return this.currentInputFileSize > 0L ? (int) ((double) this.currentInputFileBytesProcessed / (double) this.currentInputFileSize * 100.0D) : 0;
    }

    public boolean makeUserInterview(Shell shell) {
        this.outputFileName = selectOutputFile(shell, this.getOuputFilenameSuggestion(this.media.get(0).getFileName()));
        return this.outputFileName != null;
    }

    protected void startConversion() {
        Executors.newWorkStealingPool().execute(this);
    }

    public void run() {
        List<Future<ConverterOutput>> futures = new ArrayList<>();
        long jobId = System.currentTimeMillis();
        try {

            for (MediaInfo mediaInfo : media) {
                ++currentFileNumber;
                String tempOutput = new File(System.getProperty("java.io.tmpdir"), "~ABC-v2-" + jobId + "-" + currentFileNumber + ".m4b").getAbsolutePath();


                Future<ConverterOutput> converterFuture =
                        Executors.newWorkStealingPool()
                                .submit(new FFMpegConverter(mediaInfo, tempOutput));
                futures.add(converterFuture);
            }

            List<ConverterOutput> outputs = new ArrayList<>();
            File metaFile = new File(System.getProperty("java.io.tmpdir"), "FFMETADATAFILE" + jobId);
            File fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt");
            List<String> outFiles = new ArrayList<>();
            List<String> metaData = new ArrayList<>();

            metaData.add(";FFMETADATA1");
            metaData.add("major_brand=M4A");
            metaData.add("minor_version=512");
            metaData.add("compatible_brands=isomiso2");
            metaData.add("title=" + mp4Tags.getTitle());
            metaData.add("artist=" + mp4Tags.getWriter());
            metaData.add("album=" + (StringUtils.isNotBlank(mp4Tags.getSeries()) ? mp4Tags.getSeries() : mp4Tags.getTitle()));
            metaData.add("composer=" + mp4Tags.getNarrator());
            metaData.add("comment=" + mp4Tags.getComment());
            metaData.add("track=" + mp4Tags.getTrack() + "/" + mp4Tags.getTotalTracks());
            metaData.add("media_type=2");
            metaData.add("genre=Audiobook");
            metaData.add("encoder=" + "https://github.com/yermak/AudioBookConverter");

            long totalDuration = 0;
            for (int i = 0; i < futures.size(); i++) {
                ConverterOutput output = futures.get(i).get();
                outputs.add(output);
                metaData.add("[CHAPTER]");
                metaData.add("TIMEBASE=1/1000");
                metaData.add("START=" + totalDuration);
                totalDuration += output.getDuration();
                metaData.add("END=" + totalDuration);
                metaData.add("title=Chapter " + (i + 1));

                outFiles.add("file '" + output.getOutputFileName() + "'");
            }

            FileUtils.writeLines(metaFile, metaData);
            FileUtils.writeLines(fileListFile, outFiles);

            Concatenator concatenator = new FFMpegConcatenator(outputs, this.outputFileName, metaFile.getAbsolutePath(), fileListFile.getAbsolutePath());
            concatenator.concat();

            FileUtils.deleteQuietly(metaFile);
            FileUtils.deleteQuietly(fileListFile);

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
        return Messages.getString("BatchConversionStrategy.file") + " " + this.currentFileNumber + "/" + this.media.size();
    }
}

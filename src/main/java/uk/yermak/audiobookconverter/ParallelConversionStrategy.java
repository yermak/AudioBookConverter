package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.conversionstrategy.AbstractConversionStrategy;
import org.apache.commons.io.FileUtils;
import org.eclipse.swt.widgets.Shell;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private String outputFileName;

    public boolean makeUserInterview(Shell shell, String fileName) {
        this.outputFileName = selectOutputFile(shell, getOuputFilenameSuggestion(fileName));
        return this.outputFileName != null;
    }

    protected void startConversion() {
        Executors.newWorkStealingPool().execute(this);
    }

    public void run() {
        List<Future<ConverterOutput>> futures = new ArrayList<>();
        long jobId = System.currentTimeMillis();
        String tempFile = getTempFileName(jobId, 999999, ".m4b");
        try {

            List<MediaInfo> dest = new ArrayList<>();
            for (MediaInfo mediaInfo : media) {
                dest.add(mediaInfo);
            }
            Collections.sort(dest, (o1, o2) -> (int) (o2.getDuration() - o1.getDuration()));

            for (int i = 0; i < dest.size(); i++) {
                MediaInfo mediaInfo = dest.get(i);
                String tempOutput = getTempFileName(jobId, mediaInfo.hashCode(), ".m4b");
                Future<ConverterOutput> converterFuture =
                        Executors.newWorkStealingPool()
                                .submit(new FFMpegConverter(mediaInfo, tempOutput, progressCallbacks.get(mediaInfo.getFileName())));
                futures.add(converterFuture);
            }

            for (Future<ConverterOutput> future : futures) {
                future.get();
            }

            File metaFile = new File(System.getProperty("java.io.tmpdir"), "FFMETADATAFILE" + jobId);
            File fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt");

            List<String> outFiles = new ArrayList<>();
            List<String> metaData = new ArrayList<>();

            prepareFilesAndFillMeta(jobId, outFiles, metaData, mp4Tags, media);

            FileUtils.writeLines(metaFile, "UTF-8", metaData);
            FileUtils.writeLines(fileListFile, "UTF-8", outFiles);

            Concatenator concatenator = new FFMpegConcatenator(tempFile, metaFile.getAbsolutePath(), fileListFile.getAbsolutePath(), progressCallbacks.get("output"));
            concatenator.concat();

            FileUtils.deleteQuietly(metaFile);
            FileUtils.deleteQuietly(fileListFile);

            for (int i = 0; i < media.size(); i++) {
                FileUtils.deleteQuietly(new File(getTempFileName(jobId, media.get(i).hashCode(), ".m4b")));
            }

            Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder(media, tempFile, jobId);
            artBuilder.coverArt();

            FileUtils.moveFile(new File(tempFile), new File(outputFileName));

        } catch (InterruptedException | ExecutionException | IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            StateDispatcher.getInstance().finishedWithError(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            this.finished = true;
            StateDispatcher.getInstance().finished();
        }
    }


    protected String getTempFileName(long jobId, int index, String extension) {
        return Utils.getTmp(jobId, index, extension);
    }

}

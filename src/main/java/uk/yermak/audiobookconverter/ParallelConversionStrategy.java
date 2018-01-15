package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.Messages;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private String outputFileName;
    private ExecutorService executorService = Executors.newWorkStealingPool();

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

        File fileListFile = null;
        File metaFile = null;

        MediaInfo maxMedia = maximiseEncodingParameters();

        try {
            fileListFile = prepareFiles(jobId);
            metaFile = prepareMeta(jobId);

            List<MediaInfo> prioritizedMedia = prioritiseMedia(maxMedia);
            for (MediaInfo mediaInfo : prioritizedMedia) {
                String tempOutput = getTempFileName(jobId, mediaInfo.hashCode(), ".m4b");
                ProgressCallback callback = progressCallbacks.get(mediaInfo.getFileName());
                Future<ConverterOutput> converterFuture = executorService.submit(new FFMpegConverter(mediaInfo, tempOutput, callback));
                futures.add(converterFuture);
            }

            for (Future<ConverterOutput> future : futures) {
                if (canceled) return;
                future.get();
            }

            if (canceled) return;
            Concatenator concatenator = new FFMpegConcatenator(tempFile, metaFile.getAbsolutePath(), fileListFile.getAbsolutePath(), progressCallbacks.get("output"));
            concatenator.concat();

            if (canceled) return;
            Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder(media, tempFile, jobId);
            artBuilder.coverArt();

            if (canceled) return;
            FileUtils.moveFile(new File(tempFile), new File(outputFileName));

        } catch (InterruptedException | ExecutionException | IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            StateDispatcher.getInstance().finishedWithError(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            finilize();
            for (MediaInfo mediaInfo : media) {
                FileUtils.deleteQuietly(new File(getTempFileName(jobId, mediaInfo.hashCode(), ".m4b")));
            }
            FileUtils.deleteQuietly(metaFile);
            FileUtils.deleteQuietly(fileListFile);
        }
    }

    private List<MediaInfo> prioritiseMedia(MediaInfo maxMedia) {
        List<MediaInfo> sortedMedia = new ArrayList<>();
        for (MediaInfo mediaInfo : media) {
            sortedMedia.add(mediaInfo);
            mediaInfo.setFrequency(maxMedia.getFrequency());
            mediaInfo.setChannels(maxMedia.getChannels());
            mediaInfo.setBitrate(maxMedia.getBitrate());
        }
        Collections.sort(sortedMedia, (o1, o2) -> (int) (o2.getDuration() - o1.getDuration()));
        return sortedMedia;
    }


    protected String getTempFileName(long jobId, int index, String extension) {
        return Utils.getTmp(jobId, index, extension);
    }

    public String getAdditionalFinishedMessage() {
        return Messages.getString("JoiningConversionStrategy.outputFilename") + ":\n" + this.outputFileName;
    }

}

package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JoiningConversionStrategy extends AbstractConversionStrategy implements Runnable {


    @Override
    protected String getTempFileName(long jobId, int index, String extension) {
        return media.get(index).getFileName();
    }

    public void run() {
        long jobId = System.currentTimeMillis();

        String tempFile = Utils.getTmp(jobId, 999999, ".m4b");

        File metaFile = null;
        File fileListFile = null;

        try {
            MediaInfo maxMedia = maximiseEncodingParameters();

            metaFile = prepareMeta(jobId);
            fileListFile = prepareFiles(jobId);
            if (canceled) return;
            Concatenator concatenator = new FFMpegLinearConverter(tempFile, metaFile.getAbsolutePath(), fileListFile.getAbsolutePath(), maxMedia, progressCallbacks.get("output"));
            concatenator.concat();
            if (canceled) return;
            Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder();
            artBuilder.coverArt(media, tempFile);
            if (canceled) return;
            FileUtils.moveFile(new File(tempFile), new File(outputDestination));
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            StateDispatcher.getInstance().finishedWithError(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            finilize();
            FileUtils.deleteQuietly(metaFile);
            FileUtils.deleteQuietly(fileListFile);
        }
    }


    @Override
    public void setOutputDestination(String outputDestination) {
        if (new File(outputDestination).exists()) {
            this.outputDestination = Utils.makeFilenameUnique(outputDestination);
        } else {
            this.outputDestination = outputDestination;
        }
    }

    protected File prepareFiles(long jobId) throws IOException {
        File fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt");
        List<String> outFiles = IntStream.range(0, media.size()).mapToObj(i -> "file '" + getTempFileName(jobId, i, ".m4b") + "'").collect(Collectors.toList());

        FileUtils.writeLines(fileListFile, "UTF-8", outFiles);

        return fileListFile;
    }
}

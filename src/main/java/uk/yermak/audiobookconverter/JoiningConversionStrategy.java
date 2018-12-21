package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JoiningConversionStrategy implements ConversionStrategy {


    private final StatusChangeListener listener;
    private Conversion conversion;
    private Map<String, ProgressCallback> progressCallbacks;

    public JoiningConversionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks) {
        this.conversion = conversion;
        this.progressCallbacks = progressCallbacks;
        listener = new StatusChangeListener();
        conversion.addStatusChangeListener(listener);
    }

    protected String getTempFileName(long jobId, int index, String extension) {
        return conversion.getMedia().get(index).getFileName();
    }

    public void run() {
        long jobId = System.currentTimeMillis();

        String tempFile = Utils.getTmp(jobId, 999999, ".m4b");

        File metaFile = null;
        File fileListFile = null;

        try {
            conversion.getOutputParameters().updateAuto(conversion.getMedia());

            metaFile = MetadataBuilder.prepareMeta(jobId, conversion.getBookInfo(), conversion.getMedia());
            fileListFile = prepareFiles(jobId);
            if (listener.isCancelled()) return;
            Concatenator concatenator = new FFMpegLinearConverter(conversion, tempFile, metaFile.getAbsolutePath(), fileListFile.getAbsolutePath(), conversion.getOutputParameters(), progressCallbacks.get("output"));
            concatenator.concat();
            if (listener.isCancelled()) return;
            Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder(conversion);
            artBuilder.coverArt(conversion.getMedia(), tempFile);
            if (listener.isCancelled()) return;
            FileUtils.moveFile(new File(tempFile), new File(conversion.getOutputDestination()));
            conversion.finished();
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            conversion.error(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            FileUtils.deleteQuietly(metaFile);
            FileUtils.deleteQuietly(fileListFile);
            conversion.removeStatusChangeListener(listener);
        }
    }



    protected File prepareFiles(long jobId) throws IOException {
        File fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt");
        List<String> outFiles = IntStream.range(0, conversion.getMedia().size()).mapToObj(i -> "file '" + getTempFileName(jobId, i, ".m4b") + "'").collect(Collectors.toList());

        FileUtils.writeLines(fileListFile, "UTF-8", outFiles);

        return fileListFile;
    }
}

package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JoiningConversionStrategy implements ConversionStrategy {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Conversion conversion;
    private Map<String, ProgressCallback> progressCallbacks;

    public JoiningConversionStrategy(Conversion conversion, Map<String, ProgressCallback> progressCallbacks) {
        this.conversion = conversion;
        this.progressCallbacks = progressCallbacks;
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

            metaFile = new MetadataBuilder().prepareMeta(jobId, conversion.getBookInfo(), conversion.getMedia());
            fileListFile = prepareFiles(jobId);
            if (conversion.getStatus().isOver()) return;
            FFMpegLinearNativeConverter concatenator = new FFMpegLinearNativeConverter(conversion, tempFile, metaFile.getAbsolutePath(), fileListFile.getAbsolutePath(), conversion.getOutputParameters(), progressCallbacks.get("output"));
            concatenator.concat();
            if (conversion.getStatus().isOver()) return;
            Mp4v2ArtBuilder artBuilder = new Mp4v2ArtBuilder(conversion);
            artBuilder.coverArt(tempFile);
            if (conversion.getStatus().isOver()) return;
            FileUtils.moveFile(new File(tempFile), new File(conversion.getOutputDestination()));
            conversion.finished();
        } catch (Exception e) {
            logger.error("Error during joining conversion", e);
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            conversion.error(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            FileUtils.deleteQuietly(metaFile);
            FileUtils.deleteQuietly(fileListFile);
        }
    }



    protected File prepareFiles(long jobId) throws IOException {
        File fileListFile = new File(System.getProperty("java.io.tmpdir"), "filelist." + jobId + ".txt");
        List<String> outFiles = IntStream.range(0, conversion.getMedia().size()).mapToObj(i -> "file '" + getTempFileName(jobId, i, ".m4b") + "'").collect(Collectors.toList());

        FileUtils.writeLines(fileListFile, "UTF-8", outFiles);

        return fileListFile;
    }
}

package uk.yermak.audiobookconverter;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class ConverterOutput {
    private long unpackedSize;
    private String outputFileName;
    private String[] inputFileList;
    private long duration;

    public ConverterOutput(long unpackedSize, long duration, String outputFileName, String... inputFileList) {
        this.unpackedSize = unpackedSize;
        this.duration = duration;
        this.outputFileName = outputFileName;
        this.inputFileList = inputFileList;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public long getDuration() {
        return duration;
    }
}

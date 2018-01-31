package uk.yermak.audiobookconverter;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class ConverterOutput {
    private MediaInfo mediaInfo;
    private final String outputFileName;

    public ConverterOutput(MediaInfo mediaInfo, String outputFileName) {
        this.mediaInfo = mediaInfo;
        this.outputFileName = outputFileName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public long getDuration() {
        return mediaInfo.getDuration();
    }

    public MediaInfo getMediaInfo() {
        return mediaInfo;
    }
}

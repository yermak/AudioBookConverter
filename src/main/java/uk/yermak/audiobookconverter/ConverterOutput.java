package uk.yermak.audiobookconverter;

public class ConverterOutput {
    private MediaInfo mediaInfo;
    private final String outputFileName;

    public MediaInfo mediaInfo() {
        return this.mediaInfo;
    }

    public void mediaInfo_$eq(final MediaInfo x$1) {
        this.mediaInfo = x$1;
    }

    public String outputFileName() {
        return this.outputFileName;
    }

    public String getOutputFileName() {
        return this.outputFileName();
    }

    public long getDuration() {
        return this.mediaInfo().getDuration();
    }

    public MediaInfo getMediaInfo() {
        return this.mediaInfo();
    }

    public ConverterOutput(final MediaInfo mediaInfo, final String outputFileName) {
        this.mediaInfo = mediaInfo;
        this.outputFileName = outputFileName;
    }
}

        
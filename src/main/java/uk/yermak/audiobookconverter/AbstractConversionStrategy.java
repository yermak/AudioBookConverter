package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import org.eclipse.swt.widgets.Shell;

public abstract class AbstractConversionStrategy implements ConversionStrategy {
    protected boolean finished;
    protected boolean cancelled;
    protected boolean paused;
    protected AudioBookInfo bookInfo;
    protected List<MediaInfo> media;
    protected Map<String, ProgressCallback> progressCallbacks;
    protected String outputDestination;


    protected AbstractConversionStrategy() {
        ConverterApplication.getContext().getConversion().addStatusChangeListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case CANCELLED:
                    cancelled = true;
                    break;
                case PAUSED:
                    paused = true;
                    break;
            }
        });
    }

    public void setBookInfo(AudioBookInfo audioBookInfo) {
        this.bookInfo = audioBookInfo;
    }

    @Override
    public void setMedia(List<MediaInfo> media) {
        this.media = media;
    }

    @Override
    public void setCallbacks(Map<String, ProgressCallback> progressCallbacks) {
        this.progressCallbacks = progressCallbacks;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }



    protected File prepareMeta(long jobId) throws IOException {
        File metaFile = new File(System.getProperty("java.io.tmpdir"), "FFMETADATAFILE" + jobId);
        List<String> metaData = new ArrayList<>();

        metaData.add(";FFMETADATA1");
        metaData.add("major_brand=M4A");
        metaData.add("minor_version=512");
        metaData.add("compatible_brands=isomiso2");
        metaData.add("title=" + bookInfo.getTitle());
        metaData.add("artist=" + bookInfo.getWriter());
        metaData.add("album=" + (StringUtils.isNotBlank(bookInfo.getSeries()) ? bookInfo.getSeries() : bookInfo.getTitle()));
        metaData.add("composer=" + bookInfo.getNarrator());
        metaData.add("comment=" + bookInfo.getComment());
        metaData.add("track=" + bookInfo.getBookNumber() + "/" + bookInfo.getTotalTracks());
        metaData.add("media_type=2");
        metaData.add("genre=" + bookInfo.getGenre());
        metaData.add("encoder=" + "https://github.com/yermak/AudioBookConverter");

        long totalDuration = 0;
        for (int i = 0; i < media.size(); i++) {
            metaData.add("[CHAPTER]");
            metaData.add("TIMEBASE=1/1000");
            metaData.add("START=" + totalDuration);
            totalDuration += media.get(i).getDuration();
            metaData.add("END=" + totalDuration);
            metaData.add("title=Chapter " + (i + 1));
        }
        FileUtils.writeLines(metaFile, "UTF-8", metaData);
        return metaFile;

    }

    protected abstract File prepareFiles(long jobId) throws IOException;


    protected abstract String getTempFileName(long jobId, int index, String extension);

    protected MediaInfo maximiseEncodingParameters() {
        int maxChannels = 0;
        int maxFrequency = 0;
        int maxBitrate = 0;

        for (MediaInfo mediaInfo : media) {
            if (mediaInfo.getChannels() > maxChannels) maxChannels = mediaInfo.getChannels();
            if (mediaInfo.getFrequency() > maxFrequency) maxFrequency = mediaInfo.getFrequency();
            if (mediaInfo.getBitrate() > maxBitrate) maxBitrate = mediaInfo.getBitrate();
        }

        MediaInfoBean mediaInfo = new MediaInfoBean("");
        mediaInfo.setBitrate(maxBitrate);
        mediaInfo.setChannels(maxChannels);
        mediaInfo.setFrequency(maxFrequency);
        return mediaInfo;
    }


}

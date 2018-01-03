package com.freeipodsoftware.abc.conversionstrategy;

import com.freeipodsoftware.abc.Mp4Tags;
import com.freeipodsoftware.abc.StateListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.ProgressCallback;
import uk.yermak.audiobookconverter.StateDispatcher;

import java.util.List;
import java.util.Map;

public abstract class AbstractConversionStrategy implements ConversionStrategy, StateListener {
    protected boolean finished;
    protected boolean canceled;
    protected boolean paused;
    protected Mp4Tags mp4Tags;
    protected List<MediaInfo> media;
    protected Map<String, ProgressCallback> progressCallbacks;


    protected AbstractConversionStrategy() {
    }

    public void setMp4Tags(Mp4Tags mp4Tags) {
        this.mp4Tags = mp4Tags;
    }

    @Override
    public String getAdditionalFinishedMessage() {
        return "";
    }

    public void start(Shell shell) {
        this.canceled = false;
        this.finished = false;
        StateDispatcher.getInstance().addListener(this);
        this.startConversion();
    }

    protected static String selectOutputFile(Shell shell, String filenameSuggestion) {
        FileDialog fileDialog = new FileDialog(shell, 8192);
        fileDialog.setFilterNames(new String[]{" (*.m4b)"});
        fileDialog.setFilterExtensions(new String[]{"*.m4b"});
        fileDialog.setFileName(filenameSuggestion);
        String fileName = fileDialog.open();
        if (fileName == null) return null;
        if (!fileName.toUpperCase().endsWith(".m4b".toUpperCase())) {
            fileName = fileName + ".m4b";
        }
        return fileName;
    }


    protected abstract void startConversion();


    protected String getOuputFilenameSuggestion(String fileName) {
        String mp3Filename = fileName;
        return mp3Filename.replaceFirst("\\.\\w*$", ".m4b");
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

    @Override
    public void finishedWithError(String error) {

    }

    @Override
    public void finished() {

    }

    @Override
    public void canceled() {
        canceled = true;
    }

    @Override
    public void paused() {

    }

    @Override
    public void resumed() {

    }
}

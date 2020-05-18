package uk.yermak.audiobookconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.fx.ConversionProgress;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by Yermak on 06-Feb-18.
 */
public class ConversionGroup {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private OutputParameters outputParameters;
    private AudioBookInfo bookInfo;
    private List<ArtWork> posters;
    private final List<ConversionJob> jobs = new ArrayList<>();

    public ConversionProgress start(Convertable convertable, String outputDestination) {

        Map<String, ProgressCallback> progressCallbacks = new HashMap<>();
        ConversionJob conversionJob = new ConversionJob(this, convertable, progressCallbacks, outputDestination);

        int size = convertable.getMedia().size();
        long duration = convertable.getDuration();
        ConversionProgress conversionProgress = new ConversionProgress(conversionJob);

        progressCallbacks.put("output", new ProgressCallback("output", conversionProgress));
        convertable.getMedia().stream().map(m -> (m.getFileName() + "-" + m.getDuration())).forEach(key -> progressCallbacks.put(key, new ProgressCallback(key, conversionProgress)));

        jobs.add(conversionJob);
        Executors.newSingleThreadExecutor().execute(conversionProgress);
        ConverterApplication.getContext().addJob(conversionJob);
        return conversionProgress;
    }

    public OutputParameters getOutputParameters() {
        return outputParameters;
    }

    public AudioBookInfo getBookInfo() {
        return bookInfo;
    }

    public List<ArtWork> getPosters() {
        return posters;
    }

    public String getWorkfileExtension() {
        return outputParameters.format.extension;
    }

    public void setOutputParameters(OutputParameters outputParameters) {
        this.outputParameters = outputParameters;
    }

    public void setBookInfo(AudioBookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public void setPosters(List<ArtWork> posters) {
        this.posters = posters;
    }

    public boolean isOver() {
        if (jobs.isEmpty()) return false;
        for (ConversionJob job : jobs) {
            if (!job.getStatus().isOver()) return false;
        }
        return true;
    }
}



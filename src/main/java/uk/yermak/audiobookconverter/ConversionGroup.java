package uk.yermak.audiobookconverter;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.fx.ConversionProgress;
import uk.yermak.audiobookconverter.fx.ProgressComponent;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by Yermak on 06-Feb-18.
 */
public class ConversionGroup {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<ConversionJob> jobs = new ArrayList<>();
    private boolean cancelled;

    private Book book;
    private AudioBookInfo bookInfo;
    private List<MediaInfo> media;
    private List<ArtWork> posters;
    private OutputParameters outputParameters;
    private boolean detached;
    private long jobId = System.currentTimeMillis();

    public ConversionProgress start(Convertable convertable, String outputDestination) {

        Map<String, ProgressCallback> progressCallbacks = new HashMap<>();
        ConversionJob conversionJob = new ConversionJob(this, convertable, progressCallbacks, outputDestination);

        ConversionProgress conversionProgress = new ConversionProgress(conversionJob);

        progressCallbacks.put("output", new ProgressCallback("output", conversionProgress));
        convertable.getMedia().stream().map(m -> (m.getFileName() + "-" + m.getDuration())).forEach(key -> progressCallbacks.put(key, new ProgressCallback(key, conversionProgress)));

        jobs.add(conversionJob);
        Executors.newSingleThreadExecutor().execute(conversionProgress);
        AudiobookConverter.getContext().addJob(conversionJob);
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


    public boolean isOver() {
        if (cancelled) return true;
        if (jobs.isEmpty()) return false;
        for (ConversionJob job : jobs) {
            if (!job.getStatus().isOver()) return false;
        }
        return true;
    }

    public void cancel() {
        cancelled = true;
    }

    public Book getBook() {
        return book;
    }

    public List<MediaInfo> getMedia() {
        return media;
    }

    public boolean isRunning() {
        for (ConversionJob job : jobs) {
            if (!job.getStatus().isOver()) return false;
        }

        //TODO add running check to prevent images update
        return false;
    }

    public boolean isStarted() {
        for (ConversionJob job : jobs) {
            if (job.getStatus().isStarted()) return true;
        }
        return false;
    }

    public void setMedia(List<MediaInfo> media) {
        this.media = media;
    }

    public void setPosters(List<ArtWork> posters) {
        this.posters = posters;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public void setBookInfo(AudioBookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public void setOutputParameters(OutputParameters outputParameters) {
        this.outputParameters = outputParameters;
    }

    public void launch(ListView<ProgressComponent> progressQueue, ProgressComponent progressComponent, String outputDestination) {
        Book book = this.getBook();
        if (book == null) {
            book = new Book(this.getBookInfo());
            book.construct(this.getMedia());
        }

        ObservableList<Part> parts = book.getParts();
        Format format = this.getOutputParameters().getFormat();
//        String extension = FilenameUtils.getExtension(outputDestination);
        this.getOutputParameters().setupFormat(format);


        if (this.getOutputParameters().isSplitChapters()) {
            List<Chapter> chapters = parts.stream().flatMap(p -> p.getChapters().stream()).toList();
            logger.debug("Found {} chapters in the book", chapters.size());
            for (int i = 0; i < chapters.size(); i++) {
                Chapter chapter = chapters.get(i);
                String finalDesination = outputDestination;
                if (chapters.size() > 1) {
                    finalDesination = finalDesination.replace("." + format.toString(), ", Chapter " + (i + 1) + "." + format);
                }
                String finalName = new File(finalDesination).getName();
                logger.debug("Adding conversion for chapter {}", finalName);

                ConversionProgress conversionProgress = this.start(chapter, finalDesination);
                Platform.runLater(() -> progressQueue.getItems().add(0, new ProgressComponent(conversionProgress)));

            }
        } else {
            logger.debug("Found {} parts in the book", parts.size());
            for (int i = 0; i < parts.size(); i++) {
                Part part = parts.get(i);
                String finalDesination = outputDestination;
                if (parts.size() > 1) {
                    finalDesination = finalDesination.replace("." + format.toString(), ", Part " + (i + 1) + "." + format);
                }
                String finalName = new File(finalDesination).getName();
                logger.debug("Adding conversion for part {}", finalName);

                ConversionProgress conversionProgress = this.start(part, finalDesination);
                Platform.runLater(() -> progressQueue.getItems().add(0, new ProgressComponent(conversionProgress)));
            }
        }

        Platform.runLater(() -> progressQueue.getItems().remove(progressComponent));
    }

    public void setDetached(boolean detached) {
        this.detached = detached;
    }

    public boolean isDetached() {
        return detached;
    }


    public long getJobId() {
        return jobId;
    }
}



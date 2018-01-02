package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.Mp4Tags;
import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class Mp4v2Tagger implements Tagger {

    private final Mp4Tags tags;
    private final String outputFileName;

    public Mp4v2Tagger(Mp4Tags mp4Tags, String outputFileName) {
        this.tags = mp4Tags;
        this.outputFileName = outputFileName;
    }

    public void tagIt() throws IOException, ExecutionException, InterruptedException {
        ProcessBuilder tagProcessBuilder = new ProcessBuilder("external/x64/mp4tags.exe",
                "-i", "Audiobook",
                "-s", "\"" + tags.getTitle() + "\"",
                "-A", "\"" + tags.getSeries() + "\"",
                "-a", "\"" + tags.getWriter() + "\"",
                "-w", "\"" + tags.getNarrator() + "\"",
                "-g", "\"" + tags.getGenre() + "\"",
                "-y", "\"" + tags.getYear() + "\"",
                "-c", "\"" + tags.getComment() + "\"",
                "-l", "\"" + tags.getLongDescription() + "\"",
                "-e", "\"https://github.com/yermak/AudioBookConverter\"",
                "-E", "\"ffmpeg,faac,mp4v2\""
        );

        if (tags.getTrack() <= 0) {
            tagProcessBuilder.command().add("-t");
            tagProcessBuilder.command().add(String.valueOf(tags.getTrack()));
        }
        if (tags.getTotalTracks() <= 0) {
            tagProcessBuilder.command().add("-T");
            tagProcessBuilder.command().add(String.valueOf(tags.getTotalTracks()));
        }

        tagProcessBuilder.command().add(outputFileName);

        Process tagsProcess = tagProcessBuilder.start();


        StreamCopier tagsToOut = new StreamCopier(tagsProcess.getInputStream(), System.out);
        Future<Long> tagsFuture = Executors.newWorkStealingPool().submit(tagsToOut);
        StreamCopier tagsToErr = new StreamCopier(tagsProcess.getErrorStream(), System.err);
        Future<Long> tagsErrFuture = Executors.newWorkStealingPool().submit(tagsToErr);
        tagsFuture.get();
    }
}

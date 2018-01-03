package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class Mp4v2ChapterBuilder implements ChapterBuilder {

    private final List<ConverterOutput> outputs;
    private final String outputFileName;

    public Mp4v2ChapterBuilder(List<ConverterOutput> outputs, String outputFileName) {
        this.outputs = outputs;
        this.outputFileName = outputFileName;
    }

    private static String getChapterTime(long millis) {
        String hms = String.format("%02d:%02d:%02d.%03d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
                millis % TimeUnit.SECONDS.toMillis(1));
        return hms;
    }

    public void chapters() throws IOException, ExecutionException, InterruptedException {
        File chaptersFile = null;
        long duration = 0;
        int chapter = 0;
        try {
            chaptersFile = new File(Utils.determineTempFilename(outputFileName, "m4b", "", "chapters.txt", false, new File(outputFileName).getParent()));
            List<String> chapters = new ArrayList<>();
            for (ConverterOutput output : outputs) {
                duration += output.getDuration();
                chapter++;
                chapters.add(getChapterTime(duration) + " " + "Chapter " + chapter);
            }
            FileUtils.writeLines(chaptersFile, chapters, false);

            ProcessBuilder chapterProcessBuilder = new ProcessBuilder("external/x64/mp4chaps.exe",
                    "-i",
                    "-z",
                    outputFileName);

            Process chapterProcess = chapterProcessBuilder.start();
//            PrintWriter chapterOut = new PrintWriter(new OutputStreamWriter(chapterProcess.getOutputStream()));

            StreamCopier chapterToOut = new StreamCopier(chapterProcess.getInputStream(), System.out);
            Future<Long> chapterFuture = Executors.newWorkStealingPool().submit(chapterToOut);
            StreamCopier chapterToErr = new StreamCopier(chapterProcess.getErrorStream(), System.err);
//            StreamCopier chapterToErr = new StreamCopier(chapterProcess.getErrorStream(), NullOutputStream.NULL_OUTPUT_STREAM);
            Future<Long> chapterErrFuture = Executors.newWorkStealingPool().submit(chapterToErr);
            chapterFuture.get();
        } finally {
            FileUtils.deleteQuietly(chaptersFile);
        }
    }
}

package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.Util;
import com.freeipodsoftware.abc.conversionstrategy.AbstractConversionStrategy;
import com.freeipodsoftware.abc.conversionstrategy.Messages;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Header;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.eclipse.swt.widgets.Shell;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ParallelConversionStrategy extends AbstractConversionStrategy implements Runnable {
    private int currentFileNumber;
    private int channels;
    private int frequency;
    private int bitrate;
    private long duration;
    private String outputFileName;

    public long getOutputSize() {
        return this.canceled ? 0L : (new File(this.outputFileName)).length();
    }

    public int calcPercentFinishedForCurrentOutputFile() {
        return this.currentInputFileSize > 0L ? (int) ((double) this.currentInputFileBytesProcessed / (double) this.currentInputFileSize * 100.0D) : 0;
    }

    public boolean makeUserInterview(Shell shell) {
        this.outputFileName = selectOutputFile(shell, this.getOuputFilenameSuggestion(this.inputFileList));
        return this.outputFileName != null;
    }

    protected void startConversion() {
        Executors.newWorkStealingPool().execute(this);
    }

    public boolean supportsTagEditor() {
        return false;
    }

    public void run() {
        List<Future<Converter.Output>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < this.inputFileList.length; ++i) {
                this.currentFileNumber = i + 1;
                String filename = this.determineTempFilename(this.inputFileList[i], "mp3", "~", "m4b", true, System.getProperty("java.io.tmpdir"));
                this.determineChannelsAndFrequency(this.inputFileList[i]);
                this.mp4Tags = Util.readTagsFromInputFile(this.inputFileList[i]);

                Converter converter = new Converter(bitrate, channels, frequency, duration, filename, inputFileList[i]);
                Future<Converter.Output> converterFuture = Executors.newWorkStealingPool().submit(converter);
                futures.add(converterFuture);
            }
            concat(futures);
            chapters(futures);

        } catch (InterruptedException | ExecutionException | IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            this.finishListener.finishedWithError(e.getMessage() + "; " + sw.getBuffer().toString());
        } finally {
            this.finished = true;
            this.finishListener.finished();
        }
    }

    private String determineTempFilename(String inputFilename, final String extension, String prefix, final String suffix, boolean uniqie, String folder) {
        File file = new File(inputFilename);
        File outFile = new File(folder, prefix + file.getName());
        String result = outFile.getAbsolutePath().replaceAll("(?i)\\." + extension, "." + suffix);
        if (!result.endsWith("." + suffix)) {
            result = result + "." + suffix;
        }
        if (uniqie) {
            return Util.makeFilenameUnique(result);
        }
        return result;
    }

    private void concat(List<Future<Converter.Output>> futures) throws IOException, ExecutionException, InterruptedException {

        ProcessBuilder ffmpegProcessBuilder = new ProcessBuilder("external/ffmpeg.exe",
                "-protocol_whitelist", "file,pipe,concat",
                "-f", "concat",
                "-safe", "0",
                "-i", "-",
                "-f", "ipod",
                "-c", "copy",
                this.outputFileName);

        Process ffmpegProcess = ffmpegProcessBuilder.start();
        PrintWriter ffmpegOut = new PrintWriter(ffmpegProcess.getOutputStream());

        StreamCopier ffmpegToOut = new StreamCopier(ffmpegProcess.getInputStream(), NullOutputStream.NULL_OUTPUT_STREAM);
        Future<Long> ffmpegFuture = Executors.newWorkStealingPool().submit(ffmpegToOut);
        StreamCopier ffmpegToErr = new StreamCopier(ffmpegProcess.getErrorStream(), NullOutputStream.NULL_OUTPUT_STREAM);
        Future<Long> ffmpegErrFuture = Executors.newWorkStealingPool().submit(ffmpegToErr);


        for (Future<Converter.Output> future : futures) {
            Converter.Output output = future.get();
            ffmpegOut.println("file '" + output.getOutputFileName() + "'");
            ffmpegOut.flush();
        }
        ffmpegOut.close();
        ffmpegFuture.get();

        for (Future<Converter.Output> future : futures) {
            Converter.Output output = future.get();
            FileUtils.deleteQuietly(new File(output.getOutputFileName()));
        }
    }

    private String getChapterTime(long millis) {
        String hms = String.format("%02d:%02d:%02d.%03d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1),
                millis % TimeUnit.SECONDS.toMillis(1));
        return hms;
    }

    private void chapters(List<Future<Converter.Output>> futures) throws IOException, ExecutionException, InterruptedException {
        File chaptersFile = null;
        long duration = 0;
        int chapter = 0;
        try {
            chaptersFile = new File(determineTempFilename(outputFileName, "m4b", "", "chapters.txt", false, new File(outputFileName).getParent()));
            List<String> chapters = new ArrayList<>();
            for (Future<Converter.Output> future : futures) {
                Converter.Output output = future.get();
                duration += output.getDuration();
                chapter++;
                chapters.add(getChapterTime(duration) + " " + "Chapter " + chapter);
            }
            FileUtils.writeLines(chaptersFile, chapters, false);

            ProcessBuilder chapterProcessBuilder = new ProcessBuilder("external/mp4chaps.exe",
                    "-i",
                    outputFileName);

            Process chapterProcess = chapterProcessBuilder.start();
//            PrintWriter chapterOut = new PrintWriter(new OutputStreamWriter(chapterProcess.getOutputStream()));

            StreamCopier chapterToOut = new StreamCopier(chapterProcess.getInputStream(), NullOutputStream.NULL_OUTPUT_STREAM);
            Future<Long> chapterFuture = Executors.newWorkStealingPool().submit(chapterToOut);
            StreamCopier chapterToErr = new StreamCopier(chapterProcess.getErrorStream(), NullOutputStream.NULL_OUTPUT_STREAM);
            Future<Long> chapterErrFuture = Executors.newWorkStealingPool().submit(chapterToErr);
            chapterFuture.get();
        } finally {
            FileUtils.deleteQuietly(chaptersFile);
        }
    }

    public String getInfoText() {
        return Messages.getString("BatchConversionStrategy.file") + " " + this.currentFileNumber + "/" + this.inputFileList.length;
    }

    private void determineChannelsAndFrequency(String filename) {
        this.channels = 0;
        this.frequency = 0;
        FileInputStream in = null;
        try {
            in = new FileInputStream(filename);
            BufferedInputStream sourceStream = new BufferedInputStream(in);
            Bitstream stream = new Bitstream(sourceStream);
            Header header = stream.readFrame();
            this.channels = header.mode() == 3 ? 1 : 2;
            this.frequency = header.frequency();
            this.bitrate = header.bitrate();
            long tn = in.getChannel().size();
            this.duration = (long) header.total_ms((int) tn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}

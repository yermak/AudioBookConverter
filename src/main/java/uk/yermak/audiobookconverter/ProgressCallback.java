package uk.yermak.audiobookconverter;

/**
 * Created by Yermak on 03-Jan-18.
 */
public class ProgressCallback {
    private String fileName;
    private JobProgress jobProgress;

    public ProgressCallback(String fileName, JobProgress jobProgress) {
        this.fileName = fileName;
        this.jobProgress = jobProgress;
    }


    public void converted(long timeInMillis, long size) {
        jobProgress.converted(fileName, timeInMillis, size);
    }


    public void completedConversion() {
        jobProgress.incCompleted(fileName);
    }

}

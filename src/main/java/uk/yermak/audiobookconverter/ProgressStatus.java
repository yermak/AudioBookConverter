package uk.yermak.audiobookconverter;

/**
 * Created by yermak on 08-Feb-18.
 */
public enum ProgressStatus {
    READY(false), IN_PROGRESS(false), PAUSED(false), FINISHED(true), ERROR(true), CANCELLED(true);

    private boolean over;

    ProgressStatus(boolean over) {
        this.over = over;
    }

    public boolean isOver() {
        return over;
    }}

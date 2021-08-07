package uk.yermak.audiobookconverter;

/**
 * Created by yermak on 08-Feb-18.
 */
public enum ProgressStatus {
    READY(false, false), IN_PROGRESS(true, false), PAUSED(true, false), FINISHED(true, true), ERROR(true, true), CANCELLED(true, true);

    private final boolean over;
    private final boolean started;

    ProgressStatus(boolean started, boolean over) {
        this.over = over;
        this.started = started;
    }

    public boolean isOver() {
        return over;
    }

    public boolean isStarted() {
        return started;
    }
}

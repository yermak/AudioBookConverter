package uk.yermak.audiobookconverter;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Created by yermak on 06/23/2018.
 */
public class StatusChangeListener implements ChangeListener<ProgressStatus> {
    private boolean cancelled;
    private boolean paused;
    private boolean finished;

    @Override
    public void changed(ObservableValue<? extends ProgressStatus> observable, ProgressStatus oldValue, ProgressStatus newValue) {
        switch (newValue) {
            case CANCELLED:
                cancelled = true;
                break;
            case PAUSED:
                paused = true;
                break;
            case IN_PROGRESS:
                paused = false;
                break;
            case FINISHED:
                finished = true;
                break;
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isPaused() {
        return paused;
    }


    public boolean isFinished() {
        return finished;
    }
}

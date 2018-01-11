package com.freeipodsoftware.abc;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import uk.yermak.audiobookconverter.ConversionMode;
import uk.yermak.audiobookconverter.StateDispatcher;
import uk.yermak.audiobookconverter.StateListener;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class ProgressView extends ProgressViewGui implements StateListener {
    private boolean suspended = false;

    public ProgressView(Composite parent) {
        super(parent);
        this.infoLabel.setText("");
        this.cancelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                cancel();
            }
        });
        this.pauseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                suspendOrResume();
            }
        });
        StateDispatcher.getInstance().addListener(this);


    }

    private void suspendOrResume() {
        suspended = !suspended;
        if (suspended) {
            pauseButton.setText("Resume");
            pauseButton.setSelection(true);
            StateDispatcher.getInstance().paused();
        } else {
            pauseButton.setText("Pause");
            pauseButton.setSelection(false);
            StateDispatcher.getInstance().resumed();
        }
    }

    private void cancel() {
        MessageBox msgbox = new MessageBox(ProgressView.this.getShell(), 196);
        msgbox.setText(Messages.getString("ProgressView.confirmation"));
        msgbox.setMessage(Messages.getString("ProgressView.cancelConfirmText"));
        int result = msgbox.open();
        if (result == 64) {
            ProgressView.this.cancelButton.setEnabled(false);
            ProgressView.this.pauseButton.setSelection(false);
            ProgressView.this.pauseButton.setEnabled(false);
            StateDispatcher.getInstance().canceled();
        }
    }

    public void setButtonWidthHint(int buttonWidthHint) {
        this.setWidthHintForControl(buttonWidthHint, this.cancelButton);
        this.setWidthHintForControl(buttonWidthHint, this.pauseButton);
    }

    private void setWidthHintForControl(int buttonWidthHint, Control widget) {
        GridData gridData = (GridData) widget.getLayoutData();
        gridData.widthHint = buttonWidthHint;
    }

    public void setProgress(int progress) {
        this.progressBar.setSelection(progress);
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTimeLabel.setText(Messages.getString("ProgressView.timeElapsed") + " " + this.formatTime(elapsedTime));
    }

    private String formatTime(long millis) {

        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        return hms;
    }

    public void setEstimatedFinalOutputSize(long bytes) {
        if (bytes == -1L) {
            this.outputFileSizeValueLabel.setText("---");
        } else {
            DecimalFormat mbFormat = new DecimalFormat("0");
            this.outputFileSizeValueLabel.setText(mbFormat.format((double) bytes / 1048576.0D) + " MB");
        }

    }

    public void setRemainingTime(long remainingTime) {
        remainingTimeLabel.setText(Messages.getString("ProgressView.timeRemaining") + " " + formatTime(remainingTime));
    }

    public void setInfoText(String infoText) {
        this.infoLabel.setText(infoText);
    }


    public void reset() {
        this.cancelButton.setEnabled(true);
        this.pauseButton.setEnabled(true);
    }

    @Override
    public void finishedWithError(String error) {
        getDisplay().syncExec(() -> {
            this.cancelButton.setEnabled(false);
            this.pauseButton.setSelection(false);
            this.pauseButton.setEnabled(false);
            this.progressBar.setSelection(0);
        });
    }

    public void finished() {
        getDisplay().syncExec(() -> {
            this.cancelButton.setEnabled(false);
            this.pauseButton.setSelection(false);
            this.pauseButton.setEnabled(false);
            this.progressBar.setSelection(0);
            setInfoText("");
        });
    }

    @Override
    public void canceled() {
    }

    @Override
    public void paused() {
    }

    @Override
    public void resumed() {
    }

    @Override
    public void fileListChanged() {
    }

    @Override
    public void modeChanged(ConversionMode mode) {
    }

}
